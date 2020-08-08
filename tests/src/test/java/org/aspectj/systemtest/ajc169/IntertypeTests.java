/*******************************************************************************
 * Copyright (c) 2010 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc169;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * Tests exploring intertype declared inner types and the new intertype syntax.
 * 
 * Some design notes.<br>
 * <p>
 * Supporting inner types is not quite as straightforward as for the other kinds of ITD like methods, fields and constructors. When
 * 'resolving' methods/fields/constructors they may refer to intertyped inner types, these means they must be in place early on -
 * ahead of any member resolution. In order to achieve this they are done really early (for both the cases of pulling in a
 * binarytypebinding - see AjLookupEnvironment.weaveInterTypeDeclarations(), and for sourcetypebindings
 * AjLookupEnvironment.completeTypeBindings() where it calls processInterTypeMemberTypes).
 * <p>
 * The logic in AjLookupEnvironment.weaveInterTypeDeclarations() is temporarily disabled because I can't write a testcase that
 * requires it! It should be an incremental build where a class is loaded as a binary type binding on a secondary (incremental)
 * compile and that class needs the inner class applying.
 * <p>
 * Eclipse polices the names of inner types when loading binary type data. The name of the inner is actually maintained as the
 * aspect name (so an inner type of Foo in an aspect Bar will be called Bar$Foo). The compiler looks after 'attaching' the inner
 * type to the target type binding when required.
 * 
 * @author Andy Clement
 */
public class IntertypeTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	/**
	 * This is testing that on a secondary compile we can work out what happened in the first compile (where an inner type was
	 * intertyped onto another type). I think we need two conditions to be true for this to pass: <br>
	 * 1) we need an innerclass attribute on the target type <br>
	 * 2) we need the name of the innerclass to match its target rather than the declaring aspect<br>
	 * Line 277 in ClassScope:<br>
	 * 
	 * memberTypeBindings[count++] = memberScope.buildType(sourceType, sourceType.fPackage, accessRestriction);<br>
	 * which then: builds the name based on the containing aspect.
	 * 
	 * TypeDeclaration.generateCode()
	 * 
	 */
	public void testSeparateCompilation() throws Exception {
		runTest("separate compilation");
	}

	/**
	 * Interestingly this test makes no reference in the type Basic to the new ITD'd inner type, this causes the Basic type to be
	 * missing the innertype attribute!
	 */
	public void testSeparateCompilation2() throws Exception {
		runTest("separate compilation - 2");
	}

	public void testErrorTargettingTypeThatAlreadyHasIt() {
		runTest("already has it");
	}

	public void testConstruction4() {
		runTest("construction4");
	}

	public void testConstruction3() {
		runTest("construction3");
	}

	public void testConstruction2() {
		runTest("construction2");
	}

	public void testConstruction() {
		runTest("construction");
	}

	// now the itd and the itd member class are in different aspects
	public void testVoteItdMixture2() {
		runTest("vote - itd mixture 2");
	}

	// This test is necessary because it verifies what happens when other ITDs are around
	// in addition to the itd member class. In order to apply the other ITDs the methods in a target
	// may get resolved. When they are resolved their types (return,etc) are resolved. If this
	// happens before the innertype has been added to the target and the types involved reference that
	// member type, then a failure will occur.
	public void testVoteItdMixture() {
		runTest("vote - itd mixture");
	}

	public void testVoteMethodReference() {
		runTest("vote - method reference");
	}

	public void testVoteFieldReference() {
		runTest("vote - field reference");
	}

	public void testVoteInnerInner() {
		runTest("vote - inner inner");
	}

	public void testVoteReferenceViaAnnotation2() {
		runTest("vote - reference via annotation - two");
	}

	public void testVoteReferenceViaAnnotation() {
		runTest("vote - reference via annotation");
	}

	public void testVoteConstruction() {
		runTest("vote - construction");
	}

	public void testVoteBasic() {
		runTest("vote - basic");
	}

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
		IRelationshipMap irm = AsmManager.lastActiveStructureModel.getRelationshipMap();
		pw.write(irm.toString());
		pw.flush();
		String model = baos.toString();
		assertTrue(model.contains("<{Choice.java[Choice=[aspect declarations]"));
		assertTrue(model.contains("<{Choice.java'X[Keys=[declared on]"));
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

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(IntertypeTests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("intertype.xml");
	}

}