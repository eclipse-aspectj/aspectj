/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.taskdefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.harness.bridge.Globals;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

import junit.framework.TestCase;


/**
 * Test AjcTaskCompileCommand adapter.
 * This assumes it is run from a peer directory of the
 * taskdefs module, which contains the target files.
 */
public class AjcTaskCompileCommandTest extends TestCase {
    static boolean loggedWarning = false;
    static boolean runAllTests = true;
    static List<File> tempFiles = new ArrayList<>();
    
    private static File getClassesDir() {
        File tempDir = FileUtil.getTempDir("AjcTaskCompileCommandTest-classes");
        tempFiles.add(tempDir);
        return tempDir;
    }

    private static void addCommonArgs(List<String> list) {
        list.add("-d");
        list.add(getClassesDir().getAbsolutePath());
        list.add("-classpath");
        StringBuilder classpath = new StringBuilder();
        classpath.append(Globals.F_aspectjrt_jar.getAbsolutePath());
        if (LangUtil.is19VMOrGreater()) {
        	classpath.append(File.pathSeparator).append(LangUtil.getJrtFsFilePath());
        }
        list.add(classpath.toString());
    }

    static boolean doWait(IMessageHolder holder, int seconds, int timeout) {
        return AjcTaskCompileCommand
            .waitUntilMessagesQuiet(holder, seconds, timeout);        
    }

    public AjcTaskCompileCommandTest(String name) {
        super(name);
    }

    public void testWaitUntilMessagesQuiet_InputErrors() {
        MessageHandler mhandler = new MessageHandler();
        assertFalse(doWait(mhandler, 0, 10));
        assertFalse(doWait(mhandler, 10, 0));
        assertFalse(doWait(mhandler, 1, 1));
        assertFalse(doWait(mhandler, 10, 1));
        boolean thrown = false;
        try {
            doWait(null, 1, 10);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue("no exception thrown", thrown);
    }

    public void testDefault() {
        runSimpleTest("../taskdefs/testdata/Default.java", 0);
    }

    public void testDefaultList() {
        runSimpleTest("../taskdefs/testdata/default.lst", 0);
    }
    
    public void testCompileErrorList() {
        runSimpleTest("../taskdefs/testdata/compileError.lst", 1);
    }

    
    public void testWaitUntilMessagesQuiet_1_2() {
        if (runAllTests) checkWait(1, 2, 0, 0);
    }
    
    public void testWaitUntilMessagesQuiet_1_10() {
        if (runAllTests) checkWait(1, 10, 0, 0);
    }
    
    public void testWaitUntilMessagesQuiet_8_10() {
        checkWait(8, 10, 0, 0);
    }
    
    // XXX two async tests might fail if adder thread starved
    
    public void testWaitUntilMessagesQuiet_1_10_4_1() {
        if (runAllTests) checkWait(1, 10, 4, 1);
    }
    
    public void testWaitUntilMessagesQuiet_8_10_2_1() {
        if (runAllTests) checkWait(8, 20, 2, 1);
    }

    void runSimpleTest(String path, int expectedErrors) {
        File file = new File(path);
        assertTrue(path, file.canRead());
        ArrayList<String> list = new ArrayList<>();
        addCommonArgs(list);
        if (path.endsWith(".lst")) {
            list.add("-argfile");
            list.add(file.getAbsolutePath());
        } else if (FileUtil.hasSourceSuffix(path)) {
            list.add(file.getAbsolutePath());
        } else {
            assertTrue("unrecognized file: " + path, false);
            return;
        }
        runTest(list, expectedErrors);
    }
    
    void runTest(ArrayList<String> args, int expectedErrors) {
        AjcTaskCompileCommand command =
            new AjcTaskCompileCommand();
        MessageHandler handler = new MessageHandler();
        String[] parms = (String[]) args.toArray(new String[0]);
        boolean result = command.runCommand(parms, handler);
        boolean expectPass = (0 == expectedErrors);
        final boolean pass = (result == expectPass);
        if (!pass) {
            String m = expectPass ? "pass" : "fail";
        
            assertTrue("expected " + m + ": " + args+"\n Messages:"+handler.getUnmodifiableListView(), false);
        }
    }

    void checkWait(final int seconds, final int timeout, int toAdd, int addInterval) {
        final String testCase = "checkWait(seconds=" 
                            + seconds + ", timeout=" + timeout; 
        final MessageHandler mhandler = new MessageHandler();
        final long startTime = System.currentTimeMillis();
        final long testTimeout = startTime + (timeout * 2000l);
        final boolean result;
        if (0 == toAdd) { // do no-adds synchronously
            result = doWait(mhandler, seconds, timeout);
            assertTrue("result " + testCase, result);
        } else {
            if (!loggedWarning) {
                System.out.println("warning - test will fail if adder thread starved");
                loggedWarning = true;
            }
            final MessageAdder adder 
                = new MessageAdder(mhandler, toAdd, addInterval);
            final String label = testCase + " wait(" + toAdd + ", " + addInterval + ")";
            class Result {
                boolean result;
                Thread addedThread;
            }
            final Result waitResult = new Result();
            Thread testThread = new Thread( new Runnable() {
                public void run() {
                    waitResult.addedThread
                        = new Thread(adder, label + "-child");
                    waitResult.addedThread.start();
                    waitResult.result = 
                        AjcTaskCompileCommandTest.doWait(mhandler, seconds, timeout);
                }
            }, label);
            
            testThread.start();
            
            try {
                testThread.join(testTimeout - startTime);            
            } catch (InterruptedException e) {
                // ignore
            }
            try {
                if (null != waitResult.addedThread) {
                    long wait = testTimeout - System.currentTimeMillis();
                    if (0 < wait) {
                        waitResult.addedThread.join(wait);
                    }            
                }
            } catch (InterruptedException e) {
                // ignore
            }
            result = waitResult.result;
            int added = adder.getNumAdded(); 
            assertEquals(testCase + " added", added, toAdd);
            if (!result) {
                assertTrue(testCase + " result " + adder, false);
            }
        }
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        assertTrue(seconds + " seconds: " + elapsed, elapsed >= (seconds*1000));
        assertTrue(timeout + " timeout: " + elapsed, elapsed <= (timeout*1000));
    }

}

class MessageAdder implements Runnable {
    /** 30-second max test */
    public static long MAX_MILLIS = 1000 * 30;
    public boolean stop;
    public boolean wait;
    
