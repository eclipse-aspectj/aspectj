/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
