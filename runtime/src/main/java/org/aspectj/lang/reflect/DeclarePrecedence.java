/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
	 * @return the declaring aspect
	 */
	AjType getDeclaringType();

	/**
	 * @return an ordered set of type patterns. An aspect matching
	 * a type pattern at a lower index in the array takes precedence
	 * over an aspect that only matches a type pattern at a higher
	 * index in the array.
	 */
	TypePattern[] getPrecedenceOrder();
}
