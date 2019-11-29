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
package org.aspectj.systemtest.ajc162;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc162Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// AspectJ1.6.2

	// When faulting in the binary hierarchy:
	// <root> [java source file]
	// Clazz.java [java source file] C:\temp\ajcSandbox\aspectj16_2\ajcTest3344.tmp\Clazz.java:1::0
	// import declarations [import reference]
	// Clazz [class] C:\temp\ajcSandbox\aspectj16_2\ajcTest3344.tmp\Clazz.java:1::13
	// foo() [method] C:\temp\ajcSandbox\aspectj16_2\ajcTest3344.tmp\Clazz.java:2::36
	// Asp.class (binary) [class] C:\temp\ajcSandbox\aspectj16_2\ajcTest3344.tmp\binaryaspect.jar!Asp.class:1::0
	// import declarations [import reference]
	// Asp [aspect] C:\temp\ajcSandbox\aspectj16_2\ajcTest3344.tmp\binaryaspect.jar!Asp.class:1::0
	// before(): <anonymous pointcut> [advice] C:\temp\ajcSandbox\aspectj16_2\ajcTest3344.tmp\binaryaspect.jar!Asp.class:2::0
	// Hid:1:(targets=1) [Asp.class (binary)}Asp&before (advises) {Clazz.java[Clazz~foo
	// Hid:2:(targets=1) {Clazz.java[Clazz~foo (advised by) [Asp.class (binary)}Asp&before

	// without faulting in the model they stop at the top level (the class level)

	// Hid:1:(targets=1) "{Clazz.java[Clazz~foo" (advised by) "{Asp.class"
	// Hid:2:(targets=1) "{Asp.class" (advises) "{Clazz.java[Clazz~foo"

	// what I want for the hid is:

	// <somethingIndicatingBinary>"[Asp.class}Asp&before"
	/*
	 * public void testBinaryAspectModeling() throws Exception { runTest("binary aspects model");
	 * AsmManager.dumptree(AsmManager.getDefault().getHierarchy().getRoot(), 0); PrintWriter pw = new PrintWriter(System.out);
	 * AsmManager.getDefault().dumprels(pw); pw.flush(); }
	 */
	public void testPerClause() {
		runTest("ltw perclause");
	}

	public void testNullDelegateForArray_pr247683() {
		runTest("null delegate for array");
	}

	public void testPerClause2() {
		runTest("ltw perclause - 2");
	}

	public void testPerClause3() {
		runTest("ltw perclause - 3");
	}

	public void testPerClause4_IncorrectPerClause() {
		runTest("ltw perclause - 4");
	}

	public void testAnnoValueBinding_pr246264() {
		runTest("anno value binding");
	}

	public void testAroundAdviceProceed_pr211607() {
		runTest("around advice proceed");
	}

	// public void testAdvisingInterfaces_pr163005() { runTest("advising joinpoints in interfaces"); }
	public void testChainedItds_pr176905() {
		runTest("chained itds");
	}

	public void testDecAtAnnoDecP_pr198341() {
		runTest("dec atanno and decp");
	}

	public void testMissingMarkers_pr197720() {
		runTest("missing markers on inherited annotated method");
	}

	public void testLostGenericsSigOnItd_pr211146() {
		runTest("lost generic sig on itd");
	}

	public void testLostGenericsSigOnItd_pr211146_2() {
		runTest("lost generic sig on itd - 2");
	}

	public void testLostGenericsSigOnItd_pr211146_3() {
		runTest("lost generic sig on itd - 3");
	}

	public void testLostGenericsSigOnItd_pr211146_4() {
		runTest("lost generic sig on itd - 4");
	}

	public void testLostGenericsSigOnItd_pr211146_5() {
		runTest("lost generic sig on itd - 5");
	}

	public void testMissingContext_pr194429() {
		runTest("missing context");
	}

	public void testWarningsForLimitations_pr210114() {
		runTest("warnings for limitations");
	}

	public void testPTW_pr244830() {
		runTest("ptw initFailureCause");
	}

	public void testGenericItdsOverrides_pr222648() {
		runTest("generic itds - overrides");
	}

	public void testGenericItdsOverrides_pr222648_2() {
		runTest("generic itds - overrides - 2");
	}

	public void testItdCallingGenericMethod_pr145391() {
		runTest("itd calling generic method");
	}

	public void testItdCallingGenericMethod_pr145391_2() {
		runTest("itd calling generic method - 2");
	}

	public void testPublicPointcut_pr239539() {
		runTest("public pointcut");
	}

	public void testGenericDecp_pr241047() {
		runTest("generic decp");
	}

	public void testGenericDecp_pr241047_2() {
		runTest("generic decp - 2");
	}

	public void testGenericItds_pr242797_1() {
		runTest("generic itds - 1");
	}

	public void testGenericItds_pr242797_2() {
		runTest("generic itds - 2");
	}

	public void testGenericItds_pr242797_3() {
		runTest("generic itds - 3");
	}

	public void testPrivilegedGenerics_pr240693() {
		runTest("privileged generics");
	}

	// public void testParamAnnosPipelining_pr241847() { runTest("param annos pipelining");}
	// public void testParamAnnoInner_pr241861() {
	// runTest("param annotation inner class");
	// }

	// public void testParamAnnoInner_pr241861_2() {
	// runTest("param annotation inner class - 2");
	// }

	// public void testParamAnnoInner_pr241861_3() {
	// runTest("param annotation inner class - 3");
	// }

	public void testAnnotationDecp_pr239441() {
		runTest("annotation decp");
	}

	public void testAtAspectJPerTarget_pr198181() {
		runTest("ataspectj ltw pertarget");
	}

	public void testAnnotationValueDecp_pr238992() {
		runTest("annotation value decp");
	}

	public void testAnnotationValueDecp_pr238992_2() {
		runTest("annotation value decp - 2");
	}

	public void testAnnotationValueDecp_pr238992_3() {
		runTest("annotation value decp - 3");
	}

	public void testAnnotationValueDecp_pr238992_4() {
		runTest("annotation value decp - 4");
	}

	public void testAnnotationValueDecp_pr238992_5() {
		runTest("annotation value decp - 5");
	}

	/*
	 * test plan execution((..,String,..)) args(..,String,..)
	 * 
	 * @args(..,Foo,..)
	 */
	// public void testParameterSubsettingMatching_pr233718_Matching() { runTest("parameter subsetting - matching");}
	// public void testParameterSubsettingMatching_pr233718_ArgsMatching() { runTest("parameter subsetting - args matching");}
	// public void testParameterSubsettingMatching_pr233718_ArgsBinding() { runTest("parameter subsetting - args binding");}
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc162Tests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc162.xml");
	}

}