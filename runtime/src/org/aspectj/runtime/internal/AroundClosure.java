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


package org.aspectj.runtime.internal;

import org.aspectj.lang.JoinPoint;

public abstract class AroundClosure {
    //private Object[] state;

    public AroundClosure(/* Object[] state */) {
        // this.state = state;
    }
    
    public Object[] state; // ALEX Andy. Made public so can be accessed from JoinPointImpl.proceed()
    
    protected Object[] preInitializationState;
    public AroundClosure(Object[] state) {
    	this.state = state;
    }
    
	public Object[] getPreInitializationState() {
		return preInitializationState;
	}

	/**
	 * This takes in the same arguments as are passed to the proceed
	 * call in the around advice (with primitives coerced to Object types)
	 */
    public abstract Object run(Object[] args) throws Throwable;

    //ALEX Andy. Added by Alex for some caller??
    public JoinPoint getJoinPoint() {
        JoinPoint jp = (JoinPoint)state[state.length-1];
        jp.set$AroundClosure(this);
        return jp;
    }
}
