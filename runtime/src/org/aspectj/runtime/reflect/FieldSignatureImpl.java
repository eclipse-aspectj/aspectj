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
 
import org.aspectj.lang.reflect.FieldSignature;

public class FieldSignatureImpl extends MemberSignatureImpl implements FieldSignature {
    Class fieldType;
    
    FieldSignatureImpl(int modifiers, String name, Class declaringType, 
        Class fieldType)
    {
        super(modifiers, name, declaringType);
        this.fieldType = fieldType;
    }
    
    FieldSignatureImpl(String stringRep) {
        super(stringRep);
    }
    
    public Class getFieldType() {
        if (fieldType == null) fieldType = extractType(3);
        return fieldType;
    }
    
    String toString(StringMaker sm) {
        StringBuffer buf = new StringBuffer();
        buf.append(sm.makeModifiersString(getModifiers()));
        if (sm.includeArgs) buf.append(sm.makeTypeName(getFieldType()));
        if (sm.includeArgs) buf.append(" ");        
        buf.append(sm.makePrimaryTypeName(getDeclaringType()));
        buf.append(".");
        buf.append(getName());        
        return buf.toString();
    } 
}
