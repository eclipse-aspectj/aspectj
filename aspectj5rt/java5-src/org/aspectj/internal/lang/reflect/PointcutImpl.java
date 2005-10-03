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
import java.util.StringTokenizer;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
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
	private String[] parameterNames = new String[0];
	
	protected PointcutImpl(String name, String pc, Method method, AjType declaringType, String pNames) {
		this.name = name;
		this.pc = new PointcutExpressionImpl(pc);
		this.baseMethod = method;
		this.declaringType = declaringType;
		this.parameterNames = splitOnComma(pNames);
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

	public AjType<?>[] getParameterTypes() {
		Class<?>[] baseParamTypes =  baseMethod.getParameterTypes();
		AjType<?>[] ajParamTypes = new AjType<?>[baseParamTypes.length];
		for (int i = 0; i < ajParamTypes.length; i++) {
			ajParamTypes[i] = AjTypeSystem.getAjType(baseParamTypes[i]);
		}
		return ajParamTypes;
	}

	public AjType getDeclaringType() {
		return declaringType;
	}
	
	public String[] getParameterNames() {
		return parameterNames;
	}

	private String[] splitOnComma(String s) {
		StringTokenizer strTok = new StringTokenizer(s,",");
		String[] ret = new String[strTok.countTokens()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = strTok.nextToken().trim();
		}
		return ret;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName());
		sb.append("(");
		AjType<?>[] ptypes = getParameterTypes();
		for (int i = 0; i < ptypes.length; i++) {
			sb.append(ptypes[i].getName());
			if (this.parameterNames != null && this.parameterNames[i] != null) {
				sb.append(" ");
				sb.append(this.parameterNames[i]);
			}
			if (i+1 < ptypes.length) sb.append(",");
		}
		sb.append(") : ");
		sb.append(getPointcutExpression().asString());
		return sb.toString();
	}
}
