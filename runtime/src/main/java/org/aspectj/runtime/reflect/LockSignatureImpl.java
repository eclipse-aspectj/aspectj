/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/


package org.aspectj.runtime.reflect;

import java.lang.reflect.Modifier;

import org.aspectj.lang.reflect.LockSignature;

class LockSignatureImpl extends SignatureImpl implements LockSignature {
    private Class parameterType;
    
    LockSignatureImpl(Class c) {
        super(Modifier.STATIC, "lock", c);
        parameterType = c;
    }
    
    LockSignatureImpl(String stringRep) {
        super(stringRep);
    }
    
    protected String createToString(StringMaker sm) {
        if (parameterType == null) parameterType = extractType(3);
        return "lock("+sm.makeTypeName(parameterType)+")";
    }    

    public Class getParameterType() {
        if (parameterType == null) parameterType = extractType(3);
        return parameterType;
    }
    
}
