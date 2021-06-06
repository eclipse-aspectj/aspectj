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

import org.aspectj.lang.reflect.PointcutExpression;

/**
 * @author colyer
 *
 */
public class PointcutExpressionImpl implements PointcutExpression {
	private String expression;

	public PointcutExpressionImpl(String aPointcutExpression) {
		this.expression = aPointcutExpression;
	}

	public String asString() {
		return expression;
	}

	public String toString() { return asString(); }
}
