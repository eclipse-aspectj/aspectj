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
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;

import org.aspectj.ajdt.ajc.*;
import org.aspectj.bridge.ICommand;
//import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;

/**
 * @author hugunin
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BasicCommandTestCase extends CommandTestCase {

	/**
	 * Constructor for CommandTestCase.
	 * @param name
	 */
	public BasicCommandTestCase(String name) {
		super(name);
	}
	
	public void testA() {
		checkCompile("src1/A.java", NO_ERRORS);
	}
	
	public void testA1() {
		checkCompile("src1/A1.java", NO_ERRORS);
	}
	
	public void testBadA() {
		checkCompile("src1/BadA.java", new int[] {7, 8});
	}
	
	public void testHello() {
		checkCompile("src1/Hello.java", NO_ERRORS);
	}

	public void testBadHello() {
		checkCompile("src1/BadHello.java", new int[] {5});
	}

	public void testMissingHello() {
		checkCompile("src1/MissingHello.java", TOP_ERROR);
	}
	
	public void testBadBinding() {
		checkCompile("src1/BadBinding.java", new int[] {2, 4, 8, 10, 13, 16, 19});
	}
	public void testThisAndModifiers() {
		checkCompile("src1/ThisAndModifiers.java", NO_ERRORS);
	}
	
	public void testDeclares() {
		checkCompile("src1/Declares.java", new int[] {2});
	}	
	
	public void testDeclareWarning() {
		checkCompile("src1/DeclareWarning.java", NO_ERRORS);
	}	
	
	
	public void testP1() {
		checkCompile("src1/p1/Foo.java", NO_ERRORS);
	}
	
	public void testUnimplementedSyntax() {
		checkCompile("src1/UnimplementedSyntax.java", 
			new int[] {5, 15, 16, 22, 25});
	}
	public void testXlintWarn() {
		checkCompile("src1/Xlint.java", NO_ERRORS);
	}
	public void testXlintError() {
		List<String> args = new ArrayList<>();

		args.add("-d");
		args.add(getSandboxName());
		
		args.add("-classpath");
		StringBuilder classpath = new StringBuilder();
		classpath.append(getRuntimeClasspath());
		classpath.append(File.pathSeparator).append("../lib/junit/junit.jar;../testing-client/bin");
		args.add(classpath.toString());
		args.add("-Xlint:error");
		args.add(Constants.TESTDATA_PATH + "/src1/Xlint.java");
		
		runCompiler(args, new int[] {2});
	}
	public void testMissingJarError() {
		List<String> args = new ArrayList<>();

		args.add("-d");
		args.add(getSandboxName());
		
		args.add("-classpath");
		args.add(getRuntimeClasspath() + File.pathSeparator +
			"../lib/junit/junit.jar;../testing-client/bin;not_found_anywhere.jar");
		args.add(Constants.TESTDATA_PATH + "/src1/ThisAndModifiers.java");
		
		ICommand command = new AjdtCommand();
		MessageHandler myHandler = new MessageHandler();
		//myHandler.setInterceptor(org.aspectj.tools.ajc.Main.MessagePrinter.TERSE);
		/*boolean result = */command.runCommand((String[])args.toArray(new String[0]), myHandler);

		//System.err.println("messages: " + Arrays.asList(myHandler.getMessages(IMessage.INFO, true)));
		// DON'T yet have a way of testing that we actually got a particular info message
		assertEquals("only info for missing jar", 0, myHandler.getErrors().length);

	}
	public void testMissingRuntimeError() {
		List<String> args = new ArrayList<>();

		args.add("-d");
		args.add(getSandboxName());
		
		args.add("-classpath");
		args.add("../lib/junit/junit.jar;../testing-client/bin");
		args.add(Constants.TESTDATA_PATH + "/src1/ThisAndModifiers.java");
		
		ICommand command = new AjdtCommand();
		MessageHandler myHandler = new MessageHandler();
		myHandler.setInterceptor(org.aspectj.tools.ajc.Main.MessagePrinter.TERSE);
		/*boolean result = */command.runCommand((String[])args.toArray(new String[0]), myHandler);

		assertEquals("error for org.aspectj.lang.JoinPoint not found", 1, myHandler.getErrors().length);
	}
	
	public void testImplicitOutputDir() {
		List args = new ArrayList();
		
		args.add("-classpath");
		args.add(getRuntimeClasspath() + File.pathSeparator +
			"../lib/junit/junit.jar;../testing-client/bin");
		
		File f1 = new File(Constants.TESTDATA_PATH + "/src1/p1/Foo.class");
		File f2 = new File(Constants.TESTDATA_PATH + "/src1/WrongPackage.class");
		File f3 = new File(Constants.TESTDATA_PATH + "/src1/WrongPackage$1.class");
		
		if (f1.exists()) f1.delete();
		if (f2.exists()) f2.delete();
		if (f3.exists()) f3.delete();
		
		args.add(Constants.TESTDATA_PATH + "/src1/p1/Foo.java");
		args.add(Constants.TESTDATA_PATH + "/src1/WrongPackage.java");
		
		runCompiler(args, NO_ERRORS);
		
		assertTrue(f1.getPath(), f1.exists());
		assertTrue(f2.getPath(), f2.exists());
		assertTrue(f3.getPath(), f3.exists());
		
		if (f1.exists()) f1.delete();
		if (f2.exists()) f2.delete();
		if (f3.exists()) f3.delete();
		

	}
	
	public void testSizeChanges() {
		File f1 = new File(getSandboxName(),"SizeIssues.class");
		
		List<String> args = new ArrayList<>();

		args.add("-d");
		args.add(getSandboxName());
		
		args.add("-classpath");
		args.add(getRuntimeClasspath() + File.pathSeparator +
			"../lib/junit/junit.jar;../testing-client/bin");
		
		args.add(Constants.TESTDATA_PATH + "/src1/SizeIssuesAspect.java");		
		args.add(Constants.TESTDATA_PATH + "/src1/SizeIssues.java");

		runCompiler(args, NO_ERRORS);
		long size = f1.length();
		for (int i=0; i < 1; i++) {
			f1.delete();
			runCompiler(args, NO_ERRORS);
			assertEquals(size, f1.length());
		}
	}
}
