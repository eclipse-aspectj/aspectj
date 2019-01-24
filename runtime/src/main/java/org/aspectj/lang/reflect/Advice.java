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

import java.lang.reflect.Type;

/**
 * Runtime representation of an advice declaration inside an aspect
 */
public interface Advice {

	/**
	 * The declaring aspect
	 */
	AjType getDeclaringType();
	
	/**
	 * The kind of advice (before, after-returning, after-throwing, etc.)
	 */
	AdviceKind getKind();
	
	/**
	 * Returns the advice name, or the empty string if the advice is anonymous.
	 * If using the @AspectJ annotations, the advice name is the name of the
	 * annotated advice method. If using the code style, the advice is
	 * anonymous, unless the advice is annotated with the @AdviceName annotation,
	 * in which case the name given in the annotation is returned. 
	 */
	String getName();
	
	/**
	 * The advice parameters
	 */
	AjType<?>[] getParameterTypes();
	
	/**
	 * The generic parameter types, @see java.lang.reflect.Method.getGenericParameterTypes
	 */
	Type[] getGenericParameterTypes();
	
	/**
	 * The declared thrown exceptions by the advice
	 */
	AjType<?>[] getExceptionTypes();
	
	/**
	 * The pointcut expression associated with the advice declaration.
	 */
	PointcutExpression getPointcutExpression();
}
