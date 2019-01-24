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

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import org.aspectj.lang.reflect.AdviceSignature;

class AdviceSignatureImpl extends CodeSignatureImpl implements AdviceSignature {
    Class returnType;
	private Method adviceMethod = null;
    
    AdviceSignatureImpl(int modifiers, String name, Class declaringType, 
        Class[] parameterTypes, String[] parameterNames, Class[] exceptionTypes,
        Class returnType)
    {
        super(modifiers, name, declaringType, parameterTypes, parameterNames,
            exceptionTypes);
        this.returnType = returnType;
    }
    
    AdviceSignatureImpl(String stringRep) {
        super(stringRep);
    }    
    /* name is consistent with reflection API  
    before and after always return Void.TYPE
    (some around also return Void.Type)      */
    public Class getReturnType() {
        if (returnType == null) returnType = extractType(6);
        return returnType;
    }

    protected String createToString(StringMaker sm) {
        StringBuffer buf = new StringBuffer();
//        buf.append(sm.makeModifiersString(getModifiers()));
        if (sm.includeArgs) buf.append(sm.makeTypeName(getReturnType()));
        if (sm.includeArgs) buf.append(" ");        
        buf.append(sm.makePrimaryTypeName(getDeclaringType(),getDeclaringTypeName()));
        buf.append(".");
        buf.append(toAdviceName(getName()));        
        sm.addSignature(buf, getParameterTypes());
        sm.addThrows(buf, getExceptionTypes());
        return buf.toString();
    }
    
    private String toAdviceName(String methodName) {
    		if (methodName.indexOf('$') == -1) return methodName;
    		StringTokenizer strTok = new StringTokenizer(methodName,"$");
    		while (strTok.hasMoreTokens()) {
    			String token = strTok.nextToken();
    			if ( token.startsWith("before") ||
    				 token.startsWith("after") ||
    				 token.startsWith("around") ) return token;    			   
    		}
    		return methodName;
    }
    
    /* (non-Javadoc)
	 * @see org.aspectj.runtime.reflect.MemberSignatureImpl#createAccessibleObject()
	 */
	public Method getAdvice() {
		if (adviceMethod == null) {
			try {
				adviceMethod = getDeclaringType().getDeclaredMethod(getName(),getParameterTypes());
			} catch (Exception ex) {
				; // nothing we can do, caller will see null
			}
		}
		return adviceMethod;
	}
}
