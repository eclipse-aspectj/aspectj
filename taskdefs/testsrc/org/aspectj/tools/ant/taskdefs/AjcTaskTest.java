/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

import java.io.File;

import junit.framework.TestCase;

/**
 * 
 */
public class AjcTaskTest extends TestCase {

    private static final Class NO_EXCEPTION = null;
	private static final String NOFILE = "NOFILE";
	
    private static File tempDir;
    
    private static void deleteTempDir() {
        if ((null != tempDir) && tempDir.exists()) {
            FileUtil.deleteContents(tempDir);
            tempDir.delete();
        }
    }    
    private static final File getTempDir() {
        if (null == tempDir) {
            tempDir = new File("IncrementalAjcTaskTest-temp");            
        }
        return tempDir;
    }    
	
    public AjcTaskTest(String name) {
		super(name);
	}

    public void tearDown() {
        deleteTempDir();
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
        		task.setArgfiles(input);
        	} else {
        		task.setArgfile(new File(input));
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
        task.setVerbose(true); // XXX    
        return task;
    }
    
    // ---------------------------------------- argfile
    public void testDefaultList() {
        AjcTask task = getTask("testdata/default.lst");
        runTest(task, NO_EXCEPTION, IMessageHolderChecker.INFOS);
    }

    public void testCompileErrorList() {
        AjcTask task = getTask("testdata/compileError.lst");
        runTest(task, NO_EXCEPTION, IMessageHolderChecker.ONE_ERROR);
    }

    public void testCompileWarningList() {
        AjcTask task = getTask("testdata/compileWarning.lst");
        runTest(task, NO_EXCEPTION, IMessageHolderChecker.ONE_WARNING);
    }

    public void testNoSuchFileList() {
        AjcTask task = getTask("testdata/NoSuchFile.lst");
        runTest(task, NO_EXCEPTION, IMessageHolderChecker.ONE_ERROR);
    }

    // ---------------------------------------- ant drivers?
    // doesn't work..
//    public void testAntScript() {
//    	Ant ant = new Ant();
//    	ant.setProject(new Project());
//    	ant.setDir(new File("."));
//    	ant.setAntfile("test-build.xml");
//		ant.execute();
//    }
    // ---------------------------------------- sourcefile
    public void testDefaultFile() {
        AjcTask task = getTask("testdata/Default.java");
        runTest(task, NO_EXCEPTION, IMessageHolderChecker.INFOS);
    }


    public void testNoFile() {
        AjcTask task = getTask(NOFILE);
        runTest(task, NO_EXCEPTION, IMessageHolderChecker.ONE_ERROR);
    }
    
    // XXX find out how to feed files into MatchingTask
//    public void testCompileErrorFile() {
//        AjcTask task = getTask("testdata/CompilerError.java");
//        runTest(task, NO_EXCEPTION, IMessageHolderChecker.ONE_ERROR);
//    }
//
//    public void testCompileWarningFile() {
//        AjcTask task = getTask("testdata/CompilerWarning.lst");
//        runTest(task, NO_EXCEPTION, IMessageHolderChecker.ONE_WARNING);
//    }
//
//    public void testNoSuchFile() {
//        AjcTask task = getTask("testdata/NoSuchFile.java");
//        runTest(task, NO_EXCEPTION, IMessageHolderChecker.ONE_ERROR);
//    }
//
//    public void testDefaultFileComplete() {
//        AjcTask task = getTask("testdata/Default.java");
//        task.setDebugLevel("none");
//        task.setDeprecation(true);
//        task.setFailonerror(false);
//        task.setNoExit(true); // ok to override Ant?
//        task.setNoImportError(true);
//        task.setNowarn(true);
//        task.setNoweave(true);
//        task.setPreserveAllLocals(true);
//        task.setProceedOnError(true);
//        task.setReferenceInfo(true);
//        task.setSource("1.3");
//        task.setTarget("1.1");
//        task.setTime(true);
//        task.setVerbose(true);
//        task.setXlintenabled(true);
//        runTest(task, NO_EXCEPTION, IMessageHolderChecker.INFOS);
//    }

    protected void runTest(
        AjcTask task, 
        Class exceptionType, 
        IMessageHolderChecker checker) {
        Throwable thrown = null;
        MessageHandler holder = new MessageHandler();
        task.setMessageHolder(holder);        
        try {
            task.execute();
        } catch (Throwable t) {
            thrown = t;
        } finally {
            deleteTempDir();
        }
        if (null == exceptionType) {
            if (null != thrown) {
                assertTrue("thrown: " + render(thrown), false);
            }
        } else if (null == thrown) {
            assertTrue("expected " + exceptionType.getName(), false);
        } else if (!(exceptionType.isAssignableFrom(thrown.getClass()))) {
            assertTrue("expected " + exceptionType.getName()
                + " got " + render(thrown), false);
        }
        if (null == checker) {
            checker = IMessageHolderChecker.NONE;
        }
        checker.check(holder);
    }
    
    protected String render(Throwable thrown) {
        return LangUtil.renderException(thrown);
    }
    
    static class IMessageHolderChecker {  // XXX export to testing-utils
        /** use as value to ignore results */
        static int IGNORE = Integer.MIN_VALUE;
        
        static IMessageHolderChecker NONE = 
            new IMessageHolderChecker(0,0,0,0,0);
        /** any number (0+) of info messages */
        static IMessageHolderChecker INFOS = 
            new IMessageHolderChecker(0,0,0,0,IGNORE);
        /** one error, any number of info messages */
        static IMessageHolderChecker ONE_ERROR= 
            new IMessageHolderChecker(0,0,1,0,IGNORE);
        /** one warning, any number of info messages */
        static IMessageHolderChecker ONE_WARNING = 
            new IMessageHolderChecker(0,0,0,1,IGNORE);

        int aborts, fails, errors, warnings, infos;
        public IMessageHolderChecker(int aborts, int fails, int errors, int warnings, int infos) {
            this.aborts = aborts;
            this.fails = fails;
            this.errors = errors;
            this.warnings = warnings;
            this.infos = infos;
        }
        public void check(IMessageHolder holder) {
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
            		MessageUtil.print(System.err, holder, "failed?");
            	}
            }
        }
        
        private void check(IMessageHolder holder, int num, IMessage.Kind kind) {
            if (num != IGNORE) {
            	int actual = holder.numMessages(kind, false);
            	if (num != actual) {
            		if (actual > 0) {
	            		MessageUtil.print(System.err, holder, "expected " + num + " got " + actual);
            		}
	                assertEquals(num, actual);
            	}
            }
        }
    }
}