    private final IMessageHolder messages;
    private final int numToAdd;
    private final int interval;
    private int numAdded;
    
    /**
     * @param holder the IMessageHolder to add to
     * @param num the int number of messages to add
     * @param interval the int seconds between messages added
     */
     MessageAdder(IMessageHolder holder, int num, int interval) {
        LangUtil.throwIaxIfNull(holder, "holder");
        LangUtil.throwIaxIfFalse(num > 0, "numToAdd: " + num);
        LangUtil.throwIaxIfFalse(interval > 0, "interval: " + interval);
        LangUtil.throwIaxIfFalse(num*interval*1000 < MAX_MILLIS, "too long");
        this.messages = holder;
        this.numToAdd = num;
        this.interval = interval;
    }

    public void run() {
        final long waitBetweenAdds = interval * 1000l;
        long curTime = System.currentTimeMillis();
        final long timeout = curTime + MAX_MILLIS;
//        final Thread thread = Thread.currentThread();
        int numAdded = 0;
        while (!stop && (timeout > curTime) 
            && (numAdded < numToAdd)) {
            long targetTime = curTime + waitBetweenAdds;
            while (!stop && (curTime < timeout)
                && (curTime < targetTime)) {
                try {
                    Thread.sleep(targetTime - curTime);
                } catch (InterruptedException e) {
                    // ignore
                }
                curTime = System.currentTimeMillis();
            }
            if (!stop && (curTime < timeout)) {
                MessageUtil.info(messages, "time is " + curTime);
                numAdded++;
            }
        }
        this.numAdded = numAdded;
    }
    int getNumAdded() {
        return numAdded;
    }
    
    public String toString() {
        return "MessageAdder("
            + "numAdded=" + numAdded
            + ", numToAdd=" + numToAdd
            + ", interval=" + interval
            + ", stop=" + stop
            + ", wait=" + wait
            + ", numMessages="
            + (null == messages 
                ? 0 
                : messages.numMessages(null, true))
            + ")";
    }  
}