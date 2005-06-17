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

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.Pointcut;
import org.aspectj.lang.reflect.PointcutExpression;

/**
 * @author colyer
 *
 */
public class PointcutImpl implements Pointcut {

	private final String name;
	private final PointcutExpression pc;
	private final Method baseMethod;
	private final AjType declaringType;
	
	protected PointcutImpl(String name, String pc, Method method, AjType declaringType) {
		this.name = name;
		this.pc = new PointcutExpressionImpl(pc);
		this.baseMethod = method;
		this.declaringType = declaringType;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.Pointcut#getPointcutExpression()
	 */
	public PointcutExpression getPointcutExpression() {
		return pc;
	}
	
	public String getName() {
		return name;
	}

	public int getModifiers() {
		return baseMethod.getModifiers();
	}

	public Class<?>[] getParameterTypes() {
		return baseMethod.getParameterTypes();
	}

	public AjType getDeclaringType() {
		return declaringType;
	}

}
