package org.aspectj.runtime;
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

import java.io.*;

import org.aspectj.lang.*;
import org.aspectj.runtime.reflect.JoinPointImplTest;
import org.aspectj.runtime.reflect.RuntimePerformanceTest;
import org.aspectj.runtime.reflect.SignatureTest;

import junit.framework.*;

public class RuntimeModuleTests extends TestCase {

    public static TestSuite suite() { 
        TestSuite suite = new TestSuite(RuntimeModuleTests.class.getName());
        suite.addTestSuite(RuntimeModuleTests.class); // minimum 1 test (testNothing)
        suite.addTestSuite(SignatureTest.class);
		suite.addTestSuite(JoinPointImplTest.class);
		suite.addTestSuite(RuntimePerformanceTest.class);
        return suite;
    }

    public RuntimeModuleTests(String name) { super(name); }
    
    public void testNoAspectBoundException() {
        RuntimeException fun = new RuntimeException("fun");
        NoAspectBoundException nab = new NoAspectBoundException("Foo", fun);
        assertEquals(fun,nab.getCause());
    }

    public void testSoftExceptionPrintStackTrace() {
        // let's see
//        Throwable t = new Error("xyz");       
//        new SoftException(t).printStackTrace();

        // save to specified PrintStream
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(sink);
        new SoftException(new Error("xyz")).printStackTrace(out);
        String s = new String(sink.toByteArray());
        out.flush();
        checkSoftExceptionString(s);

        // save to specified PrintWriter
        sink = new ByteArrayOutputStream();
        PrintWriter pout = new PrintWriter(sink);
        new SoftException(new Error("xyz")).printStackTrace(pout);
        pout.flush();
        s = new String(sink.toByteArray());
        checkSoftExceptionString(s);

        // check System.err redirect
        PrintStream systemErr = System.err;
        try {
            sink = new ByteArrayOutputStream();
            out = new PrintStream(sink);
            System.setErr(out);
            new SoftException(new Error("xyz")).printStackTrace();
            out.flush();
            s = new String(sink.toByteArray());
            checkSoftExceptionString(s);
        } finally {
            System.setErr(systemErr);
        }
    }
   
    
    static void checkSoftExceptionString(String s) {        
        assertTrue(-1 != s.indexOf("SoftException"));
        assertTrue(-1 != s.indexOf("Caused by: java.lang.Error"));
        assertTrue(-1 != s.indexOf("xyz"));
        assertTrue(-1 != s.indexOf("testSoftExceptionPrintStackTrace"));
    }
}  
