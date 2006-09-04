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

import java.lang.reflect.Constructor;

import org.aspectj.lang.reflect.ConstructorSignature;

class ConstructorSignatureImpl extends CodeSignatureImpl implements ConstructorSignature {
	private Constructor constructor;
	
    ConstructorSignatureImpl(int modifiers, Class declaringType, 
        Class[] parameterTypes, String[] parameterNames, Class[] exceptionTypes)
    {
        super(modifiers, "<init>", declaringType, parameterTypes, parameterNames, exceptionTypes);
    }    
    
    ConstructorSignatureImpl(String stringRep) {
        super(stringRep);
    }
    
    public String getName() { return "<init>"; }
    
    protected String createToString(StringMaker sm) {
        StringBuffer buf = new StringBuffer();
        buf.append(sm.makeModifiersString(getModifiers()));
        buf.append(sm.makePrimaryTypeName(getDeclaringType(),getDeclaringTypeName()));
        sm.addSignature(buf, getParameterTypes());
        sm.addThrows(buf, getExceptionTypes());
        return buf.toString();
    }
    
    /* (non-Javadoc)
	 * @see org.aspectj.runtime.reflect.MemberSignatureImpl#createAccessibleObject()
	 */
	public Constructor getConstructor() {
		if (constructor == null) {
			try {
				constructor = getDeclaringType().getDeclaredConstructor(getParameterTypes());
			} catch (Exception ex) {
				; // nothing we can do, caller will see null
			}
		}
		return constructor;
	}
}
