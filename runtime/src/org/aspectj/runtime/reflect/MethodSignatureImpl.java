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


package org.aspectj.runtime.reflect;

import org.aspectj.lang.reflect.MethodSignature;

class MethodSignatureImpl extends CodeSignatureImpl implements MethodSignature {
    Class returnType;
    
    MethodSignatureImpl(int modifiers, String name, Class declaringType, 
        Class[] parameterTypes, String[] parameterNames, Class[] exceptionTypes,
        Class returnType)
    {
        super(modifiers, name, declaringType, parameterTypes, parameterNames,
            exceptionTypes);
        this.returnType = returnType;
    }
    
    MethodSignatureImpl(String stringRep) {
        super(stringRep);
    }

    /* name is consistent with reflection API */
    public Class getReturnType() {
        if (returnType == null) returnType = extractType(6);
        return returnType;
    }
    
    String toString(StringMaker sm) {
        StringBuffer buf = new StringBuffer();
        buf.append(sm.makeModifiersString(getModifiers()));
        if (sm.includeArgs) buf.append(sm.makeTypeName(getReturnType()));
        if (sm.includeArgs) buf.append(" ");        
        buf.append(sm.makePrimaryTypeName(getDeclaringType()));
        buf.append(".");
        buf.append(getName());        
        sm.addSignature(buf, getParameterTypes());
        sm.addThrows(buf, getExceptionTypes());
        return buf.toString();
    }
}
