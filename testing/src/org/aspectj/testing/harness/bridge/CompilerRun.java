/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.ReflectionFactory;
import org.aspectj.testing.ajde.CompileCommand;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.WrappedRunIterator;
import org.aspectj.testing.xml.SoftMessage;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Run the compiler once.
 * The lifecycle is as follows:
 * <ul>
 * <li>Spec (specification) is created.</li>
 * <li>This is created using the Spec.</li>
 * <li>setupAjcRun(Sandbox, Validator) is invoked,
 *     at which point this populates the shared sandbox
 *     with values derived from the spec and also
 *     sets up internal state based on both the sandbox
 *     and the spec.</li>
 * <li>run(IRunStatus) is invoked, and this runs the compiler
 *     based on internal state, the spec, and the sandbox.</li>
 * </ul>
 * Programmer notes:
 * <ul>
 * <li>Paths are resolved absolutely, which fails to test the
 *     compiler's ability to find files relative to a source base</li>
 * <li>This does not enforce the lifecycle.</li>
 * <li>This must be used as the initial compile 
 *     before doing an incremental compile </li>
 * </ul>
 */
public class CompilerRun implements IAjcRun {
    static final String AJDE_COMPILER = CompileCommand.class.getName();

	static final String[] RA_String = new String[0];
    
    static final String[] JAR_SUFFIXES = new String[] { ".jar", ".zip" };

    static final String[] SOURCE_SUFFIXES 
        = (String[]) FileUtil.SOURCE_SUFFIXES.toArray(new String[0]);
        
    /** specifications, set on construction */
    Spec spec;

    //------------ calculated during setup
    /** get shared stuff during setup */       
    Sandbox sandbox;
    
    /** 
     * During run, these String are passed as the source and arg files to compile.
     * The list is set up in setupAjcRun(..), when arg files are prefixed with "@".
     */
    final List /*String*/ arguments;
    
    /** 
     * During run, these String are collapsed and passed as the injar option.
     * The list is set up in setupAjcRun(..).
     */
    final List /*String*/ injars;
   
    private CompilerRun(Spec spec) {
        if (null == spec) {
            throw new IllegalArgumentException("null spec");
        }
        this.spec = spec;
        arguments = new ArrayList();
        injars = new ArrayList();
    }           
    
