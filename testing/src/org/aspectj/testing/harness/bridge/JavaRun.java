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

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.Tester;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.WrappedRunIterator;
import org.aspectj.testing.util.TestClassLoader;
import org.aspectj.testing.xml.SoftMessage;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Run a class in this VM using reflection.
 * Forked mode supported, but through system properties:
 * - javarun.fork: anything to enable forking
 * - javarun.java: path to java executable (optional)
 * - javarun.java.home: JAVA_HOME for java (optional)
 *   (normally requires javarun.java)
 * - javarun.classpath: a prefix to the run classpath (optional)
 */
public class JavaRun implements IAjcRun {
    public static String FORK_KEY = "javarun.fork";
    public static String JAVA_KEY = "javarun.java";
    public static String JAVA_HOME_KEY = "javarun.java.home";
    public static String BOOTCLASSPATH_KEY = "javarun.bootclasspath";
    private static final boolean FORK;
    private static final String JAVA;
    private static final String JAVA_HOME;
    static final String BOOTCLASSPATH;
    static {
        FORK = (null != getProperty(FORK_KEY));
        JAVA = getProperty(JAVA_KEY);
        JAVA_HOME = getProperty(JAVA_HOME_KEY);
        BOOTCLASSPATH = getProperty(BOOTCLASSPATH_KEY);
    }
    private static String getProperty(String key) {
        try {
            return System.getProperty(key);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void appendClasspath(StringBuffer cp, File[] libs, File[] dirs) {
        if (!LangUtil.isEmpty(BOOTCLASSPATH)) {
            cp.append(BOOTCLASSPATH);
            cp.append(File.pathSeparator);    
        }
        for (int i = 0; i < dirs.length; i++) {
            cp.append(dirs[i].getAbsolutePath());
            cp.append(File.pathSeparator);    
        }
        for (int i = 0; i < libs.length; i++) {
            cp.append(libs[i].getAbsolutePath());
            cp.append(File.pathSeparator);    
        }
        // ok to have trailing path separator I guess...
    }
    
    Spec spec;
	private Sandbox sandbox;   

    /** programmatic initialization per spec */
    public JavaRun(Spec spec) {
        this.spec = spec;
    }
    // XXX init(Spec)
    
    /**
     * This checks the spec for a class name
     * and checks the sandbox for a readable test source directory,
     * a writable run dir, and (non-null, possibly-empty) lists
     * of readable classpath dirs and jars.
	 * @return true if all checks pass
     * @see org.aspectj.testing.harness.bridge.AjcTest.IAjcRun#setup(File, File)
	 */
	public boolean setupAjcRun(Sandbox sandbox, Validator validator) {
		this.sandbox = sandbox;        
		return (validator.nullcheck(spec.className, "class name")
            && validator.nullcheck(sandbox, "sandbox")
            && validator.canReadDir(sandbox.getTestBaseSrcDir(this), "testBaseSrc dir")
            && validator.canWriteDir(sandbox.runDir, "run dir")
            && validator.canReadFiles(sandbox.getClasspathJars(true, this), "classpath jars")
            && validator.canReadDirs(sandbox.getClasspathDirectories(true, this, true), "classpath dirs")
            );            
        
	}
    
    /** caller must record any exceptions */
    public boolean run(IRunStatus status)
        throws IllegalAccessException,
                InvocationTargetException,
                ClassNotFoundException,
                NoSuchMethodException {
        boolean completedNormally = false;
        if (!LangUtil.isEmpty(spec.dirChanges)) {
            MessageUtil.info(status, "XXX dirChanges not implemented in JavaRun");
        }
        try {
            final boolean readable = true;
            File[] libs = sandbox.getClasspathJars(readable, this);
            boolean includeClassesDir = true;
            File[] dirs = sandbox.getClasspathDirectories(readable, this, includeClassesDir);
            completedNormally = FORK // || spec.fork
                ? runInOtherVM(status, libs, dirs)
                : runInSameVM(status, libs, dirs);
        } finally {
            if (!completedNormally) {
                MessageUtil.info(status, spec.toLongString());
                MessageUtil.info(status, "sandbox: " + sandbox);
            }
        }
        return completedNormally;
    }
    protected boolean runInSameVM(IRunStatus status, File[] libs, File[] dirs) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ClassLoader loader = null;
        URL[] urls = FileUtil.getFileURLs(libs);
        boolean completedNormally = false;
        Class targetClass = null;
        try {
            loader = new TestClassLoader(urls, dirs);
            // make the following load test optional
            // Class testAspect = loader.loadClass("org.aspectj.lang.JoinPoint");
            targetClass = loader.loadClass(spec.className);
            Method main = targetClass.getMethod("main", Globals.MAIN_PARM_TYPES);
            setupTester(sandbox.getTestBaseSrcDir(this), loader, status);
            main.invoke(null, new Object[] { spec.getOptionsArray() });
            completedNormally = true;
        } catch (ClassNotFoundException e) {
            String[] classes = FileUtil.listFiles(sandbox.classesDir);
            MessageUtil.info(status, "sandbox.classes: " + Arrays.asList(classes));
            MessageUtil.fail(status, null, e);
        } finally {
            if (!completedNormally) {
                MessageUtil.info(status, "targetClass: " + targetClass);
                MessageUtil.info(status, "loader: " + loader);
            }
        }
        return completedNormally;
    }
    
    /**
     * Run in another VM by grabbing Java, bootclasspath, classpath, etc.
     * This assumes any exception or output to System.err is a failure,
     * and any normal completion is a pass.
     * @param status
     * @param libs
     * @param dirs
     * @return
     */
    protected boolean runInOtherVM(IRunStatus status, File[] libs, File[] dirs) {
        ArrayList cmd = new ArrayList();
        String classpath;
        {
            StringBuffer cp = new StringBuffer();
            if (!LangUtil.isEmpty(BOOTCLASSPATH)) {
                cp.append(BOOTCLASSPATH);
                cp.append(File.pathSeparator);
            }
            appendClasspath(cp, libs, dirs);
            classpath = cp.toString();
        }
        String java = JAVA;
        if (null == java) {
            File jfile = LangUtil.getJavaExecutable(classpath);
            if (null == jfile) {
                throw new IllegalStateException("Unable to get java");
            }
            java = jfile.getAbsolutePath();
        } 
        cmd.add(java);
        cmd.add("-classpath");
        cmd.add(classpath);
        cmd.add(spec.className);
        cmd.addAll(spec.options);
        String[] command = (String[]) cmd.toArray(new String[0]);

        final IMessageHandler handler = status;
        // setup to run asynchronously, pipe streams through, and report errors
        class DoneFlag {
            boolean done;
            boolean failed;
        }
        final StringBuffer commandLabel = new StringBuffer();
        final DoneFlag doneFlag = new DoneFlag();
        LangUtil.ProcessController controller
            = new LangUtil.ProcessController() {
                protected void doCompleting(Thrown ex, int result) {
                    if (!ex.thrown && (0 == result)) {
                        doneFlag.done = true;
                        return; // no errors
                    }
                    // handle errors
                    String context = spec.className 
                        + " command \"" 
                        + commandLabel 
                        + "\"";
                    if (null != ex.fromProcess) {
                        String m = "Exception running " + context;
                        MessageUtil.abort(handler, m, ex.fromProcess);
                        doneFlag.failed = true;
                    } else if (0 != result) {
                        String m = result + " result code from running " + context;
                        MessageUtil.fail(handler, m);
                        doneFlag.failed = true;
                    }
                    if (null != ex.fromInPipe) {
                        String m = "Error processing input pipe for " + context;
                        MessageUtil.abort(handler, m, ex.fromInPipe);
                        doneFlag.failed = true;
                    }
                    if (null != ex.fromOutPipe) {
                        String m = "Error processing output pipe for " + context;
                        MessageUtil.abort(handler, m, ex.fromOutPipe);
                        doneFlag.failed = true;
                    }
                    if (null != ex.fromErrPipe) {
                        String m = "Error processing error pipe for " + context;
                        MessageUtil.abort(handler, m, ex.fromErrPipe);
                        doneFlag.failed = true;
                    }
                    doneFlag.done = true;
                }
            };
        controller.init(command, spec.className);
        if (null != JAVA_HOME) {
            controller.setEnvp(new String[] {"JAVA_HOME=" + JAVA_HOME});
        }
        commandLabel.append(Arrays.asList(controller.getCommand()).toString());
        final ByteArrayOutputStream errSnoop 
            = new ByteArrayOutputStream();
        controller.setErrSnoop(errSnoop);
        controller.start();
        // give it 3 minutes...
        long maxTime = System.currentTimeMillis() + 3 * 60 * 1000;
        boolean waitingForStop = false;
        while (!doneFlag.done) {
            if (maxTime < System.currentTimeMillis()) {
                if (waitingForStop) { // hit second timeout - bail
                    break;
                }
                MessageUtil.fail(status, "timeout waiting for process"); 
                doneFlag.failed = true;
                controller.stop(); 
                // wait 1 minute to evaluate results of stopping
                waitingForStop = true;
                maxTime = System.currentTimeMillis() + 1 * 60 * 1000;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (0 < errSnoop.size()) {
            MessageUtil.error(handler, errSnoop.toString());
            if (!doneFlag.failed) {
                doneFlag.failed = true;
            } 
        }
        if (doneFlag.failed) {
            MessageUtil.info(handler, "other-vm command-line: " + commandLabel);
        }
        return !doneFlag.failed;
    }
    
    /**
     * Clear (static) testing state and setup base directory,
     * unless spec.skipTesting.
     * @return null if successful, error message otherwise
     */
    protected void setupTester(File baseDir, ClassLoader loader, IMessageHandler handler) {
        if (null == loader) {
            setupTester(baseDir, handler);
            return;
        }
        File baseDirSet = null;
        try {
            if (!spec.skipTester) { 
                Class tc = loader.loadClass("org.aspectj.testing.Tester");
                // Tester.clear();
                Method m = tc.getMethod("clear", new Class[0]);
                m.invoke(null, new Object[0]);             
                // Tester.setMessageHandler(handler);
                m = tc.getMethod("setMessageHandler", new Class[] {IMessageHandler.class});
                m.invoke(null, new Object[] { handler});             
                
                //Tester.setBASEDIR(baseDir);
                m = tc.getMethod("setBASEDIR", new Class[] {File.class});
                m.invoke(null, new Object[] { baseDir});             

                //baseDirSet = Tester.getBASEDIR();
                m = tc.getMethod("getBASEDIR", new Class[0]);
                baseDirSet = (File) m.invoke(null, new Object[0]);             
                
                if (!baseDirSet.equals(baseDir)) {
                    String l = "AjcScript.setupTester() setting "
                             + baseDir + " returned " + baseDirSet;
                    MessageUtil.debug(handler, l);
                }
            }
        } catch (Throwable t) {
            MessageUtil.abort(handler, "baseDir=" + baseDir, t);
        }
    }

    /**
     * Clear (static) testing state and setup base directory,
     * unless spec.skipTesting.
     * This implementation assumes that Tester is defined for the
     * same class loader as this class.
     * @return null if successful, error message otherwise
     */
    protected void setupTester(File baseDir, IMessageHandler handler) {
        File baseDirSet = null;
        try {
            if (!spec.skipTester) {                
                Tester.clear();
                Tester.setMessageHandler(handler);
                Tester.setBASEDIR(baseDir);
                baseDirSet = Tester.getBASEDIR();
                if (!baseDirSet.equals(baseDir)) {
                    String l = "AjcScript.setupTester() setting "
                             + baseDir + " returned " + baseDirSet;
                    MessageUtil.debug(handler, l);
                }
            }
        } catch (Throwable t) {
            MessageUtil.abort(handler, "baseDir=" + baseDir, t);
        }
    }
	public String toString() {
        return "JavaRun(" + spec + ")";
	}
        
    /** 
     * Initializer/factory for JavaRun.
     * The classpath is not here but precalculated in the Sandbox. XXX libs?
     */
    public static class Spec extends AbstractRunSpec {
        public static final String XMLNAME = "run";
        /**
         * skip description, skip sourceLocation, 
         * do keywords, do options, skip paths, do comment,
         * skip staging,   skip badInput,
         * do dirChanges, do messages but skip children. 
         */
        private static final XMLNames NAMES = new XMLNames(XMLNames.DEFAULT,
                "", "", null, null, "", null, "", "", false, false, true);
                
        /** fully-qualified name of the class to run */
        protected String className;
        
        /** minimum required version of Java, if any */
        protected String javaVersion;
        
        /** if true, skip Tester setup (e.g., if Tester n/a) */
        protected boolean skipTester;
        
        public Spec() {
            super(XMLNAME);
            setXMLNames(NAMES);
        }
        
        /**
         * @param version "1.1", "1.2", "1.3", "1.4"
         * @throws IllegalArgumentException if version is not recognized
         */
        public void setJavaVersion(String version) {
            LangUtil.supportsJava(version);
            this.javaVersion = version;
        }
        
        /** @className fully-qualified name of the class to run */
        public void setClassName(String className) {
            this.className = className;
        }
        
        /** @param skip if true, then do not set up Tester */
        public void setSkipTester(boolean skip) {
            skipTester = skip;
        }
        
        /** override to set dirToken to Sandbox.RUN_DIR */
        public void addDirChanges(DirChanges.Spec spec) {
            if (null == spec) {
                return;
            }
            spec.setDirToken(Sandbox.RUN_DIR);
            super.addDirChanges(spec);
        }

        /** @return a JavaRun with this as spec if setup completes successfully. */
        public IRunIterator makeRunIterator(Sandbox sandbox, Validator validator) {
            JavaRun run = new JavaRun(this);
            if (run.setupAjcRun(sandbox, validator)) {
                // XXX need name for JavaRun
                return new WrappedRunIterator(this, run);
            }
            return null;
        }

        /** 
         * Write this out as a run element as defined in
         * AjcSpecXmlReader.DOCTYPE.
         * @see AjcSpecXmlReader#DOCTYPE 
         * @see IXmlWritable#writeXml(XMLWriter) 
         */
        public void writeXml(XMLWriter out) {
            String attr = XMLWriter.makeAttribute("class", className);
            out.startElement(xmlElementName, attr, false);
            if (skipTester) {
                out.printAttribute("skipTester", "true");
            }
            if (null != javaVersion) {
                out.printAttribute("vm", javaVersion);
            }
            super.writeAttributes(out);
            out.endAttributes();
            if (!LangUtil.isEmpty(dirChanges)) {
                DirChanges.Spec.writeXml(out, dirChanges);
            }
            SoftMessage.writeXml(out, getMessages());
            out.endElement(xmlElementName);
        }
        public String toLongString() {
            return toString() + "[" + super.toLongString() + "]";        
        }
        
        public String toString() {
            if (skipTester) {
                return "JavaRun(" + className + ", skipTester)";
            } else {
                return "JavaRun(" + className + ")";
            }
        }
        
        /**
         * This implementation skips if:
         * <ul>
         * <li>current VM is not at least any specified javaVersion </li>
         * </ul>
         * @return false if this wants to be skipped, true otherwise
         */
        protected boolean doAdoptParentValues(RT parentRuntime, IMessageHandler handler) {
            if (!super.doAdoptParentValues(parentRuntime, handler)) {
                return false;
            }
            if ((null != javaVersion) && (!LangUtil.supportsJava(javaVersion))) {
                skipMessage(handler, "requires Java version " + javaVersion);
                return false;
            }
            return true;
        }  
     }
}
