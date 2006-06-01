/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC),
 *               2004 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.lang;

import java.io.PrintStream;
import java.io.PrintWriter;

/** 
 * Wrapper for checked exceptions matched by a 'declare soft'.
 * You can soften checked exceptions at join points by using
 * the form <code>declare soft: TypePattern: Pointcut</code>.
 * At the join points, any exceptions thrown which match
 * TypePattern will be wrapped in <code>SoftException</code>
 * and rethrown. You can get the original exception using
 * <code>getWrappedThrowable()</code> or
 * <code>getCause()</code>.
 */
public class SoftException extends RuntimeException {

    private static final boolean HAVE_JAVA_14;

    static {
        boolean java14 = false;
        try {
            Class.forName("java.nio.Buffer");
            java14 = true;
        } catch (Throwable t) {
            // still false;
        }
        HAVE_JAVA_14 = java14;
    }

    // shouldn't field be private final, constructor default or private? 
    // but either would be a binary incompatible change.

    Throwable inner; 

    public SoftException(Throwable inner) {
        super();
        this.inner = inner;
    }
    
    public Throwable getWrappedThrowable() { return inner; }
    public Throwable getCause() { return inner; }
    
    public void printStackTrace() {
        printStackTrace(System.err);                
    }
    
    public void printStackTrace(PrintStream stream) {
        super.printStackTrace(stream);
        final Throwable _inner = this.inner;
        if (!HAVE_JAVA_14 && (null != _inner)) {
            stream.print("Caused by: ");
            _inner.printStackTrace(stream);
        }
    }
    
    public void printStackTrace(PrintWriter stream) {
        super.printStackTrace(stream);
        final Throwable _inner = this.inner;
        if (!HAVE_JAVA_14 && (null != _inner)) {
            stream.print("Caused by: ");
            _inner.printStackTrace(stream);
        }
    }
}
