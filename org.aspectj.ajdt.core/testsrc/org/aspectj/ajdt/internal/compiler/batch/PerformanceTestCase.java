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

package org.aspectj.ajdt.internal.compiler.batch;

import java.io.IOException;

import org.aspectj.testing.util.TestUtil;


public class PerformanceTestCase extends CommandTestCase {

	public PerformanceTestCase(String name) {
		super(name);
	}

	
	// this is a nice test, but not strictly needed
	public void xxx_testLazyTjpOff() throws IOException {
		checkCompile("src1/LazyTjp.aj", NO_ERRORS);
		try {
			TestUtil.runMain("out", "LazyTjp");
			fail("expected an exception when running without -XlazyTjp");
		} catch (IllegalStateException e) {
			// expected exception thrown when no -XlazyTjp
		}
	}
	
	public void testLazyTjp() throws IOException {
		checkCompile("src1/LazyTjp.aj", new String[] {"-XlazyTjp","-Xlint:error"}, new int[] {96});
	//	TestUtil.runMain("out", "LazyTjp");
	}
}