    /** 
     * This checks that the spec is reasonable and does setup:
     * <ul>
     * <li>calculate and set sandbox testBaseSrcDir as {Sandbox.testBaseDir}/
     * {Spec.testSrcDirOffset}/<li>
     * <li>get the list of source File to compile as {Sandbox.testBaseSrcDir} /
     * {Spec.getPaths..}</li>
     * <li>get the list of extraClasspath entries to add to default classpath as
     * {Sandbox.testBaseSrcDir} / {Spec.classpath..}</li>
     * <li>get the list of aspectpath entries to use as the aspectpath as
     * {Sandbox. testBaseSrcDir} / {Spec.aspectpath..}</li>
     * </ul>
     * All sources must be readable at this time.
     * If staging, the source files and source roots are copied
     * to a separate staging directory so they can be modified
     * for incremental tests.   Note that (as of this writing) the
     * compiler only handles source roots for incremental tests.
     * @param classesDir the File
	 * @see org.aspectj.testing.harness.bridge.AjcTest.IAjcRun#setup(File, File)
     * @throws AbortException containing IOException or IllegalArgumentException
     *          if the staging operations fail
	 */
	public boolean setupAjcRun(Sandbox sandbox, Validator validator) {

        if (!validator.nullcheck(spec.getOptionsArray(), "localOptions")
            || !validator.nullcheck(sandbox, "sandbox")
            || !validator.nullcheck(spec.compiler, "compilerName")
            || !validator.canRead(Globals.F_aspectjrt_jar, "aspectjrt.jar")
            || !validator.canRead(Globals.F_testingclient_jar, "testing-client.jar")
            ) {
            return false;
        }
         
        this.sandbox = sandbox;
        
        String rdir = spec.testSrcDirOffset;
        File testBaseSrcDir;
        if ((null == rdir) || (0 == rdir.length())) {
            testBaseSrcDir = sandbox.testBaseDir;
        } else {
            testBaseSrcDir = new File(sandbox.testBaseDir, rdir);
            if (!validator.canReadDir(testBaseSrcDir, "sandbox.testBaseSrcDir")) {
                return false;
            }
        }
        sandbox.setTestBaseSrcDir(testBaseSrcDir, this);
        
        
        // Sources come as relative paths - check read, copy if staging.
        // This renders paths absolute before run(RunStatusI) is called.
        // For a compile run to support relative paths + source base,
        // change so the run calculates the paths (differently when staging)

        final String[] injarPaths; 
        final String[] srcPaths;
        { 
            final String[] paths = spec.getPathsArray();
            srcPaths = LangUtil.endsWith(paths, CompilerRun.SOURCE_SUFFIXES, true);
            injarPaths = LangUtil.endsWith(paths, CompilerRun.JAR_SUFFIXES, true);
        } 
        // validate readable for sources
        if (!validator.canRead(testBaseSrcDir, srcPaths, "sources")
            || !validator.canRead(testBaseSrcDir, injarPaths, "injars")
            || !validator.canRead(testBaseSrcDir, spec.argfiles, "argfiles")
            || !validator.canRead(testBaseSrcDir, spec.classpath, "classpath")
            || !validator.canRead(testBaseSrcDir, spec.aspectpath, "aspectpath")
            || !validator.canRead(testBaseSrcDir, spec.sourceroots, "sourceroots")
            ) {
            return false;
        }
        
        int numSources = srcPaths.length + injarPaths.length 
            + spec.argfiles.length + spec.sourceroots.length;
        if (numSources < 1) {
            validator.fail("no input jars, arg files, or source files or roots");
            return false;
        } 
        
        final File[] argFiles = FileUtil.getBaseDirFiles(testBaseSrcDir, spec.argfiles);
        final File[] injarFiles = FileUtil.getBaseDirFiles(testBaseSrcDir, injarPaths);
        final File[] aspectFiles = FileUtil.getBaseDirFiles(testBaseSrcDir, spec.aspectpath);
        final File[] classFiles = FileUtil.getBaseDirFiles(testBaseSrcDir, spec.classpath);
        // hmm - duplicates validation above, verifying getBaseDirFiles?
        if (!validator.canRead(argFiles, "argFiles")
            || !validator.canRead(injarFiles, "injarfiles")
            || !validator.canRead(aspectFiles, "aspectfiles")
            || !validator.canRead(classFiles, "classfiles")) {
            return false;
        }

        final File[] srcFiles;
        File[] sourcerootFiles = new File[0];
        // source text files are copied when staging incremental tests
        if (!spec.isStaging()) { // XXX why this? was always? || (testBaseSrcDir != sandbox.stagingDir))) {
            srcFiles = FileUtil.getBaseDirFiles(testBaseSrcDir, srcPaths, CompilerRun.SOURCE_SUFFIXES);
            if (!LangUtil.isEmpty(spec.sourceroots)) {
                sourcerootFiles = FileUtil.getBaseDirFiles(testBaseSrcDir, spec.sourceroots, null);
            }
        } else { // staging - copy files
            try {
                // copy all files, then remove tagged ones
                // XXX make copyFiles support a filter?
                srcFiles = FileUtil.copyFiles(testBaseSrcDir, srcPaths, sandbox.stagingDir);
                if (!LangUtil.isEmpty(spec.sourceroots)) {
                    sourcerootFiles = FileUtil.copyFiles(testBaseSrcDir, spec.sourceroots, sandbox.stagingDir);
                    // delete incremental files in sourceroot after copying // XXX inefficient
                    FileFilter pickIncFiles = new FileFilter() {
                        // an incremental file has an extra "." in name
                        // most .java files don't, because they are named after
                        // the principle type they contain, and simple type names
                        // have no dots.
                        public boolean accept(File file) {
                            if (file.isDirectory()) { // continue recursion
                                return true;
                            }
                            String path = file.getPath();
                            // only source files are relevant to staging
                            if (!FileUtil.hasSourceSuffix(path)) { 
                                return false;
                            }
                            int first = path.indexOf(".");
                            int last = path.lastIndexOf(".");
                            return (first != last);
                        }
                    };
                    for (int i = 0; i < sourcerootFiles.length; i++) {
                        FileUtil.deleteContents(sourcerootFiles[i], pickIncFiles, false);
                    }
                }
            } catch (IllegalArgumentException e) {
                validator.fail("staging - bad input", e);
                return false;
            } catch (IOException e) {
                validator.fail("staging - operations", e);
                return false;
            }
        }
        if (!validator.canRead(srcFiles, "copied paths")) {
            return false;
        }
        arguments.clear();
        if (!LangUtil.isEmpty(sourcerootFiles)) {
            arguments.add("-sourceroots");
            String sr = FileUtil.flatten(sourcerootFiles, null);
            arguments.add(sr);
        }
        if (!LangUtil.isEmpty(srcFiles)) {
            arguments.addAll(Arrays.asList(FileUtil.getPaths(srcFiles)));
        }
        injars.clear();
        if (!LangUtil.isEmpty(injarFiles)) {
            injars.addAll(Arrays.asList(FileUtil.getPaths(injarFiles)));
        }
        if (!LangUtil.isEmpty(argFiles)) {
            String[] ra = FileUtil.getPaths(argFiles);
            for (int j = 0; j < ra.length; j++) {
                arguments.add("@" + ra[j]);
            }
            if (spec.isStaging) {
                validator.info("warning: files listed in argfiles not staged");
            }               
        }

        // save classpath and aspectpath in sandbox for this and other clients
        final boolean checkReadable = true; // hmm - third validation?
        File[] cp = new File[2 + classFiles.length];
        System.arraycopy(classFiles, 0, cp, 0, classFiles.length);
        int index = classFiles.length;
        cp[index++] = Globals.F_aspectjrt_jar;
        cp[index++] = Globals.F_testingclient_jar;
        sandbox.setClasspath(cp, checkReadable, this);
        // set aspectpath
        if (0 < aspectFiles.length) {
            sandbox.setAspectpath(aspectFiles, checkReadable, this);
        }
        
        // set bootclasspath, if set as system property - urk!
        if (!LangUtil.isEmpty(JavaRun.BOOTCLASSPATH)) {
            sandbox.setBootclasspath(JavaRun.BOOTCLASSPATH, this);
        }
        return true;
    }
    
