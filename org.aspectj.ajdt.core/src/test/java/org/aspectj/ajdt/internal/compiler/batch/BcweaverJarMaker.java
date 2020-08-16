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

package org.aspectj.ajdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajdt.ajc.Constants;

public class BcweaverJarMaker {

	private static String cp = "../lib/test/aspectjrt.jar;../lib/test/testing-client.jar" + File.pathSeparator + System.getProperty("aspectjrt.path");
	
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

		makeURLWeavingClassLoaderJars();
		
		makeDuplicateManifestTestJars();

		makeOutjarTestJars();
		makeAspectPathTestJars();
		makeAjc11TestJars();
	}
	
	public static void makeJar0() throws IOException {
		List<String> args = new ArrayList<>();
		args.add("-outjar");
		args.add("../weaver/testdata/tracing.jar");

		args.add("-classpath");
		args.add(Constants.aspectjrtClasspath());
		
		args.add(Constants.TESTDATA_PATH + "/src1/Trace.java");
		args.add(Constants.TESTDATA_PATH + "/src1/MyTrace.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	

	public static void makeJar1() throws IOException {
		List<String> args = new ArrayList<>();
		args.add("-outjar");
		args.add("../weaver/testdata/megatrace.jar");

		args.add("-classpath");
        args.add(Constants.aspectjrtClasspath());
		
		args.add(Constants.TESTDATA_PATH + "/src1/trace/MegaTrace.java");
		args.add(Constants.TESTDATA_PATH + "/src1/trace/ExecTrace.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	
	public static void makeJarObviousNothing() throws IOException {
		List<String> args = new ArrayList<>();
		args.add("-outjar");
		args.add("../weaver/testdata/megatrace0easy.jar");

		args.add("-classpath");
        args.add(Constants.aspectjrtClasspath());
		
		args.add(Constants.TESTDATA_PATH + "/src1/trace/MegaTrace.java");
		args.add(Constants.TESTDATA_PATH + "/src1/trace/ObviousTraceNothing.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	public static void makeJarHardNothing() throws IOException {
		List<String> args = new ArrayList<>();
		args.add("-outjar");
		args.add("../weaver/testdata/megatrace0hard.jar");

		args.add("-classpath");
        args.add(Constants.aspectjrtClasspath());
		
		args.add(Constants.TESTDATA_PATH + "/src1/trace/MegaTrace.java");
		args.add(Constants.TESTDATA_PATH + "/src1/trace/HardTraceNothing.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	
	public static void makeJar1a() throws IOException {
		List<String> args = new ArrayList<>();
		args.add("-outjar");
		args.add("../weaver/testdata/megatraceNoweave.jar");

		args.add("-XterminateAfterCompilation");

		args.add("-classpath");
        args.add(Constants.aspectjrtClasspath());
		
		args.add(Constants.TESTDATA_PATH + "/src1/trace/MegaTrace.java");
		args.add(Constants.TESTDATA_PATH + "/src1/trace/ExecTrace.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	
	public static void makeJar2() throws IOException {
		List<String> args = new ArrayList<>();
		args.add("-outjar");
		args.add("../weaver/testdata/dummyAspect.jar");

		args.add("-classpath");
        args.add(Constants.aspectjrtClasspath());
		
		args.add(Constants.TESTDATA_PATH + "/src1/DummyAspect.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}	
	
	public static void makeTestJars() throws IOException {
		List<String> args = new ArrayList<>();

		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar" +
			File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../tests/new/options11/aspectlib1.jar");		
		args.add("../tests/new/options11/library1/*.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		args = new ArrayList<>();

		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../tests/new/options11/aspectlib2.jar");		
		args.add("../tests/new/options11/library2/*.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		args = new ArrayList<>();

		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar"  +
			File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../tests/new/options11/injar.jar");		
		args.add("../tests/new/options11/injar/*.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		args = new ArrayList<>();

		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar"  +
			File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../tests/bugs/serialVersionUID/injar.jar");		
		args.add("../tests/bugs/serialVersionUID/Test.java");
		args.add("../tests/bugs/serialVersionUID/Util.java");
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		
		// For PR55341
		args = new ArrayList<>();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar"  +
			File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../tests/bugs/StringToString/helloworld.jar");		
		args.add("../tests/bugs/StringToString/HW.java");		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		buildShowWeaveInfoTestingJars();		
	}
	
	public static void makeURLWeavingClassLoaderJars() throws IOException {
		List<String> args = new ArrayList<>();

		/*
		 * Vanilla classes
		 */
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../weaver/testdata/ltw-classes.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWHelloWorld.java");
		args.add(Constants.TESTDATA_PATH + "/src1/ltw/LTWPackageTest.java");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/* 
		 * Woven classes
		 */
		args = new ArrayList<>();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar;../weaver/testdata/ltw-classes.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-XnotReweavable");
		args.add("-outjar");
		args.add("../weaver/testdata/ltw-woven.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWHelloWorld.java");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWAspect.aj");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/*
		 * Advice
		 */
		args = new ArrayList<>();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar;../weaver/testdata/ltw-classes.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../weaver/testdata/ltw-aspects.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWAspect.aj");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/*
		 * Declare warning advice
		 */
		args = new ArrayList<>();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar;../weaver/testdata/ltw-classes.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../weaver/testdata/ltw-dwaspects.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWDeclareWarning.aj");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/*
		 * Declare error advice
		 */
		args = new ArrayList();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar;../weaver/testdata/ltw-classes.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../weaver/testdata/ltw-deaspects.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWDeclareError.aj");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/*
		 * Around closure advice
		 */
		args = new ArrayList<>();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar;../weaver/testdata/ltw-classes.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../weaver/testdata/ltw-acaspects.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWAroundClosure.aj");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/*
		 * ITD
		 */
		args = new ArrayList<>();
		args.add("-Xlint:ignore"); 
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar;../weaver/testdata/ltw-classes.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../weaver/testdata/ltw-itdaspects.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWInterfaceITD.aj");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWFieldITD.aj");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWMethodITD.aj");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/*
		 * perXXX()
		 */
		args = new ArrayList();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar;../weaver/testdata/ltw-classes.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../weaver/testdata/ltw-peraspects.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/LTWPerthis.aj");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}	
	
	private static void buildJarWithClasspath(String outjar,String input,String deps,boolean nodebug) {
		System.out.println("  Building "+outjar);
		List<String> args = new ArrayList<>();
		if (nodebug) args.add("-g:none");		
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar"  +
				 File.pathSeparator + System.getProperty("aspectjrt.path") +
				 (deps!=null?File.pathSeparator + "../ajde/testdata/WeaveInfoMessagesTest/"+deps:""));
		args.add("-outjar");
		args.add("../ajde/testdata/WeaveInfoMessagesTest/"+outjar);		
		args.add("../ajde/testdata/WeaveInfoMessagesTest/"+input);
		
		System.err.println(args);
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);	
	}
	
	private static void buildShowWeaveInfoTestingJars() {
		System.out.println("For binary weave info message testing (ShowWeaveMessagesTestCase.java)");
		buildJarWithClasspath("Simple.jar","Simple.java",null,false);
		// Build with javac and jar
		// buildJarWithClasspath("Simple_nodebug.jar","Simple.java",null,true);
		buildJarWithClasspath("AspectAdvice.jar","AspectAdvice.aj",null,false);
		buildJarWithClasspath("AspectAdvice_nodebug.jar","AspectAdvice.aj","Simple.jar",true);
		buildJarWithClasspath("AspectDeclare.jar","AspectDeclare.aj","Simple.jar",false);
		buildJarWithClasspath("AspectDeclare_nodebug.jar","AspectDeclare.aj","Simple.jar",true);
		buildJarWithClasspath("AspectITD.jar","AspectITD.aj","Simple.jar",false);
		buildJarWithClasspath("AspectITD_nodebug.jar","AspectITD.aj","Simple.jar",true);
		buildJarWithClasspath("AspectDeclareSoft.jar","AspectDeclareSoft.aj","Simple.jar",false);
		buildJarWithClasspath("AspectDeclareSoft_nodebug.jar","AspectDeclareSoft.aj","Simple.jar",true);
	}	
	
	public static void makeDuplicateManifestTestJars() throws IOException {
		List<String> args = new ArrayList<>();

		/*
		 * injar
		 */
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../ajde/testdata/DuplicateManifestTest/injar.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/Hello.java");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/*
		 * aspectjar
		 */
		args = new ArrayList();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("../ajde/testdata/DuplicateManifestTest/aspectjar.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/Trace.java");
		args.add(Constants.TESTDATA_PATH + "/src1/TraceHello.java");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}	
	
	public static void makeAspectPathTestJars() throws IOException {
		List<String> args = new ArrayList<>();

		args.clear();
		args.add("-classpath"); 
		args.add(cp);
		args.add("-outjar");
		args.add("../tests/bugs/perCflowAndJar/lib.jar");
		args.add("../tests/bugs/perCflowAndJar/PerCFlowCompileFromJar.java");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	public static void makeAjc11TestJars() throws IOException {
		List<String> args = new ArrayList<>();
		args.clear();
		args.add("-classpath"); 
		args.add(cp);
		args.add("-outjar");
		args.add("../tests/bugs/cflowAndJar/lib.jar");
		args.add("../tests/bugs/cflowAndJar/AbstractAspect.aj");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}
	
	public static void makeOutjarTestJars() throws IOException {
		List<String> args = new ArrayList<>();

		// parent
		args.clear();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar" +
		   File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("./testdata/OutjarTest/parent.jar");
		args.add(Constants.TESTDATA_PATH + "/OutjarTest/src/jar1/Parent.java");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/*
		 * child
		 */
		args.clear();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar"
			+ File.pathSeparator + System.getProperty("aspectjrt.path")
			+ File.pathSeparator + "./testdata/OutjarTest/parent.jar");
		args.add("-outjar");
		args.add("./testdata/OutjarTest/child.jar");
		args.add(Constants.TESTDATA_PATH + "/OutjarTest/src/jar2/Child.java");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/*
		 * aspects
		 */
		args.clear();
		args.add("-classpath"); 
		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar"
			+ File.pathSeparator + System.getProperty("aspectjrt.path"));
		args.add("-outjar");
		args.add("./testdata/OutjarTest/aspects.jar");
		args.add(Constants.TESTDATA_PATH + "/OutjarTest/src/jar3/Aspect.aj");
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);

		/*
		 * aspectjar
		 */
//		args = new ArrayList();
//		args.add("-classpath"); 
//		args.add("../lib/test/aspectjrt.jar;../lib/test/testing-client.jar" +
//		   File.pathSeparator + System.getProperty("aspectjrt.path"));
//		args.add("-outjar");
//		args.add("../ajde/testdata/DuplicateManifestTest/aspectjar.jar");
//		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/Trace.java");
//		args.add(AjdtAjcTests.TESTDATA_PATH + "/src1/TraceHello.java");
//		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
	}	
}
