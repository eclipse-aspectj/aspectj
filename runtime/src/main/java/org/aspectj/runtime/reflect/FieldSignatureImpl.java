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
 
import java.lang.reflect.Field;

import org.aspectj.lang.reflect.FieldSignature;

public class FieldSignatureImpl extends MemberSignatureImpl implements FieldSignature {
    Class fieldType;
	private Field field;
    
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
    
    protected String createToString(StringMaker sm) {
        StringBuffer buf = new StringBuffer();
        buf.append(sm.makeModifiersString(getModifiers()));
        if (sm.includeArgs) buf.append(sm.makeTypeName(getFieldType()));
        if (sm.includeArgs) buf.append(" ");        
        buf.append(sm.makePrimaryTypeName(getDeclaringType(),getDeclaringTypeName()));
        buf.append(".");
        buf.append(getName());        
        return buf.toString();
    } 
    
    /* (non-Javadoc)
	 * @see org.aspectj.runtime.reflect.MemberSignatureImpl#createAccessibleObject()
	 */
	public Field getField() {
		if (field == null) {
			try {
				field = getDeclaringType().getDeclaredField(getName());
			} catch (Exception ex) {
				; // nothing we can do, caller will see null
			}
		}
		return field;
	}
}
