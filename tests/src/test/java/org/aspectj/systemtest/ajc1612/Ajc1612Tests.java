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
package org.aspectj.systemtest.ajc1612;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc1612Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// public void testAnnoCopying_345515() {
	// runTest("anno copying");
	// }

	// public void testDoubleITDF() throws Exception {
	// runTest("double itdf");
	// }

	// public void testItdSplitCompilation_354683() throws Exception {
	// runTest("itd split compilation");
	// }

	public void testClassCast_327141() {
		runTest("classcast");
	}

	public void testXmlDefs() {
		runTest("xml defined advice");
	}

	public void testXmlDefs2() {
		runTest("xml defined advice 2");
	}

	// using a parameter
	public void testXmlDefs3() {
		runTest("xml defined advice 3");
	}

	// using bad parameters
	public void testXmlDefs4() {
		runTest("xml defined advice 4");
	}

	// binding
	public void testXmlDefs5() {
		runTest("xml defined advice 5");
	}

	// before and after advice
	public void testXmlDefs6() {
		runTest("xml defined advice 6");
	}

	// thisJoinPoint
	public void testXmlDefs7() {
		runTest("xml defined advice 7");
	}

	// thisJoinPointStaticPart
	public void testXmlDefs8() {
		runTest("xml defined advice 8");
	}

	// void around advice
	public void testXmlDefs9() {
		runTest("xml defined advice 9");
	}

	// non-void around advice
	public void testXmlDefs10() {
		runTest("xml defined advice 10");
	}

	// non-void around advice with proceed
	public void testXmlDefs11() {
		runTest("xml defined advice 11");
	}

	// non primitive return (that is actually used) and proceed
	public void testXmlDefs12() {
		runTest("xml defined advice 12");
	}

	// delegate type in package
	public void testXmlDefs13() {
		runTest("xml defined advice 13");
	}

	public void testCorrespondingType_357582() {
		for (int i = 0; i < 100000; i++) {
			assertEquals("AAA", convert("AAA"));
			assertEquals("AAA", convert("a.b.c.AAA"));
			assertEquals("A", convert("aa.ba.ca.A"));
			assertEquals("AAA<>", convert("a.b.c.AAA<>"));
			assertEquals("AAA<A>", convert("a.b.c.AAA<A>"));
			assertEquals("AAA<A>", convert("a.b.c.AAA<aa.A>"));
			assertEquals("AAA<A,B>", convert("a.b.c.AAA<aa.A,bb.B>"));
			assertEquals("AAA<A<B>>", convert("a.b.c.AAA<aa.A<bb.B>>"));
			assertEquals("AAA<A<B>,AA<GG<KK>>>", convert("a.b.c.AAA<aa.A<bb.B>,a.b.c.AA<GG<KK>>>"));
		}
		long time = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			assertEquals("AAA", convert("AAA"));
			assertEquals("AAA", convert("a.b.c.AAA"));
			assertEquals("A", convert("aa.ba.ca.A"));
			assertEquals("AAA<>", convert("a.b.c.AAA<>"));
			assertEquals("AAA<A>", convert("a.b.c.AAA<A>"));
			assertEquals("AAA<A>", convert("a.b.c.AAA<aa.A>"));
			assertEquals("AAA<A,B>", convert("a.b.c.AAA<aa.A,bb.B>"));
			assertEquals("AAA<A<B>>", convert("a.b.c.AAA<aa.A<bb.B>>"));
			assertEquals("AAA<A<B>,AA<GG<KK>>>", convert("a.b.c.AAA<aa.A<bb.B>,a.b.c.AA<GG<KK>>>"));
		}
		System.out.println(System.currentTimeMillis() - time);
	}

	private String convert(String totrim) {
		return ProgramElement.trim(totrim);
	}

	public void testPervasivePerthis_354470() throws Exception {
		runTest("perthis too pervasive");
	}

	public void testPervasivePerthis_354470_2() throws Exception {
		runTest("perthis too pervasive 2");
	}

	public void testPervasivePerthis_354470_3() throws Exception {
		runTest("perthis too pervasive 3");
	}

	public void testNotEqualWithAnnotationValues_357013() throws Exception {
		runTest("annotation values not equal");
	}

	public void testNotEqualWithAnnotationValues_357013_2() throws Exception {
		runTest("annotation values not equal 2");
	}

	public void testClassRef_357012() throws Exception {
		runTest("class reference in annotation value");
	}

	public void testClassRefInvalidName_357012_2() throws Exception {
		runTest("class reference in annotation value - invalid typename");
	}

	public void testClassRef_357012_3() throws Exception {
		runTest("class reference in annotation value 3");
	}

	public void testAnnotationFieldBindingOptimization_356612() throws Exception {
		runTest("annotation field binding optimization");
	}

	public void testAnnotationFieldBindingOptimization_356612_2() throws Exception {
		runTest("annotation field binding optimization - 2");
	}

	public void testThisAspectInstance_239649_1() throws Exception {
		// simple case
		runTest("thisAspectInstance - 1");
	}

	public void testThisAspectInstance_239649_2() throws Exception {
		// before advice toggling on/off through if called method
		runTest("thisAspectInstance - 2");
	}

	public void testThisAspectInstance_239649_3() throws Exception {
		// after advice toggling on/off through if called method
		runTest("thisAspectInstance - 3");
	}

	public void testThisAspectInstance_239649_4() throws Exception {
		// before advice, also using thisJoinPointStaticPart
		runTest("thisAspectInstance - 4");
	}

	public void testThisAspectInstance_239649_5() throws Exception {
		// before advice, also using thisJoinPoint
		runTest("thisAspectInstance - 5");
	}

	public void testThisAspectInstance_239649_6() throws Exception {
		// before advice, also using thisEnclosingJoinPointStaticPart
		runTest("thisAspectInstance - 6");
	}

	public void testThisAspectInstance_239649_7() throws Exception {
		// before advice, also using thisJoinPoint and thisJoinPointStaticPart
		runTest("thisAspectInstance - 7");
	}

	public void testThisAspectInstance_239649_8() throws Exception {
		// before advice, also using abstract aspects
		runTest("thisAspectInstance - 8");
	}

	public void testThisAspectInstance_239649_9() throws Exception {
		// before advice, also using abstract aspects 2
		runTest("thisAspectInstance - 9");
	}

	public void testThisAspectInstance_239649_10() throws Exception {
		// aspects in a package
		runTest("thisAspectInstance - 10");
	}

	public void testThisAspectInstance_239649_11() throws Exception {
		// non-singleton aspect - should be an error for now
		runTest("thisAspectInstance - 11");
	}

	public void testThisAspectInstance_239649_12() throws Exception {
		// arg binding and tjpsp
		runTest("thisAspectInstance - 12");
	}

	public void testThisAspectInstance_239649_13() throws Exception {
		// pass instance
		runTest("thisAspectInstance - 13");
	}

	public void testThisAspectInstance_239649_14() throws Exception {
		// multiple ifs
		runTest("thisAspectInstance - 14");
	}

	public void testThisAspectInstance_239649_15() throws Exception {
		// abstract aspects
		runTest("thisAspectInstance - 15");
	}

	// public void testVerifyPerthis() throws Exception {
	// runTest("verify perthis");
	// }

	public void testRangeForLocalVariables_353936() throws ClassNotFoundException {
		runTest("local variable tables");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "X");
		Method[] meths = jc.getMethods();
		boolean checked = false;
		for (Method method : meths) {
			if (method.getName().equals("ajc$before$X$2$3444dde4")) {
				System.out.println(method.getName());
				System.out.println(stringify(method.getLocalVariableTable()));
				System.out.println(method.getCode().getLength());
				checked = true;
				assertEquals("LX; this(0) start=0 len=48", stringify(method.getLocalVariableTable(), 0));
				assertEquals("Lorg/aspectj/lang/JoinPoint; thisJoinPoint(1) start=0 len=48",
						stringify(method.getLocalVariableTable(), 1));
				assertEquals("I i(2) start=8 len=22", stringify(method.getLocalVariableTable(), 2));
			}
		}
		assertTrue(checked);
	}

	public void testEmptyPattern_pr352363() {
		runTest("empty pattern");
	}

	public void testGenericsIssue_pr351592() {
		runTest("generics issue");
	}

	public void testGenericsIssue_pr351592_2() {
		runTest("generics issue - 2");
	}

	public void testGenericsNpe_pr350800() {
		runTest("generics npe");
	}

	public void testGenericsNpe_pr350800_code() {
		runTest("generics npe - code");
	}

	public void testGenericsNpe_pr350800_3() {
		runTest("generics npe - 3");
	}

	public void testOrdering_pr349961() {
		runTest("ordering");
	}

	public void testOrdering_pr349961_2() {
		runTest("ordering - 2");
	}

	/*
	 * public void testVerifyError_pr347395() { runTest("verifyerror - inline"); }
	 */

	public void testDuplicateMethods_349398() {
		runTest("duplicate methods");
	}

	public void testBindingInts_347684() {
		runTest("binding ints");
	}

	public void testBindingInts_347684_2() {
		runTest("binding ints - 2");
	}

	public void testBindingInts_347684_3() {
		runTest("binding ints - 3");
	}

	public void testBindingInts_347684_4() {
		runTest("binding ints - 4");
	}

	public void testBindingInts_347684_5() {
		runTest("binding ints - 5");
	}

	public void testBindingInts_347684_6() {
		runTest("binding ints - 6");
	}

	public void testIncorrectAnnos_345172() {
		runTest("incorrect annos");
	}

	public void testIncorrectAnnos_345172_2() {
		runTest("incorrect annos 2");
	}

	public void testIncorrectAnnos_345172_3() {
		runTest("incorrect annos 3");
	}

	public void testSyntheticMethods_327867() {
		runTest("synthetic methods");
	}

	// public void testSignedJarLtw_328099() {
	// runTest("signed jar ltw");
	// }

	public void testVerifyError_315398() {
		runTest("verifyerror");
	}

	public void testVerifyError_315398_2() {
		runTest("verifyerror - 2");
	}

	public void testRawTypePointcut_327134() {
		runTest("rawtype pointcut");
	}

	public void testRawTypeWarning_335810() {
		runTest("rawtype warning");
	}

	// public void testDecpGenerics_344005() {
	// runTest("decp generics");
	// }

	public void testIllegalAccessError_343051() {
		runTest("illegalaccesserror");
	}

	public void testItitNpe_339974() {
		runTest("itit npe");
	}

	// public void testNoImportError_342605() {
	// runTest("noimporterror");
	// }

	public void testClashingLocalTypes_342323() {
		runTest("clashing local types");
	}

	public void testITIT_338175() {
		runTest("itit");
	}

	public void testThrowsClause_292239() {
		runTest("throws clause");
	}

	public void testThrowsClause_292239_2() {
		runTest("throws clause - 2");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1612Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc1612.xml");
	}

}