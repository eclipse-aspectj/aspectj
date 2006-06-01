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
	Test body;

	public Not(Test left) {
		super();
		this.body = left;
	}

	public void accept(ITestVisitor v) {
		v.visit(this);
	}
	
	public Test getBody() {
		return body;
	}
	
	public String toString() {
		return "!" + body;
	}

	public boolean equals(Object other) {
		if (other instanceof Not) {
			Not o = (Not) other;
			return o.body.equals(body);
		} else {
			return false;
		}
	}
}
