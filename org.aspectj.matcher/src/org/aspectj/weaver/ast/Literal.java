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


public final class Literal extends Test {

	boolean noTest;
	boolean val;

	private Literal(boolean val, boolean noTest) {
		super();
		this.val = val;
		this.noTest = noTest;
	}
	
	public void accept(ITestVisitor v) {
		v.visit(this);
	}
	
	public static final Literal TRUE = new Literal(true, false);
	public static final Literal FALSE = new Literal(false, false);
//	public static final Literal NO_TEST = new Literal(false, true);
	
	public String toString() {
		return noTest ? "NO_TEST" : val ? "TRUE" : "FALSE";
	}
}