    /**
     * Setup result evaluation and command line, run, and evaluate result.
     * <li>setup an AjcMessageHandler using the expected messages from
     *     {@link Spec#getMessages()}.<li>
     * <li>heed any globals interpreted into a TestSetup by reading
     *     {@link Spec@getOptions()}.  For a list of supported globals, see
     *     {@link setupArgs(ArrayList, IMessageHandler}.</li>
     * <li>construct a command line, using as classpath 
     *     {@link Sandbox.classpathToString()}<li>
     * <li>construct a compiler using {@link Spec#compilerName}
     *     or any overriding value set in TestSetup.<li>
     * <li>Just before running, set the compiler in the sandbox using 
     *     {@link Sandbox.setCompiler(ICommand)}.<li>
     * <li>After running, report AjcMessageHandler results to the status parameter.
     *     If the AjcMessageHandler reports a failure, then send info messages
     *     for the Spec, TestSetup, and command line.<li>
     * XXX better to upgrade AjcMessageHandler to adopt status
     *     so the caller can control fast-fail, etc.
	 * @see org.aspectj.testing.run.IRun#run(IRunStatus)
	 */
	public boolean run(IRunStatus status) {
        if (null == spec.testSetup) {
            MessageUtil.abort(status, "no test setup - adoptParentValues not called");
            return false;
        } else if (!spec.testSetup.result) {
            MessageUtil.abort(status, spec.testSetup.failureReason);
            return false;
        }
        AjcMessageHandler handler = new AjcMessageHandler(spec.getMessages());
        handler.init();
        boolean handlerResult = false;
        boolean result = false;
        boolean commandResult = false;
        ArrayList argList = new ArrayList();
        final Spec.TestSetup setupResult = spec.testSetup;
        try {
            argList.addAll(setupResult.commandOptions);
            argList.add("-d");
            String outputDirPath = sandbox.classesDir.getAbsolutePath();
            try { // worth it to try for canonical?
                outputDirPath = sandbox.classesDir.getCanonicalPath();        
            } catch (IOException e) {
                MessageUtil.abort(status, "canonical " + sandbox.classesDir, e);
            }
            argList.add(outputDirPath);

            String path = sandbox.classpathToString(this);
            if (!LangUtil.isEmpty(path)) {
                argList.add("-classpath");
                argList.add(path);
            }
            path = sandbox.getBootclasspath(this);
            if (!LangUtil.isEmpty(path)) {
                argList.add("-bootclasspath");
                argList.add(path);
            }
            
            path = sandbox.aspectpathToString(this);
            if (!LangUtil.isEmpty(path)) {
                argList.add("-aspectpath");
                argList.add(path);
            }

            if (0 < injars.size()) {
                argList.add("-injars");
                argList.add(FileUtil.flatten((String[]) injars.toArray(new String[0]), null));
            }

            // add both java/aspectj and argfiles
            argList.addAll(arguments);
            
            // XXX hack - seek on request as a side effect. reimplement as listener 
            if (null != setupResult.seek) {
                String slopPrefix = Spec.SEEK_MESSAGE_PREFIX + " slop - ";
                PrintStream slop = MessageUtil.handlerPrintStream(
                    status, 
                    IMessage.INFO, 
                    System.err, 
                    slopPrefix);
                List found = FileUtil.lineSeek(setupResult.seek, arguments, false, slop);
                if (!LangUtil.isEmpty(found)) {
                    for (Iterator iter = found.iterator(); iter.hasNext();) {
                        MessageUtil.info(status, Spec.SEEK_MESSAGE_PREFIX + iter.next());
					}
                }
            }
            ICommand compiler = spec.reuseCompiler 
                ? sandbox.getCommand(this) // throws IllegalStateException if null
                : ReflectionFactory.makeCommand(setupResult.compilerName, status);            
            DirChanges dirChanges = null;
            if (null == compiler) {
                MessageUtil.fail(status, "unable to make compiler " + setupResult.compilerName);
                return false;
            } else {
                if (setupResult.compilerName != Spec.DEFAULT_COMPILER) {
                    MessageUtil.info(status, "compiler: " + setupResult.compilerName);
                }
                if (status.aborted()) {
                    MessageUtil.debug(status, "aborted, but compiler valid?: " + compiler);
                } else {
                    // same DirChanges handling for JavaRun, CompilerRun, IncCompilerRun 
                    // XXX around advice or template method/class
                    if (!LangUtil.isEmpty(spec.dirChanges)) {
                        LangUtil.throwIaxIfFalse(1 == spec.dirChanges.size(), "expecting 0..1 dirChanges");
                        dirChanges = new DirChanges((DirChanges.Spec) spec.dirChanges.get(0));
                        if (!dirChanges.start(status, sandbox.classesDir)) {
                            return false; // setup failed
                        }
                    }
                    MessageUtil.info(status, compiler + "(" + argList + ")");
                    sandbox.setCommand(compiler, this);
                    String[] args = (String[]) argList.toArray(RA_String);
                    commandResult = compiler.runCommand(args, handler);
                }
            }
            if (!setupResult.ignoreWarningsSet) {
                handlerResult = handler.passed();
            } else {
                handlerResult = handler.passed(setupResult.ignoreWarnings);
            }
            if (!handlerResult) {
                return false;
            } else {
                result = (commandResult == handler.expectingCommandTrue()); 
                if (! result) {
                    String m = commandResult 
                        ? "compile did not fail as expected"
                        : "compile failed unexpectedly";                        
                    MessageUtil.fail(status, m);
                } else if (null != dirChanges) {
                    result = dirChanges.end(status, sandbox.testBaseDir);
                }
            }
            return result;
        } finally {
            if (!handlerResult) { // more debugging context in case of failure
                MessageUtil.info(handler, spec.toLongString());
                MessageUtil.info(handler, "" + argList);
                if (null != setupResult) {
                    MessageUtil.info(handler, "" + setupResult);
                }
            }
            handler.report(status); // XXX weak - actual messages not reported in real-time, no fast-fail
        }             
    }
    

