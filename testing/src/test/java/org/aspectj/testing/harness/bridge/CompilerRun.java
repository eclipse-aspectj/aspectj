/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Wes Isberg     2003 updates
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.ReflectionFactory;
import org.aspectj.testing.ajde.CompileCommand;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.WrappedRunIterator;
import org.aspectj.testing.taskdefs.AjcTaskCompileCommand;
import org.aspectj.testing.util.options.Option;
import org.aspectj.testing.util.options.Option.Family;
import org.aspectj.testing.util.options.Option.InvalidInputException;
import org.aspectj.testing.util.options.Options;
import org.aspectj.testing.util.options.Values;
import org.aspectj.testing.xml.SoftMessage;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

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
 *     before doing an incremental compile.
 *     In that case, staging must be enabled.</li>
 * </ul>
 */
public class CompilerRun implements IAjcRun {
    //    static final String JAVAC_COMPILER 
    //        = JavacCompileCommand.class.getName();

    static final String[] RA_String = new String[0];

    static final String[] JAR_SUFFIXES = new String[] { ".jar", ".zip" };

    static final String[] SOURCE_SUFFIXES =
        (String[]) FileUtil.SOURCE_SUFFIXES.toArray(new String[0]);

    /** specifications, set on construction */
    Spec spec;

    //------------ calculated during setup
    /** get shared stuff during setup */
    Sandbox sandbox;

    /** 
     * During run, these String are passed as the source and arg files to compile.
     * The list is set up in setupAjcRun(..), when arg files are prefixed with "@".
     */
    final List /*String*/
    arguments;

    /** 
     * During run, these String are collapsed and passed as the injar option.
     * The list is set up in setupAjcRun(..).
     */
    final List /*String*/
    injars;

    /** 
     * During run, these String are collapsed and passed as the inpath option.
     * The list is set up in setupAjcRun(..),
     * which extracts only directories from the files attribute.
     */
    final List inpaths;

    private CompilerRun(Spec spec) {
        if (null == spec) {
            throw new IllegalArgumentException("null spec");
        }
        this.spec = spec;
        arguments = new ArrayList();
        injars = new ArrayList();
        inpaths = new ArrayList();
    }
    

    /**
     * Select from input String[] if readable directories
     * @param inputs String[] of input - null ignored
     * @param baseDir the base directory of the input
     * @return String[] of input that end with any input
     */
    public static String[] selectDirectories(String[] inputs, File baseDir) {
        if (LangUtil.isEmpty(inputs)) {
            return new String[0];
        }
        ArrayList result = new ArrayList();
		for (String input : inputs) {
			if (null == input) {
				continue;
			}
			File inputFile = new File(baseDir, input);
			if (inputFile.canRead() && inputFile.isDirectory()) {
				result.add(input);
			}
		}
        return (String[]) result.toArray(new String[0]);
    }
    
    /**
     * Select from input String[] based on suffix-matching
     * @param inputs String[] of input - null ignored
     * @param suffixes String[] of suffix selectors - null ignored
     * @param ignoreCase if true, ignore case
     * @return String[] of input that end with any input
     */
    public static String[] endsWith(String[] inputs, String[] suffixes, boolean ignoreCase) {
        if (LangUtil.isEmpty(inputs) || LangUtil.isEmpty(suffixes)) {
            return new String[0];
        }
        if (ignoreCase) {
            String[] temp = new String[suffixes.length];
            for (int i = 0; i < temp.length; i++) {                
				String suff = suffixes[i];
                temp[i] = (null ==  suff ? null : suff.toLowerCase());
			}
            suffixes = temp;
        }
        ArrayList result = new ArrayList();
		for (String s : inputs) {
			String input = s;
			if (null == input) {
				continue;
			}
			if (!ignoreCase) {
				input = input.toLowerCase();
			}
			for (String suffix : suffixes) {
				if (null == suffix) {
					continue;
				}
				if (input.endsWith(suffix)) {
					result.add(input);
					break;
				}
			}
		}
        return (String[]) result.toArray(new String[0]);
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
     * All sources must be readable at this time, 
     * unless spec.badInput is true (for invalid-input tests).
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
            || !validator.canRead(
                Globals.F_testingclient_jar,
                "testing-client.jar")) {
            return false;
        }

        this.sandbox = sandbox;

        String rdir = spec.testSrcDirOffset;
        File testBaseSrcDir;
        if ((null == rdir) || (0 == rdir.length())) {
            testBaseSrcDir = sandbox.testBaseDir;
        } else {
            testBaseSrcDir = new File(sandbox.testBaseDir, rdir);
            // XXX what if rdir is two levels deep?
            if (!validator
                .canReadDir(testBaseSrcDir, "sandbox.testBaseSrcDir")) {
                return false;
            }
        }

        // Sources come as relative paths - check read, copy if staging.
        // This renders paths absolute before run(RunStatusI) is called.
        // For a compile run to support relative paths + source base,
        // change so the run calculates the paths (differently when staging)

        final String[] inpathPaths;
        final String[] injarPaths;
        final String[] srcPaths;
        {
            final String[] paths = spec.getPathsArray();
            srcPaths =
                endsWith(
                    paths,
                    CompilerRun.SOURCE_SUFFIXES,
                    true);
            injarPaths =
                endsWith(paths, CompilerRun.JAR_SUFFIXES, true);
            inpathPaths =
                selectDirectories(paths, testBaseSrcDir);
            if (!spec.badInput) {
                int found = inpathPaths.length + injarPaths.length + srcPaths.length;
                if (paths.length !=  found) {
                    validator.fail("found " + found + " of " + paths.length + " sources");
                }
            }
        }
        
