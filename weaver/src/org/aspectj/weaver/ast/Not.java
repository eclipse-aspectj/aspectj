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


public class Not extends Test {
	Test test;

	public Not(Test test) {
		super();
		this.test = test;
	}

	public void accept(ITestVisitor v) {
		v.visit(this);
	}
	
	public Test getBody() {
		return test;
	}
	
	public String toString() {
		return "!" + test;
	}

	public boolean equals(Object other) {
		if (other instanceof Not) {
			Not o = (Not) other;
			return o.test.equals(test);
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return test.hashCode();
	}
}
