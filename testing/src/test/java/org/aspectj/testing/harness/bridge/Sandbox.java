/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.testing.taskdefs.AjcTaskCompileCommand;
import org.aspectj.testing.util.Diffs;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * A sandbox holds state shared by AjcTest sub-runs,
 * mostly directories relevant to testing.
 * It permits a limited amount of coordination and
 * setup/cleanup operations (todo XXX).
 * <p>
 * AjcTest creates the Sandbox and initializes the final fields.
 * To coordinate with each other, run components may set and get values, 
 * with the sources running first and the sinks second.  
 * To make the interactions clear 
 * (and to avoid accidentally violating these semantics),
 * setters/getters for a coordinated property are constrained two ways:
 * <li>Both have an extra (typed) "caller" parameter which must not
 *     be null, authenticating that the caller is known & valid.</li>
 * <li>A getter throws IllegalStateException if called before the setter</li>
 * <li>A setter throws IllegalStateException if called after the getter<li>
 * XXX subclass more general sandbox?
 */
public class Sandbox {
    /** classes directory token for DirChanges.Spec */
    public static final String RUN_DIR = "run";
    
    /** run directory token for DirChanges.Spec */
    public static final String CLASSES_DIR = "classes";
    
    private static boolean canRead(File dir) {
        return ((null != dir) && dir.isDirectory() && dir.canRead());
    }

    private static boolean canWrite(File dir) {
        return ((null != dir) && dir.isDirectory() && dir.canWrite());
    }

    private static void iaxWrite(File dir, String label) {
        if (!canWrite(dir)) {
            throw new IllegalArgumentException(label + " - " + dir);
        }
    }

    private static void iaxRead(File dir, String label) {
        if (!canRead(dir)) {
            throw new IllegalArgumentException(label + " - " + dir);
        }
    }
    
    /** @throws IllegalStateException(message) if test */
    private static void assertState(boolean test, String message) {
        if (!test) {
            throw new IllegalStateException(message);
        }
    }
    
    /** 
     * The (read-only) base of the test sources (which may or may not
     * be the base of the java sources)
     */
    public final File testBaseDir;
    
    /** the parent of a temporary workspace, probably includes some others */
    public final File sandboxDir;

    /** a shared working dir */
    public final File workingDir;

    /** a shared classes dir */
    public final File classesDir;

    /** a run dir (which will be ignored in non-forking runs) */
    public final File runDir;
    
    /** staging directory for IAjcRun requiring files be copied, deleted, etc. */
    public final File stagingDir;
    
    /** 
     * This manages creation and deletion of temporary directories.
     * We hold a reference so that our clients can signal whether 
     * this should be deleted.
     */
    private final Validator validator; // XXX required after completing tests? 
    
    /** original base of the original java sources, set by CompileRun.setup(..) */
    private File testBaseSrcDir;

    /** directories and libraries on the classpath, set by CompileRun.setup(..)  */
    private File[] compileClasspath;

    private String bootClasspath;
    
    /** aspectpath entries, set by CompileRun.setup(..)  */
    private File[] aspectpath;

    /** track whether classpath getter ran */
    private boolean gotClasspath;
        
    /** command shared between runs using sandbox - i.e., compiler */
    private ICommand command;

    /** track whether command getter ran */
    private boolean gotCommand;
        
    /** cache results of rendering final fields */
    private transient String toStringLeader;
    
    private transient boolean compilerRunInit;
    
    /** @throws IllegalArgumentException unless validator validates
     *           testBaseDir as readable
     */
    public Sandbox(File testBaseDir, Validator validator) {
        LangUtil.throwIaxIfNull(validator, "validator");
        this.validator = validator;
        Sandbox.iaxRead(testBaseDir, "testBaseDir");
        this.testBaseDir = testBaseDir;
        {
           File baseDir = FileUtil.getTempDir("Sandbox");
           if (!baseDir.isAbsolute()) {
               baseDir = baseDir.getAbsoluteFile();
           }
           sandboxDir = baseDir;
        }
        Sandbox.iaxWrite(sandboxDir, "sandboxDir"); // XXX not really iax

        workingDir = FileUtil.makeNewChildDir(sandboxDir, "workingDir");
        Sandbox.iaxWrite(workingDir, "workingDir");             

        classesDir = FileUtil.makeNewChildDir(sandboxDir, "classes");
        Sandbox.iaxWrite(classesDir, "classesDir");             

        runDir = FileUtil.makeNewChildDir(sandboxDir, "run");
        Sandbox.iaxWrite(runDir, "runDir"); 

        stagingDir = FileUtil.makeNewChildDir(sandboxDir, "staging");
        Sandbox.iaxWrite(stagingDir, "stagingDir"); 

        validator.registerSandbox(this);        
    }

