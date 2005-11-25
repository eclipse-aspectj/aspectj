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

import org.aspectj.lang.annotation.AdviceName;
import org.aspectj.lang.reflect.Advice;
import org.aspectj.lang.reflect.AdviceKind;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PointcutExpression;

/**
 * @author colyer
 *
 */
public class AdviceImpl implements Advice {
	
	private static final String AJC_INTERNAL = "org.aspectj.runtime.internal";

	private final AdviceKind kind;
	private final Method adviceMethod;
	private PointcutExpression pointcutExpression;
	private boolean hasExtraParam = false;
	private Type[] genericParameterTypes;
	private AjType[] parameterTypes;
	private AjType[] exceptionTypes;
	
	protected AdviceImpl(Method method, String pointcut, AdviceKind type) {
		this.kind = type;
		this.adviceMethod = method;
		this.pointcutExpression = new PointcutExpressionImpl(pointcut);
	}
	
	protected AdviceImpl(Method method, String pointcut, AdviceKind type, String extraParamName) {
		this(method,pointcut,type);
		this.hasExtraParam = true;
	}
	
	public AjType getDeclaringType() {
		return AjTypeSystem.getAjType(adviceMethod.getDeclaringClass());
	}
	
	public Type[] getGenericParameterTypes() {
		if (this.genericParameterTypes == null) {
			Type[] genTypes = adviceMethod.getGenericParameterTypes();
			int syntheticCount = 0;
			for (Type t : genTypes) {
				if (t instanceof Class) {
					if (((Class)t).getPackage().getName().equals(AJC_INTERNAL)) syntheticCount++;
				}
			}
			this.genericParameterTypes = new Type[genTypes.length - syntheticCount];
			for (int i = 0; i < genericParameterTypes.length; i++) {
				if (genTypes[i] instanceof Class) {
					this.genericParameterTypes[i] = AjTypeSystem.getAjType((Class<?>)genTypes[i]);
				} else {
					this.genericParameterTypes[i] = genTypes[i];
				}
			}
		}
		return this.genericParameterTypes;
	}
	
	public AjType<?>[] getParameterTypes() {
		if (this.parameterTypes == null) {
			Class<?>[] ptypes = adviceMethod.getParameterTypes();
			int syntheticCount = 0;
			for(Class<?> c : ptypes) {
				if (c.getPackage().getName().equals(AJC_INTERNAL)) syntheticCount++;
			}
			this.parameterTypes = new AjType<?>[ptypes.length - syntheticCount];
			for (int i = 0; i < parameterTypes.length; i++) {
				this.parameterTypes[i] = AjTypeSystem.getAjType(ptypes[i]);
			}
		}
		return this.parameterTypes;
	}
	
	public AjType<?>[] getExceptionTypes() {
		if (this.exceptionTypes == null) {
			Class<?>[] exTypes = adviceMethod.getExceptionTypes();
			this.exceptionTypes = new AjType<?>[exTypes.length];
			for (int i = 0; i < exTypes.length; i++) {
				this.exceptionTypes[i] = AjTypeSystem.getAjType(exTypes[i]);
			}
		}
		return this.exceptionTypes;
	}
	
	public AdviceKind getKind() {
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
	
	public PointcutExpression getPointcutExpression() {
		return pointcutExpression;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (getName().length() > 0) {
			sb.append("@AdviceName(\"");
			sb.append(getName());
			sb.append("\") ");
		}
		if (getKind() == AdviceKind.AROUND) {
			sb.append(adviceMethod.getGenericReturnType().toString());
			sb.append(" ");
		}
		switch(getKind()) {
		case AFTER:
			sb.append("after(");
			break;
		case AFTER_RETURNING:
			sb.append("after(");
			break;
		case AFTER_THROWING:
			sb.append("after(");
			break;
		case AROUND:
			sb.append("around(");
			break;
		case BEFORE:
			sb.append("before(");
			break;
		}
		AjType<?>[] ptypes = getParameterTypes();
		int len = ptypes.length;
		if (hasExtraParam) len--;
		for (int i = 0; i < len; i++) {
			sb.append(ptypes[i].getName());
			if (i+1 < len) sb.append(",");
		}
		sb.append(") ");
		switch(getKind()) {
		case AFTER_RETURNING:
			sb.append("returning");
			if (hasExtraParam) {
				sb.append("(");
				sb.append(ptypes[len-1].getName());
				sb.append(") ");
			}
		case AFTER_THROWING:
			sb.append("throwing");
			if (hasExtraParam) {
				sb.append("(");
				sb.append(ptypes[len-1].getName());
				sb.append(") ");
			}
		default: // no-op
		}
		AjType<?>[] exTypes = getExceptionTypes();
		if (exTypes.length > 0) {
			sb.append("throws ");
			for (int i = 0; i < exTypes.length; i++) {
				sb.append(exTypes[i].getName());
				if (i+1 < exTypes.length) sb.append(",");
			}
			sb.append(" ");
		}
		sb.append(": ");
		sb.append(getPointcutExpression().asString());
		return sb.toString();
	}
	
}
