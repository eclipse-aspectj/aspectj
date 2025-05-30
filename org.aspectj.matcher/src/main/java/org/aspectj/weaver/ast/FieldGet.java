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

public class FieldGet extends Expr {
	Member field;
	ResolvedType resolvedType;

	public FieldGet(Member field, ResolvedType resolvedType) {
		super();
		this.field = field;
		this.resolvedType = resolvedType;
	}

	public ResolvedType getType() {
		return resolvedType;
	}

	public String toString() {
		return "(FieldGet " + field + ")";
	}

	public void accept(IExprVisitor v) {
		v.visit(this);
	}

	public Member getField() {
		return field;
	}

}
