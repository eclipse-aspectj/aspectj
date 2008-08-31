/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.ast;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;

public abstract class Expr extends ASTNode {

	public Expr() {
		super();
	}

	public static final Expr[] NONE = new Expr[0];

	public abstract void accept(IExprVisitor v);

	public abstract ResolvedType getType();

	public static CallExpr makeCallExpr(Member member, Expr[] exprs, ResolvedType returnType) {
		return new CallExpr(member, exprs, returnType);
	}

}
