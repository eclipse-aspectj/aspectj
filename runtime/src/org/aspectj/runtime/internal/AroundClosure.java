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

public abstract class AroundClosure {
    //private Object[] state;

    public AroundClosure(/* Object[] state */) {
        // this.state = state;
    }
    
    protected Object[] state;
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
}
