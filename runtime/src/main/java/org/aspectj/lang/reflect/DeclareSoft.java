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
 * AspectJ runtime representation of a declare soft member within an aspect.
 */
public interface DeclareSoft {

	/**
	 * @return the aspect that declared this member
	 */
	AjType getDeclaringType();

	/**
	 * @return the softened exception type
	 * @throws ClassNotFoundException if exception type cannot be found
	 */
	AjType getSoftenedExceptionType() throws ClassNotFoundException;

	/**
	 * @return the pointcut determining the join points at which the exception is to be softened.
	 */
	PointcutExpression getPointcutExpression();

}
