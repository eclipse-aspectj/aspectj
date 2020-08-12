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
import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.reflect.MethodSignature;

class MethodSignatureImpl extends CodeSignatureImpl implements MethodSignature {
	private Method method;
	Class returnType;

	MethodSignatureImpl(int modifiers, String name, Class declaringType, Class[] parameterTypes, String[] parameterNames,
			Class[] exceptionTypes, Class returnType) {
		super(modifiers, name, declaringType, parameterTypes, parameterNames, exceptionTypes);
		this.returnType = returnType;
	}

	MethodSignatureImpl(String stringRep) {
		super(stringRep);
	}

	/* name is consistent with reflection API */
	public Class getReturnType() {
		if (returnType == null)
			returnType = extractType(6);
		return returnType;
	}

	protected String createToString(StringMaker sm) {
		StringBuffer buf = new StringBuffer();
		buf.append(sm.makeModifiersString(getModifiers()));
		if (sm.includeArgs)
			buf.append(sm.makeTypeName(getReturnType()));
		if (sm.includeArgs)
			buf.append(" ");
		buf.append(sm.makePrimaryTypeName(getDeclaringType(), getDeclaringTypeName()));
		buf.append(".");
		buf.append(getName());
		sm.addSignature(buf, getParameterTypes());
		sm.addThrows(buf, getExceptionTypes());
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.lang.reflect.MemberSignature#getAccessibleObject()
	 */
	public Method getMethod() {
		if (method == null) {
			Class dtype = getDeclaringType();
			try {
				method = dtype.getDeclaredMethod(getName(), getParameterTypes());
			} catch (NoSuchMethodException nsmEx) {
				// pr154427 - search
				Set searched = new HashSet();
				searched.add(dtype); // avoids another getDeclaredMethod() on dtype
				method = search(dtype, getName(), getParameterTypes(), searched);
			}
		}
		return method;
	}

	/**
	 * Hunt for a method up the hierarchy for a specified type.
	 * 
	 * @param type the type on which to look for the method
	 * @param name the name of the method
	 * @param params the parameters of the method
	 * @param searched a set of types already searched to avoid looking at anything twice
	 * @return the method if found, or null if not found
	 */
	private Method search(Class type, String name, Class[] params, Set searched) {
		if (type == null) {
			return null;
		}
		if (!searched.contains(type)) {
			searched.add(type);
			try {
				return type.getDeclaredMethod(name, params);
			} catch (NoSuchMethodException nsme) {
				// drop through and check superclass and interfaces
			}
		}
		Method m = search(type.getSuperclass(), name, params, searched);
		if (m != null) {
			return m;
		}
		Class[] superinterfaces = type.getInterfaces();
		if (superinterfaces != null) {
			for (Class superinterface : superinterfaces) {
				m = search(superinterface, name, params, searched);
				if (m != null) {
					return m;
				}
			}
		}
		return null;
	}
}
