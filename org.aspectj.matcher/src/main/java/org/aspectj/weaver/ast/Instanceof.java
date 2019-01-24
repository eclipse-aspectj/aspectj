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

import org.aspectj.weaver.UnresolvedType;

public class Instanceof extends Test {
	Var var;
	UnresolvedType type;

	public Instanceof(Var left, UnresolvedType right) {
		super();
		this.var = left;
		this.type = right;
	}

	public void accept(ITestVisitor v) {
		v.visit(this);
	}
	
	public String toString() {
		return "(" + var + " instanceof " + type + ")";
	}

	public boolean equals(Object other) {
		if (other instanceof Instanceof) {
			Instanceof o = (Instanceof) other;
			return o.var.equals(var) && o.type.equals(type);
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return var.hashCode()*37+type.hashCode();
	}

    public Var getVar() {
        return var;
    }

    public UnresolvedType getType() {
        return type;
    }
}
