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


/*
 * StreamGrabberTest.java created on May 16, 2002
 *
 */
package org.aspectj.testing.util;


import java.io.PrintStream;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.textui.TestRunner;

/**
 * 
 * @author isberg
 */
public class StreamGrabberTest extends TestCase {


    private static final String ME 
        = "org.aspectj.testing.util.StreamGrabberTest";

    /** @param args ignored */
    public static void main(String[] args) throws Exception {
        class C extends TestRunner {
            public TestResult go(String[] a) throws Exception {
                return start(a);
            }
        }
        TestResult r = new C().go(args);
        if (!r.wasSuccessful()) {
            System.err.println(r.errorCount() + "/" + r.failureCount());
        }
    }

    public StreamGrabberTest(String s) { super(s); }
    
    public void testHide() {
        PrintStream restore = System.out;
        System.setOut(new PrintStream(NullPrintStream.NULL_OutputStream));
        System.out.println("OutputStream should not print!!!!!!!!!!!!!!!!!!!");
        System.setOut(new PrintStream(NullPrintStream.NULL_PrintStream));
        System.out.println("PrintStream should not print!!!!!!!!!!!!!!!!!!!");
        System.setOut(restore); 
    }
    
    /**
     * Test StreamSniffer by setting up a delegate System.out
     * and a normal System.out (delegating to System.out)
     * and verifying that both get the same result.
     */
    public void testGrab() {
        StringBuffer delegate = new StringBuffer();
        StreamSniffer out = new StreamSniffer(System.out);
        out.setBuffer(delegate);
        System.setOut(new PrintStream(out));
        StreamSniffer g = new StreamSniffer(System.out);
        System.setOut(new PrintStream(g));
        StringBuffer buf = new StringBuffer();
        g.setBuffer(buf);

        printLoop("f", buf, delegate);
        printLoop("now is the time for all good men...", buf, delegate);
        printlnLoop("f", buf, delegate);
        printlnLoop("now is the time for all good men...", buf, delegate);           
    }

    private void printLoop(String expect, StringBuffer buf, StringBuffer delegate) {
        System.out.print(expect);
        String actual = buf.toString();
        String delegateActual = delegate.toString();
        assertTrue(expect + "=" + actual, expect.equals(actual));
        assertTrue(expect + "=" + delegateActual, expect.equals(delegateActual));
        buf.setLength(0);
        delegate.setLength(0);
        System.out.print(expect);
        
        actual = buf.toString();
        delegateActual = delegate.toString();
        assertTrue(expect + "=" + actual, expect.equals(actual));
        assertTrue(expect + "=" + delegateActual, expect.equals(delegateActual));
        buf.setLength(0);
        delegate.setLength(0);
    }
    
    private void printlnLoop(String expect, StringBuffer buf, StringBuffer delegate) {
        // copy/paste of printLoop, using println
        expect = expect.trim();
        System.out.println(expect);
        String actual = buf.toString().trim();
        String delegateActual = delegate.toString().trim();
        assertTrue(expect + "=" + actual, expect.equals(actual));
        assertTrue(expect + "=" + delegateActual, expect.equals(delegateActual));
        buf.setLength(0);
        delegate.setLength(0);
        
        System.out.println(expect);
        actual = buf.toString().trim();
        delegateActual = delegate.toString().trim();
        assertTrue(expect + "=" + actual, expect.equals(actual));
        assertTrue(expect + "=" + delegateActual, expect.equals(delegateActual));
        buf.setLength(0);
        delegate.setLength(0);
    }

}