        // validate readable for sources
        if (!spec.badInput) {
            if (!validator.canRead(testBaseSrcDir, srcPaths, "sources")
                // see validation of inpathPaths below due to ambiguous base dir
                || !validator.canRead(
                    testBaseSrcDir,
                    spec.argfiles,
                    "argfiles")
                || !validator.canRead(
                    testBaseSrcDir,
                    spec.classpath,
                    "classpath")
                || !validator.canRead(
                    testBaseSrcDir,
                    spec.aspectpath,
                    "aspectpath")
                || !validator.canRead(
                    testBaseSrcDir,
                    spec.sourceroots,
                    "sourceroots") 
                || !validator.canRead(
                    testBaseSrcDir,
                    spec.extdirs,
                    "extdirs")) {
                return false;
            }
        }

        int numSources =
            srcPaths.length
                + injarPaths.length
                + inpathPaths.length
                + spec.argfiles.length
                + spec.sourceroots.length;
        if (!spec.badInput && (numSources < 1)) {
            validator.fail(
                "no input jars, arg files, or source files or roots");
            return false;
        }

        final File[] argFiles =
            FileUtil.getBaseDirFiles(testBaseSrcDir, spec.argfiles);
        final File[] injarFiles =
            FileUtil.getBaseDirFiles(testBaseSrcDir, injarPaths);
        final File[] inpathFiles =
            FileUtil.getBaseDirFiles(testBaseSrcDir, inpathPaths);
        final File[] aspectFiles =
            FileUtil.getBaseDirFiles(testBaseSrcDir, spec.aspectpath);
        final File[] extdirFiles =
            FileUtil.getBaseDirFiles(testBaseSrcDir, spec.extdirs);
        final File[] classFiles =
            FileUtil.getBaseDirFiles(testBaseSrcDir, spec.classpath);
        final File[] xlintFiles = (null == spec.xlintfile ? new File[0]
            : FileUtil.getBaseDirFiles(testBaseSrcDir, new String[] {spec.xlintfile}));

        // injars might be outjars in the classes dir...
        for (int i = 0; i < injarFiles.length; i++) {
            if (!injarFiles[i].exists()) {
                injarFiles[i] = new File(sandbox.classesDir, injarPaths[i]);
            }
        }
        for (int i = 0; i < inpathFiles.length; i++) {
            if (!inpathFiles[i].exists()) {
                inpathFiles[i] = new File(sandbox.classesDir, inpathPaths[i]);
            }
        }
        // moved after solving any injars that were outjars
        if (!validator.canRead(injarFiles, "injars")
                || !validator.canRead(injarFiles, "injars")) {
            return false;
        }
        
        // hmm - duplicates validation above, verifying getBaseDirFiles?
        if (!spec.badInput) {
            if (!validator.canRead(argFiles, "argFiles")
                || !validator.canRead(injarFiles, "injarFiles")
                || !validator.canRead(inpathFiles, "inpathFiles")
                || !validator.canRead(aspectFiles, "aspectFiles")
                || !validator.canRead(classFiles, "classFiles")
                || !validator.canRead(xlintFiles, "xlintFiles")) {
                return false;
            }
        }