    public String toString() {
        return "CompilerRun(" + spec + ")";
    }
//        String[] sourcePaths = (null == this.sourcePaths ? new String[0] : this.sourcePaths);
//        List sources = (null == this.sources ? Collections.EMPTY_LIST : this.sources);
//        return "CompilerRun-" + compilerName 
//            + "(" + Arrays.asList(sourcePaths)
//            + ", " + sources
//            + ", " + Arrays.asList(globalOptions) 
//            + ", " + Arrays.asList(localOptions) 
//            + ")";
    
    /** 
     * Initializer/factory for CompilerRun
     * any path or file is relative to this test base dir
     */
    public static class Spec extends AbstractRunSpec {
        public static final String XMLNAME = "compile";
        static final String SEEK_PREFIX = "-seek:";
        static final String SEEK_MESSAGE_PREFIX = "found: ";
        
        /** no support in the harness for these otherwise-valid options */
        private static final String[] INVALID_OPTIONS = new String[]
            { "-workingdir", "-argfile", "-sourceroot", "-outjar"}; 
            // when updating these, update tests/harness/selectionTest.xml

        /** no support in the eclipse-based compiler for these otherwise-valid options */
        private static final String[] INVALID_ECLIPSE_OPTIONS = new String[]
            { "-lenient", "-strict", "-usejavac", "-preprocess",
              "-XOcodeSize", "-XSerializable", "-XaddSafePrefix",
              "-XserializableAspects", "-XtargetNearSource" };

        /** options supported by the harness */
        private static final String[] VALID_OPTIONS = new String[]
            {
                SEEK_PREFIX,
                // eajc does not support -usejavac, -preprocess
                // testFlag() handles -ajc, -eclipse, -ignoreWarnings
                "-usejavac", "-preprocess",          
                "-Xlint",  "-lenient", "-strict", 
                "-source14", "-verbose", "-emacssym", 
                "-ajc", "-eclipse", "-ajdeCompiler", 
                "-ignoreWarnings",
                // XXX consider adding [!^]ajdeCompiler
                "!usejavac", "!preprocess",          
                "!Xlint",  "!lenient", "!strict", 
                "!source14", "!verbose", "!emacssym", 
                "!ajc", "!eclipse", 

                "^usejavac", "^preprocess",          
                "^Xlint",  "^lenient", "^strict", 
                "^source14", "^verbose", "^emacssym", 
                "^ajc", "^eclipse"
            };
        public static final String DEFAULT_COMPILER 
            = ReflectionFactory.ECLIPSE;
//            = ReflectionFactory.OLD_AJC;
        /**
         * Retitle description to title, paths to files, do comment
         * do dirChanges, and print no chidren. 
         */
        private static final XMLNames NAMES = new XMLNames(XMLNames.DEFAULT,
            "title", null, null, null, "files", null, null, false, false, true);
        
        protected String compiler;
        
        // use same command - see also IncCompiler.Spec.fresh
        protected boolean reuseCompiler;
        
        protected TestSetup testSetup;
        
        protected String[] argfiles = new String[0];
        protected String[] aspectpath = new String[0];
        protected String[] classpath = new String[0]; // XXX unused
        protected String[] sourceroots = new String[0];
        
        /** src path = {suiteParentDir}/{testBaseDirOffset}/{testSrcDirOffset}/{path} */
        protected String testSrcDirOffset;
        
        public Spec() {
            super(XMLNAME);
            setXMLNames(NAMES);
            compiler = DEFAULT_COMPILER;
        }
        
        public void setReuseCompiler(boolean reuse) {
            this.reuseCompiler = reuse;
        }
        
        public void setCompiler(String compilerName) {
            this.compiler = compilerName;
        }        

        public void setTestSrcDirOffset(String s) {
            if (null != s) {
                testSrcDirOffset = s;
            }
        }
        
        /** override to set dirToken to Sandbox.CLASSES and default suffix to ".class" */
        public void addDirChanges(DirChanges.Spec spec) {
            if (null == spec) {
                return;
            }
            spec.setDirToken(Sandbox.CLASSES_DIR);
            spec.setDefaultSuffix(".class");
            super.addDirChanges(spec);
        }
        protected String getPrintName() {
            return "CompilerRun.Spec " + getShortCompilerName();
        }
        
