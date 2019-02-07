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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.InterTypeMethodDeclaration;

/**
 * @author colyer
 *
 */
public class InterTypeMethodDeclarationImpl extends InterTypeDeclarationImpl
		implements InterTypeMethodDeclaration {

	private String name;
	private Method baseMethod;
	private int parameterAdjustmentFactor = 1; // no of fake params at start of baseMethod
	private AjType<?>[] parameterTypes;
	private Type[] genericParameterTypes;
	private AjType<?> returnType;
	private Type genericReturnType;
	private AjType<?>[] exceptionTypes;
	
	public InterTypeMethodDeclarationImpl(AjType<?> decType, String target,
			int mods, String name, Method itdInterMethod) {
		super(decType, target, mods);
		this.name = name;
		this.baseMethod = itdInterMethod;
	}

	public InterTypeMethodDeclarationImpl(AjType<?> decType, AjType<?> targetType, Method base, int modifiers) {
		super(decType,targetType,modifiers);
		this.parameterAdjustmentFactor = 0;
		this.name = base.getName();
		this.baseMethod = base;
	}
	
	public String getName() {
		return this.name;
	}

	public AjType<?> getReturnType() {
		return AjTypeSystem.getAjType(baseMethod.getReturnType());
	}

	public Type getGenericReturnType() {
		Type gRet = baseMethod.getGenericReturnType();
		if (gRet instanceof Class) {
			return AjTypeSystem.getAjType((Class<?>)gRet);
		}
		return gRet;
	}

	public AjType<?>[] getParameterTypes() {
		Class<?>[] baseTypes = baseMethod.getParameterTypes();
		AjType<?>[] ret = new AjType<?>[baseTypes.length -parameterAdjustmentFactor];
		for (int i = parameterAdjustmentFactor; i < baseTypes.length; i++) {
			ret[i-parameterAdjustmentFactor] = AjTypeSystem.getAjType(baseTypes[i]);
		}
		return ret;
	}

	public Type[] getGenericParameterTypes() {
		Type[] baseTypes = baseMethod.getGenericParameterTypes();
		Type[] ret = new AjType<?>[baseTypes.length-parameterAdjustmentFactor];
		for (int i = parameterAdjustmentFactor; i < baseTypes.length; i++) {
			if (baseTypes[i] instanceof Class) {
				ret[i-parameterAdjustmentFactor] = AjTypeSystem.getAjType((Class<?>)baseTypes[i]);
			} else {
				ret[i-parameterAdjustmentFactor] = baseTypes[i];
			}
		}
		return ret;
	}

	public TypeVariable<Method>[] getTypeParameters() {
		return baseMethod.getTypeParameters();
	}

	public AjType<?>[] getExceptionTypes() {
		Class<?>[] baseTypes = baseMethod.getExceptionTypes();
		AjType<?>[] ret = new AjType<?>[baseTypes.length];
		for (int i = 0; i < baseTypes.length; i++) {
			ret[i] = AjTypeSystem.getAjType(baseTypes[i]);
		}
		return ret;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(java.lang.reflect.Modifier.toString(getModifiers()));
		sb.append(" ");
		sb.append(getReturnType().toString());
		sb.append(" ");
		sb.append(this.targetTypeName);
		sb.append(".");
		sb.append(getName());
		sb.append("(");
		AjType<?>[] pTypes = getParameterTypes();
		for(int i = 0; i < (pTypes.length - 1); i++) {
			sb.append(pTypes[i].toString());
			sb.append(", ");
		}
		if (pTypes.length > 0) {
			sb.append(pTypes[pTypes.length -1].toString());
		}
		sb.append(")");
		return sb.toString();
	}

}
