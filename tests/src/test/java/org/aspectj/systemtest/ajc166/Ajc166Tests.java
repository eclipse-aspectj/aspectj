/*******************************************************************************
 * Copyright (c) 2008 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc166;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc166Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testParentsLTW_286473_binary() {
		runTest("parents ltw - binary");
	}

	public void testParentsLTW_286473_ltw() {
		runTest("parents ltw - ltw");
	}

	public void testNpeForJavacBuilt_290227() {
		runTest("npe for javac built");
	}

	public void testBinaryDecpSuperRewrite_290087() {
		runTest("binary decp super rewrite");
	}

	public void testITDannos_288049() {
		runTest("itd decanno");
	}

	public void testVerifyErrorAnnoStyle_288712() {
		runTest("verifyerror anno style");
	}

	public void testMungerCCE_288635() {
		runTest("munger cce");
	}

	public void testMungerCCE_288635_2() {
		runTest("munger cce - 2");
	}

	public void testNPEonBadAspectDecl_286375() {
		runTest("npe on bad aspect decl");
	}

	//
	// public void testAnnoModifierOrdering_287597_1() {
	// runTest("anno modifier ordering - 1");
	// }
	//
	// public void testDeclareAnnoCompoundExpression_287613() {
	// runTest("dec anno compound expressions");
	// }

	public void testHasMember_284862() {
		runTest("npe hasmember");
	}

	public void testHasMember_284862_2() {
		runTest("npe hasmember - 2");
	}

	public void testHasMember_284862_3() {
		runTest("npe hasmember - 3");
	}

	// fix is too disruptive for 1.6.5
	public void testGenericsBridge_pr279983() {
		runTest("generics bridge");
	}

	public void testGenericsDecp_pr282299() {
		runTest("generics and decp");
	}

	public void testGenericSigProblem_pr284297() {
		runTest("generic signature problem");
	}

	public void testInterfacesSerializable_pr283229() {
		runTest("interfaces and serializable");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc166Tests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc166.xml");
	}

}