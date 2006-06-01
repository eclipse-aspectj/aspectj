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


package org.aspectj.runtime;

public class CFlow {
    private Object _aspect;

    public CFlow() {
        this(null);
    }

    public CFlow(Object _aspect) {
        this._aspect = _aspect;
    }

    public Object getAspect() {
        return this._aspect;
    }

    public void setAspect(Object _aspect) {
        this._aspect = _aspect;
    }

    public Object get(int index) {
        return null;
    }
}
