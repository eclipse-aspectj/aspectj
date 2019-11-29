/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc151;

import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/*
 * The design:
 * 
 * There are 3 instructions that create arrays:
 * 
 * - NEWARRAY for primitive arrays
 * - ANEWARRAY for object arrays
 * - MULTIANEWARRAY for multidimensional arrays
 * 
 * The changes to expose the new joinpoint are in:
 *   BcelClassWeaver.match(LazyMethodGen mg,InstructionHandle ih,BcelShadow enclosingShadow,List shadowAccumulator)
 *   
 * Determining the type of the array is easy.  Determining the size of the array is not easy statically, it is on the stack.
 * 
 * 
 * What still needs testing:
 * - structure model
 * 
 */

public class NewarrayJoinpointTests extends XMLBasedAjcTestCase {

	// when its the creation of a new 'object' (not a primitive) single dimension array
	public void testTheBasics_1() {
		runTest("basics");
	}

	public void testTheBasics_2() {
		runTest("basics - 2");
	}

	public void testWhatShouldntMatch() {
		runTest("shouldnt match");
	}

	public void testThisJoinPoint() {
		runTest("thisjoinpoint");
	}

	public void testThisJoinPoint19() {
		try {
			System.setProperty("ASPECTJ_OPTS", "-Xajruntimetarget:1.9");
			runTest("thisjoinpoint");
		} finally {
			System.setProperty("ASPECTJ_OPTS", "");
		}
	}

	public void testDifferentAdviceKinds() {
		runTest("different advice kinds");
	}

	public void testArgs() {
		runTest("args");
	}

	// when it is the creation of a new array of primitives
	public void testBasicWithAPrimitiveArray() {
		runTest("basic primitive array creation");
	}

	// when it is the creation of a new multi-dimensional array
	public void testBasicWithAMultiDimensionalArray() {
		runTest("multi dimensional array creation");
	}

	public void testArgsWithAMultiDimensionalArray() {
		runTest("multi dimensional array args");
	}

	// various
	public void testOptionoff() {
		runTest("option deactivated - no match expected");
	}

	public void testUsingTargetAndAfterReturningAdvice() {
		runTest("using target and after returning");
	}

	public void testUsingItForReal() {
		runTest("using it for real");
	}

	public void testDifferentiatingArrayTypes() {
		runTest("differentiating array types");
	}

	public void testStructureModel() {
		// AsmManager.setReporting("c:/foo.txt",true,true,true,true);
		runTest("structure model");
		IProgramElement ipe = AsmManager.lastActiveStructureModel.getHierarchy().findElementForType("", "Five");
		assertTrue("Couldnt find 'Five' type in the model", ipe != null);
		List<IProgramElement> kids = ipe.getChildren();
		assertTrue("Couldn't find 'main' method in the 'Five' type", kids != null && kids.size() == 1);
		List<IProgramElement> codenodes = kids.get(0).getChildren();
		assertTrue("Couldn't find nodes below 'main' method", codenodes != null && codenodes.size() == 1);
		IProgramElement arrayCtorCallNode = codenodes.get(0);
		String exp = "constructor-call(void java.lang.Integer[].<init>(int))";
		assertTrue("Expected '" + exp + "' but found " + arrayCtorCallNode.toString(), arrayCtorCallNode.toString().equals(exp));
		List<IRelationship> rels = AsmManager.lastActiveStructureModel.getRelationshipMap().get(arrayCtorCallNode);
		assertTrue("Should have a relationship from the ctorcall node, but didn't find one?", rels != null && rels.size() == 1);
	}

	//
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(NewarrayJoinpointTests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("newarray_joinpoint.xml");
	}

}
