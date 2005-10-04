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
	 * The name of this method
	 */
	String getName();
	
	/**
	 * The method return type
	 */
	AjType<?> getReturnType();
	
	/**
	 * The generic return type
	 */
	Type getGenericReturnType();
	
	/**
	 * The method parameters
	 */
	AjType<?>[] getParameterTypes();
	
	/**
	 * The generic method parameters
	 */
	Type[] getGenericParameterTypes();
	
	/**
	 * The type variables declared by this method 
	 */
	TypeVariable<Method>[] getTypeParameters();
	
	/**
	 * The declared exceptions thrown by this method
	 */
	AjType<?>[] getExceptionTypes();
}
