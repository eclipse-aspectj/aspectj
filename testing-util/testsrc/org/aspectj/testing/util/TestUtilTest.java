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

package org.aspectj.testing.util;

import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FileUtil;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * 
 */
public class TestUtilTest extends TestCase {

	public TestUtilTest(String name) {
		super(name);
	}
    
    public void testFileCompareNonClass() throws IOException {
        MessageHandler holder = new MessageHandler();
        File thisFile = new File("testsrc/org/aspectj/testing/util/TestUtilTest.java");
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
        File basedir = new File("testdata/testCompareTextFiles/sameFile");
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

    public void testFileCompareClass() throws IOException {
        if (!TestUtil.ClassLineator.haveDisassembler()) {
            System.err.println("skipping testFileCompareClass - no disassembler on classpath");
            return;
        }
        MessageHandler holder = new MessageHandler();
        File classBase = new File("testdata/testCompareClassFiles");
        String path = "org/aspectj/testing/util/TestCompareClassFile.class";
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
