/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the compiler and core tools for the AspectJ(tm)
 * programming language; see http://aspectj.org
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is AspectJ.
 *
 * The Initial Developer of the Original Code is Xerox Corporation. Portions
 * created by Xerox Corporation are Copyright (C) 1999-2002 Xerox Corporation.
 * All Rights Reserved.
 *
 * Contributor(s):
 */
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
                holder.clear();
            }
            tempFile.delete();
        }
    }

    /** added line here */
    public void testFileCompareClass() throws IOException {
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