        public String toLongString() {
            return getPrintName() + "(" + super.containedSummary() + ")";
        }

        public String toString() {
            return getPrintName() + "(" + super.containedSummary() + ")";
        }

        /** bean mapping for writers */        
        public void setFiles(String paths) {
            addPaths(paths);
        }
        
        /**
         * Add to default classpath
         * (which includes aspectjrt.jar and testing-client.jar).
         * @param files comma-delimited list of classpath entries - ignored if
         * null or empty
         */
        public void setClasspath(String files) {
            if (!LangUtil.isEmpty(files)) {
                classpath = XMLWriter.unflattenList(files);
            }
        }

        /** 
         * Set source roots, deleting any old ones
         * @param files comma-delimited list of directories
         *  - ignored if null or empty
         */        
        public void setSourceroots(String dirs) {
            if (!LangUtil.isEmpty(dirs)) {
                sourceroots = XMLWriter.unflattenList(dirs);
            }
        }

        /** 
         * Set aspectpath, deleting any old ones
         * @param files comma-delimited list of aspect jars - ignored if null or
         * empty
         */        
        public void setAspectpath(String files) {
            if (!LangUtil.isEmpty(files)) {
                aspectpath = XMLWriter.unflattenList(files);
            }
        }

        /** 
         * Set argfiles, deleting any old ones
         * @param files comma-delimited list of argfiles - ignored if null or empty
         */        
        public void setArgfiles(String files) {
            if (!LangUtil.isEmpty(files)) {
                argfiles = XMLWriter.unflattenList(files);
            }
        }

        /** @return String[] copy of argfiles array */
        public String[] getArgfilesArray() {
            String[] argfiles = this.argfiles;
            if (LangUtil.isEmpty(argfiles)) {
                return new String[0];
            }
            return (String[]) LangUtil.copy(argfiles);
        }
        
        /**
         * This implementation skips if:
         * <ul>
         * <li>incremental test, but using ajc (not eclipse)</li>
         * <li>usejavac, but javac is not available on the classpath</li>
         * <li>eclipse, but -usejavac or -preprocess test</li>
         * <li>-source14, but running under 1.2 (XXX design)</li>
         * <li>local/global option conflicts (-lenient/-strict)</li>
         * <li>semantic conflicts (e.g., -lenient/-strict)</li>
         * </ul>
         * @return false if this wants to be skipped, true otherwise
         */
        protected boolean doAdoptParentValues(RT parentRuntime, IMessageHandler handler) {
            if (!super.doAdoptParentValues(parentRuntime, handler)) {
                return false;
            }
            testSetup = setupArgs(handler);
            if (!testSetup.result) {
                skipMessage(handler, testSetup.failureReason);
            }
            return testSetup.result;
        }  
        

	    private String getShortCompilerName() {
			String cname = compiler;
            if (null != testSetup) {
                cname = testSetup.compilerName;
            }
			if (null != cname) {
			    int loc = cname.lastIndexOf(".");
			    if (-1 != loc) {
			        cname = cname.substring(loc+1);
			    }
			}
			return cname;
		}
        
        /** @return a CompilerRun with this as spec if setup completes successfully. */
		public IRunIterator makeRunIterator(Sandbox sandbox, Validator validator) {
			CompilerRun run = new CompilerRun(this);
            if (run.setupAjcRun(sandbox, validator)) {
                // XXX need name for compilerRun
                return new WrappedRunIterator(this, run);
            }
            return null;
		}
        
