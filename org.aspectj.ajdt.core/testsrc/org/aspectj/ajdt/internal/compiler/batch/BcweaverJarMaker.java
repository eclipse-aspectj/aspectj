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

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;

import org.aspectj.ajdt.ajc.AjdtAjcTests;

public class BcweaverJarMaker {

	public BcweaverJarMaker() {
		super();
	}

	public static void main(String[] args) throws IOException {
		makeJar0();
		makeJar1();
		makeJar1a();
		makeJar2();
		
		makeJarObviousNothing();
		makeJarHardNothing();
		
		
		makeTestJars();
	}
	
	public static void makeJar0() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("../weaver/testdata/tracing.jar");

		args.add("-classpath");
		args.add("../runtime/bin" + File.pathSeparator + 
			System.getProperty("aspectjrt.path"));
		
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/Trace.java");
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/MyTrace.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	public static void makeJar1() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("../weaver/testdata/megatrace.jar");

		args.add("-classpath");
		args.add("../runtime/bin" + File.pathSeparator + 
			System.getProperty("aspectjrt.path"));
		
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/trace/MegaTrace.java");
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/trace/ExecTrace.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	
	public static void makeJarObviousNothing() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("../weaver/testdata/megatrace0easy.jar");

		args.add("-classpath");
		args.add("../runtime/bin" + File.pathSeparator + 
			System.getProperty("aspectjrt.path"));
		
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/trace/MegaTrace.java");
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/trace/ObviousTraceNothing.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	public static void makeJarHardNothing() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("../weaver/testdata/megatrace0hard.jar");

		args.add("-classpath");
		args.add("../runtime/bin" + File.pathSeparator + 
			System.getProperty("aspectjrt.path"));
		
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/trace/MegaTrace.java");
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/trace/HardTraceNothing.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	
	public static void makeJar1a() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("../weaver/testdata/megatraceNoweave.jar");

		args.add("-noweave");

		args.add("-classpath");
		args.add("../runtime/bin" + File.pathSeparator + 
			System.getProperty("aspectjrt.path"));
		
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/trace/MegaTrace.java");
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/trace/ExecTrace.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	
	public static void makeJar2() throws IOException {
		List args = new ArrayList();
		args.add("-outjar");
		args.add("../weaver/testdata/dummyAspect.jar");

		args.add("-classpath");
		args.add("../runtime/bin" + File.pathSeparator + 
			System.getProperty("aspectjrt.path"));
		
		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/DummyAspect.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}	
	
	public static void makeTestJars() throws IOException {
		List args = new ArrayList();

		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar" +
			File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../tests/new/options11/aspectlib1.jar");		
		args.add("../tests/new/options11/library1/*.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		args = new ArrayList();

		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../tests/new/options11/aspectlib2.jar");		
		args.add("../tests/new/options11/library2/*.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		args = new ArrayList();

		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar"  +
			File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../tests/new/options11/injar.jar");		
		args.add("../tests/new/options11/injar/*.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		args = new ArrayList();

		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar"  +
			File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../tests/bugs/serialVersionUID/injar.jar");		
		args.add("../tests/bugs/serialVersionUID/Test.java");
		args.add("../tests/bugs/serialVersionUID/Util.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}	
	
	
}
