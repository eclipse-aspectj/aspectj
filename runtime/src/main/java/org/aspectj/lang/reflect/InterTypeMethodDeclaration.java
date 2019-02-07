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
package org.aspectj.lang.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Represents an inter-type method declaration member within an aspect.
 */
public interface InterTypeMethodDeclaration extends InterTypeDeclaration {

	/**
	 * @return the name of this method
	 */
	String getName();
	
	/**
	 * @return the method return type
	 */
	AjType<?> getReturnType();
	
	/**
	 * @return the generic return type
	 */
	Type getGenericReturnType();
	
	/**
	 * @return the method parameters
	 */
	AjType<?>[] getParameterTypes();
	
	/**
	 * @return the generic method parameters
	 */
	Type[] getGenericParameterTypes();
	
	/**
	 * @return the type variables declared by this method 
	 */
	TypeVariable<Method>[] getTypeParameters();
	
	/**
	 * @return the declared exceptions thrown by this method
	 */
	AjType<?>[] getExceptionTypes();
}
