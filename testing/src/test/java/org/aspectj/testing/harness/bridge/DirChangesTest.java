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

package org.aspectj.testing.harness.bridge;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.xml.MessageListXmlReader;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.LangUtil;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * (broken) tests for unutilized feature to read in
 * expected directory changes as messages.
 */
public class DirChangesTest extends TestCase {
    private static final boolean PRINTING = false;
    
    /** name of file in srcBaseDir with any expected messages */
	public static final String EXPECTED_NAME = "expectedMessages.xml";
    
	public DirChangesTest(String name) {
		super(name);
	}

    /* XXX update tests to read expected messages from files
     * then just iterate over directories in dirChangesTestDir
     */

    /** 
     * Uses testdata/dirChangesTestDir/same 
     */
    public void skip_testSameExpDir() {
        doCheck("same");
    }
    public void testNothingForAntJUnit() {}
    /** 
     * Uses testdata/dirChangesTestDir/diff
     */
    public void skip_testDiffExpDir() {
        doCheck("diff");
    }

    public void skip_testWriteEmpty() {
        DirChanges.Spec spec = new DirChanges.Spec();
        String expected = "";
        checkWrite(spec, expected);                
    }

    public void skip_testWriteExpDir() {
        DirChanges.Spec spec = new DirChanges.Spec();
        spec.setExpDir("expected directory");
        String expected = 
            "<dir-changes expDir=\"expected directory\"/>" 
            + LangUtil.EOL;
        checkWrite(spec, expected);                
    }

    public void skip_testWriteAdded() {
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
    
    private void doCheck(String dir) {
        DirChanges.Spec spec = new DirChanges.Spec();
        File srcBaseDir = new File("testdata/dirChangesTestDir/" + dir);
        // actual = baseDir
        File baseDir = new File(srcBaseDir, "actual");
        // expected = srcBaseDir + spec.expDir
        spec.setExpDir("expected");
        IMessage[] expected = null;
        File expMssgFile = new File(srcBaseDir, EXPECTED_NAME);
        if (expMssgFile.canRead()) {
            try {
                expected = new MessageListXmlReader().readMessages(expMssgFile);
            } catch (IOException e) {
                System.err.println("Continuing after error reading " + expMssgFile);
                e.printStackTrace(System.err);
            }
        }
        checkDirChanges(spec, baseDir, srcBaseDir, null, expected);
    }

    // XXX WEAK upgrade to read expected-diffs from file in directory
    private void checkDirChanges(
        DirChanges.Spec spec, 
        File baseDir,
        File srcBaseDir,
        Runnable dirChanger,
        IMessage[] expected) {
        DirChanges dc = new DirChanges(spec);
        MessageHandler handler = new MessageHandler();
        try {
            if (!dc.start(handler, baseDir)) {
                //assertTrue(!shouldPass);
                assertSameMessages(expected, handler);
                return;  // exiting after (XXX) undertested expected failure?
            } else {
                assertTrue(0 == handler.numMessages(IMessage.ERROR, true));
            }
            if (null != dirChanger) {
                dirChanger.run();
            }
            if (!dc.end(handler, srcBaseDir)) {
                //assertTrue(!shouldPass);
                assertSameMessages(expected, handler);
            } else {
                assertTrue(0 == handler.numMessages(IMessage.ERROR, true));
            }
        } catch (Throwable t) {
            if (PRINTING) {
                t.printStackTrace(System.err);
            }
            throw new AssertionFailedError(LangUtil.renderException(t));
        } finally {
            if (0 < handler.numMessages(null, true)) {
                if (PRINTING) {
                    MessageUtil.print(System.err, handler, "checkDirChanges: ");
                }
                IMessage[] toprint = handler.getMessages(null, true);
                File output = new File(srcBaseDir, EXPECTED_NAME);
                try {
                    //toprint[0].getISourceLocation().getSourceFile();
                    System.out.println("XXX writing to " + output
                        + " messages " + LangUtil.arrayAsList(toprint));
                    new MessageListXmlReader().writeMessages(output, toprint);
                } catch (IOException e) {
                    System.err.println("Error writing to " + output
                        + " messages " + LangUtil.arrayAsList(toprint));
                    e.printStackTrace(System.err);
                }
            }
        }
    }

	/**
	 * Assert unless messages in handler match all expected messages.
	 * @param expected
	 * @param handler
	 */
	private void assertSameMessages(
		IMessage[] expected,
		MessageHandler handler) {
        IMessage[] actual = handler.getMessages(null, true);
        for (int i = 0; i < actual.length; i++) {
			int found = find(actual[i], expected);
            if (-1 != found) {
                expected[found] = null;
                actual[i] = null;
            }
		}
        StringBuffer sb = new StringBuffer();
        {
            IMessage[] expNotFound = (IMessage[])
                LangUtil.safeCopy(expected, new IMessage[0]);
            if (0 < expNotFound.length) {
                sb.append("expected not found: ");
                sb.append(LangUtil.arrayAsList(expNotFound).toString());
            }
        }
        {
            IMessage[] actFound = (IMessage[])
                LangUtil.safeCopy(actual, new IMessage[0]);
            if (0 < actFound.length) {
                sb.append(" not expected but found: ");
                sb.append(LangUtil.arrayAsList(actFound).toString());
            }
        }

        if (0 < sb.length()) {
            assertTrue(sb.toString(), false);
        }
    }

	/**
     * Find message in array, comparing by contents
     * but ignoring exceptions thrown and source location XXX.
	 * @param message the IMessage to find
	 * @param expected the 
	 * @return int
	 */
	private int find(IMessage message, IMessage[] expected) {
        if ((null != expected) && (0 != expected.length)
            && (null != message)) {
            final IMessage.Kind kind = message.getKind();
            final String text = message.getMessage();
            for (int i = 0; i < expected.length; i++) {
                IMessage exp = expected[i];
				if (null != exp) {
                    if (kind.equals(exp.getKind())
                        && text.equals(exp.getMessage())) {
                        return i;
                    }
                }
			}
        }
		return -1;
	}


}
