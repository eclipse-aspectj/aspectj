/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * ******************************************************************/
package org.aspectj.systemtest.java14;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Java14Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Java14Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("java14.xml");
	}


	public void test001(){
		runTest("assert flow");
	}

	public void test002(){
		runTest("assert flow - 2");
	}

	public void test003(){
		runTest("assert typing");
	}

	// bug in eclipse compiler, moved to ajcTestsFailing.xml
	//  public void test004(){
	//    runTest("assert coverage tests [requires 1.4]");
	//  }

	// bug in eclipse compiler, moved to ajcTestsFailing.xml
	//  public void test005(){
	//    runTest("assert coverage tests in one package [requires 1.4]");
	//  }

	public void test006(){
		runTest("compiling asserts in methods");
	}

	public void test007(){
		runTest("asserts");
	}

	public void test008(){
		runTest("asserts in aspect and declared methods [requires 1.4]");
	}

	public void test009(){
		runTest("Does the matrix coverage thing for the new method signatures");
	}

	public void test010(){
		runTest("correct types of parameters at call-sites");
	}

	public void test011(){
		runTest("target type matching with messy interface hierarchies");
	}

	public void test012(){
		if (!true) { System.err.println("Skipping test 012 not >=1.4");return;}
		runTest("assert tests in introduction [requires 1.4]");
	}

	public void test013(){
		runTest("various forms of package name pattern matching work");
	}

	public void test014(){
		runTest("assert statement in advice coverage [requires 1.4]");
	}

	public void test015(){
		runTest("assert statement in advice  [requires 1.4]");
	}

	public void test016(){
		runTest("assert and pertarget crashes compiler");
	}

	public void test017(){
		runTest("testing that assert works like .class");
	}

	public void test018(){
		runTest("JoinPoint Optimization when targetting 1.4");
	}

	public void test019(){
		runTest("XLint warning for call PCD's using subtype of defining type (-1.4 -Xlint:ignore)");
	}

	// public void test020(){
	// runTest("XLint warning for call PCD's using subtype of defining type (-1.4)");
	// }

	public void test021(){
		runTest("Class Literals as non final fields (also assert, and this$0)");
	}


}