        /** 
         * Each non-incremental run, fold the global flags in with
         * the run flags, which may involve adding or removing from
         * either list, depending on the flag prefix:
         * <ul>
         * <li>-foo: use -foo unless forced off.<li>
         * <li>^foo: (force off) remove any -foo option from the run flags</li>
         * <li>!foo: (force on) require the -foo flag </li>
         * </ul>
         * If there is a force conflict, then the test is skipped
         * ("skipping" info message, TestSetup.result is false).
         * This means an option local to the test which was specified 
         * without forcing may be overridden by a globally-forced option.
         * <p>
         * There are some flags which are interpreted by the test
         * and removed from the list of flags passed to the command
         * (see testFlag()):
         * <ul>
         * <li>eclipse: use the new eclipse compiler (can force)</li>
         * <li>ajc: use the old ajc compiler (can force)</li>
         * <li>ignoreWarnings: ignore warnings in result evaluations (no force)</li>
         * </ul>
         * <p>
         * There are some flags which are inconsistent with each other.
         * These are treated as conflicts and the test is skipped:
         * <ul>
         * <li>lenient, strict</li>
         * </ul>
         * <p>
         * The -source 1.4 flag should always be specified as -source14, 
         * as this will otherwise fail to process it correctly.  
         * This converts it back to -source 1.4.
         * <p>
         * Finally, compiler limitations are enforced here by skipping
         * tests which the compiler cannot do:
         * <ul>
         * <li>eclipse does not do -lenient, -strict, -usejavac, -preprocess,
         *     -XOcodeSize, -XSerializable, XaddSafePrefix,
         *     -XserializableAspects,-XtargetNearSource</li>
         * <li>ajc does not run in incremental (staging) mode, 
         *     nor with -usejavac if javac is not on the classpath</li>
         * </ul>
         * <u>Errors</u>:This will remove an arg not prefixed by [-|!|^] after
         * providing an info message.
         * <u>TestSetup Result</u>: 
         * If this completes successfully, then TestSetup.result is true,
         * and commandOptions is not null, and any test flags (ignore warning,
         * compiler) are reflected in the TestSetup.
         * If this fails, then TestSetup.result is false,
         * and a TestSetup.failreason is set.  
         * @return TestSetup with results 
         *          (TestSetup result=false if the run should not continue)
         */
        protected TestSetup setupArgs(IMessageHandler handler) {
            // warning: HarnessSelectionTest checks for specific error wording
            ArrayList argList = new ArrayList();
            argList.addAll(getOptionsList());
            final Spec spec = this;
            TestSetup result = new TestSetup();
            if (argList.contains("-source")) {
                result.failureReason = "use -source14 for -source 1.4: " + argList;
                return result;                
            }
            result.compilerName = spec.compiler;
            String[] globalOptions = spec.runtime.extractOptions(Spec.VALID_OPTIONS, true);
            if ((null != globalOptions) && (globalOptions.length > 0)) {
                // --- fold in globals, removing conflicts, etc.
                for (int i = 0; i < globalOptions.length; i++) {
                    String globalArg = globalOptions[i];
                    if ((null == globalArg) || (2 > globalArg.length())) {
                        continue;
                    } else if (globalArg.startsWith(SEEK_PREFIX)) { 
                        result.seek = globalArg.substring(SEEK_PREFIX.length());
                        continue;
                    } else if ("-source".equals(globalArg)) {
                        result.failureReason = "use -source14 for -source 1.4 [" + i + "]";
                        return result;                
                    }
                    char first = globalArg.charAt(0);
                    globalArg = globalArg.substring(1);
                    boolean globalForceOn   = (first == '!');
                    boolean globalForceOff  = (first == '^');
                    boolean globalSet       = (first == '-');
                    if (!globalSet && !globalForceOn && !globalForceOff) {
                        MessageUtil.info(handler, "ignoring bad global: " + globalOptions[i]);
                        continue;
                    }
                    int argIndex = indexOf(globalArg, argList);
                    if (-1 == argIndex) { // no apparent conflict - look for eclipse/ajc conflicts XXX unresolved
                        boolean ajcGlobal = true;
                        if ("ajc".equals(globalArg)) {
                            argIndex = indexOf("eclipse", argList);
                        } else if ("eclipse".equals(globalArg)) {
                            argIndex = indexOf("ajc", argList);                            
                            ajcGlobal = false;
                        }
                        if (-1 != argIndex) {   // resolve eclipse/ajc conflict
                            String arg = ((String) argList.get(argIndex));
                            char argFirst = arg.charAt(0);
                            argList.remove(arg);      // replace with resolved variant...
                            char ajcFirst;
                            char eclipseFirst;
                            if (ajcGlobal) {
                                ajcFirst = first;
                                eclipseFirst = argFirst;
                            } else {
                                ajcFirst = argFirst;
                                eclipseFirst = first;
                            }
                            if ('!' == eclipseFirst) {
                                if ('!' == ajcFirst) {
                                    result.failureReason = "conflict between !eclipse and !ajc";
                                    return result;
                                } else {
                                    argList.add("-eclipse");
                                }
                            } else if (('!' == ajcFirst)  || ('^' == eclipseFirst)) {
                                argList.add("-ajc");
                            } else if ('^' == ajcFirst) {
                                argList.add("-eclipse");
                            } else if (('-' != ajcFirst) || ('-' != eclipseFirst)) {
                                result.failureReason = "harness logic error resolving "
                                    + arg + " and global " + globalArg;
                                return result;
                            } else if (ajcGlobal) {
                                argList.add("-ajc");
                            } else {
                                argList.add("-eclipse");
                            }
                            continue; // resolved 
                        }
                    }
                    
                    if (-1 == argIndex) { // no dup, so no conflict
                        if (!globalForceOff) {
                            argList.add("-" + globalArg);
                        }
                    } else { // have conflict - resolve
                        String arg = (String) argList.get(argIndex);
                        first = arg.charAt(0);
                        boolean localForceOn   = (first == '!');
                        boolean localForceOff  = (first == '^');
                        boolean localSet       = (first == '-');
                        if (!localSet && !localForceOn && !localForceOff) {
                            result.failureReason = "only handling [-^!]{arg}: " + arg;
                            return result;
                        }
                        if ((localForceOn && globalForceOff)
                            || (localForceOff && globalForceOn)) {
                            result.failureReason = "force conflict between arg=" 
                                + arg + " and global=" + globalOptions[i];
                            return result;
                        } 
                        if (globalForceOn) {
                            if (localForceOn) { // localSet is already correct, localForceOff was conflict
                                argList.remove(arg);      // no !funkiness
                                argList.add("-" + globalArg);
                            }
                        } else if (globalSet) {
                            if (localSet) {
                                // do nothing - already correct
                            } else if (localForceOn) {
                                argList.remove(arg);      // no !funkiness
                                argList.add("-" + globalArg);
                            }
                        } else if (globalForceOff) {
                            argList.remove(arg);
                        } else {
                            throw new Error("illegal arg state?? : " + arg);
                            //MessageUtil.info(handler, "illegal arg state??: " + arg);
                        }
                    }
                }
            }
            // send info messages about
            // forced staging when -incremental
            // or staging but no -incremental flag
            int incLoc = argList.indexOf("-incremental");
            if (spec.isStaging()) {
                if (-1 == incLoc) { // staging and no flag
                    MessageUtil.info(handler, "staging but no -incremental flag");
                }
            } else if (-1 != incLoc) { // flagged but not staging - stage
                spec.setStaging(true);
                MessageUtil.info(handler, "-incremental forcing staging");
            }
            // remove funky prefixes from remainder, fixup two-part flags
            // and interpret special flags
            boolean source14 = false;
            ArrayList toAdd = new ArrayList();
            for (ListIterator iter = argList.listIterator(); iter.hasNext();) {
                String arg = (String) iter.next();
                if (testFlag(arg, result)) {
                    iter.remove();
                    continue;
                }
                char c = arg.charAt(0);
                String rest = arg.substring(1);
                if (c == '^') {
                    iter.remove();
                    continue;
                }
                if (c == '!') {
                    iter.remove();
                    if (!("source14".equals(rest))) {
                        toAdd.add("-" + rest);
                    } else {
                        source14 = true;
                    }
                    rest = null;
                } else if ("source14".equals(rest)) {
                    iter.remove();
                    source14 = true;
                }
            }
            if (source14) {
                // must run under 1.4 VM or (if ajc) set up bootclasspath
                if (!LangUtil.supportsJava("1.4")) {
                    if (ReflectionFactory.ECLIPSE.equals(result.compilerName)) {
                        result.failureReason 
                            = "eclipse must run under 1.4 to implement -source 1.4";
                        return result;                        
                    }
                    
                    if (!FileUtil.canReadFile(Globals.J2SE14_RTJAR)) {
                        result.failureReason 
                            = "unable to get 1.4 libraries to implement -source 1.4";
                        return result;
                    }
                    toAdd.add("-bootclasspath");
                    toAdd.add(Globals.J2SE14_RTJAR.getAbsolutePath());
                }
                toAdd.add("-source");
                toAdd.add("1.4");
            }
            argList.addAll(toAdd);
    
            // finally, check for semantic conflicts
            String[] badOptions = LangUtil.selectOptions(argList, Spec.INVALID_OPTIONS);
            if (!LangUtil.isEmpty(badOptions)) {
                result.failureReason = "no support for (normally-valid) options "
                     + Arrays.asList(badOptions);
            } else if (argList.contains("-lenient") && argList.contains("-strict")) {
                result.failureReason = "semantic conflict -lenient | -strict";
            } else if (ReflectionFactory.OLD_AJC.equals(result.compilerName)) {
                if (spec.isStaging) {
                    result.failureReason = "OLD_AJC does not do incremental compiles";
                } else if (argList.contains("-usejavac") && !haveJavac()) {
                    result.failureReason = "-usejavac but no javac on classpath";
                } else {
                    result.result = true;
                }
            } else if (!ReflectionFactory.ECLIPSE.equals(result.compilerName)
                && !AJDE_COMPILER.equals(result.compilerName)) {
                result.failureReason = "unrecognized compiler: " + result.compilerName;
            } else {
                badOptions = LangUtil.selectOptions(argList, Spec.INVALID_ECLIPSE_OPTIONS);
                if (!LangUtil.isEmpty(badOptions)) {                    
                    result.failureReason = "no support in eclipse-based compiler"
                        + " for (normally-valid) options "+ Arrays.asList(badOptions);
                } else {
                    result.result = true;
                }
            }
            if (result.result) {
                result.commandOptions = argList;
            }
            return result; 
        }

