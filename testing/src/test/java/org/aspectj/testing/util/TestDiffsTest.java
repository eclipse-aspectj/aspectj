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

package org.aspectj.testing.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.aspectj.util.FileUtil;

import junit.framework.TestCase;

/**
 * 
 */
public class TestDiffsTest extends TestCase {

   /**
     * Expected results in test below.
     */
    private static void genTestInput(File expected, File actual) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(expected);
            PrintWriter pw = new PrintWriter(writer);
            pw.println("PASS passed in both");
            pw.println("## random text to ignore: " + System.currentTimeMillis());
            pw.println("FAIL failed in both");
            pw.println("PASS failed in actual (broken)");
            pw.println("FAIL passed in actual (fixed)");
            pw.println("PASS    not in actual (missing-pass)");
            pw.println("FAIL    not in actual (missing-fail)");
            pw.flush();
            writer.close();
            
            writer = new FileWriter(actual);
            pw = new PrintWriter(writer);
            pw.println("PASS passed in actual (fixed)");
            pw.println("## random text to ignore: " + System.currentTimeMillis());
            pw.println("PASS  not in expected (added-pass)");
            pw.println("FAIL failed in both");
            pw.println("PASS passed in both");
            pw.println("FAIL failed in actual (broken)");
            pw.println("FAIL  not in expected (added-fail)");
            pw.flush();
            writer.close();
            writer = null;
        } finally {
            if (null != writer) {
                try { writer.close(); }
                catch (IOException e) { } // ignore                    
            }
        }
    }

    List tempFiles;
	/**
	 * Constructor for FileUtilTest.
	 * @param arg0
	 */
	public TestDiffsTest(String arg0) {
		super(arg0);
	   tempFiles = new ArrayList();
    }

    public void tearDown() {
        for (ListIterator iter = tempFiles.listIterator(); iter.hasNext();) {
            File dir = (File) iter.next();
            FileUtil.deleteContents(dir);
            dir.delete();
            iter.remove();
        }
    }


    public void testCompareResults() {
        File tempDir = org.aspectj.util.FileUtil.getTempDir("testCompareResults");
        File expected = new File(tempDir, "expected.txt");
        File actual = new File(tempDir, "actual.txt");
        tempFiles.add(expected);
        tempFiles.add(actual);
        try {
            genTestInput(expected, actual);
        } catch (IOException e) {
            assertTrue(e.getMessage(), false);
        }
        TestDiffs result = TestDiffs.compareResults(expected, actual);
        assertEquals(2, result.missing.size());
        assertEquals(2, result.added.size());
        assertEquals(1, result.fixed.size());
        assertEquals(1, result.broken.size());
        assertEquals(3, result.actualFailed.size());
        assertEquals(3, result.actualPassed.size());
        assertEquals(6, result.actual.size());
        assertEquals(3, result.expectedFailed.size());
        assertEquals(3, result.expectedPassed.size());
        assertEquals(6, result.expected.size());
        assertEquals(1, result.stillFailing.size());
        assertEquals(1, result.stillPassing.size());
    }
}
