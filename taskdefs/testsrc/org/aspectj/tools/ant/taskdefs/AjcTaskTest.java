/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC)
 *               2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

import java.io.*;
import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Some API tests, but mostly functional tests driving
 * the task execute using data in ../taskdefs/testdata.
 * This will re-run in forked mode for any nonfailing
 * compile if aspectjtools-dist is built into
 * ../aj-build/dist/tools/lib/aspectjtools.jar.
 */
public class AjcTaskTest extends TestCase {

    private static final Class NO_EXCEPTION = null;
	private static final String NOFILE = "NOFILE";
	
    private static final File tempDir;
    private static final String aspectjtoolsJar;
    private static final String testdataDir;
    private static final StringBuffer MESSAGES = new StringBuffer();
    
    static {
        tempDir = new File("IncrementalAjcTaskTest-temp");            
        String toolsPath = "../aj-build/dist/tools/lib/aspectjtools.jar";
        File toolsjar = new File(toolsPath);
        if (toolsjar.canRead()) {
            aspectjtoolsJar = toolsjar.getAbsolutePath();
        } else {
            aspectjtoolsJar = null;
            String s = 
                "AjcTaskTest not forking - build aspectjtools-dist to get " 
                + toolsPath;
            System.out.println(s);
        }
        File dir = new File("../taskdefs/testdata");
        if (dir.canRead() && dir.isDirectory()) {
            testdataDir = dir.getAbsolutePath();
        } else {
            testdataDir = null;
        }
    }
    
    /** 
      * Check that aspectjtools are found on the classpath,
      * reporting any errors to System.err.
      * 
      * Run multiple times with different classpaths.
      * This should find variants
      * aspectjtools.jar,
      * aspectj-tools.jar,
      * aspectj-tools-1.1.jar, and
      * aspectjtools-1.0.6.jar
      * but not
      * aspectjrt.jar or
      * aspectj/tools.jar.
      * XXX use testing aspect to stub out 
      * <code>System.getProperty("java.class.path")</code>
      * @param args a String[], first is expected path, if any
      */
     public static void main(String[] args) {
         java.io.File toolsjar = AjcTask.findAspectjtoolsJar();
         if ((null == args) || (0 == args.length)) {
             if (null != toolsjar) {
                 System.err.println("FAIL - not expected: " + toolsjar);
             }
         } else if ("-help".equals(args[0])) {
             System.out.println("java " + AjcTaskTest.class.getName()
                   + " <expectedPathToAspectjtoolsJar>");
         } else if (null == toolsjar) {
             System.err.println("FAIL - expected: " + args[0]);
         } else {
             String path = toolsjar.getAbsolutePath();
             if (!path.equals(args[0])) {
                 System.err.println("FAIL - expected: "
                     + args[0]
                     + " actual: "
                     + path
                     );
             }
         }
     }
     
     public static void collectMessage(String s) {
         MESSAGES.append(s);
     }
     
     private static void deleteTempDir() {
        if ((null != tempDir) && tempDir.exists()) {
            FileUtil.deleteContents(tempDir);
            tempDir.delete();
        }
    }    
    private static final File getTempDir() {
        return tempDir;
    }    
	
    public AjcTaskTest(String name) {
		super(name);
	}

    public void tearDown() {
        deleteTempDir();
        MESSAGES.setLength(0);
    }
    