		/** @return true if javac is available on the classpath */
		private boolean haveJavac() { // XXX copy/paste from JavaCWrapper.java
            Class compilerClass = null;
            try {
                compilerClass = Class.forName("com.sun.tools.javac.Main");
            } catch (ClassNotFoundException ce1) {
                try {
                    compilerClass = Class.forName("sun.tools.javac.Main");
                } catch (ClassNotFoundException ce2) {
                }
            }
            return (null != compilerClass);
		}

        
        /**
         * Handle flags that are interpreted by the test rather than the
         * underlying command.  These flags are to be removed from the
         * arg list.
         *  @return true if this is a flag to remove from the arg list
         */
        protected boolean testFlag(String arg, TestSetup result) {
            if ("-ajdeCompiler".equals(arg)) {
                result.compilerName = AJDE_COMPILER;
                return true;
            } else if ("-eclipse".equals(arg) || "!eclipse".equals(arg) || "^ajc".equals(arg)) {
                result.compilerName = ReflectionFactory.ECLIPSE;
                return true;
            } else if ("-ajc".equals(arg) || "!ajc".equals(arg) || "^eclipse".equals(arg)) {
                result.compilerName = ReflectionFactory.OLD_AJC;
                return true;
            } else if ("-ignoreWarnings".equals(arg)) {
                result.ignoreWarnings = true;
                result.ignoreWarningsSet = true;
                return true;
            }
            return false;
        }
        
