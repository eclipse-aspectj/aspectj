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
import java.util.*;
import java.util.ArrayList;

public class BcweaverJarMaker {

	public BcweaverJarMaker() {
		super();
	}

	public static void main(String[] args) throws IOException {
		makeJar0();
		makeJar1();
		makeJar1a();
		makeJar2();
		
		makeTestJars();
	}
	
	public static void makeJar0() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("../weaver/testdata/tracing.jar");

		args.add("-classpath");
		args.add("../runtime/bin");
		
		args.add("testdata/src1/Trace.java");
		args.add("testdata/src1/MyTrace.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	public static void makeJar1() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("../weaver/testdata/megatrace.jar");

		args.add("-classpath");
		args.add("../runtime/bin");
		
		args.add("testdata/src1/trace/MegaTrace.java");
		args.add("testdata/src1/trace/ExecTrace.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	
	public static void makeJar1a() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("../weaver/testdata/megatraceNoweave.jar");

		args.add("-noweave");

		args.add("-classpath");
		args.add("../runtime/bin");
		
		args.add("testdata/src1/trace/MegaTrace.java");
		args.add("testdata/src1/trace/ExecTrace.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	
	public static void makeJar2() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("../weaver/testdata/dummyAspect.jar");

		args.add("-classpath");
		args.add("../runtime/bin");
		
		args.add("testdata/src1/DummyAspect.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}	
	
	public static void makeTestJars() throws IOException {
		List args = new ArrayList();

		args.add("-classpath"); args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar");
		args.add("-outjar");
		args.add("../tests/new/options11/aspectlib1.jar");		
		args.add("../tests/new/options11/library1/*.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		args = new ArrayList();

		args.add("-classpath"); args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar");
		args.add("-outjar");
		args.add("../tests/new/options11/aspectlib2.jar");		
		args.add("../tests/new/options11/library2/*.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		args = new ArrayList();

		args.add("-classpath"); args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar");
		args.add("-outjar");
		args.add("../tests/new/options11/injar.jar");		
		args.add("../tests/new/options11/injar/*.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}	
	
	
}
