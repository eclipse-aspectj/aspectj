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

import org.aspectj.weaver.ResolvedType;

public class Var extends Expr {
	public static final Var[] NONE = new Var[0];
	ResolvedType variableType;

	public Var(ResolvedType variableType) {
		super();
		this.variableType = variableType;
	}

	public ResolvedType getType() {
		return variableType;
	}

	public String toString() {
		return "(Var " + variableType + ")";
	}

	public void accept(IExprVisitor v) {
		v.visit(this);
	}

	/**
	 * For an annotation this will return a variable that can access a specific field of the annotation (of the specified type) TODO
	 * what kind of behaviour happens for two annotation fields of the same type?
	 * 
	 * @param formalType
	 * @param formalName
	 * @return
	 */
	public Var getAccessorForValue(ResolvedType formalType, String formalName) {
		throw new IllegalStateException("Only makes sense for annotation variables");
	}
}
