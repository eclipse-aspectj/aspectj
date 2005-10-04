/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.internal.lang.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.InterTypeConstructorDeclaration;

/**
 * @author colyer
 *
 */
public class InterTypeConstructorDeclarationImpl extends
		InterTypeDeclarationImpl implements InterTypeConstructorDeclaration {

	private Method baseMethod;
	
	/**
	 * @param decType
	 * @param target
	 * @param mods
	 */
	public InterTypeConstructorDeclarationImpl(AjType<?> decType,
			String target, int mods, Method baseMethod) {
		super(decType, target, mods);
		this.baseMethod = baseMethod;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.InterTypeConstructorDeclaration#getParameters()
	 */
	public AjType<?>[] getParameterTypes() {
		Class<?>[] baseTypes = baseMethod.getParameterTypes();
		AjType<?>[] ret = new AjType<?>[baseTypes.length-1];
		for (int i = 1; i < baseTypes.length; i++) {
			ret[i-1] = AjTypeSystem.getAjType(baseTypes[i]);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.InterTypeConstructorDeclaration#getGenericParameters()
	 */
	public Type[] getGenericParameterTypes() {
		Type[] baseTypes = baseMethod.getGenericParameterTypes();
		Type[] ret = new AjType<?>[baseTypes.length-1];
		for (int i = 1; i < baseTypes.length; i++) {
			if (baseTypes[i] instanceof Class) {
				ret[i-1] = AjTypeSystem.getAjType((Class<?>)baseTypes[i]);
			} else {
				ret[i-1] = baseTypes[i];
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.InterTypeConstructorDeclaration#getDeclaredExceptionTypes()
	 */
	public AjType<?>[] getExceptionTypes() {
		Class<?>[] baseTypes = baseMethod.getExceptionTypes();
		AjType<?>[] ret = new AjType<?>[baseTypes.length];
		for (int i = 0; i < baseTypes.length; i++) {
			ret[i] = AjTypeSystem.getAjType(baseTypes[i]);
		}
		return ret;
	}

}