    public void testLimitTo() {
        int numArgs = 100;
        String arg = "123456789";
        String[] args = new String[numArgs];
        for (int i = 0; i < args.length; i++) {
            args[i] = arg;
        }
        // no limit
        int max = numArgs*(arg.length() + 1);
        Location location = new Location("AjcTaskTest.java");
        String[] newArgs = AjcTask.GuardedCommand.limitTo(args, max, location);
        assertTrue("same", args == newArgs);

        // limited - read file and verify arguments
        max--;
        newArgs = AjcTask.GuardedCommand.limitTo(args, max, location);
        assertTrue("not same", args != newArgs);
        assertTrue("not null", null != newArgs);
        String label = "newArgs " + Arrays.asList(newArgs);
        assertTrue("size 2" + label, 2 == newArgs.length);
        assertEquals("-argfile", newArgs[0]);
        File file = new File(newArgs[1]);
        assertTrue("readable newArgs[1]" + label, file.canRead());
        FileReader fin = null;
        try {
            fin = new FileReader(file);
            BufferedReader reader = new BufferedReader(fin);
            String line;
            int i = 0;
            while (null != (line = reader.readLine())) {
                assertEquals(i + ": ", args[i++], line);
            }
            assertEquals("num entries", i, args.length);
        } catch (IOException e) {
            assertTrue("IOException " + e.getMessage(), false);
        } finally {
            if (null != fin) {
                try {
                    fin.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            file.delete();
        }
    }

    public void testFindAspectjtoolsJar() {
        File toolsJar = AjcTask.findAspectjtoolsJar();
        if (null != toolsJar) {
            assertNull("tools jar found?: " + toolsJar, toolsJar);
        }
        // not found when unit testing b/c not on system classpath
        // so just checking for exceptions.
        // XXX need aspect to stub out System.getProperty(..) 
    }
    
    protected AjcTask getTask(String input) {
        AjcTask task = new AjcTask();
        Project p = new Project();
        task.setProject(p);
        task.setDestdir(getTempDir());
        if (NOFILE.equals(input)) {
        	// add nothing
        } else if (input.endsWith(".lst")) {
        	if (-1 != input.indexOf(",")) {
                throw new IllegalArgumentException("lists not supported: " + input);
        	} else if (null == testdataDir) {
                throw new Error("testdata not found - run in ../taskdefs");
            } else {
                String path = testdataDir + File.separator + input;
        		task.setArgfiles(new Path(task.getProject(), path));
        	}
        } else if ((input.endsWith(".java") || input.endsWith(".aj"))) {
        	// not working
        	FilenameSelector fns = new FilenameSelector();
        	fns.setName(input);
        	task.addFilename(fns);
        } else {
        	File dir = new File(input);
        	if (dir.canRead() && dir.isDirectory()) {
        		task.setSourceRoots(new Path(task.getProject(), input));
        	}        
        }
        task.setClasspath(new Path(p, "../lib/test/aspectjrt.jar"));
        task.setVerbose(true); // XXX    
        return task;
    }
    
    /** used in testMessageHolderClassName */
    public static class Holder extends MessageHandler {
        public Holder() {}
        public boolean handleMessage(IMessage message) {
            IMessage.Kind kind = message.getKind();
            if (IMessage.ERROR.isSameOrLessThan(kind)) {
                String m = kind.toString();
                AjcTaskTest.collectMessage(m.substring(0,1));
            }
            return true;
        }
    }
    
    public void testMessageHolderClassName() {
        AjcTask task = getTask("compileError.lst");
        task.setFailonerror(false);
        MESSAGES.setLength(0);
        runTest(task, null, MessageHolderChecker.ONE_ERROR,
            Holder.class.getName());
        String result = MESSAGES.toString();
        MESSAGES.setLength(0);
        assertEquals("messages", "e", result);        
    }

    public void testDefaultListForkedNoTools() {
        AjcTask task = getTask("default.lst");
        task.setFork(true);
        boolean passed = false;
        try {
            runTest(task, BuildException.class, MessageHolderChecker.NONE);
            passed = true;
        } finally {
            if (!passed) {
                String m = "AjcTaskTest.testDefaultListForkedNoTools()"
                    + " fails if aspectjtools.jar is on the classpath";
                System.err.println(m);
            }
        }
    }

    public void testDefaultListForkedIncremental() {
        AjcTask task = getTask("default.lst");
        task.setFork(true);
        task.setIncremental(true);
        runTest(task, BuildException.class, MessageHolderChecker.NONE);
    }

    /** failonerror should default to true, unlike other booleans */
    public void testCompileErrorFailOnErrorDefault() {
        AjcTask task = getTask("compileError.lst");
        runTest(task, BuildException.class, MessageHolderChecker.ONE_ERROR);
    }
    
    public void testDefaultList() {
        AjcTask task = getTask("default.lst");
        runTest(task, NO_EXCEPTION, MessageHolderChecker.INFOS);
    }

    public void testCompileErrorList() {
        AjcTask task = getTask("compileError.lst");
        task.setFailonerror(false);
        runTest(task, NO_EXCEPTION, MessageHolderChecker.ONE_ERROR);
    }

    public void testCompileWarningList() {
        AjcTask task = getTask("compileWarning.lst");
        runTest(task, NO_EXCEPTION, MessageHolderChecker.ONE_WARNING);
    }

    public void testNoSuchFileList() {
        AjcTask task = getTask("NoSuchFile.lst");
        task.setFailonerror(false);
        runTest(task, NO_EXCEPTION, MessageHolderChecker.ONE_ERROR_ONE_ABORT);
    }

    public void testClasspath() {
        AjcTask task = getTask(NOFILE);
        String[] cmd = task.makeCommand();
        String classpath = null;
        String bootclasspath = null;
        for (int i = 0; i < cmd.length; i++) {
            if ("-classpath".equals(cmd[i])) {
                classpath = cmd[i+1];
            } else if ("-bootclasspath".equals(cmd[i])) {
                bootclasspath = cmd[i+1];
            }        
        }
        assertTrue("not expecting bootclasspath", 
            null == bootclasspath);
        assertTrue("expecting aspectj in classpath", 
            (-1 != classpath.indexOf("aspectjrt.jar")));
    }
    
    // ---------------------------------------- sourcefile
    // XXX need to figure out how to specify files directly programmatically
//    public void testDefaultFile() {
//        AjcTask task = getTask("testdata/Default.java");
//        runTest(task, NO_EXCEPTION, MessageHolderChecker.INFOS);
//    }

    public void testNoFile() {
        AjcTask task = getTask(NOFILE);
        task.setFailonerror(false);
        runTest(task, NO_EXCEPTION, MessageHolderChecker.ONE_ERROR_ONE_ABORT);
    }
    
    public void testCompileErrorFile() {
        AjcTask task = getTask("compileError.lst");
        task.setFailonerror(false);
        runTest(task, NO_EXCEPTION, MessageHolderChecker.ONE_ERROR);
    }

    public void testCompileWarningFile() {
        AjcTask task = getTask("compileWarning.lst");
        task.setFailonerror(false);
        runTest(task, NO_EXCEPTION, MessageHolderChecker.ONE_WARNING);
    }

    public void testNoSuchFile() {
        AjcTask task = getTask("NoSuchFile.lst");
        task.setFailonerror(false);
        runTest(task, NO_EXCEPTION, MessageHolderChecker.ONE_ERROR_ONE_ABORT);
    }

    public void testDefaultFileComplete() {
        AjcTask task = getTask("default.lst");
        task.setDebugLevel("none");
        task.setDeprecation(true);
        task.setFailonerror(false);
        task.setNoExit(true); // ok to override Ant?
        task.setNoImportError(true);
        task.setNowarn(true);
        task.setXNoweave(true);
        task.setPreserveAllLocals(true);
        task.setProceedOnError(true);
        task.setReferenceInfo(true);
        task.setSource("1.3");
        task.setTarget("1.1");
        task.setTime(true);
        task.setVerbose(true);
        task.setXlint("info");
        runTest(task, NO_EXCEPTION, MessageHolderChecker.INFOS);
    }

    protected void runTest(
        AjcTask task, 
        Class exceptionType, 
        MessageHolderChecker checker,
        String messageHolderClass) {
        task.setMessageHolderClass(messageHolderClass);
        runTest(task, exceptionType, checker, (MessageHandler) null);
    }
    
    protected void runTest(
        AjcTask task, 
        Class exceptionType, 
        MessageHolderChecker checker) {
        MessageHandler holder = new MessageHandler();
        task.setMessageHolder(holder);
        runTest(task, exceptionType, checker, holder);
    }    

    protected void runTest(
        AjcTask task, 
        Class exceptionType, 
        MessageHolderChecker checker,
        MessageHandler holder) {     
        Throwable thrown = null;
        // re-run forked iff tools.jar and expect to pass
        boolean rerunForked 
            = ((null != aspectjtoolsJar)
            && (null == exceptionType)
            && ((null == checker) || !checker.expectFail()));
        String label = "same-vm ";
        while (true) {  // same vm, then perhaps forked   
            try {
                task.execute();
            } catch (Throwable t) {
                thrown = t;
            } finally {
                deleteTempDir();
            }
            if (null == exceptionType) {
                if (null != thrown) {
                    assertTrue(label + "thrown: " + render(thrown), false);
                }
            } else if (null == thrown) {
                assertTrue(label + "expected " + exceptionType.getName(), false);
            } else if (!(exceptionType.isAssignableFrom(thrown.getClass()))) {
                assertTrue(label + "expected " + exceptionType.getName()
                    + " got " + render(thrown), false);
            }
            if (null != holder) {
                if (null == checker) {
                    checker = MessageHolderChecker.NONE;
                }
                checker.check(holder, label);
            }
            if (!rerunForked) {
                break;
            } else {
                label = "other-vm ";
                rerunForked = false;
                // can't reset without losing values...
                task.setFork(true);
                task.setFailonerror(true);
                task.setForkclasspath(new Path(task.getProject(), 
                    aspectjtoolsJar));
            }
        }
    }
    
    protected String render(Throwable thrown) {
        return LangUtil.renderException(thrown);
    }
    
    static class MessageHolderChecker {  // XXX export to testing-utils
        /** use as value to ignore results */
        static int IGNORE = Integer.MIN_VALUE;
        
        static MessageHolderChecker NONE = 
            new MessageHolderChecker(0,0,0,0,0);
        /** any number (0+) of info messages */
        static MessageHolderChecker INFOS = 
            new MessageHolderChecker(0,0,0,0,IGNORE);
        /** one error, any number of info messages */
        static MessageHolderChecker ONE_ERROR= 
            new MessageHolderChecker(0,0,1,0,IGNORE);
        static MessageHolderChecker ONE_ERROR_ONE_ABORT = 
            new MessageHolderChecker(1,0,1,0,IGNORE);
        /** one warning, any number of info messages */
        static MessageHolderChecker ONE_WARNING = 
            new MessageHolderChecker(0,0,0,1,IGNORE);

        int aborts, fails, errors, warnings, infos;
        public MessageHolderChecker(int aborts, int fails, int errors, int warnings, int infos) {
            this.aborts = aborts;
            this.fails = fails;
            this.errors = errors;
            this.warnings = warnings;
            this.infos = infos;
        }
        
        public boolean expectFail() {
            return (0 < (aborts + fails + errors));
        }
        
        public void check(IMessageHolder holder, String label) {
            boolean failed = true;
            try {
            	check(holder, aborts, IMessage.ABORT);
	            check(holder, fails, IMessage.FAIL);
	            check(holder, errors, IMessage.ERROR);
	            check(holder, warnings,IMessage.WARNING);
	            check(holder, infos, IMessage.INFO);
	            failed = false; 
            } finally {
            	if (failed) {
            		MessageUtil.print(System.err, holder, label + "failed?");
            	}
            }
        }
        
        private void check(IMessageHolder holder, int num, IMessage.Kind kind) {
            if (num != IGNORE) {
            	int actual = holder.numMessages(kind, false);
            	if (num != actual) {
            		if (actual > 0) {
	            		MessageUtil.print(System.err, holder, kind + " expected " + num + " got " + actual);
            		}
	                assertEquals(kind.toString(), num, actual);
            	}
            }
        }
    }
}
