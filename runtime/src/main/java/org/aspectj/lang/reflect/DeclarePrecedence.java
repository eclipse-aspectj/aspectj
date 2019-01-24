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
 * AspectJ runtime representation of a declare precedence statement as 
 * declared in an aspect.
 */
public interface DeclarePrecedence {

	/**
	 * The declaring aspect
	 */
	AjType getDeclaringType();
	
	/**
	 * Returns an ordered set of type patterns. An aspect matching
	 * a type pattern at a lower index in the array takes precedence
	 * over an aspect that only matches a type pattern at a higher
	 * index in the array.
	 */
	TypePattern[] getPrecedenceOrder();
}
