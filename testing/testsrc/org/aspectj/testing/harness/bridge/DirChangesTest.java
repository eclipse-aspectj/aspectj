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

package org.aspectj.testing.harness.bridge;

import org.aspectj.bridge.*;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.LangUtil;

import java.io.*;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 *
 */
public class DirChangesTest extends TestCase {
    private static final boolean PRINTING = false;
    
	public DirChangesTest(String name) {
		super(name);
	}

    /* XXX update tests to read expected messages from files
     * then just iterate over directories in dirChangesTestDir
     */

    /** 
     * Uses testdata/dirChangesTestDir/same 
     */
    public void testSameExpDir() {
        doCheck("same", true);
    }
    
    /** 
     * Uses testdata/dirChangesTestDir/diff
     */
    public void testDiffExpDir() {
        doCheck("diff", false);
    }

    public void testWriteEmpty() {
        DirChanges.Spec spec = new DirChanges.Spec();
        String expected = "";
        checkWrite(spec, expected);                
    }

    public void testWriteExpDir() {
        DirChanges.Spec spec = new DirChanges.Spec();
        spec.setExpDir("expected directory");
        String expected = 
            "<dir-changes expDir=\"expected directory\"/>" 
            + LangUtil.EOL;
        checkWrite(spec, expected);                
    }

    public void testWriteAdded() {
        DirChanges.Spec spec = new DirChanges.Spec();
        spec.setAdded("one,two,three");
        String expected = 
            "<dir-changes added=\"one,two,three\"/>" 
            + LangUtil.EOL;
        checkWrite(spec, expected);                
    }

    /** write spec to XML and compare with expected */
    private void checkWrite(DirChanges.Spec spec, String expected) {
        StringWriter actual = new StringWriter();
        XMLWriter writer = new XMLWriter(new PrintWriter(actual, true));
        spec.writeXml(writer);
        assertEquals(expected, actual.toString());
    }
    
    private void doCheck(String dir, boolean expectPass) {
        DirChanges.Spec spec = new DirChanges.Spec();
        File srcBaseDir = new File("testdata/dirChangesTestDir/" + dir);
        // actual = baseDir
        File baseDir = new File(srcBaseDir, "actual");
        // expected = srcBaseDir + spec.expDir
        spec.setExpDir("expected");
        checkDirChanges(spec, expectPass, baseDir, srcBaseDir, null);
    }

    // XXX WEAK upgrade to read expected-diffs from file in directory
    private void checkDirChanges(
        DirChanges.Spec spec, 
        boolean shouldPass,
        File baseDir,
        File srcBaseDir,
        Runnable dirChanger) {
        DirChanges dc = new DirChanges(spec);
        MessageHandler handler = new MessageHandler();
        try {
            if (!dc.start(handler, baseDir)) {
                assertTrue(!shouldPass);
                return;  // exiting after (XXX) undertested expected failure?
            } else {
                assertTrue(0 == handler.numMessages(IMessage.ERROR, true));
            }
            if (null != dirChanger) {
                dirChanger.run();
            }
            if (!dc.end(handler, srcBaseDir)) {
                assertTrue(!shouldPass);
            } else {
                assertTrue(0 == handler.numMessages(IMessage.ERROR, true));
                assertTrue(shouldPass);
            }
        } catch (Throwable t) {
            if (PRINTING && shouldPass) {
                t.printStackTrace(System.err);
            }
            throw new AssertionFailedError(LangUtil.renderException(t));
        } finally {
            if (PRINTING && 0 < handler.numMessages(null, true)) {
                MessageUtil.print(System.err, handler, "checkDirChanges: ");
            }
        }
    }
}