    private String getToStringLeader() {
        if (null == toStringLeader) {
            toStringLeader = "Sandbox(" + sandboxDir.getName() 
                + ", " + testBaseSrcDir.getName(); 
        }
        return toStringLeader;
    }
    
    /** @return "Sandbox(sandbox, src, classes)" with names only */
    public String toString() {
        return getToStringLeader() + ", " + classesDir.getName() + ")";
    }
    
    /** @return "Sandbox(sandbox, src, classes)" with paths */
    public String toLongString() {
        return getToStringLeader() + ", " + classesDir.getPath()
            + (null == command ? ", (null command)" : ", " + command) + ")";
    }
    
    void setCommand(ICommand command, CompilerRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller"); 
        LangUtil.throwIaxIfNull(command, "command"); 
        LangUtil.throwIaxIfFalse(!gotCommand, "no command"); 
        this.command = command;
    }
    
    /** When test is completed, clear the compiler to avoid memory leaks */
    void clearCommand(AjcTest caller) {
        LangUtil.throwIaxIfNull(caller, "caller"); 
        if (null != command) { // need to add ICommand.quit()
            if (command instanceof AjcTaskCompileCommand) { // XXX urk!
                ((AjcTaskCompileCommand) command).quit();
            }
            command = null;
        }
        // also try to clear sandbox/filesystem.  
        // If locked by suite, we can't.
        if (null != validator) {
            validator.deleteTempFiles(true);
        }
    }
    
//    /** 
//     * Populate the staging directory by copying any files in the
//     * source directory ending with fromSuffix 
//     * to the staging directory, after renaming them with toSuffix.
//     * If the source file name starts with "delete", then the
//     * corresponding file in the staging directory is deleting.
//     * @return a String[] of the files copied or deleted
//     *          (path after suffix changes and relative to staging dir)
//     * @throws Error if no File using fromSuffix are found
//     */
//    String[] populateStagingDir(String fromSuffix, String toSuffix, IAjcRun caller) {
//        LangUtil.throwIaxIfNull(fromSuffix, "fromSuffix");
//        LangUtil.throwIaxIfNull(toSuffix, "toSuffix");
//        LangUtil.throwIaxIfNull(caller, "caller");
//
//        ArrayList result = new ArrayList();
//        FileUtil.copyDir(
//            srcBase,
//            targetSrc,
//            fromSuffix,
//            toSuffix,
//            collector);
//
//        final String canonicalFrom = srcBase.getCanonicalPath();
//        final Definition[] defs = getDefinitions(srcBase);
//        if ((null == defs) || (defs.length < 9)) {
//            throw new Error("did not get definitions");
//        }
//        MessageHandler compilerMessages = new MessageHandler();
//        StringBuffer commandLine = new StringBuffer();
//        for (int i = 1; result && (i < 10); i++) { 
//            String fromSuffix = "." + i + "0.java";
//            // copy files, collecting as we go...
//            files.clear();
//            if (0 == files.size()) { // XXX detect incomplete?
//                break;
//            }
//
//        
//        return (String[]) result.toArray(new String[0]);        
//    }
        
    // XXX move to more general in FileUtil
    void reportClassDiffs(
        final IMessageHandler handler, 
        IncCompilerRun caller,
        long classesDirStartTime,
        String[] expectedSources) {
        LangUtil.throwIaxIfFalse(0 < classesDirStartTime, "0 >= " + classesDirStartTime);
        boolean acceptPrefixes = true;
        Diffs diffs = org.aspectj.testing.util.FileUtil.dirDiffs(
            "classes", 
            classesDir, 
            classesDirStartTime, 
            ".class", 
            expectedSources, 
            acceptPrefixes);
        diffs.report(handler, IMessage.ERROR);
    }

//    // XXX replace with IMessage-based implementation
//    // XXX move to more general in FileUtil
//    void reportClassesDirDiffs(final IMessageHandler handler, IncCompilerRun caller,
//                               String[] expectedSources) {
//        // normalize sources to ignore
//        final ArrayList sources = new ArrayList();
//        if (!LangUtil.isEmpty(expectedSources)) {
//            for (int i = 0; i < expectedSources.length; i++) {
//                String srcPath = expectedSources[i];
//                int clip = FileUtil.sourceSuffixLength(srcPath);
//                if (0 != clip) {
//                    srcPath = srcPath.substring(0, srcPath.length() - clip);
//                    sources.add(FileUtil.weakNormalize(srcPath));
//                } else if (srcPath.endsWith(".class")) {
//                    srcPath = srcPath.substring(0, srcPath.length() - 6);
//                    sources.add(FileUtil.weakNormalize(srcPath));
//                } else {
//                    MessageUtil.info(handler, "not source file: " + srcPath);
//                }
//			}
//        }
//        
//        // gather, normalize paths changed
//        final ArrayList changed = new ArrayList();
//        FileFilter touchedCollector = new FileFilter() {
//			public boolean accept(File file) {
//                if (file.lastModified() > classesDirTime) {
//                    String path = file.getPath();
//                    if (!path.endsWith(".class")) {
//                        MessageUtil.info(handler, "changed file not a class: " + file);
//                    } else {
//                        String classPath = path.substring(0, path.length() - 6);
//                        classPath = FileUtil.weakNormalize(classPath);
//                        if (sources.contains(classPath)) {
//                            sources.remove(classPath);
//                        } else {
//                            changed.add(classPath);
//                        }                      
//                    }
//                }
//                return false;
//			}
//        };      
//        classesDir.listFiles(touchedCollector);
//        
//        // report any unexpected changes
//        Diffs diffs = new Diffs("classes", sources, changed, String.CASE_INSENSITIVE_ORDER);
//        diffs.report(handler, IMessage.ERROR);
//    }
    
