/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.batch;

import java.io.IOException;
import java.util.*;


public class BinaryFormsTestCase extends CommandTestCase {

	public BinaryFormsTestCase(String name) {
		super(name);
	}
	
	public void testDummy() {}


	public void XXXtestJar1() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("out/megatrace.jar");

		args.add("-classpath");
		args.add("../runtime/bin");
		
		args.add("testdata/src1/trace/MegaTrace.java");
		args.add("testdata/src1/trace/ExecTrace.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		args = new ArrayList();
		args.add("-aspectpath");
		args.add("out/megatrace.jar");

		args.add("-classpath");
		args.add("../runtime/bin");
		
		args.add("testdata/src1/tracep1/TraceTest.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
}
