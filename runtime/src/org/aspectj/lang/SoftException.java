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


package org.aspectj.lang;

/** 
 * Wrapper for checked exceptions matched by a 'declare soft'.
 * You can soften checked exceptions at join points by using
 * the form <code>declare soft: TypePattern: Pointcut</code>.
 * At the join points, any exceptions thrown which match
 * TypePattern will be wrapped in <code>SoftException</code>
 * and rethrown. You can get the original exception using
 * <code>getWrappedThrowable()</code>.
 */
public class SoftException extends RuntimeException {
    Throwable inner;
    public SoftException(Throwable inner) {
        super();
        this.inner = inner;
    }
    
    public Throwable getWrappedThrowable() { return inner; }
    
    //XXX should add a getCause() method to parallel j2se 1.4's new
    //XXX chained exception mechanism
}