    ICommand getCommand(CompilerRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        assertState(null != command, "command never set"); 
        return command;
    }

    ICommand getCommand(IncCompilerRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        assertState(null != command, "command never set"); 
        return command;
    }

    File getTestBaseSrcDir(IncCompilerRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        return testBaseSrcDir;
    }
    
    /**
     * Get the files with names (case-sensitive)
     * under the staging or test base directories.
     * @param names
     * @return
     */
    File[] findFiles(final String[] names) {
        ArrayList result = new ArrayList();
        NamesFilter filter = new NamesFilter(names);
        File[] bases = { testBaseDir, sandboxDir };
		for (File base : bases) {
			if ((null == base) || !base.canRead()) {
				continue;
			}
			result.addAll(Arrays.asList(FileUtil.listFiles(base, filter)));
		}
        return (File[]) result.toArray(new File[0]);
    }
    File getTestBaseSrcDir(JavaRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        return testBaseSrcDir;
    }
    
    void defaultTestBaseSrcDir(JavaRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        if (null != testBaseSrcDir) {
            throw new IllegalStateException("testBaseSrcDir not null");
        }
        testBaseSrcDir = testBaseDir;
    }
    
    static boolean readableDir(File dir) {
        return ((null != dir) && dir.isDirectory() && dir.canRead());
    }

    void compilerRunInit(CompilerRun caller, File testBaseSrcDir,
            File[] aspectPath, boolean aspectpathReadable,
            File[] classPath, boolean classpathReadable,
            String bootclassPath
            ) {
        if (null != testBaseSrcDir) {
            setTestBaseSrcDir(testBaseSrcDir, caller);
        }
        if ((null != aspectPath) && (0 < aspectPath.length)) {
            setAspectpath(aspectPath, aspectpathReadable, caller);
        }
        if ((null != classPath) && (0 < classPath.length)) {
            setClasspath(classPath, classpathReadable, caller);
        }
        
        setBootclasspath(bootclassPath, caller);
        compilerRunInit = true;
    }
    void javaRunInit(JavaRun caller) {
        if (!compilerRunInit) {
            testBaseSrcDir = testBaseDir;
            // default to aspectjrt.jar?
            compileClasspath = new File[0]; 
            
        }
    }

    /** @throws IllegalArgumentException unless a readable directory */
    private void setTestBaseSrcDir(File dir, CompilerRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        if ((null == dir) || !dir.isDirectory() || !dir.canRead()) {
            throw new IllegalArgumentException("bad test base src dir: " + dir);
        }
        testBaseSrcDir = dir;
    }
    