        // XXX need keys, cache...
        /** @return index of global in argList, ignoring first char */
        protected int indexOf(String global, ArrayList argList) {
            int max = argList.size();
            for (int i = 0; i < max; i++) {
                 if (global.equals(((String) argList.get(i)).substring(1))) {
                    return i;
                 }
            }
            return -1;
        }
        
        /** 
         * Write this out as a compile element as defined in
         * AjcSpecXmlReader.DOCTYPE.
         * @see AjcSpecXmlReader#DOCTYPE 
         * @see IXmlWritable#writeXml(XMLWriter) 
         */
        public void writeXml(XMLWriter out) {
            out.startElement(xmlElementName, false);
            if (!LangUtil.isEmpty(testSrcDirOffset)) {
                out.printAttribute("dir", testSrcDirOffset);
            }
            super.writeAttributes(out);
            if (!DEFAULT_COMPILER.equals(compiler)) {
                out.printAttribute("compiler", compiler);
            }
            if (!reuseCompiler) {
                out.printAttribute("reuseCompiler", "true");
            }
            if (!LangUtil.isEmpty(argfiles)) {
                out.printAttribute("argfiles", XMLWriter.flattenFiles(argfiles));
            }
            if (!LangUtil.isEmpty(aspectpath)) {
                out.printAttribute("aspectpath", XMLWriter.flattenFiles(argfiles));
            }
            if (!LangUtil.isEmpty(sourceroots)) {
                out.printAttribute("sourceroots", XMLWriter.flattenFiles(argfiles));
            }
            out.endAttributes();
            if (!LangUtil.isEmpty(dirChanges)) {
                DirChanges.Spec.writeXml(out, dirChanges);
            }
            List messages = getMessages();
            if (!LangUtil.isEmpty(messages)) {
                SoftMessage.writeXml(out, messages);
            }
            out.endElement(xmlElementName);
        }
        
        /** 
         * Encapsulate the directives that can be set using
         * global arguments supplied in {@link Spec.getOptions()}.
         * This supports changing the compiler and ignoring warnings.
         */
        class TestSetup {
            /** null unless overriding the compiler to be used */
            String compilerName;
            /** 
             * true if we should tell AjcMessageHandler whether
             * to ignore warnings in its result evaluation
             */
            boolean ignoreWarningsSet;
            
            /** if telling AjcMessageHandler, what we tell it */
            boolean ignoreWarnings;
            
            /** false if setup failed */
            boolean result;
            
            /** if setup failed, this has the reason why */
            String failureReason;
            
            /** beyond running test, also seek text in sources */
            String seek;
            
            /** if setup completed, this has the combined global/local options */
            ArrayList commandOptions;
            
            public String toString() {
                return "TestSetup("
                    + (null == compilerName ? "" : compilerName + " ")
                    + (!ignoreWarningsSet ?  "" 
                        : (ignoreWarnings ? "" : "do not ")
                            + "ignore warnings ")
                    + (result ? "" : "setup failed")
                    + ")";
            }
        }
    } // CompilerRun.Spec
} // CompilerRun
        
//        /** 
//         * Write this out as a compile element as defined in
//         * AjcSpecXmlReader.DOCTYPE.
//         * @see AjcSpecXmlReader#DOCTYPE 
//         * @see IXmlWritable#writeXml(XMLWriter) 
//         */
//        public void writeXml(XMLWriter out) {
//            StringBuffer sb = new StringBuffer();
//            Spec spec = this;
//            final String elementName = "compile";
//            List list = spec.getOptionsList();
//            String args = XMLWriter.flattenList(spec.getOptionsList()).trim();
//            String argsAttr = out.makeAttribute("options", args).trim();
//            String files = XMLWriter.flattenFiles(spec.getPathsArray()).trim();
//            String filesAttr = out.makeAttribute("files", files).trim();
//            List messages = spec.getMessages();
//            int nMessages = messages.size();
//            int both = argsAttr.length() + filesAttr.length();
//            final int MAX = 55;
//    
//            // tortured logic to make more readable XML...        
//            if ((both < MAX) || (0 == args.length())) {
//                // if short enough, print entire or just start
//                if (0 != args.length()) {
//                    filesAttr = argsAttr + " " + filesAttr;
//                }
//                if (0 == nMessages) {
//                    out.printElement(elementName, filesAttr);
//                    return;
//                } else {
//                    out.startElement(elementName, filesAttr, true);
//                }
//            } else if (argsAttr.length() < filesAttr.length()) {
//                out.startElement(elementName, argsAttr, false);
//                out.printAttribute("files", files);   
//                out.endAttributes();
//            } else { 
//                out.startElement(elementName, filesAttr, false);
//                out.printAttribute("options", args);   
//                out.endAttributes();
//            }
//            if (0 < nMessages) {
//                SoftMessage.writeXml(out, messages);
//            }
//            out.endElement(elementName);
//        }

