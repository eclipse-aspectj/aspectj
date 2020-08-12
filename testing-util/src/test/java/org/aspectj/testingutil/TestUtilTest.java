/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testingutil;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FileUtil;
import org.aspectj.testing.util.TestUtil;

/**
 * 
 */
public class TestUtilTest extends TestCase {

	public TestUtilTest(String name) {
		super(name);
	}
    
    public void testFileCompareNonClass() throws IOException {
        MessageHandler holder = new MessageHandler();
        File thisFile = new File(UtilTests.TESTING_UTIL_PATH + "/src/test/java/org/aspectj/testingutil/TestUtilTest.java");
        //File thisFile = new File("src/testing-util.lst");
        assertTrue(TestUtil.sameFiles(holder, thisFile, thisFile));
        
        File tempFile = File.createTempFile("TestUtilTest", ".tmp");
        FileUtil.copyFile(thisFile, tempFile);
        long len = tempFile.length();
        assertTrue(0 != len);
        long tlen = thisFile.length();
        assertEquals(tlen, len);
        assertTrue(TestUtil.sameFiles(holder, tempFile, thisFile));
        try {
            String path = thisFile.getName();
            File basedir = tempFile.getParentFile();        
            File renamed = new File(basedir, path);
            if (!tempFile.renameTo(renamed)) {
                MessageUtil.warn(holder, "unable to rename " + tempFile + " to " + renamed);
            } else {
                len = renamed.length();
                assertEquals(tlen, len);
                assertTrue(TestUtil.sameFiles(holder, basedir, thisFile.getParentFile(), path));
            }
        } finally {
            if (0 < holder.numMessages(null, true)) {
                MessageUtil.print(System.out, holder);
                holder.clearMessages();
            }
            tempFile.delete();
        }
    }

    public void testFileCompareNonClassStaticPositive() throws IOException {
        MessageHandler holder = new MessageHandler();
        File basedir = new File(UtilTests.TESTING_UTIL_PATH + "/testdata/testCompareTextFiles/sameFile");
        File expectedBaseDir = new File(basedir, "expected");
        File actualBaseDir = new File(basedir, "actual");
        String filename = "TestUtilTest.java";
        File expected = new File(expectedBaseDir, filename);
        File actual = new File(actualBaseDir, filename);

        assertTrue(TestUtil.sameFiles(holder, expected, actual));

        assertTrue(TestUtil.sameFiles(holder, expectedBaseDir, actualBaseDir, filename));
    }

    public void testFileCompareNonClassStaticNegative() throws IOException {
        MessageHandler holder = new MessageHandler();
        File basedir = new File("testdata/testCompareTextFiles/differentFile");
        File expectedBaseDir = new File(basedir, "expected");
        File actualBaseDir = new File(basedir, "actual");
        String filename = "TestUtilTest.java";
        File expected = new File(expectedBaseDir, filename);
        File actual = new File(actualBaseDir, filename);

        assertTrue(!TestUtil.sameFiles(holder, expected, actual));

        assertTrue(!TestUtil.sameFiles(holder, expectedBaseDir, actualBaseDir, filename));
    }

    public void testParseBoolean() {
        {
            String[] trues = {"true", "TRUE", "on", "ON" };
			for (String aTrue : trues) {
				assertTrue(aTrue, TestUtil.parseBoolean(aTrue));
			}
        }
        {
            String[] falses = {"false", "FALSE", "off", "off" };
			for (String fals : falses) {
				assertTrue(fals, !TestUtil.parseBoolean(fals));
			}
        }
        String[] errors = {"fals", "tru", "T", "on of" };
        boolean fail = false;
        final int MAX = errors.length-1;
        for (int i = 0; i <= MAX; i++) {
            try {
                TestUtil.parseBoolean(errors[i], fail);
                assertTrue("no exception: " + errors[i], !fail);
            } catch (IllegalArgumentException e) {
                assertTrue("exception: " + errors[i], fail);
                String m = e.getMessage();
                if (!m.contains(errors[i])) {
                    fail(errors[i] + " not in " + m);
                }
            }
            if ((i == MAX) && !fail) {
                i = -1;
                fail = true;
            }
        }
        
    }
    public void testFileCompareClass() throws IOException {
        if (!TestUtil.ClassLineator.haveDisassembler()) {
            System.err.println("skipping testFileCompareClass - no disassembler on classpath");
            return;
        }
        MessageHandler holder = new MessageHandler();
        File classBase = new File(UtilTests.TESTING_UTIL_PATH + "/testdata/testCompareClassFiles");
        String path = "org/aspectj/testingutil/TestCompareClassFile.class";
        File classFile = new File(classBase, path);
        
        try {
            assertTrue(TestUtil.sameFiles(holder, classFile, classFile));
            assertTrue(TestUtil.sameFiles(holder, classBase, classBase, path));
        } finally {
            if (0 < holder.numMessages(null, true)) {
                MessageUtil.print(System.out, holder);
            }
        }
    }
    
}
