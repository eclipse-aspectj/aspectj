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

import org.aspectj.lang.reflect.InitializerSignature;

import java.lang.reflect.Modifier;

class InitializerSignatureImpl extends CodeSignatureImpl implements InitializerSignature {
    InitializerSignatureImpl(int modifiers, Class declaringType) {
        super(modifiers, Modifier.isStatic(modifiers) ? "<clinit>" : "<init>", declaringType, EMPTY_CLASS_ARRAY, 
              EMPTY_STRING_ARRAY, EMPTY_CLASS_ARRAY);
    }
    
    InitializerSignatureImpl(String stringRep) {
        super(stringRep);
    }
    
    public String getName() {
    	return Modifier.isStatic(getModifiers()) ? "<clinit>": "<init>";
    }

    String toString(StringMaker sm) {
        StringBuffer buf = new StringBuffer();
        buf.append(sm.makeModifiersString(getModifiers()));    
        buf.append(sm.makePrimaryTypeName(getDeclaringType()));
        buf.append(".");
        buf.append(getName());        
        return buf.toString();
    }
}
