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

import java.lang.reflect.Type;

/**
 * Runtime representation of an advice declaration inside an aspect
 */
public interface Advice {

	/**
	 * @return the declaring aspect
	 */
	AjType getDeclaringType();

	/**
	 * @return the kind of advice (before, after-returning, after-throwing, etc.)
	 */
	AdviceKind getKind();

	/**
	 * If using the @AspectJ annotations, the advice name is the name of the
	 * annotated advice method. If using the code style, the advice is
	 * anonymous, unless the advice is annotated with the @AdviceName annotation,
	 * in which case the name given in the annotation is returned.
	 *
	 * @return the advice name, or the empty string if the advice is anonymous.
	 */
	String getName();

	/**
	 * @return the advice parameters
	 */
	AjType<?>[] getParameterTypes();

	/**
	 * @return the generic parameter types, @see java.lang.reflect.Method.getGenericParameterTypes
	 */
	Type[] getGenericParameterTypes();

	/**
	 * @return the declared thrown exceptions by the advice
	 */
	AjType<?>[] getExceptionTypes();

	/**
	 * @return the pointcut expression associated with the advice declaration.
	 */
	PointcutExpression getPointcutExpression();
}
