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
 * AspectJ runtime representation of a pointcut member inside a class or aspect.
 */
public interface Pointcut {

	/**
	 * @return the declared name of the pointcut.
	 */
	String getName();

	/**
	 * @return the modifiers associated with the pointcut declaration.
	 * Use java.lang.reflect.Modifier to interpret the return value
	 */
	int getModifiers();

	/**
	 * @return the pointcut parameter types.
	 */
	AjType<?>[] getParameterTypes();

	/**
	 * @return the pointcut parameter names. Returns an array of empty strings
	 * of length getParameterTypes().length if parameter names are not
	 * available at runtime.
	 */
	String[] getParameterNames();

	/**
	 * @return the type that declared this pointcut
	 */
	AjType getDeclaringType();

	/**
	 * @return the pointcut expression associated with this pointcut.
	 */
	PointcutExpression getPointcutExpression();

}
