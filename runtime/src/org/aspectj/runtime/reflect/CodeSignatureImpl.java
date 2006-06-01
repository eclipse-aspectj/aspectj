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


package org.aspectj.runtime.reflect;

import org.aspectj.lang.reflect.CodeSignature;

abstract class CodeSignatureImpl extends MemberSignatureImpl implements CodeSignature {
    Class[] parameterTypes;
    String[] parameterNames;
    Class[] exceptionTypes;
    
    CodeSignatureImpl(int modifiers, String name, Class declaringType, 
        Class[] parameterTypes, String[] parameterNames, Class[] exceptionTypes)
    {
        super(modifiers, name, declaringType);
        this.parameterTypes = parameterTypes;
        this.parameterNames = parameterNames;
        this.exceptionTypes = exceptionTypes;
    }
    CodeSignatureImpl(String stringRep) {
        super(stringRep);
    }
    
    public Class[] getParameterTypes() {
        if (parameterTypes == null) parameterTypes = extractTypes(3);
        return parameterTypes;
    }
    public String[] getParameterNames() {
        if (parameterNames == null) parameterNames = extractStrings(4);
        return parameterNames;
    }
    public Class[] getExceptionTypes() {
        if (exceptionTypes == null) exceptionTypes = extractTypes(5);
        return exceptionTypes;
    }
}
