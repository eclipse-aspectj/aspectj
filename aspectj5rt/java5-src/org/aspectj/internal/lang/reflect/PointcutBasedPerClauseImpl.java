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

import org.aspectj.lang.reflect.PerClauseKind;
import org.aspectj.lang.reflect.PointcutBasedPerClause;
import org.aspectj.lang.reflect.PointcutExpression;

/**
 * @author colyer
 *
 */
public class PointcutBasedPerClauseImpl extends PerClauseImpl implements
		PointcutBasedPerClause {

	private final PointcutExpression pointcutExpression;

	public PointcutBasedPerClauseImpl(PerClauseKind kind,
			String pointcutExpression) {
		super(kind);
		this.pointcutExpression = new PointcutExpressionImpl(pointcutExpression);
	}
	
	public PointcutExpression getPointcutExpression() {
		return pointcutExpression;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		switch(getKind()) {
		case PERCFLOW: sb.append("percflow("); break;
		case PERCFLOWBELOW: sb.append("percflowbelow("); break;
		case PERTARGET: sb.append("pertarget("); break;
		case PERTHIS: sb.append("perthis("); break;
		}
		sb.append(this.pointcutExpression.asString());
		sb.append(")");
		return sb.toString();
	}
}
