/*******************************************************************************
 * Copyright (c) 2010 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc169;

import java.io.File;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * Tests exploring intertype declared inner types and the new intertype syntax.
 * 
 * @author Andy Clement
 */
public class IntertypeTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// inter type declared classes - working scenarios
	public void testFieldAccess() throws Exception {
		runTest("field access");
	}

	public void testMethodAccess() throws Exception {
		runTest("method access");
	}

	public void testRooScenario() throws Exception {
		runTest("choice");
	}

	// compiler limitation tests
	public void testNotAllowedOnInterface() throws Exception {
		runTest("on interface");
	}

	public void testNotAllowedOnEnum() throws Exception {
		runTest("on enum");
	}

	public void testNotAllowedOnAnnotation() throws Exception {
		runTest("on annotation");
	}

	public void testOnlyStatic() throws Exception {
		runTest("only static");
	}

	// tests for alternate syntax, not yet supported in the grammar

	// intertype {} syntax
	// public void testWillItParseEmptyIntertype() throws Exception {
	// runTest("simplest", true);
	// }
	//
	// public void testWithAnInnerClass() throws Exception {
	// runTest("simplest 2");
	// }
	//
	// public void testIntertypeMethodInNewStyle() throws Exception {
	// runTest("simplest 3");
	// }
	// --

	public SyntheticRepository createRepos(File cpentry) {
		ClassPath cp = new ClassPath(cpentry + File.pathSeparator + System.getProperty("java.class.path"));
		return SyntheticRepository.getInstance(cp);
	}

	protected JavaClass getClassFrom(File where, String clazzname) throws ClassNotFoundException {
		SyntheticRepository repos = createRepos(where);
		return repos.loadClass(clazzname);
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(IntertypeTests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc169/intertype.xml");
	}

}