/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Alexandre Vasseur
 *******************************************************************************/
package org.aspectj.systemtest.ajc150.ataspectj;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * A suite for @AspectJ aspects located in java5/ataspectj
 * 
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjSyntaxTests extends XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(AtAjSyntaxTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("syntax.xml");
	}

	public void testSimpleBefore() {
		runTest("SimpleBefore");
	}

	public void testSimpleAfter() {
		runTest("SimpleAfter");
	}

	public void testSingletonAspectBindings() {
		// Note AV: uncomment setReporting to get it in modules/tests folder
		// org.aspectj.asm.AsmManager.setReporting("debug.txt",true,true,true,true);
		runTest("singletonAspectBindings");
		// same stuff with AJ
		// org.aspectj.asm.AsmManager.setReporting("debug-aj.txt",true,true,true,true);
		// runTest("singletonAspectBindings2");

	}

	public void testCflowTest() {
		runTest("CflowTest");
	}

	public void testPointcutReferenceTest() {
		runTest("PointcutReferenceTest");
	}

	public void testXXJoinPointTest() {
		runTest("XXJoinPointTest");
	}

	public void testPrecedenceTest() {
		runTest("PrecedenceTest");
	}

	public void testAfterXTest() {
		runTest("AfterXTest");
	}

	public void testBindingTest() {
		runTest("BindingTest");
	}

	public void testBindingTestNoInline() {
		runTest("BindingTest no inline");
	}

	public void testPerClause() {
		runTest("PerClause");
	}

	public void testAroundInlineMunger_XnoInline() {
		runTest("AroundInlineMunger -XnoInline");
	}

	public void testAroundInlineMunger() {
		try {
			runTest("AroundInlineMunger");
		} finally {
			System.out.println(ajc.getLastCompilationResult().getStandardError());
		}
	}

	public void testAroundInlineMunger2() {
		runTest("AroundInlineMunger2");
	}

	public void testDeow() {
		runTest("Deow");
	}

	public void testSingletonInheritance() {
		runTest("singletonInheritance");
	}

	public void testPerClauseInheritance() {
		runTest("perClauseInheritance");
	}

	public void testIfPointcut() {
		runTest("IfPointcutTest");
	}

	public void testIfPointcut2() {
		runTest("IfPointcut2Test");
	}

	public void testMultipleBinding() {
		runTest("MultipleBinding");
	}

	public void testBug104212() {
		runTest("Bug104212");
	}

	public void testDeclareParentsInterface() {
		runTest("DeclareParentsInterface");
	}

	public void testDeclareParentsImplements() {
		runTest("DeclareParentsImplements");
	}

	public void testAbstractAspectNPE() {
		runTest("AbstractAspectNPE");
	}

	public void testAbstractInherited() {
		runTest("AbstractInherited");
	}

}