    /** 
     * Set aspectpath.
     * @param readable if true, then throw IllegalArgumentException if not readable 
     */
    private void setAspectpath(File[] files, boolean readable, CompilerRun caller) {
        LangUtil.throwIaxIfNull(files, "files");
        LangUtil.throwIaxIfNull(caller, "caller");
        assertState(null == aspectpath, "aspectpath already written");
        aspectpath = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            LangUtil.throwIaxIfNull(files[i], "files[i]");
            if (readable && !files[i].canRead()) {
                throw new IllegalArgumentException("bad aspectpath entry: " + files[i]);
            }
            aspectpath[i] = files[i];
        }
    }

    /**
     * Set bootclasspath, presumed to be delimited by
     * File.pathSeparator and have valid entries.
     * @param bootClasspath
     * @param caller
     */
    private void setBootclasspath(String bootClasspath, CompilerRun caller) {
        this.bootClasspath = bootClasspath;
    }
    
    /** 
     * Set compile classpath.
     * @param readable if true, then throw IllegalArgumentException if not readable 
     */
    private void setClasspath(File[] files, boolean readable, CompilerRun caller) {
        LangUtil.throwIaxIfNull(files, "files");
        LangUtil.throwIaxIfNull(caller, "caller");
        assertState(!gotClasspath, "classpath already read");
        compileClasspath = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            LangUtil.throwIaxIfNull(files[i], "files[i]");
            if (readable && !files[i].canRead()) {
                throw new IllegalArgumentException("bad classpath entry: " + files[i]);
            }
            compileClasspath[i] = files[i];
		}
    }

//    /**
//     * Get run classpath 
//     * @param caller unused except to restrict usage to non-null JavaRun.
//     * @throws IllegalStateException if compileClasspath was not set.
//     * @throws IllegalArgumentException if caller is null
//     */
//    File[] getRunClasspath(JavaRun caller) {
//        LangUtil.throwIaxIfNull(caller, "caller");
//        assertState(null != compileClasspath, "classpath not set");
//        int compilePathLength = compileClasspath.length;
//        int aspectPathLength = (null == aspectpath ? 0 : aspectpath.length);
//        File[] result = new File[aspectPathLength + compilePathLength];
//        System.arraycopy(compileClasspath, 0, result, 0, compilePathLength);
//        if (0 < aspectPathLength) {
//            System.arraycopy(aspectpath, 0, result, compilePathLength, aspectPathLength);
//        }
//        return result;
//    }
    
    /** 
     * Get directories for the run classpath by selecting them
     * from the compile classpath.
     * This ignores aspectpath since it may contain only jar files.
     * @param readable if true, omit non-readable directories 
     */
    File[] getClasspathDirectories(
        boolean readable, 
        JavaRun caller, 
        boolean includeOutput) {
        LangUtil.throwIaxIfNull(caller, "caller");
        assertState(null != compileClasspath, "classpath not set");
        ArrayList result = new ArrayList();
        File[] src = compileClasspath;
		for (File f : src) {
			if ((null != f) && (f.isDirectory()) && (!readable || f.canRead())) {
				result.add(f);
			}
		}
        if (includeOutput && (null != classesDir) 
            && (!readable || classesDir.canRead())) {
            result.add(classesDir);                
        }
        return (File[]) result.toArray(new File[0]);
    }

    /** 
     * Get the jars belonging on the run classpath, including classpath
     * and aspectpath entries.
     * @param readable if true, omit non-readable directories 
     */
    File[] getClasspathJars(boolean readable, JavaRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        assertState(null != compileClasspath, "classpath not set");
        ArrayList result = new ArrayList();
        File[][] src = new File[][] { compileClasspath, aspectpath };
		for (File[] paths : src) {
			int len = (null == paths ? 0 : paths.length);
			for (int j = 0; j < len; j++) {
				File f = paths[j];
				if (FileUtil.isZipFile(f) && (!readable || f.canRead())) {
					result.add(f);
				}
			}
		}
        return (File[]) result.toArray(new File[0]);
    }
    
    /**
     * Get the list of aspect jars as a String. 
     * @return String of classpath entries delimited internally by File.pathSeparator 
     */
    String aspectpathToString(CompilerRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        return FileUtil.flatten(aspectpath, File.pathSeparator);
    }
    
    /** 
     * Get the compile classpath as a String.
     * @return String of classpath entries delimited internally by File.pathSeparator 
     */
    String classpathToString(CompilerRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        return FileUtil.flatten(compileClasspath, File.pathSeparator);
    }
        
    /** 
     * Get the bootClasspath as a String.
     * @return String of bootclasspath entries delimited internally by File.pathSeparator 
     */
    String getBootclasspath(CompilerRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        return bootClasspath;
    }

    /** 
     * Get the bootClasspath as a String.
     * @return String of bootclasspath entries delimited internally by File.pathSeparator 
     */
    String getBootclasspath(JavaRun caller) {
        LangUtil.throwIaxIfNull(caller, "caller");
        return bootClasspath;
    }
    private static class NamesFilter implements FileFilter {
        private final String[] names;
        private NamesFilter(String[] names) {
            this.names = names;
        }
        public boolean accept(File file) {
            if (null != file) {
                String name = file.getName();
                if ((null != name) && (null != names)) {
					for (String s : names) {
						if (name.equals(s)) {
							return true;
						}
					}
                }
            }
            return false;
        }
    }
}
