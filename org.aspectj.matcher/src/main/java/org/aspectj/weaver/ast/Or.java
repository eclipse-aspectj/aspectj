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

public class Or extends Test {
	Test left, right;

	public Or(Test left, Test right) {
		super();
		this.left = left;
		this.right = right;
	}

	public void accept(ITestVisitor v) {
		v.visit(this);
	}
	
	public String toString() {
		return "(" + left + " || " + right + ")";
	}

	public boolean equals(Object other) {
		if (other instanceof Or) {
			Or o = (Or) other;
			return o.left.equals(left) && o.right.equals(right);
		} else {
			return false;
		}
	}

    public int hashCode() {
        int result = 19;
        result = 37*result + left.hashCode();
        result = 37*result + right.hashCode();
        return result;
    }

    public Test getLeft() {
        return left;
    }

    public Test getRight() {
        return right;
    }
}
