/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.ast;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedTypeX;


public class FieldGet extends Expr {
	Member field;
	ResolvedTypeX resolvedType;

	public FieldGet(Member field, ResolvedTypeX resolvedType) {
		super();
		this.field = field;		
		this.resolvedType = resolvedType;
	}
    
	public ResolvedTypeX getType() {
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

	public ResolvedTypeX getResolvedType() {
		return resolvedType;
	}

}
