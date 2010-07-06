/********************************************************************
 * Copyright (c) 2010 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement (SpringSource)         initial implementation
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

/**
 * Incremental compilation tests. MultiProjectIncrementalTests was getting unwieldy - started this new test class for 1.6.10.
 * 
 * @author Andy Clement
 * @since 1.6.10
 */
public class IncrementalCompilationTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

	/**
	 * Build a pair of files, then change the throws clause in the first one (add a throws clause where there wasnt one). The second
	 * file should now have a 'unhandled exception' error on it.
	 */
	public void testModifiedThrowsClauseShouldTriggerError_318884() throws Exception {
		String p = "pr318884_1";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("B.java:4:0::0 Unhandled exception type IOException", getErrorMessages(p).get(0));
	}

	/**
	 * Build a pair of files, then change the throws clause in the first one (change the type of the thrown exception). The second
	 * file should now have a 'unhandled exception' error on it.
	 */
	public void testModifiedThrowsClauseShouldTriggerError_318884_2() throws Exception {
		String p = "pr318884_2";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("B.java:4:0::0 Unhandled exception type Exception", getErrorMessages(p).get(0));
	}

	// changing method return type parameterization
	public void testModifiedMethodReturnTypeGenericTypeParameterShouldTriggerError_318884_3() throws Exception {
		String p = "pr318884_3";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("The return type is incompatible with B.foo()", getErrorMessages(p).get(0));
	}

	// changing method parameter type parameterization
	public void testModifiedMethodParameterGenericTypeParameterShouldTriggerError_318884_4() throws Exception {
		String p = "pr318884_4";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains(
				"Name clash: The method foo(List<String>) of type A has the same erasure as foo(List<Integer>) of type B but does not override it",
				getErrorMessages(p).get(0));
	}

	// changing constructor parameter type parameterization
	public void testModifiedConstructorParameterGenericTypeParameterShouldTriggerError_318884_5() throws Exception {
		String p = "pr318884_5";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("The constructor B(List<String>) is undefined", getErrorMessages(p).get(0));
	}

	// changing field type parameterization
	public void testModifiedFieldTypeGenericTypeParameterShouldTriggerError_318884_6() throws Exception {
		String p = "pr318884_6";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("Type mismatch: cannot convert from element type Integer to String", getErrorMessages(p).get(0));
	}

	// removing static inner class
	public void testInnerClassChanges_318884_7() throws Exception {
		String p = "pr318884_7";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 3);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("B.C cannot be resolved to a type", getErrorMessages(p).get(0));
	}

	// removing constructor from a static inner class
	public void testInnerClassChanges_318884_9() throws Exception {
		String p = "pr318884_9";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 3);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("The constructor B.C(String) is undefined", getErrorMessages(p).get(0));
	}

	// removing class
	public void testInnerClassChanges_318884_10() throws Exception {
		AjdeInteractionTestbed.VERBOSE = true;
		String p = "pr318884_10";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(2, getErrorMessages(p).size());
		assertContains("B cannot be resolved to a type", getErrorMessages(p).get(0));
	}
}
