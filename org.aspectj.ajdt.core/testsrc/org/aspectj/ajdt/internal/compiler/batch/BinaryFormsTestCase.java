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
import java.util.ArrayList;
import java.util.List;

import org.aspectj.testing.util.TestUtil;


public class BinaryFormsTestCase extends CommandTestCase {

	public BinaryFormsTestCase(String name) {
		super(name);
	}
	

	public void testJar1() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("out/lib.jar");

		args.add("-classpath");
		args.add("../runtime/bin");
		
		args.add("-d");
		args.add("out");
		
		args.add("testdata/src1/binary/lib/ConcreteA.aj");
		args.add("testdata/src1/binary/lib/AbstractA.aj");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		args = new ArrayList();
		args.add("-aspectpath");
		args.add("out/lib.jar");

		args.add("-classpath");
		args.add("../runtime/bin");
		
		args.add("-d");
		args.add("out");
		
		args.add("testdata/src1/binary/client/Client.java");
		args.add("testdata/src1/binary/client/Client1.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		TestUtil.runMain("out;out/lib.jar", "client.Client");
		TestUtil.runMain("out;out/lib.jar", "client.Client1");
		
		args = new ArrayList();
		args.add("-aspectpath");
		args.add("out/lib.jar");

		args.add("-classpath");
		args.add("../runtime/bin");
		
		args.add("-d");
		args.add("out");
		
		args.add("testdata/src1/binary/client/MyAspect.aj");
		args.add("testdata/src1/binary/client/Client1.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		TestUtil.runMain("out;out/lib.jar", "client.Client1");

		args = new ArrayList();
		args.add("-aspectpath");
		args.add("out/lib.jar");

		args.add("-classpath");
		args.add("../runtime/bin");
		
		args.add("-d");
		args.add("out");
		
		args.add("testdata/src1/binary/client/MyAspect1.aj");
		args.add("testdata/src1/binary/client/Client1.java");
		
		CommandTestCase.runCompiler(args, new int[] {24, 30});

		args = new ArrayList();
		args.add("-classpath");
		args.add("../runtime/bin;out/lib.jar");
		
		args.add("-d");
		args.add("out");
		
		args.add("testdata/src1/binary/client/Client1.java");
		
		CommandTestCase.runCompiler(args, new int[] {9, 11, 15, 17});
		
		args = new ArrayList();
		args.add("-classpath");
		args.add("../runtime/bin;out/lib.jar");
		args.add("-Xlint:error");
		
		args.add("-d");
		args.add("out");
		
		args.add("testdata/src1/binary/client/MyAspect.aj");
		args.add("testdata/src1/binary/client/Client1.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
}
