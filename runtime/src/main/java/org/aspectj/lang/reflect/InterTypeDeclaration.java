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

/**
 * Represents an inter-type method, field, or constructor declared in an aspect.
 */
public interface InterTypeDeclaration {

	/**
	 * The declaring aspect
	 */
	AjType<?> getDeclaringType();
	
	/**
	 * The target type of this ITD
	 */
	AjType<?> getTargetType() throws ClassNotFoundException;
	
	/**
	 * Member modifiers, can be interpreted using java.lang.reflect.Modifier
	 */
	int getModifiers();
}
