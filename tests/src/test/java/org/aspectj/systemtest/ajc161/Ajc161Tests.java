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
package org.aspectj.systemtest.ajc161;

import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc161Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// AspectJ1.6.1
	public void testSignatureProcessing_pr237447() {
		runTest("signature processing");
	}

	public void testGenericAtAround_pr237419() {
		runTest("generics ataround");
	}

	public void testGenericMarkerMatch_pr203367() {
		runTest("generic marker match");
	}

	// public void testSuperItds_pr134425() { runTest("super itds"); }
	public void testSuperItds_pr198196_1() {
		runTest("super itds - 2");
	}

	public void testSuperItds_pr198196_2() {
		runTest("super itds - 3");
	}

	public void testSuperItds_pr198196_3() {
		runTest("super itds - 4");
	}

	public void testDeow_pr237381_1() {
		runTest("ataspectj deow - 1");
	}

	public void testDeow_pr237381_2() {
		runTest("ataspectj deow - 2");
	}

	public void testRunningBrokenCode_pr102733_2() {
		runTest("running broken code - 2");
	}

	public void testRunningBrokenCode_pr102733() {
		runTest("running broken code");
	}

	public void testErrorOnNonabstractGenericAtAspectJAspect_pr168982() {
		runTest("error on non-abstract generic ataspectj aspect");
	}

	public void testIgnoringTypeLevelSuppression_pr234933() {
		runTest("ignoring type level suppress");
	}

	public void testDuplicateMethodSignature_pr223226_2() {
		runTest("duplicate method signature - 2");
	}

	public void testDuplicateMethodSignature_pr223226() {
		runTest("duplicate method signature");
	}

	public void testProtectedMethodsAroundAdvice_pr197719_2() {
		runTest("protected methods and around advice - again - 2");
	}

	public void testProtectedMethodsAroundAdvice_pr197719() {
		runTest("protected methods and around advice - again");
	}

	public void testProtectedMethodsAroundAdvice_pr230075() {
		runTest("protected methods and around advice");
	}

	public void testFinalStringsAnnotationPointcut_pr174385() {
		runTest("static strings in annotation pointcuts");
	}

	public void testComplexBoundsGenericAspect_pr199130_1() {
		runTest("complex bounds on generic aspect - 1");
	}

	public void testComplexBoundsGenericAspect_pr199130_2() {
		runTest("complex bounds on generic aspect - 2");
	}

	public void testComplexBoundsGenericAspect_pr199130_3() {
		runTest("complex bounds on generic aspect - 3");
	}

	public void testPrivilegedGenericAspect_pr235505() {
		runTest("privileged generic aspect");
	}

	public void testPrivilegedGenericAspect_pr235505_2() {
		runTest("privileged generic aspect - 2");
	}

	public void testParsingAroundNoReturn_pr64222() {
		runTest("parsing around advice no return");
	}

	public void testParsingBeforeArrayRef_pr159268() {
		runTest("before array name");
	}

	public void testGenericAspectAroundAdvice_pr226201() {
		runTest("generic aspect around advice");
	}

	public void testCrazyGenericsInnerTypes_pr235829() {
		runTest("crazy generics and inner types");
	}

	public void testAnnotationExposureGenerics_pr235597() {
		runTest("annotation exposure and generics");
	}

	public void testIncorrectRelationship_pr235204() {
		runTest("incorrect call relationship");
		IRelationshipMap irm = AsmManager.lastActiveStructureModel.getRelationshipMap();
		Set entries = irm.getEntries();
		boolean gotSomethingValid = false;
		String expected = "<recursivepackage{RecursiveCatcher.java'RecursiveCatcher~recursiveCall~I?method-call(void recursivepackage.RecursiveCatcher.recursiveCall(int))";
		for (Object entry : entries) {
			String str = (String) entry;
			if (str.contains(expected)) {
				gotSomethingValid = true;
			}
		}
		if (!gotSomethingValid) {
			fail("Did not find a relationship with the expected data in '" + expected + "'");
		}
	}

	public void testITDPrecedence_pr233838_1() {
		runTest("itd precedence - 1");
	}

	public void testITDPrecedence_pr233838_2() {
		runTest("itd precedence - 2");
	}

	public void testGetFieldGenerics_pr227401() {
		runTest("getfield problem with generics");
	}

	public void testGenericAbstractAspects_pr231478() {
		runTest("generic abstract aspects");
	}

	public void testFieldJoinpointsAndAnnotationValues_pr227993() {
		runTest("field jp anno value");
	}

	public void testGenericsBoundsDecp_pr231187() {
		runTest("generics bounds decp");
	}

	public void testGenericsBoundsDecp_pr231187_2() {
		runTest("generics bounds decp - 2");
	}

	public void testLtwInheritedCflow_pr230134() {
		runTest("ltw inherited cflow");
	}

	public void testAroundAdviceOnFieldSet_pr229910() {
		runTest("around advice on field set");
	}

	public void testPipelineCompilationGenericReturnType_pr226567() {
		runTest("pipeline compilation and generic return type");
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc161Tests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc161.xml");
	}

}