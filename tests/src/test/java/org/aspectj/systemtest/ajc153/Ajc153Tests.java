/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc153;

import java.net.URL;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.bcel.Utility;

import junit.framework.Test;

public class Ajc153Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testFormalCommentsAreSetForConstructorIPEs_pr164340() {
		runTest("formal comments are set for constructor ipes");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.CONSTRUCTOR, "C()");
		assertNotNull("expected formal comment to be non null but" + " found that it was null", ipe.getFormalComment());
	}

	// public void testGenericsProblem_pr151978() { runTest("generics problem");}
	// public void testArgnamesAndJavac_pr148381() { runTest("argNames and javac");}
	// public void testCFlowXMLAspectLTW_pr149096() { runTest("cflow xml concrete aspect"); }
	// public void testAmbiguousBinding_pr121805() { runTest("ambiguous binding");}
	// public void testNegatedAnnotationMatchingProblem_pr153464() { runTest("negated annotation matching problem");}
	public void testAnnotationStyleBcException_pr162135() {
		runTest("bcexception in annotation style around advice");
	}

	public void testAnnotationStyleBcException_pr162135_2() {
		runTest("bcexception in annotation style around advice - 2");
	}

	public void testAnnotationStyleBcException_pr162135_3() {
		runTest("bcexception in annotation style around advice - 3");
	}

	public void testAnnotationStyleBcException_pr162135_4() {
		runTest("bcexception in annotation style around advice - 4");
	}

	public void testAnnotationStyleBcException_pr162135_5() {
		runTest("bcexception in annotation style around advice - 5");
	}

	public void testAnnotationStyleBcException_pr162135_6() {
		runTest("bcexception in annotation style around advice - 6");
	}

	public void testAnnotationStyleBcException_pr162135_7() {
		runTest("bcexception in annotation style around advice - 7");
	}

	public void testIncompatibleClassChangeWithITD_pr164633() {
		runTest("incompatibleclasschange");
	}

	public void testComplexPointcut_pr162657() {
		runTest("complex pointcut");
	}

	public void testGenericsInPointcuts_pr161502() {
		runTest("generics in pointcuts");
	}

	public void testGenericsInPointcuts_pr161502_2() {
		runTest("generics in pointcuts - 2");
	}

	public void testNoNPEDueToMissingType_pr149908() {
		runTest("ensure no npe due to missing type");
	}

	public void testNoNPEDueToMember_pr149908() {
		runTest("ensure no npe due to missing member");
	}

	public void testPTWgetWithinTypeName_pr123423_1() {
		runTest("basic usage of getWithinTypeName");
	}

	public void testPTWgetWithinTypeName_pr123423_2() {
		runTest("basic usage of getWithinTypeName - multiple types");
	}

	public void testPTWgetWithinTypeName_pr123423_3() {
		runTest("basic usage of getWithinTypeName - non matching types");
	}

	public void testPTWgetWithinTypeName_pr123423_4() {
		runTest("basic usage of getWithinTypeName - types in packages");
	}

	public void testPTWgetWithinTypeName_pr123423_5() {
		runTest("basic usage of getWithinTypeName - annotation style");
	}

	public void testTurningOffBcelCaching_pr160674() {
		runTest("turning off bcel caching");
	}

	public void testNoIllegalStateExceptionWithGenericInnerAspect_pr156058() {
		runTest("no IllegalStateException with generic inner aspect");
	}

	public void testNoIllegalStateExceptionWithGenericInnerAspect_pr156058_2() {
		runTest("no IllegalStateException with generic inner aspect - 2");
	}

	public void testDeclareMethodAnnotations_pr159143() {
		runTest("declare method annotations");
	}

	public void testVisibilityProblem_pr149071() {
		runTest("visibility problem");
	}

	public void testMissingLineNumbersInStacktraceAfter_pr145442() {
		runTest("missing line numbers in stacktrace after");
	}

	public void testMissingLineNumbersInStacktraceAround_pr145442() {
		runTest("missing line numbers in stacktrace around");
	}

	public void testGenericArrays_pr158624() {
		runTest("generics and arrays");
	}

	public void testMissingLineNumbersInStacktraceBefore_pr145442() {
		runTest("missing line numbers in stacktrace before");
	}

	public void testMissingLineNumbersInStacktraceBefore_pr145442_Binary() {
		runTest("missing line numbers in stacktrace before - binary");
	}

	public void testAnnotationStylePointcutNPE_pr158412() {
		runTest("annotation style pointcut npe");
	}

	public void testAnnotationStylePointcutNPE_pr158412_2() {
		runTest("annotation style pointcut npe - 2");
	}

	public void testAnnotationsCallConstructors_pr158126() {
		runTest("annotations, call and constructors problem");
	}

	public void testIllegalStateExceptionGenerics_pr153845() {
		runTest("IllegalStateException at GenericSignatureParser.java");
	}

	public void testNoIllegalStateExceptionFromAsmDelegate_pr153490_1() {
		runTest("no illegal state exception from AsmDelegate - 1");
	}

	public void testNoIllegalStateExceptionFromAsmDelegate_pr153490_2() {
		runTest("no illegal state exception from AsmDelegate - 2");
	}

	public void testNoIllegalStateExceptionFromAsmDelegate_pr153490_3() {
		runTest("no illegal state exception from AsmDelegate - 3");
	}

	public void testAnnotMethod_pr156962() {
		runTest("Test Annot Method");
	}

	public void testAnnotMethodHasMember_pr156962() {
		runTest("Test Annot Method Has Member");
	}

	public void testMixingGenerics_pr152848() {
		runTest("mixing generics");
	}

	public void testIncorrectStaticinitializationWeaving_pr149560_1() {
		runTest("incorrect staticinitialization weaving - codestyle");
	}

	public void testIncorrectStaticinitializationWeaving_pr149560_2() {
		runTest("incorrect staticinitialization weaving - annstyle");
	}

	public void testIncorrectDeprecatedAnnotationProcessing_pr154332() {
		runTest("incorrect deprecated annotation processing");
	}

	public void testPipeliningProblemWithAnnotationsDecp_pr153380_1() {
		runTest("pipelining decps");
	}

	public void testUnwantedPointcutWarning_pr148219() {
		runTest("unwanted warning for pointcut");
	}

	public void testDecpAndCflowadderMungerClash_pr152631() {
		runTest("decp and cflowadder munger clash");
	}

	public void testGenericInheritanceDecp_pr150095() {
		runTest("generics, inheritance and decp");
	}

	public void testIllegalStateException_pr148737() {
		runTest("illegalstateexception for non generic type");
	}

	public void testAtajInheritance_pr149305_1() {
		runTest("ataj inheritance - 1");
	}

	public void testAtajInheritance_pr149305_2() {
		runTest("ataj inheritance - 2");
	}

	public void testAtajInheritance_pr149305_3() {
		runTest("ataj inheritance - 3");
	}

	// public void testVerificationFailureForAspectOf_pr148693() {
	// runTest("verification problem"); // build the code
	// Utils.verifyClass(ajc, "mypackage.MyAspect"); // verify it <<< BRAND NEW VERIFY UTILITY FOR EVERYONE TO TRY ;)
	// }

	public void testIncorrectAnnotationValue_pr148537() {
		runTest("incorrect annotation value");
	}

	public void testVerifyErrNoTypeCflowField_pr145693_1() {
		runTest("verifyErrNoTypeCflowField");
	}

	public void testVerifyErrInpathNoTypeCflowField_pr145693_2() {
		runTest("verifyErrInpathNoTypeCflowField");
	}

	public void testCpathNoTypeCflowField_pr145693_3() {
		runTest("cpathNoTypeCflowField");
	}

	// public void testAdviceNotWovenAspectPath_pr147841() { runTest("advice not woven on aspectpath");}
	public void testGenericSignatures_pr148409() {
		runTest("generic signature problem");
	}

	public void testCantFindType_pr149322_01() {
		runTest("can't find type on interface call 1");
	}

	public void testCantFindType_pr149322_02() {
		runTest("can't find type on interface call 2");
	}

	public void testCantFindType_pr149322_03() {
		runTest("can't find type on interface call 3");
	}

	public void testParsingBytecodeLess_pr152871() {
		Utility.testingParseCounter = 0;
		runTest("parsing bytecode less");
		assertTrue("Should have called parse 2 times, not " + Utility.testingParseCounter + " times",
				Utility.testingParseCounter == 2);
		// 5 means: // 3 of these are gone with change to UnwovenClassFileWTPMB to take a classname in ctor
		// (1)=registerAspect
		// (2,3)=checkingIfShouldWeave,AcceptingResult for class
		// (4,5)=checkingIfShouldWeave,AcceptingResult for aspect
	}

	public void testMatchVolatileField_pr150671() {
		runTest("match volatile field");
	}

	public void testDuplicateJVMTIAgents_pr151938() {
		runTest("Duplicate JVMTI agents");
	}

	public void testLTWWorldWithAnnotationMatching_pr153572() {
		runTest("LTWWorld with annotation matching");
	}

	public void testReweavableAspectNotRegistered_pr129525() {
		runTest("reweavableAspectNotRegistered error");
	}

	public void testNPEinConstructorSignatureImpl_pr155972() {
		runTest("NPE in ConstructorSignatureImpl");
	}

	public void testNPEinFieldSignatureImpl_pr155972() {
		runTest("NPE in FieldSignatureImpl");
	}

	public void testNPEinInitializerSignatureImpl_pr155972() {
		runTest("NPE in InitializerSignatureImpl");
	}

	public void testLineNumberTableCorrectWithGenericsForEachAndContinue_pr155763() {
		runTest("ensure LineNumberTable correct with generics, for each and continue");
	}

	public void testDeclareSoftDoesntAllowUndeclaredExInAnonInnerClass_pr151772() {
		runTest("ensure declare soft doesn't allow undeclared exception in anonymous inner class");
	}

	public void testDeclareSoftDoesntAllowUndeclaredExInAnonInnerClass_pr151772_2() {
		runTest("ensure declare soft doesn't allow undeclared exception in anonymous inner class - 2");
	}

	public void testDeclareSoftAndInnerClasses_pr125981() {
		runTest("declare soft and inner classes");
	}

	public void testGetSourceSignature_pr148908() {
		runTest("ensure getSourceSignature correct with static field");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();
		IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.FIELD, "MY_COMPARATOR");
		String expected = "static final Comparator MY_COMPARATOR = null;\n";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.FIELD, "aString");
		expected = "static final String aString = \"Constant String\";\n";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.FIELD, "bString");
		expected = "static final String bString = aString;\n";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.FIELD, "cString");
		expected = "static final String cString = (aString + bString);\n";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.FIELD, "dString");
		expected = "static final String dString = ((aString + \" and \") + bString);\n";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.FIELD, "eString");
		expected = "static final String eString = \"Hello World\";\n";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.FIELD, "aList");
		expected = "public List<String> aList = null;\n";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.FIELD, "bList");
		expected = "public List<String> bList = null;\n";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.FIELD, "cList");
		expected = "public List<String> cList = null;\n";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());
	}

	// public void testNPEWithCustomAgent_pr158205() {
	// runTest("NPE with custom agent");
	// }

	public void testWeaveConcreteSubaspectWithAdvice_pr132080() {
		runTest("Weave concrete sub-aspect with advice");
	}

	public void testWeaveConcreteSubaspectWithITD_pr132080() {
		runTest("Weave concrete sub-aspect with ITD");
	}

	public void testWeaveConcreteSubaspectWithAroundClosure_pr132080() {
		runTest("Weave concrete sub-aspect with around closure");
	}

	public void testWeaveConcreteSubaspectWithCflow_pr132080() {
		runTest("Weave concrete sub-aspect with cflow");
	}

	public void testNPEWithLTWPointcutLibraryAndMissingAspectDependency_pr158957() {
		runTest("NPE with LTW, pointcut library and missing aspect dependency");
	}

	public void testNoInvalidAbsoluteTypeNameWarning_pr156904_1() {
		runTest("ensure no invalidAbsoluteTypeName when do match - 1");
	}

	public void testNoInvalidAbsoluteTypeNameWarning_pr156904_2() {
		runTest("ensure no invalidAbsoluteTypeName when do match - 2");
	}

	public void testNoInvalidAbsoluteTypeNameWarning_pr156904_3() {
		runTest("ensure no invalidAbsoluteTypeName when do match - 3");
	}

	public void testNoInvalidAbsoluteTypeNameWarning_pr156904_4() {
		runTest("ensure no invalidAbsoluteTypeName when do match - 4");
	}

	public void testNoNPEWithThrownExceptionWarningAndAtAspectj_pr161217() {
		runTest("NPE with thrown exception warning and at aspectj");
	}

	public void testJavadocCommentsAreSetIfHaveNormalComments_pr164340() {
		runTest("javadoc comments are set if have normal comments");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();

		IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.METHOD, "foo()");
		assertNotNull("expected formal comment to be non null but" + " found that it was null", ipe.getFormalComment());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.METHOD, "bar()");
		assertNotNull("expected formal comment to be non null but" + " found that it was null", ipe.getFormalComment());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.METHOD, "goo()");
		assertNull("expected formal comment to be null but" + " found that it was " + ipe.getFormalComment(), ipe
				.getFormalComment());

	}

	public void testBinaryWeavingIntoJava6Library_pr164384() {
		runTest("binary weaving into java 6 library");
	}

	public void testCompilanceJava6ThrowsUsageError_pr164384() {
		runTest("compliance java 6 throws usage error");
	}

	public void testSourceLevelJava6ThrowsUsageError_pr164384() {
		runTest("source level java 6 throws usage error");
	}

	public void testTargetLevelJava6ThrowsUsageError_pr164384() {
		runTest("target level java 6 throws usage error");
	}

	public void testStaticImport() {
		runTest("ensure static import reference have static modifier set");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();

		IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.IMPORT_REFERENCE,
				"ABC.StaticImport.Alphabet.A");
		String expected = "import static ABC.StaticImport.Alphabet.A;";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());
	}

	public void testAspected_Annotation() {
		runTest("ensure Annotations are added to import list");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();

		IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.IMPORT_REFERENCE, "annotation.A");
		String expected = "import annotation.A;";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

	}

	public void testGetSourceSignature_GenericMethods() {
		runTest("ensure getSourceSignature correct with generic method");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();

		IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.METHOD, "returnT(T)");
		String expected = "public <T> T returnT(T a)";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.METHOD, "returnQ(Q)");
		expected = "public <Q extends List> Q returnQ(Q a)";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.METHOD, "doubleGeneric(Q,T)");
		expected = "public <T, Q> void doubleGeneric(Q a, T b)";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());

		// Generic Method Constructor
		ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.CONSTRUCTOR, "C(T)");
		expected = "public <T> C(T b)";
		assertEquals("expected source signature to be " + expected + " but found " + ipe.getSourceSignature(), expected, ipe
				.getSourceSignature());
	}

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc153Tests.class);
	}

	protected URL getSpecFile() {
		return getClassResource("ajc153.xml");
	}

}