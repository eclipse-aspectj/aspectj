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
package org.aspectj.internal.lang.reflect;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.DeclareErrorOrWarning;
import org.aspectj.lang.reflect.PointcutExpression;

/**
 * @author colyer
 *
 */
public class DeclareErrorOrWarningImpl implements DeclareErrorOrWarning {

	private PointcutExpression pc;
	private String msg;
	private boolean isError;
	private AjType<?> declaringType;

	public DeclareErrorOrWarningImpl(String pointcut, String message, boolean isError, AjType decType) {
		this.pc = new PointcutExpressionImpl(pointcut);
		this.msg = message;
		this.isError = isError;
		this.declaringType = decType;
	}

	public AjType getDeclaringType() { return this.declaringType; }

	public PointcutExpression getPointcutExpression() {
		return pc;
	}

	public String getMessage() {
		return msg;
	}

	public boolean isError() {
		return isError;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("declare ");
		sb.append(isError() ? "error : " : "warning : ");
		sb.append(getPointcutExpression().asString());
		sb.append(" : ");
		sb.append("\"");
		sb.append(getMessage());
		sb.append("\"");
		return sb.toString();
	}

}
