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
package org.aspectj.systemtest.ajc169;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc169Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testMessyDecp_318241() {
		runTest("messy decp");
	}

	// public void testMultiAnnosRunning_pr315820_1() {
	// runTest("multiple annos running - 1");
	// }

	public void testMultiAnnosParsing_pr315820() {
		runTest("multiple annos parsing");
	}

	public void testDeclareAnnot_pr287613_5() {
		runTest("compound declare patterns - method - 2");
	}

	public void testDeclareAnnot_pr287613_4() {
		runTest("compound declare patterns - method - 1");
	}

	public void testDeclareAnnot_pr287613_3() {
		runTest("compound declare patterns - methodctor - 1");
	}

	public void testDeclareAnnot_pr287613_2() {
		runTest("compound declare patterns - fields - 2");
	}

	public void testDeclareAnnot_pr287613() {
		runTest("compound declare patterns - fields - 1");
	}

	public void testOptionalAspects_pr310506() {
		runTest("optional aspects");
	}

	public void testOptionalAspects_pr310506_2() {
		runTest("optional aspects 2");
	}

	// public void testClashingDeclareAnnos_313026_1() {
	// runTest("clashing declare annos");
	// }

	public void testInfiniteLoop_pr315651() {
		runTest("infinite loop");
	}

	public void testAnnoType_pr314965() {
		runTest("anno typo");
	}

	public void testValidateBranchRecursion_314840() {
		runTest("validate branch recursion");
	}

	public void testClassFileSize_312839_1() {
		runTest("class file size - 1");
		// 2531 (0x404): 1.6.9.M2 size of Class.class
		// 2494 (0x3DF): first little stab, compressing aspectnames attached to type mungers
		// 2370 (0x363): changed read/write sourcelocation to write path rather than File object:
		// 2358 (0x357): aspects affecting type compressed (weaverstate reweavable info)
		// 2102 (0x257): changed read/write sourcelocation in type munger to NOT use object streams
		// 2053 (0x1EF): changed path in sourcelocation read/write to be constant pool (so shared between both mungers)
		// 2019: changed resolvedMemberImpl name/signature to be compressed refs
		// 1954 (0x18C)

		// Aspect size (X.class) down from 6459 to 4722
		// 4551: changed exact type pattern writing to use constant pool, and
		// changed typepatternlist to not both writing/reading location

		// TODO actually test something :)
	}

	// control test - weaves everything
	public void testScopingLTW_122460_1() {
		runTest("scoping ltw - 1");
	}

	// scoped to one type
	public void testScopingLTW_122460_2() {
		runTest("scoping ltw - 2");
	}

	// scope is '!*' - nothing woven
	public void testScopingLTW_122460_3() {
		runTest("scoping ltw - 3");
	}

	public void testDeclareSoftCall_307009_1() {
		runTest("declare soft and call - 1");
	}

	public void testDeclareSoftCall_307009_2() {
		runTest("declare soft and call - 2");
	}

	public void testAmbiguousMethod_298665() {
		runTest("ambiguous method");
	}

	// public void testStaticallyAnalyzableIf_292262_1() {
	// runTest("if with statically recognizable code");
	// }

	// public void testAdvisingPrivilegedAccessMethod_307147() {
	// runTest("advising privileged access method");
	// }

	public void testRogueError_310043() {
		runTest("rogue error");
	}

	public void testItdMarkerAnnotations_309743() {
		runTest("itd marker annotations - 1");
	}

	public void testItdMarkerAnnotations_309743_2() {
		runTest("itd marker annotations - 2");
	}

	public void testPipeliningAndGenerics_309336() {
		runTest("pipelining and generics");
	}

	public void testCrashParamAnnos_309440() {
		runTest("crash param annos");
	}

	// 1.6.9 M1 below here
	public void testSubtleGenericsIssue_308773() {
		runTest("subtle generics problem");
	}

	public void testAdvisingPrivilegedAccessMember_307120() {
		runTest("advising privileged access member");
	}

	public void testAdvisingPrivilegedAccessMember_307120_2() {
		runTest("advising privileged access member - 2");
	}

	public void testTypePatternCategories_44365_Class() {
		runTest("type category type patterns - class");
	}

	public void testTypePatternCategories_44365_Interface() {
		runTest("type category type patterns - interface");
	}

	public void testTypePatternCategories_44365_Enum() {
		runTest("type category type patterns - enum");
	}

	public void testTypePatternCategories_44365_Annotation() {
		runTest("type category type patterns - annotation");
	}

	public void testTypePatternCategories_44365_Anonymous() {
		runTest("type category type patterns - anonymous");
	}

	public void testTypePatternCategories_44365_Inner() {
		runTest("type category type patterns - inner");
	}

	public void testTypePatternCategories_44365_Inner_2() {
		runTest("type category type patterns - inner - 2");
	}

	public void testTypePatternCategories_44365_Inner_3() {
		runTest("type category type patterns - inner - 3");
	}

	public void testTypePatternCategories_44365_Aspect() {
		runTest("type category type patterns - aspect");
	}

	public void testTypePatternCategories_44365_e1() {
		runTest("type category type patterns - e1");
	}

	public void testTypePatternCategories_44365_e3() {
		runTest("type category type patterns - e3");
	}

	public void testTypePatternCategories_44365_e2() {
		runTest("type category type patterns - e2");
	}

	public void testChecker() {
		runTest("inserts in messages");
	}

	/*
	 * public void testVerifyError() { runTest("verifyerror on atAj"); }
	 */
	public void testDeclareTypeWarning1() {
		runTest("declare type warning - 1");
	}

	public void testDeclareTypeWarning2() {
		runTest("declare type warning - 2");
	}

	public void testDeclareTypeWarning3() {
		runTest("declare type warning - 3");
	}

	public void testDeclareTypeError1() {
		runTest("declare type error - 1");
	}

	public void testPr298388() {
		runTest("declare mixin and generics");
	}

	// public void testPr292584() {
	// runTest("annotation around advice verifyerror");
	// }

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc169Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc169.xml");
	}

}