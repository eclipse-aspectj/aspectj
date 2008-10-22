/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.internal.tools;

import org.aspectj.weaver.ast.ITestVisitor;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.tools.MatchingContext;
import org.aspectj.weaver.tools.ContextBasedMatcher;

/**
 * Test that uses MatchingContext to match (or not)
 *
 */
public class MatchingContextBasedTest extends Test {

	private final ContextBasedMatcher matcher;
	
	public MatchingContextBasedTest(ContextBasedMatcher pc) {
		this.matcher = pc;
	}
	
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ast.Test#accept(org.aspectj.weaver.ast.ITestVisitor)
	 */
	public void accept(ITestVisitor v) {
		v.visit(this);
	}
	
	public boolean matches(MatchingContext context) {
		return this.matcher.matchesDynamically(context);
	}

}
