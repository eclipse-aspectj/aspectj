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

import org.aspectj.lang.annotation.AdviceName;
import org.aspectj.lang.reflect.Advice;
import org.aspectj.lang.reflect.AdviceType;

/**
 * @author colyer
 *
 */
public class AdviceImpl implements Advice {

	private final AdviceType kind;
	private final Method adviceMethod;
	private String pointcutExpression;
	
	protected AdviceImpl(Method method, String pointcut, AdviceType type) {
		this.kind = type;
		this.adviceMethod = method;
		this.pointcutExpression = pointcut;
	}
	
	public AdviceType getKind() {
		return kind;
	}
	
	public String getName() {
		String adviceName = adviceMethod.getName();
		if (adviceName.startsWith("ajc$")) {
			adviceName = "";
			AdviceName name = adviceMethod.getAnnotation(AdviceName.class);
			if (name != null) adviceName = name.value();
		}
		return adviceName;
	}
	
	public String getPointcutExpression() {
		return pointcutExpression;
	}
	
}
