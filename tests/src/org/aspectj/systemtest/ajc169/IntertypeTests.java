/*******************************************************************************
 * Copyright (c) 2010 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc169;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.asm.AsmManager;
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

	public void testRooScenario2() throws Exception {
		runTest("choice2");
	}

	public void testRooScenarioWeaveInfo() throws Exception {
		runTest("choice - weaveinfo");
	}

	public void testModel() throws Exception {
		runTest("choice - model");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		AsmManager.dumptree(pw, AsmManager.lastActiveStructureModel.getHierarchy().getRoot(), 0);
		pw.write(AsmManager.lastActiveStructureModel.getRelationshipMap().toString());
		pw.flush();
		String model = baos.toString();
		assertTrue(model.indexOf("<{Choice.java[Choice=[aspect declarations], <{Choice.java}X[Keys=[declared on]") != -1);
	}

	public void testGenerics1() throws Exception {
		runTest("choice - generics 1");
	}

	public void testGenerics2() throws Exception {
		runTest("choice - generics 2");
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