        final File[] srcFiles;
        File[] sourcerootFiles = new File[0];
        // source text files are copied when staging incremental tests
        if (!spec.isStaging()) {
            // XXX why this? was always? || (testBaseSrcDir != sandbox.stagingDir))) {
            srcFiles =
                FileUtil.getBaseDirFiles(
                    testBaseSrcDir,
                    srcPaths,
                    CompilerRun.SOURCE_SUFFIXES);
            if (!LangUtil.isEmpty(spec.sourceroots)) {
                sourcerootFiles =
                    FileUtil.getBaseDirFiles(
                        testBaseSrcDir,
                        spec.sourceroots,
                        null);
            }
        } else { // staging - copy files
            if (spec.badInput) {
                validator.info(
                    "badInput ignored - files checked when staging");
            }
            try {
                // copy all files, then remove tagged ones
                // XXX make copyFiles support a filter?
                srcFiles =
                    FileUtil.copyFiles(
                        testBaseSrcDir,
                        srcPaths,
                        sandbox.stagingDir);
                if (!LangUtil.isEmpty(spec.sourceroots)) {
                    sourcerootFiles =
                        FileUtil.copyFiles(
                            testBaseSrcDir,
                            spec.sourceroots,
                            sandbox.stagingDir);
                    // delete incremental files in sourceroot after copying // XXX inefficient
                    // an incremental file has an extra "." in name
                    // most .java files don't, because they are named after
                    // the principle type they contain, and simple type names
                    // have no dots.
                    FileFilter pickIncFiles = new FileFilter() {
                        public boolean accept(File file) {
                            if (file.isDirectory()) {
                                // continue recursion
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
					for (File sourcerootFile : sourcerootFiles) {
						FileUtil.deleteContents(
								sourcerootFile,
								pickIncFiles,
								false);
					}
                    if (0 < sourcerootFiles.length) {
                        FileUtil.sleepPastFinalModifiedTime(
                            sourcerootFiles);
                    }
                }
                File[] files =
                    FileUtil.getBaseDirFiles(sandbox.stagingDir, srcPaths);
                if (0 < files.length) {
                    FileUtil.sleepPastFinalModifiedTime(files);
                }
            } catch (IllegalArgumentException e) {
                validator.fail("staging - bad input", e);
                return false;
            } catch (IOException e) {
                validator.fail("staging - operations", e);
                return false;
            }
        }
        if (!spec.badInput
            && !validator.canRead(srcFiles, "copied paths")) {
            return false;
        }
        arguments.clear();
        
        if (!LangUtil.isEmpty(xlintFiles)) {
            arguments.add("-Xlintfile");
            String sr = FileUtil.flatten(xlintFiles, null);
            arguments.add(sr);
        }
        if (spec.outjar != null) {
        	arguments.add("-outjar");
        	arguments.add(new File(sandbox.classesDir,spec.outjar).getPath());
        }
        if (!LangUtil.isEmpty(extdirFiles)) {
            arguments.add("-extdirs");
            String sr = FileUtil.flatten(extdirFiles, null);
            arguments.add(sr);
        }
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
        inpaths.clear();
        if (!LangUtil.isEmpty(inpathFiles)) {
            inpaths.addAll(Arrays.asList(FileUtil.getPaths(inpathFiles)));
        }
        if (!LangUtil.isEmpty(argFiles)) {
            String[] ra = FileUtil.getPaths(argFiles);
			for (String s : ra) {
				arguments.add("@" + s);
			}
            if (!spec.badInput && spec.isStaging) {
                validator.fail(
                    "warning: files listed in argfiles not staged");
            }
        }

        // save classpath and aspectpath in sandbox for this and other clients
        final boolean checkReadable = !spec.badInput;
        int size = spec.includeClassesDir ? 3 : 2;
        File[] cp = new File[size + classFiles.length];
        System.arraycopy(classFiles, 0, cp, 0, classFiles.length);
        int index = classFiles.length;
        if (spec.includeClassesDir) {
            cp[index++] = sandbox.classesDir;
        }
        cp[index++] = Globals.F_aspectjrt_jar;
        cp[index++] = Globals.F_testingclient_jar;
        sandbox.compilerRunInit(this, testBaseSrcDir, aspectFiles,
                checkReadable, cp, checkReadable, null);

        // XXX todo set bootclasspath if set for forking?
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
     * <li>construct a compiler using {@link Spec#compiler}
     *     or any overriding value set in TestSetup.<li>
     * <li>Just before running, set the compiler in the sandbox using 
     *     {@link Sandbox.setCompiler(ICommand)}.<li>
     * <li>After running, report AjcMessageHandler results to the status parameter.
     *     If the AjcMessageHandler reports a failure, then send info messages
     *     for the Spec, TestSetup, and command line.<li>
     * @see org.aspectj.testing.run.IRun#run(IRunStatus)
     */
    public boolean run(IRunStatus status) {
        if (null == spec.testSetup) {
            MessageUtil.abort(
                status,
                "no test setup - adoptParentValues not called");
            return false;
        } else if (!spec.testSetup.result) {
            MessageUtil.abort(status, spec.testSetup.failureReason);
            return false;
        }

        AjcMessageHandler handler =
            new AjcMessageHandler(spec.getMessages());
        handler.init();
        boolean handlerResult = false;
        boolean result = false;
        boolean commandResult = false;
        ArrayList argList = new ArrayList();
        final Spec.TestSetup setupResult = spec.testSetup;
        try {
        	if (spec.outjar == null) {
	            argList.add("-d");
	            String outputDirPath = sandbox.classesDir.getAbsolutePath();
	            try { // worth it to try for canonical?
	                outputDirPath = sandbox.classesDir.getCanonicalPath();
	            } catch (IOException e) {
	                MessageUtil.abort(
	                    status,
	                    "canonical " + sandbox.classesDir,
	                    e);
	            }
	            argList.add(outputDirPath);
        	}
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
                argList.add(
                    FileUtil.flatten(
                        (String[]) injars.toArray(new String[0]),
                        null));
            }

            if (0 < inpaths.size()) {
                argList.add("-inpath");
                argList.add(
                    FileUtil.flatten(
                        (String[]) inpaths.toArray(new String[0]),
                        null));
            }

            // put specified arguments last, for better badInput tests
            argList.addAll(setupResult.commandOptions);

            // add both java/aspectj and argfiles
            argList.addAll(arguments);

            // XXX hack - seek on request as a side effect. reimplement as listener 
            if (null != setupResult.seek) {
                String slopPrefix = Spec.SEEK_MESSAGE_PREFIX + " slop - ";
                PrintStream slop =
                    MessageUtil.handlerPrintStream(
                        status,
                        IMessage.INFO,
                        System.err,
                        slopPrefix);
                List found =
                    FileUtil.lineSeek(
                        setupResult.seek,
                        arguments,
                        false,
                        slop);
                if (!LangUtil.isEmpty(found)) {
					for (Object o : found) {
						MessageUtil.info(
								status,
								Spec.SEEK_MESSAGE_PREFIX + o);
					}
                }
            }
            ICommand compiler = spec.reuseCompiler
                // throws IllegalStateException if null
    ? sandbox.getCommand(this)
    : ReflectionFactory.makeCommand(setupResult.compilerName, status);
            DirChanges dirChanges = null;
            if (null == compiler) {
                MessageUtil.fail(
                    status,
                    "unable to make compiler " + setupResult.compilerName);
                return false;
            } else {
                if (setupResult.compilerName != Spec.DEFAULT_COMPILER) {
                    MessageUtil.info(
                        status,
                        "compiler: " + setupResult.compilerName);
                }
                if (status.aborted()) {
                    MessageUtil.debug(
                        status,
                        "aborted, but compiler valid?: " + compiler);
                } else {
                    // same DirChanges handling for JavaRun, CompilerRun, IncCompilerRun 
                    // XXX around advice or template method/class
                    if (!LangUtil.isEmpty(spec.dirChanges)) {
                        LangUtil.throwIaxIfFalse(
                            1 == spec.dirChanges.size(),
                            "expecting 0..1 dirChanges");
                        dirChanges =
                            new DirChanges(
                                (DirChanges.Spec) spec.dirChanges.get(0));
                        if (!dirChanges
                            .start(status, sandbox.classesDir)) {
                            return false; // setup failed
                        }
                    }
                    MessageUtil.info(
                        status,
                        compiler + "(" + argList + ")");
                    sandbox.setCommand(compiler, this);
                    String[] args = (String[]) argList.toArray(RA_String);
                    commandResult = compiler.runCommand(args, handler);
                }
            }
            handlerResult = handler.passed();
            if (!handlerResult) {
                return false;
            } else {
                result = (commandResult == handler.expectingCommandTrue());
                if (!result) {
                    String m =
                        commandResult
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
            handler.report(status);
            // XXX weak - actual messages not reported in real-time, no fast-fail
        }
    }

    public String toString() {
        return "CompilerRun(" + spec + ")";
    }

    /** 
     * Initializer/factory for CompilerRun
     * any path or file is relative to this test base dir
     */
    public static class Spec extends AbstractRunSpec {
        public static final String XMLNAME = "compile";
        public static final String DEFAULT_COMPILER =
            ReflectionFactory.ECLIPSE;
        static final String SEEK_PREFIX = "-seek:";
        static final String SEEK_MESSAGE_PREFIX = "found: ";

        private static final CRSOptions CRSOPTIONS = new CRSOptions();
        /**
          * Retitle description to title, paths to files, do comment,
          * staging, badInput,
          * do dirChanges, and print no chidren. 
          */
        private static final AbstractRunSpec.XMLNames NAMES =
            new AbstractRunSpec.XMLNames(
                AbstractRunSpec.XMLNames.DEFAULT,
                "title",
                null,
                null,
                null,
                "files",
                null,
                null,
                null,
                false,
                false,
                true);

        /**
         * If the source version warrants, add a -bootclasspath
         * entry to the list of arguments to add. 
         * This will fail and return an error String if the 
         * required library is not found.
         * @param sourceVersion the String (if any) describing the -source option
         *        (expecting one of [null, "1.3", "1.4", "1.5"].
         * @param compilerName the String name of the target compiler
         * @param toAdd the ArrayList to add -bootclasspath to
         * @return the String describing any errors, or null if no errors
         */
        private static String updateBootclasspathForSourceVersion(
            String sourceVersion,
            String compilerName,
            List toAdd) {
            if (null == sourceVersion) {
                return null;
            }
            if (3 != sourceVersion.length()) {
                throw new IllegalArgumentException(
                    "bad version: " + sourceVersion);
            }
            if (null == toAdd) {
                throw new IllegalArgumentException("null toAdd");
            }
            int version = sourceVersion.charAt(2) - '0';
            switch (version) {
                case (3) :
                    if (Globals.supportsJava("1.4")) {
                        if (!FileUtil.canReadFile(Globals.J2SE13_RTJAR)) {
                            return "no 1.3 libraries to handle -source 1.3";
                        }
                        toAdd.add("-bootclasspath");
                        toAdd.add(Globals.J2SE13_RTJAR.getAbsolutePath());
                    }
                    break;
                case (4) :
                    if (!Globals.supportsJava("1.4")) {
                        if (ReflectionFactory
                            .ECLIPSE
                            .equals(compilerName)) {
                            return "run eclipse under 1.4 to handle -source 1.4";
                        }
                        if (!FileUtil.canReadFile(Globals.J2SE14_RTJAR)) {
                            return "no 1.4 libraries to handle -source 1.4";
                        }
                        toAdd.add("-bootclasspath");
                        toAdd.add(Globals.J2SE14_RTJAR.getAbsolutePath());
                    }
                    break;
                case (5) :
                    return "1.5 not supported in CompilerRun";
                case (0) :
                    // ignore - no version specified
                    break;
                default :
                    throw new Error("unexpected version: " + version);
            }
            return null;
        }

        static CRSOptions testAccessToCRSOptions() {
            return CRSOPTIONS;
        }

        static Options testAccessToOptions() {
            return CRSOPTIONS.getOptions();
        }

        private static String[] copy(String[] input) {
            if (null == input) {
                return null;
            }
            String[] result = new String[input.length];
            System.arraycopy(input, 0, result, 0, input.length);
            return result;
        }

        protected String compiler;

        // use same command - see also IncCompiler.Spec.fresh
        protected boolean reuseCompiler;
        protected boolean permitAnyCompiler;
        protected boolean includeClassesDir;

        protected TestSetup testSetup;

        protected String[] argfiles = new String[0];
        protected String[] aspectpath = new String[0];
        protected String[] classpath = new String[0];
        protected String[] sourceroots = new String[0];
        protected String[] extdirs = new String[0];

        /** src path = {suiteParentDir}/{testBaseDirOffset}/{testSrcDirOffset}/{path} */
        protected String testSrcDirOffset;
        protected String xlintfile;
        protected String outjar;

        public Spec() {
            super(XMLNAME);
            setXMLNames(NAMES);
            compiler = DEFAULT_COMPILER;
        }

        protected void initClone(Spec spec)
            throws CloneNotSupportedException {
            super.initClone(spec);
            spec.argfiles = copy(argfiles);
            spec.aspectpath = copy(aspectpath);
            spec.classpath = copy(classpath);
            spec.compiler = compiler;
            spec.includeClassesDir = includeClassesDir;
            spec.reuseCompiler = reuseCompiler;
            spec.permitAnyCompiler = permitAnyCompiler;
            spec.sourceroots = copy(sourceroots);
            spec.extdirs = copy(extdirs);
            spec.outjar = outjar;
            spec.testSetup = null;
            if (null != testSetup) {
                spec.testSetup = (TestSetup) testSetup.clone();
            }
            spec.testSrcDirOffset = testSrcDirOffset;
        }

        public Object clone() throws CloneNotSupportedException {
            Spec result = new Spec();
            initClone(result);
            return result;
        }

        public void setIncludeClassesDir(boolean include) {
            this.includeClassesDir = include;
        }
        public void setReuseCompiler(boolean reuse) {
            this.reuseCompiler = reuse;
        }

        public void setPermitAnyCompiler(boolean permitAny) {
            this.permitAnyCompiler = permitAny;
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
        public void setXlintfile(String path) {
             xlintfile = path;
        }
        
        public void setOutjar(String path) {
        	outjar = path;
        }

        /** 
         * Set extension dirs, deleting any old ones
         * @param files comma-delimited list of directories
         *  - ignored if null or empty
         */
        public void setExtdirs(String dirs) {
            if (!LangUtil.isEmpty(dirs)) {
                extdirs = XMLWriter.unflattenList(dirs);
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
            return (String[]) copy(argfiles);
        }
        
        /**
         * Make a copy of the array.
         * @return an array with the same component type as source
         * containing same elements, even if null.
         * @throws IllegalArgumentException if source is null
         */
        public static final Object[] copy(Object[] source) {
            LangUtil.throwIaxIfNull(source, "source");        
            final Class c = source.getClass().getComponentType();
            Object[] result = (Object[]) Array.newInstance(c, source.length);
            System.arraycopy(source, 0, result, 0, result.length);
            return result;
        }

        /**
         * This implementation skips if:
         * <ul>
         * <li>incremental test, but using ajc (not eclipse)</li>
         * <li>usejavac, but javac is not available on the classpath</li>
         * <li>eclipse, but -usejavac or -preprocess test</li>
         * <li>-source 1.4, but running under 1.2 (XXX design)</li>
         * <li>local/global option conflicts (-lenient/-strict)</li>
         * <li>semantic conflicts (e.g., -lenient/-strict)</li>
         * </ul>
         * @return false if this wants to be skipped, true otherwise
         */
        protected boolean doAdoptParentValues(
            RT parentRuntime,
            IMessageHandler handler) {
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
            String compilerClassName = compiler;
            if (null != testSetup) {
                compilerClassName = testSetup.compilerName;
            }
            if (null != compilerClassName) {
                int loc = compilerClassName.lastIndexOf(".");
                if (-1 != loc) {
                    compilerClassName =
                        compilerClassName.substring(loc + 1);
                }
            }
            return compilerClassName;
        }

        /** @return a CompilerRun with this as spec if setup completes successfully. */
        public IRunIterator makeRunIterator(
            Sandbox sandbox,
            Validator validator) {
            CompilerRun run = new CompilerRun(this);
            if (run.setupAjcRun(sandbox, validator)) {
                // XXX need name for compilerRun
                return new WrappedRunIterator(this, run);
            }
            return null;
        }

        protected String getPrintName() {
            return "CompilerRun.Spec " + getShortCompilerName();
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
         * <p>
         * This also interprets any relevant System properties,
         * e.g., from <code>JavaRun.BOOTCLASSPATH_KEY</code>.
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
         * This means the test is skipped. 
         * @return TestSetup with results 
         *          (TestSetup result=false if the run should not continue)
         */
        protected TestSetup setupArgs(IMessageHandler handler) {
            // warning: HarnessSelectionTest checks for specific error wording
            final Spec spec = this;
            final TestSetup result = new TestSetup();
            result.compilerName = spec.compiler;
            // urk - s.b. null, but expected
            Values values = gatherValues(result);

            if ((null == values) || (null != result.failureReason)) {
                return checkResult(result);
            }

            // send info messages about
            // forced staging when -incremental
            // or staging but no -incremental flag
            Option.Family getFamily =
                CRSOPTIONS.crsIncrementalOption.getFamily();
            final boolean haveIncrementalFlag =
                (null != values.firstInFamily(getFamily));

            if (spec.isStaging()) {
                if (!haveIncrementalFlag) {
                    MessageUtil.info(
                        handler,
                        "staging but no -incremental flag");
                }
            } else if (haveIncrementalFlag) {
                spec.setStaging(true);
                MessageUtil.info(handler, "-incremental forcing staging");
            }

            if (hasInvalidOptions(values, result)) {
                return checkResult(result);
            }

            // set compiler in result
            getFamily = CRSOPTIONS.ajccompilerOption.getFamily();
            Option.Value compiler = values.firstInFamily(getFamily);
            if (null != compiler) {
                result.compilerName 
                    = CRSOPTIONS.compilerClassName(compiler.option);
                if (null == result.compilerName) {
                    result.failureReason =
                        "unable to get class name for " + compiler;
                    return checkResult(result);
                }
            }
            String compilerName =
                (null == result.compilerName
                    ? spec.compiler
                    : result.compilerName);
            // check compiler semantics
            if (hasCompilerSpecErrors(compilerName, values, result)) {
                return checkResult(result);
            }
            // add toadd and finish result
            ArrayList args = new ArrayList();
            String[] rendered = values.render();
            if (!LangUtil.isEmpty(rendered)) {
                args.addAll(Arrays.asList(rendered));
            }
            // update bootclasspath
            getFamily = CRSOPTIONS.crsSourceOption.getFamily();
            Option.Value source = values.firstInFamily(getFamily);
            if (null != source) {
                String sourceVersion = source.unflatten()[1];
                ArrayList toAdd = new ArrayList();
                /*String err =*/
                    updateBootclasspathForSourceVersion(
                        sourceVersion,
                        spec.compiler,
                        toAdd);
                args.addAll(toAdd);
            }
            result.commandOptions = args;
            result.result = true;
            return checkResult(result);
        }

        /**
         * Ensure exit invariant:
         * <code>result.result == (null == result.failureReason)
         *       == (null != result.commandOptions)</code>
         * @param result the TestSetup to verify
         * @return result
         * @throws Error if invariant is not true
         */
        TestSetup checkResult(TestSetup result) {
            String err = null;
            if (null == result) {
                err = "null result";
            } else if (result.result != (null == result.failureReason)) {
                err =
                    result.result
                        ? "expected no failure: " + result.failureReason
                        : "fail for no reason";
            } else if (result.result != (null != result.commandOptions)) {
                err =
                    result.result
                        ? "expected command options"
                        : "unexpected command options";
            }
            if (null != err) {
                throw new Error(err);
            }
            return result;
        }

        boolean hasInvalidOptions(Values values, TestSetup result) {
            // not supporting 1.0 options any more
			for (Object o : CRSOPTIONS.invalidOptions) {
				Option option = (Option) o;
				if (null != values.firstOption(option)) {
					result.failureReason =
							"invalid option in harness: " + option;
					return true;
				}
			}
            return false;
        }

        boolean hasCompilerSpecErrors(
            String compilerName,
            Values values,
            TestSetup result) {
            /*
             * Describe any semantic conflicts between options.
             * This skips:
             * - old 1.0 options, including lenient v. strict
             * - old ajc options, include !incremental and usejavac w/o javac
             * - invalid eclipse options (mostly ajc) 
             * @param compilerName the String name of the target compiler
             * @return a String describing any conflicts, or null if none
             */
            if (!permitAnyCompiler
                && (!(ReflectionFactory.ECLIPSE.equals(compilerName)
                    || ReflectionFactory.OLD_AJC.equals(compilerName)
                    || CRSOptions.AJDE_COMPILER.equals(compilerName)
                    || CRSOptions.AJCTASK_COMPILER.equals(compilerName)
                    || permitAnyCompiler 
                    ))) {
                    //|| BUILDER_COMPILER.equals(compilerName))
                result.failureReason =
                    "unrecognized compiler: " + compilerName;
                return true;
            }
            // not supporting ajc right now
            if (null
                != values.firstOption(CRSOPTIONS.ajccompilerOption)) {
                result.failureReason = "ajc not supported";
                return true;
            }
            // not supporting 1.0 options any more
			for (Object o : CRSOPTIONS.ajc10Options) {
				Option option = (Option) o;
				if (null != values.firstOption(option)) {
					result.failureReason = "old ajc 1.0 option: " + option;
					return true;
				}
			}

            return false;
        }

        protected Values gatherValues(TestSetup result) {
            final Spec spec = this;
            // ---- local option values
            final Values localValues;
            final Options options = CRSOPTIONS.getOptions();
            try {
                String[] input = getOptionsArray();
                // this handles reading options, 
                // flattening two-String options, etc.
                localValues = options.acceptInput(input);
                // all local values should be picked up
                String err = Options.missedMatchError(input, localValues);
                if (!LangUtil.isEmpty(err)) {
                    result.failureReason = err;
                    return null;
                }
            } catch (InvalidInputException e) {
                result.failureReason = e.getFullMessage();
                return null;
            }

            // ---- global option values
            StringBuffer errs = new StringBuffer();
            final Values globalValues =
                spec.runtime.extractOptions(options, true, errs);
            if (errs.length() > 0) {
                result.failureReason = errs.toString();
                return null;
            }
            final Values combined =
                Values.wrapValues(
                    new Values[] { localValues, globalValues });

            String err = combined.resolve();
            if (null != err) {
                result.failureReason = err;
                return null;
            }

            return handleTestArgs(combined, result);
        }

        //            final int len = globalValues.length() + localValues.length();
        //            final Option.Value[] combinedValues = new Option.Value[len];
        //            System.arraycopy(
        //                globalValues,
        //                0,
        //                combinedValues,
        //                0,
        //                globalValues.length());
        //            System.arraycopy(
        //                localValues,
        //                0,
        //                combinedValues,
        //                globalValues.length(),
        //                localValues.length());
        //
        //            result.compilerName = spec.compiler;
        //            if (0 < combinedValues.length) {
        //                // this handles option forcing, etc.
        //                String err = Options.resolve(combinedValues);
        //                if (null != err) {
        //                    result.failureReason = err;
        //                    return null;
        //                }
        //                if (!handleTestArgs(combinedValues, result)) {
        //                    return null;
        //                }
        //            }
        //            return Values.wrapValues(combinedValues);
        //        }
        /**
         * This interprets and nullifies values for the test.
         * @param values the Option.Value[] being processed
         * @param result the TestSetup to modify
         * @return false if error (caller should return), true otherwise
         */
        Values handleTestArgs(Values values, final TestSetup result) {
            final Option.Family compilerFamily =
                CRSOPTIONS.ajccompilerOption.getFamily();
            Values.Selector selector = new Values.Selector() {
                protected boolean accept(Option.Value value) {
                    if (null == value) {
                        return false;
                    }
                    Option option = value.option;
                    if (compilerFamily.sameFamily(option.getFamily())) {
                        if (value.prefix.isSet()) {
                            String compilerClass
                                = CRSOPTIONS.compilerClassName(option);
                            if (null == compilerClass) {
                                result.failureReason =
                                    "unrecognized compiler: " + value;
                                throw Values.Selector.STOP;
                            }
                            if (!CRSOPTIONS.compilerIsLoadable(option)) {
                                result.failureReason =
                                    "unable to load compiler: " + option;
                                throw Values.Selector.STOP;
                            }
                            result.compilerName = compilerClass;
                        }
                        return true;
                    } else if (
                        CRSOPTIONS.crsIgnoreWarnings.sameOptionIdentifier(
                            option)) {
                        result.ignoreWarnings = value.prefix.isSet();
                        result.ignoreWarningsSet = true;
                        return true;
                    }
                    return false;
                }
            };
            return values.nullify(selector);
        }

        //        /**
        //         * This interprets and nullifies values for the test.
        //         * @param values the Option.Value[] being processed
        //         * @param result the TestSetup to modify
        //         * @return false if error (caller should return), true otherwise
        //         */
        //        boolean handleTestArgs(Option.Value[] values, TestSetup result) {
        //            if (!LangUtil.isEmpty(values)) {
        //                for (int i = 0; i < values.length; i++) {
        //                    Option.Value value = values[i];
        //                    if (null == value) {
        //                        continue;
        //                    }
        //                    Option option = value.option;
        //                    if (option.sameOptionFamily(ECLIPSE_OPTION)) {
        //                        if (!value.prefix.isSet()) {
        //                            values[i] = null;
        //                            continue;
        //                        }
        //                        String compilerClass =
        //                            (String) COMPILER_OPTION_TO_CLASSNAME.get(
        //                                option);
        //                        if (null == compilerClass) {
        //                            result.failureReason =
        //                                "unrecognized compiler: " + value;
        //                            return false;
        //                        }
        //                        result.compilerName = compilerClass;
        //                        values[i] = null;
        //                    } else if (
        //                        option.sameOptionFamily(crsIgnoreWarnings)) {
        //                        result.ignoreWarnings = value.prefix.isSet();
        //                        result.ignoreWarningsSet = true;
        //                        values[i] = null;
        //                    }
        //                }
        //            }
        //            return true;
        //        }

        // XXX need keys, cache...
        /** @return index of global in argList, ignoring first char */
        protected int indexOf(String global, ArrayList argList) {
            int max = argList.size();
            for (int i = 0; i < max; i++) {
                if (global
                    .equals(((String) argList.get(i)).substring(1))) {
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
            if (reuseCompiler) {
                out.printAttribute("reuseCompiler", "true");
            }
// test-only feature
//            if (permitAnyCompiler) {
//                out.printAttribute("permitAnyCompiler", "true");
//            }
            if (includeClassesDir) {
                out.printAttribute("includeClassesDir", "true");
            }
            if (!LangUtil.isEmpty(argfiles)) {
                out.printAttribute(
                    "argfiles",
                    XMLWriter.flattenFiles(argfiles));
            }
            if (!LangUtil.isEmpty(aspectpath)) {
                out.printAttribute(
                    "aspectpath",
                    XMLWriter.flattenFiles(aspectpath));
            }
            if (!LangUtil.isEmpty(sourceroots)) {
                out.printAttribute(
                    "sourceroots",
                    XMLWriter.flattenFiles(sourceroots));
            }
            if (!LangUtil.isEmpty(extdirs)) {
                out.printAttribute(
                    "extdirs",
                    XMLWriter.flattenFiles(extdirs));
            }
            out.endAttributes();
            if (!LangUtil.isEmpty(dirChanges)) {
                DirChanges.Spec.writeXml(out, dirChanges);
            }
            SoftMessage.writeXml(out, getMessages());
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
            List commandOptions;

            public Object clone() {
                TestSetup testSetup = new TestSetup();
                testSetup.compilerName = compilerName;
                testSetup.ignoreWarnings = ignoreWarnings;
                testSetup.ignoreWarningsSet = ignoreWarningsSet;
                testSetup.result = result;
                testSetup.failureReason = failureReason;
                testSetup.seek = seek;
                if (null != commandOptions) {
                    testSetup.commandOptions = new ArrayList();
                    testSetup.commandOptions.addAll(commandOptions);
                }
                return testSetup;
            }
            public String toString() {
                return "TestSetup("
                    + (null == compilerName ? "" : compilerName + " ")
                    + (!ignoreWarningsSet
                        ? ""
                        : (ignoreWarnings ? "" : "do not ")
                            + "ignore warnings ")
                    + (result ? "" : "setup failed")
                    + ")";
            }
        }

        /**
         * Options-related stuff in the spec.
         */
        static class CRSOptions {
            //    static final String BUILDER_COMPILER =
            //        "org.aspectj.ajdt.internal.core.builder.Builder.Command";
            static final String AJDE_COMPILER =
                CompileCommand.class.getName();

            static final String AJCTASK_COMPILER =
                AjcTaskCompileCommand.class.getName();

            private final Map compilerOptionToLoadable = new TreeMap();
            /*
            * The options field in a compiler test permits some arbitrary
            * command-line options to be set.  It does not permit things
            * like classpath,  aspectpath, files, etc. which are set
            * using other fields in the test specification, so the options
            * permitted are a subset of those permitted on the command-line.
            * 
            * Global options specified on the harness command-line are
            * adopted for the compiler command-line if they are permitted
            * in the options field.  That means we have to detect each
            * permitted option, rather than just letting all through
            * for the compiler.
            * 
            * Conversely, some options are targeted not at the compiler,
            * but at the test itself (e.g., to ignore warnings, or to 
            * select a compiler.
            * 
            * The harness can run many compilers, and they differ in
            * which options are permitted.  You can specify a compiler
            * as an option (e.g., -eclipse).  So the set of constraints
            * on the list of permitted options can differ from test to test.
            * 
            * The following code sets up the lists of permitted options
            * and breaks out subsets for different compiler-variant checks.
            * Most options are created but not named, but some options
            * are named to detect corresponding values for further 
            * processing.  e.g., the compiler options are saved so 
            * we can do per-compiler option verification.
            *
            */
            private final Options crsOptions;
            private final Family compilerFamily;
            private final Option crsIncrementalOption;
            private final Option crsSourceOption;
            // these are options handled/absorbed by CompilerRun
            private final Option crsIgnoreWarnings;
            private final Option eclipseOption;
            private final Option buildercompilerOption;
            private final Option ajdecompilerOption;
            private final Option javacOption;
            private final Option ajctaskcompilerOption;
            private final Option ajccompilerOption;
            private final Map compilerOptionToClassname;
            private final Set compilerOptions;
            // compiler verification - permit but flag ajc 1.0 options
            private final List ajc10Options;
            private final List invalidOptions;

            private CRSOptions() {
                crsOptions = new Options(true);
                Option.Factory factory = new Option.Factory("CompilerRun");
                // compiler options go in map            
                eclipseOption =
                    factory.create(
                        "eclipse",
                        "compiler",
                        Option.FORCE_PREFIXES,
                        false);
                compilerFamily = eclipseOption.getFamily();
                buildercompilerOption =
                    factory.create(
                        "builderCompiler",
                        "compiler",
                        Option.FORCE_PREFIXES,
                        false);
                ajctaskcompilerOption =
                    factory.create(
                        "ajctaskCompiler",
                        "compiler",
                        Option.FORCE_PREFIXES,
                        false);
                ajdecompilerOption =
                    factory.create(
                        "ajdeCompiler",
                        "compiler",
                        Option.FORCE_PREFIXES,
                        false);
                ajccompilerOption =
                    factory.create(
                        "ajc",
                        "compiler",
                        Option.FORCE_PREFIXES,
                        false);
                javacOption =
                    factory.create(
                        "javac",
                        "compiler",
                        Option.FORCE_PREFIXES,
                        false);

                Map map = new TreeMap();
                map.put(eclipseOption, ReflectionFactory.ECLIPSE);
                //map.put(BUILDERCOMPILER_OPTION, BUILDER_COMPILER);
                map.put(
                    ajctaskcompilerOption,
                    AJCTASK_COMPILER);
                map.put(ajdecompilerOption, AJDE_COMPILER);
                map.put(ajccompilerOption, ReflectionFactory.OLD_AJC);
                //map.put(JAVAC_OPTION, "XXX javac option not supported");
                compilerOptionToClassname =
                    Collections.unmodifiableMap(map);

                compilerOptions =
                    Collections.unmodifiableSet(
                        compilerOptionToClassname.keySet());
                // options not permitted in the harness
                List list = new ArrayList();
                list.add(factory.create("workingdir"));
                list.add(factory.create("argfile"));
                list.add(factory.create("sourceroots"));
                list.add(factory.create("outjar"));
                invalidOptions = Collections.unmodifiableList(list);

                // other options added directly
                crsIncrementalOption = factory.create("incremental");

                crsIgnoreWarnings = factory.create("ignoreWarnings");

                crsSourceOption =
                    factory
                        .create(
                            "source",
                            "source",
                            Option.FORCE_PREFIXES,
                            false,
                            new String[][] { new String[] { "1.3", "1.4", "1.5" }
                });

                // ajc 1.0 options
                // workingdir above in invalid options
                list = new ArrayList();
                list.add(factory.create("usejavac"));
                list.add(factory.create("preprocess"));
                list.add(factory.create("nocomment"));
                list.add(factory.create("porting"));
                list.add(factory.create("XOcodeSize"));
                list.add(factory.create("XTargetNearSource"));
                list.add(factory.create("XaddSafePrefix"));
                list.add(
                    factory.create(
                        "lenient",
                        "lenient",
                        Option.FORCE_PREFIXES,
                        false));
                list.add(
                    factory.create(
                        "strict",
                        "lenient",
                        Option.FORCE_PREFIXES,
                        false));
                ajc10Options = Collections.unmodifiableList(list);

                // -warn:.. and -g/-g:.. are not exclusive
                if (!(factory.setupFamily("debug", true)
                    && factory.setupFamily("warning", true))) {
                    System.err.println("CompilerRun debug/warning fail!");
                }
                Option[] options =
                    new Option[] {
                        crsIncrementalOption,
                        crsIgnoreWarnings,
                        crsSourceOption,
                        factory.create(
                            "Xlint",
                            "XLint",
                            Option.FORCE_PREFIXES,
                            true),
                        factory.create("verbose"),
                        factory.create("emacssym"),
                        factory.create("referenceInfo"),
                        factory.create("nowarn"),
                        factory.create("deprecation"),
                        factory.create("noImportError"),
                        factory.create("proceedOnError"),
                        factory.create("preserveAllLocals"),
                        factory.create(
                            "warn",
                            "warning",
                            Option.STANDARD_PREFIXES,
                            true),
                        factory.create(
                            "g",
                            "debug",
                            Option.STANDARD_PREFIXES,
                            false),
                        factory.create(
                            "g:",
                            "debug",
                            Option.STANDARD_PREFIXES,
                            true),
                        factory.create(
                            "1.3",
                            "compliance",
                            Option.FORCE_PREFIXES,
                            false),
                        factory.create(
                            "1.4",
                            "compliance",
                            Option.FORCE_PREFIXES,
                            false),
                        factory.create(
                            "1.5",
                            "compliance",
                            Option.FORCE_PREFIXES,
                            false),
                        factory
                            .create(
                                "target",
                                "target",
                                Option.FORCE_PREFIXES,
                                false,
                                new String[][] { new String[] {
                                    "1.1",
                                    "1.2",
                                    "1.3",
                                    "1.4",
                                    "1.5" }}),
                        factory.create("XnoInline"),
                        factory.create("XterminateAfterCompilation"),
                        factory.create("Xreweavable"),
                        factory.create("XnotReweavable"),
                        factory.create("XserializableAspects")
                    };
                    
                // among options not permitted: extdirs...

				for (Option option : options) {
					crsOptions.addOption(option);
				}
				for (Object compilerOption : compilerOptions) {
					crsOptions.addOption((Option) compilerOption);
				}
                // these are recognized but records with them are skipped
				for (Object ajc10Option : ajc10Options) {
					crsOptions.addOption((Option) ajc10Option);
				}
                crsOptions.freeze();
            }

            Options getOptions() {
                return crsOptions;
            }
            
            /** 
             * @return unmodifiable Set of options sharing the
             * family "compiler".
             */
            Set compilerOptions() {
                return compilerOptions;
            }

            /**
             * @param option the compiler Option to get name for
             * @return null if option is null or not a compiler option,
             *     or the fully-qualified classname of the ICommand
             *     implementing the compiler.
             */
            String compilerClassName(Option option) {
                if ((null == option)
                    || (!compilerFamily.sameFamily(option.getFamily()))) {
                    return null;
                }
                return (String) compilerOptionToClassname.get(option);
            }

            /**
             * Check that the compiler class associated with a compiler
             * option can be loaded.  This check only happens once;
             * the result is cached (by compilerOption key) for later calls.
             * @param compilerOption the Option (family compiler) to check
             * @return true if compiler class for this option can be loaded
             */
            boolean compilerIsLoadable(Option compilerOption) {
                LangUtil.throwIaxIfNull(compilerOption, "compilerName");
                synchronized (compilerOptionToLoadable) {
                    Boolean result =
                        (Boolean) compilerOptionToLoadable.get(
                            compilerOption);
                    if (null == result) {
                        MessageHandler sink = new MessageHandler();
                        String compilerClassname =
                            (String) compilerOptionToClassname.get(
                                compilerOption);
                        if (null == compilerClassname) {
                            result = Boolean.FALSE;
                        } else {
                            ICommand c =
                                ReflectionFactory.makeCommand(
                                    compilerClassname,
                                    sink);

                            if ((null == c)
                                || sink.hasAnyMessage(
                                    IMessage.ERROR,
                                    true)) {
                                result = Boolean.FALSE;
                            } else {
                                result = Boolean.TRUE;
                            }
                        }
                        compilerOptionToLoadable.put(
                            compilerOption,
                            result);
                    }
                    return result;
                }
            }
        } // CompilerRun.Spec.CRSOptions
    } // CompilerRun.Spec
} // CompilerRun