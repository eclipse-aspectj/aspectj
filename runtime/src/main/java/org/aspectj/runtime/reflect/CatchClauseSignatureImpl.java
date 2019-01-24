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

import org.aspectj.lang.reflect.CatchClauseSignature;

class CatchClauseSignatureImpl extends SignatureImpl implements CatchClauseSignature {
    Class parameterType;
    String parameterName;
    
    CatchClauseSignatureImpl(Class declaringType, 
        Class parameterType, String parameterName)
    {
        super(0, "catch", declaringType);
        this.parameterType = parameterType;
        this.parameterName = parameterName;
    }
    
    CatchClauseSignatureImpl(String stringRep) {
        super(stringRep);
    }
    
    public Class getParameterType() {
        if (parameterType == null) parameterType = extractType(3);
        return parameterType;
    }
    public String getParameterName() {
        if (parameterName == null) parameterName = extractString(4);
        return parameterName;
    }
    
    protected String createToString(StringMaker sm) {
        return "catch(" + sm.makeTypeName(getParameterType()) + ")";
    }    
}
