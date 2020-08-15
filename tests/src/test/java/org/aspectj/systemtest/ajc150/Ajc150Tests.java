/*******************************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.asm.AsmManager;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc150Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc150Tests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc150.xml");
	}

	public void testMixingCodeStyles_pr121385() {
		runTest("mixing aspect styles");
	}

	public void testTypeVars_pr121575() {
		runTest("different numbers of type vars");
	}

	public void testTypeVars_pr121575_2() {
		runTest("different numbers of type vars - 2");
	}

	public void testTypeVars_pr121575_3() {
		runTest("different numbers of type vars - 3");
	}

	public void testTypeVars_pr121575_4() {
		runTest("different numbers of type vars - 4");
	}

	public void testDecps1() {
		runTest("decps - 1");
	}

	public void testDecps1b() {
		runTest("decps - 1b");
	}

	public void testDecps2() {
		runTest("decps - 2");
	}

	public void testDecps2b() {
		runTest("decps - 2b");
	}

	public void testDecps3() {
		runTest("decps - 3");
	}

	public void testDecps3b() {
		runTest("decps - 3b");
	}

	public void testDecps3c() {
		runTest("decps - 3c");
	}

	public void testVarargsNPE_pr120826() {
		runTest("varargs NPE");
	}

	public void testNamedPointcutPertarget_pr120521() {
		runTest("named pointcut not resolved in pertarget pointcut");
	}

	public void testDollarClasses_pr120474() {
		runTest("Dollar classes");
	}

	public void testGenericPTW_pr119539_1() {
		runTest("generic pertypewithin aspect - 1");
	}

	public void testGenericPTW_pr119539_2() {
		runTest("generic pertypewithin aspect - 2");
	}

	public void testGenericPTW_pr119539_3() {
		runTest("generic pertypewithin aspect - 3");
	}

	/*
	 * public void testBrokenDispatchByITD_pr72834() { runTest("broken dispatch");} public void testMissingAccessor_pr73856() {
	 * runTest("missing accessor");} public void testCunningDeclareParents_pr92311() { runTest("cunning declare parents");} public
	 * void testGenericITDsAndAbstractMethodError_pr102357() { runTest("generic itds and abstract method error");}
	 */
	public void testIncorrectSignatureMatchingWithExceptions_pr119749() {
		runTest("incorrect exception signature matching");
	}

	public void testGeneratingCodeForAnOldRuntime_pr116679_1() {
		runTest("generating code for a 1.2.1 runtime - 1");
	}

	public void testGeneratingCodeForAnOldRuntime_pr116679_2() {
		runTest("generating code for a 1.2.1 runtime - 2");
	}

	public void testAmbiguousMethod_pr118599_1() {
		runTest("ambiguous method when binary weaving - 1");
	}

	public void testAmbiguousMethod_pr118599_2() {
		runTest("ambiguous method when binary weaving - 2");
	}

	public void testAroundAdviceArrayAdviceSigs_pr118781() {
		runTest("verify error with around advice array sigs");
	}

	public void testAtDeclareParents_pr117681() {
		runTest("at declare parents");
	}

	public void testPrivilegeProblem_pr87525() {
		runTest("privilege problem with switch");
	}

	public void testRangeProblem_pr109614() {
		runTest("Range problem");
	}

	public void testGenericAspects_pr115237() {
		runTest("aspectOf and generic aspects");
	}

	public void testClassFormatError_pr114436() {
		runTest("ClassFormatError binary weaving perthis");
	}

	public void testParserException_pr115788() {
		runTest("parser exception");
	}

	public void testPossibleStaticImports_pr113066_1() {
		runTest("possible static imports bug - 1");
	}

	public void testPossibleStaticImports_pr113066_2() {
		runTest("possible static imports bug - 2");
	}

	public void testPossibleStaticImports_pr113066_3() {
		runTest("possible static imports bug - 3");
	}

	public void testITDCtor_pr112783() {
		runTest("Problem with constructor ITDs");
	}

	public void testAnnotatedITDFs_pr114005_1() {
		runTest("Annotated ITDFs - 1");
	}

	public void testAnnotatedITDFs_pr114005_2() {
		runTest("Annotated ITDFs - 2");
	}

	public void testCantCallSuperMethods_pr90143() {
		runTest("cant call super methods");
	}

	public void testBrokenDecp_pr112476() {
		runTest("binary weaving decp broken");
	}

	public void testUnboundFormal_pr112027() {
		runTest("unexpected error unboundFormalInPC");
	}

	public void testNPEScopeSetup_pr115038() {
		runTest("NPE in ensureScopeSetup");
	}

	public void testCCEGenerics_pr113445() {
		runTest("Generics ClassCastException");
	}

	public void testMatthewsAspect_pr113947_1() {
		runTest("maws generic aspect - 1");
	}

	public void testMatthewsAspect_pr113947_2() {
		runTest("maws generic aspect - 2");
	}

	public void testFieldGet_pr114343() {
		runTest("field-get, generics and around advice");
	}

	public void testFieldGet_pr114343_2() {
		runTest("field-get, generics and around advice - 2");
	}

	public void testFieldGet_pr114343_3() {
		runTest("field-get, generics and around advice - 3");
	}

	public void testCaptureBinding_pr114744() {
		runTest("capturebinding wildcard problem");
	}

	public void testAutoboxingAroundAdvice_pr119210_1() {
		runTest("autoboxing around advice - 1");
	}

	public void testAutoboxingAroundAdvice_pr119210_2() {
		runTest("autoboxing around advice - 2");
	}

	public void testAutoboxingAroundAdvice_pr119210_3() {
		runTest("autoboxing around advice - 3");
	}

	public void testBadDecp_pr110788_1() {
		runTest("bad generic decp - 1");
	}

	public void testBadDecp_pr110788_2() {
		runTest("bad generic decp - 2");
	}

	public void testBadDecp_pr110788_3() {
		runTest("bad generic decp - 3");
	}

	public void testBadDecp_pr110788_4() {
		runTest("bad generic decp - 4");
	}

	// public void testSimplifiedGenericAspectITDTest() { runTest("spurious override method warning - 3");}
	// public void testSpuriousOverrideMethodWarning_pr119570_1() { runTest("spurious override method warning");}
	// public void testSpuriousOverrideMethodWarning_pr119570_2() { runTest("spurious override method warning - 2");}

	public void testBrokenSwitch_pr117854() {
		runTest("broken switch transform");
	}

	public void testVarargsITD_pr110906() {
		runTest("ITD varargs problem");
	}

	public void testBadRenderer_pr86903() {
		runTest("bcelrenderer bad");
	}

	// public void testIllegalInitialization_pr118326_1() { runTest("illegal initialization - 1");}
	// public void testIllegalInitialization_pr118326_2() { runTest("illegal initialization - 2");}
	public void testLintForAdviceSorting_pr111667() {
		runTest("lint for advice sorting");
	}

	public void testIncompatibleClassChangeError_pr113630_1() {
		runTest("IncompatibleClassChangeError - errorscenario");
	}

	public void testIncompatibleClassChangeError_pr113630_2() {
		runTest("IncompatibleClassChangeError - workingscenario");
	}

	public void testFieldGetProblemWithGenericField_pr113861() {
		runTest("field-get problems with generic field");
	}

	public void testAccesstoPrivateITDInNested_pr118698() {
		runTest("access to private ITD from nested type");
	}

	public void testDeclareAnnotationOnNonExistentType_pr99191_1() {
		runTest("declare annotation on non existent type - 1");
	}

	public void testDeclareAnnotationOnNonExistentType_pr99191_2() {
		runTest("declare annotation on non existent type - 2");
	}

	public void testDeclareAnnotationOnNonExistentType_pr99191_3() {
		runTest("declare annotation on non existent type - 3");
	}

	public void testDeclareAnnotationOnNonExistentType_pr99191_4() {
		runTest("declare annotation on non existent type - 4");
	}

	public void testDeclareAnnotationOnNonExistentType_pr99191_5() {
		runTest("declare annotation on non existent type - 5");
	}

	public void testBadGenericSigAttribute_pr110927() {
		runTest("cant create signature attribute");
		Signature sig = GenericsTests.getClassSignature(ajc, "I");
		if (sig == null)
			fail("Couldn't find signature attribute for type I");
		String sigString = sig.getSignature();
		if (!(sigString.equals("Ljava/lang/Object;LIE2;LIE1<Ljava/lang/String;>;") || sigString
				.equals("Ljava/lang/Object;LIE1<Ljava/lang/String;>;LIE2;"))) {
			fail("Signature was " + sigString
					+ " when should have been something like Ljava/lang/Object;LIE1<Ljava/lang/String;>;LIE2;");
		}
	}

	public void test_typeProcessingOrderWhenDeclareParents() {
		runTest("Order of types passed to compiler determines weaving behavior");
	}

	public void test_aroundMethod() {
		runTest("method called around in class");
	}

	public void test_aroundMethodAspect() {
		runTest("method called around in aspect");
	}

	public void test_ambiguousBindingsDetection() {
		runTest("Various kinds of ambiguous bindings");
	}

	public void test_ambiguousArgsDetection() {
		runTest("ambiguous args");
	}

	public void testIncorrectExceptionTableWhenBreakInMethod_pr78021() {
		runTest("Injecting exception into while loop with break statement causes catch block to be ignored");
	}

	public void testIncorrectExceptionTableWhenReturnInMethod_pr79554() {
		runTest("Return in try-block disables catch-block if final-block is present");
	}

	public void testMissingDebugInfoForGeneratedMethods_pr82570() throws ClassNotFoundException {
		runTest("Weaved code does not include debug lines");
		boolean f = false;
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "PR82570_1");
		Method[] meths = jc.getMethods();
		for (Method method : meths) {
			if (f)
				System.err.println("Line number table for " + method.getName() + method.getSignature() + " = "
						+ method.getLineNumberTable());
			assertTrue("Didn't find a line number table for method " + method.getName() + method.getSignature(),
					method.getLineNumberTable() != null);
		}

		// This test would determine the info isn't there if you pass -g:none ...
		// cR = ajc(baseDir,new String[]{"PR82570_1.java","-g:none"});
		// assertTrue("Expected no compile problem:"+cR,!cR.hasErrorMessages());
		// System.err.println(cR.getStandardError());
		// jc = getClassFrom(ajc.getSandboxDirectory(),"PR82570_1");
		// meths = jc.getMethods();
		// for (int i = 0; i < meths.length; i++) {
		// Method method = meths[i];
		// assertTrue("Found a line number table for method "+method.getName(),
		// method.getLineNumberTable()==null);
		// }
	}

	public void testCanOverrideProtectedMethodsViaITDandDecp_pr83303() {
		runTest("compiler error when mixing inheritance, overriding and polymorphism");
	}

	public void testPerTypeWithin_pr106554() {
		runTest("Problem in staticinitialization with pertypewithin aspect");
	}

	public void testPerTypeWithinMissesNamedInnerTypes() {
		runTest("pertypewithin() handing of inner classes (1)");
	}

	public void testPerTypeWithinMissesAnonymousInnerTypes() {
		runTest("pertypewithin() handing of inner classes (2)");
	}

	public void testPerTypeWithinIncorrectlyMatchingInterfaces() {
		runTest("pertypewithin({interface}) illegal field modifier");
	}

	public void test051_arrayCloningInJava5() {
		runTest("AJC possible bug with static nested classes");
	}

	public void testBadASMforEnums() throws IOException {
		runTest("bad asm for enums");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		AsmManager.dumptree(pw, AsmManager.lastActiveStructureModel.getHierarchy().getRoot(), 0);
		pw.flush();
		String tree = baos.toString();
		assertTrue("Expected 'Red [enumvalue]' somewhere in here:" + tree, tree.contains("Red  [enumvalue]"));
	}

	public void npeOnTypeNotFound() {
		runTest("structure model npe on type not found");
	}

	public void testNoRuntimeExceptionSoftening() {
		runTest("declare soft of runtime exception");
	}

	public void testRuntimeNoSoftenWithHandler() {
		runTest("declare soft w. catch block");
	}

	public void testSyntaxError() {
		runTest("invalid cons syntax");
	}

	public void testVarargsInConsBug() {
		runTest("varargs in constructor sig");
	}

	public void testAspectpathdirs() {
		runTest("dirs on aspectpath");
	}

	public void testIntroSample() {
		runTest("introduction sample");
	}

	public void testPTWInterface() {
		runTest("pertypewithin({interface}) illegal field modifier");
	}

	public void testEnumCalledEnumEtc() {
		runTest("enum called Enum, annotation called Annotation, etc");
	}

	public void testInternalCompilerError_pr86832() {
		runTest("Internal compiler error");
	}

	public void testCloneMethod_pr83311() {
		runTest("overriding/polymorphism error on interface method introduction");
	}

	// IfPointcut.findResidueInternal() was modified to make this test complete in a short amount
	// of time - if you see it hanging, someone has messed with the optimization.
	public void testIfEvaluationExplosion_pr94086() {
		runTest("Exploding compile time with if() statements in pointcut");
	}

	public void testReflectNPE_pr94167() {
		runTest("NPE in reflect implementation");
	}

	public void testStaticImports_pr84260() {
		runTest("static import failures");
	}

	public void testGenerics_pr99089() {
		runTest("ArrayIndexOutOfBoundsException - Generics in privileged aspects");
	}

	public void testGenerics_pr95993() {
		runTest("NPE at ClassScope.java:660 when compiling generic class");
	}

	public void testItdGenerics_pr99228() {
		runTest("ITD of a field into a generic class");
	}

	public void testItdGenerics_pr98320() {
		runTest("intertype with nested generic type");
	}

	public void testItdGenerics_pr100227() {
		runTest("inner class with generic enclosing class");
	}

	public void testItdGenerics_pr100260() {
		runTest("methods inherited from a generic parent");
	}

	public void testSyntaxErrorNPE_pr103266() {
		runTest("NPE on syntax error");
	}

	public void testFinalAbstractClass_pr109486() {
		runTest("Internal compiler error (ClassParser.java:242)");
	}

	public void testComplexBinding_pr102210() {
		runTest("NullPointerException trying to compile");
	}

	public void testIllegalStateExceptionOnNestedParameterizedType_pr106634() {
		runTest("IllegalStateException unpacking signature of nested parameterized type");
	}

	public void testParseErrorOnAnnotationStarPlusPattern() {
		runTest("(@Foo *)+ type pattern parse error");
	}

	public void test_pr106130_tooManyLocals() {
		runTest("test weaving with > 256 locals");
	}

	public void testMissingNamePattern_pr106461() {
		runTest("missing name pattern");
	}

	public void testMissingNamePattern_pr107059() {
		runTest("parser crashes on call(void (@a *)(..)");
	}

	public void testIntermediateAnnotationMatching() {
		runTest("intermediate annotation matching");
	}

	public void testBadRuntimeTestGeneration() {
		runTest("target(@Foo *)");
	}

	// ONE_EIGHT remove for now, needs some grammar changes to ensure empty type annotations are put in place for later consumption
//	public void testErrorMessageOnITDWithTypePatterns() {
//		runTest("clear error message on itd with type pattern");
//	}

	public void testAjKeywordsAsIdentifiers() {
		runTest("before and after are valid identifiers in classes");
	}

	public void testAjKeywordsAsIdentifiers2() {
		runTest("before and after are valid identifiers in classes, part 2");
	}

	public void testNoBeforeReturningAdvice() {
		runTest("before returning advice not allowed!");
	}

	public void testDetectVoidFieldType() {
		runTest("void field type in pointcut expression");
	}

	public void testPointcutOverriding() {
		runTest("overriding final pointcut from super-aspect");
	}

	public void testAtSuppressWarnings() {
		runTest("@SuppressWarnings should suppress");
	}

	public void testDEOWWithBindingPointcut() {
		runTest("declare warning : foo(str) : ...;");
	}

	public void testAroundAdviceAndInterfaceInitializer() {
		runTest("around advice on interface initializer");
	}

	public void testGoodErrorMessageOnUnmatchedMemberSyntax() {
		runTest("good error message for unmatched member syntax");
	}

	public void testITDWithNoExceptionAndIntermediary() {
		runTest("itd override with no exception clause");
	}

	public void testAnonymousInnerClasses() {
		runTest("anonymous inner classes");
	}

	public void testMultipleAnonymousInnerClasses() {
		runTest("multiple anonymous inner classes");
	}

	public void testPrivilegedMethodAccessorsGetRightExceptions_pr82989() {
		runTest("Compiler error due to a wrong exception check in try blocks");
	}

	public void testAnonymousInnerClassWithMethodReturningTypeParameter_pr107898() {
		runTest("anonymous inner class with method returning type parameter");
	}

	public void testMatchingOfObjectArray() {
		runTest("matching against Object[]");
	}

	public void testMultipleAnonymousInnerClasses_pr108104() {
		runTest("multiple anonymous inner classes 2");
	}

	public void testSignatureMatchingInMultipleOverrideScenario() {
		runTest("signature matching in override scenario");
	}

	public void testWildcardAnnotationMatching_pr108245() {
		runTest("wildcard annotation matching - pr108245");
	}

	public void testInnerTypesAndTypeVariables() {
		runTest("inner types and type variables");
	}

	public void testAtAfterThrowingWithNoFormal() {
		runTest("@AfterThrowing with no formal specified");
	}

	public void testParameterizedVarArgsMatch() {
		runTest("varargs with type variable");
	}

	public void testFieldAccessInsideITDM() {
		runTest("itd field access inside itd method");
	}

	public void testTypeVarWithTypeVarBound() {
		runTest("type variable with type variable bound");
	}

	public void testEnumSwitchInITD() {
		runTest("switch on enum inside ITD method");
	}

	public void testInnerTypeOfGeneric() {
		runTest("inner type of generic interface reference from parameterized type");
	}

	public void testDeclareParentsIntroducingCovariantReturnType() {
		runTest("declare parents introducing override with covariance");
	}

	public void testInnerClassPassedToVarargs() {
		runTest("inner class passed as argument to varargs method");
	}

	public void testInlinedFieldAccessInProceedCall() {
		runTest("inlined field access in proceed call");
	}

	public void testVisibiltyInSignatureMatchingWithOverridesPart1() {
		runTest("visibility in signature matching with overrides - 1");
	}

	public void testVisibiltyInSignatureMatchingWithOverridesPart2() {
		runTest("visibility in signature matching with overrides - 2");
	}

	public void testVisibiltyInSignatureMatchingWithOverridesPart3() {
		runTest("visibility in signature matching with overrides - 3");
	}

	public void testArgsGeneratedCorrectlyForAdviceExecution() {
		runTest("args generated correctly for advice execution join point");
	}

	public void testNoUnusedWarningsOnAspectTypes() {
		runTest("no unused warnings on aspect types");
	}

	public void testSyntheticArgumentsOnITDConstructorsNotUsedInMatching() {
		runTest("synthetic arguments on itd cons are not used in matching");
	}

	public void testParsingOfGenericTypeSignature() {
		runTest("parse generic type signature with parameterized type in interface");
	}

	public void testOverrideAndCovarianceWithDecPRuntime() {
		runTest("override and covariance with decp - runtime");
	}

	public void testOverrideAndCovarianceWithDecPRuntimeMultiFiles() {
		runTest("override and covariance with decp - runtime separate files");
	}

	public void testOverrideAndCovarianceWithDecPRuntimeMultiFilesBinaryWeaving() {
		runTest("override and covariance with decp - binary weaving");
	}

	public void testAbstractSynchronizedITDMethods() {
		runTest("abstract synchronized itdms not detected");
	}

	public void testSynchronizedITDInterfaceMethods() {
		runTest("synchronized itd interface methods");
	}

	public void testNoWarningOnUnusedPointcut() {
		runTest("unused private pointcuts");
	}

	public void testITDOnInterfaceWithExistingMember() {
		runTest("itd interface method already existing on interface");
	}

	public void testFinalITDMOnInterface() {
		runTest("final itd methods on interfaces");
	}

	public void testPrivatePointcutOverriding() {
		runTest("can't override private pointcut in abstract aspect");
	}

	public void testAdviceOnCflow() {
		runTest("advising cflow advice execution");
	}

	public void testNoTypeMismatchOnSameGenericTypes() {
		runTest("no type mismatch on generic types in itds");
	}

	public void testSuperCallInITD() {
		runTest("super call in ITD");
	}

	public void testSuperCallInITDPart2() {
		runTest("super call in ITD - part 2");
	}

	public void testAtAnnotationBadTest_pr103740() {
		runTest("Compiler failure on at_annotation");
	}

	public void testNoUnusedParameterWarningsForSyntheticAdviceArgs() {
		runTest("no unused parameter warnings for synthetic advice args");
	}

	public void testNoVerifyErrorWithSetOnInnerType() {
		runTest("no verify error with set on inner type");
	}

	public void testCantFindTypeErrorWithGenericReturnTypeOrParameter() {
		runTest("cant find type error with generic return type or parameter");
	}

	public void testNoVerifyErrorOnGenericCollectionMemberAccess() {
		runTest("no verify error on generic collection member access");
	}

	public void testRawAndGenericTypeConversionITDCons() {
		runTest("raw and generic type conversion with itd cons");
	}

	public void testAtAnnotationBindingWithAround() {
		runTest("@annotation binding with around advice");
	}

	public void testUnableToBuildShadows_pr109728() {
		runTest("Unable to build shadows");
	}

	public void testMessageOnMissingTypeInDecP() {
		runTest("declare parents on a missing type");
	}

	public void testParameterizedGenericMethods() {
		runTest("parameterized generic methods");
	}

	public void testIllegalChangeToPointcutDeclaration_pr111915() {
		runTest("test illegal change to pointcut declaration");
	}

	public void testCantProvideDefaultImplViaITD_pr110307_1() {
		runTest("Cant provide default implementation via ITD - 1");
	}

	public void testCantProvideDefaultImplViaITD_pr110307_2() {
		runTest("Cant provide default implementation via ITD - 2");
	}

	public void testCantProvideDefaultImplViaITD_pr110307_3() {
		runTest("Cant provide default implementation via ITD - 3");
	}

	public void testCantProvideDefaultImplViaITD_pr110307_4() {
		runTest("Cant provide default implementation via ITD - 4");
	}

	public void testCantProvideDefaultImplViaITD_pr110307_5() {
		runTest("Cant provide default implementation via ITD - 5");
	}

	// Needs a change in the compiler so that getType() can be overridden in the intertype scope - thats
	// where we can police whether a type variable has been used without being specified appropriately.
	// public void testCantProvideDefaultImplViaITD_pr110307_6() {runTest("Cant provide default implementation via ITD - 6");}

	public void testCantProvideDefaultImplViaITD_pr110307_7() {
		runTest("Cant provide default implementation via ITD - 7");
	}

	public void testCallJoinPointsInAnonymousInnerClasses() {
		runTest("call join points in anonymous inner classes");
	}

	public void testNoRequirementForUnwovenTypesToBeExposedToWeaver() {
		runTest("default impl of Runnable");
	}

	public void testArrayCloneCallJoinPoints() {
		runTest("array clone call join points in 1.4 vs 1.3");
	}

	public void testDebugInfoForAroundAdvice() {
		runTest("debug info in around advice inlining");
	}

	public void testCCEWithGenericWildcard_pr112602() {
		runTest("ClassCastException with generic wildcard");
	}

	public void testVarArgsIITDInConstructor() {
		runTest("ITD varargs in constructor");
	}

	public void testWeaveInfoMessageForDeclareAtMethodOnITDdMethod() {
		runTest("weaveinfo message for declare at method on an ITDd method");
	}

	public void testITDCWithNoExplicitConsCall() {
		runTest("ITDC with no explicit cons call");
	}

	public void testJava5SpecificFeaturesUsedAtJava14OrLower() {
		runTest("java 5 pointcuts and declares at pre-java 5 compliance levels - 1.7");
	}

	public void testAnonymousTypes() {
		runTest("Anonymous types and nome matching");
	}

	public void testAdviceExecutionJPToStringForms() {
		runTest("adviceexecution join point toString forms");
	}

	public void testAssertWithinPointcutExpression() {
		runTest("pointcut expression containing 'assert'");
	}

	public void testNoVerifyErrorWithTwoThisPCDs_pr113447() {
		runTest("no verify error with two this pcds");
	}

	public void testNoVerifyErrorWithTwoAtThisPCDs_pr113447() {
		runTest("no verify error with two at this pcds");
	}

	public void testNoVerifyErrorWithAtWithinPCDs_pr113447() {
		runTest("no verify error with at within pcds");
	}

	public void testNoVerifyErrorWithAtWithincodePCDs_pr113447() {
		runTest("no verify error with at withincode pcds");
	}

	public void testNoVerifyErrorWithAtAnnotationPCDs_pr113447() {
		runTest("no verify error with at annotation pcds");
	}

	public void testNoVerifyErrorWithTwoArgsPCDs_pr113447() {
		runTest("no verify error with two args pcds");
	}

	public void testNoStackOverflowWithCircularPCDInGenericAspect() {
		runTest("no StackOverflowError with circular pcd in generic aspect");
	}

	public void testNoStackOverflowWithCircularPCDInGenericAspect2() {
		runTest("no StackOverflowError with circular pcd in generic aspect - 2");
	}

	public void testNPEInThisJoinPointStaticPart() {
		runTest("thisJoinPointStaticPart in if test");
	}

	public void testPointcutParsingOfCompiledPointcuts() {
		runTest("pointcut parsing with ajc compiled pointcut references");
	}

	public void testReflectionOfAbstractITDs() {
		runTest("reflection on abstract ITDs (Billing example)");
	}

	public void testDeclareSoftWithAdviceExecution() {
		runTest("declare soft and adviceexecution");
	}

	public void testDeclareSoftWithExclusions() {
		runTest("declare soft and exclusions");
	}

	public void testReturningObjectBinding() {
		runTest("returning(Object) binding");
	}

	public void testPerTargetAndNegation() {
		runTest("pertarget and negated pointcut");
	}

	public void testParameterizedPointcutAndAdvice() {
		runTest("parameterized pointcut and advice");
	}

	public void testDoublyParameterizedAbstractType() {
		runTest("double parameter generic abstract type");
	}

	public void testArgNamesInAdviceAnnotations() {
		runTest("arg names in advice annotations");
	}

	/*
	 * Load-time weaving bugs
	 */
	public void testNPEinWeavingAdaptor_pr116626() {
		runTest("NPE in WeavingAdaptor");
	}

	public void testXlintMessageForImproperAnnotationType_pr115252_Exact() {
		runTest("xlint message for improper exact annotation type");
	}

	public void testXlintMessageForImproperAnnotationType_pr115252_OR() {
		runTest("xlint message for improper annotation type inside OR");
	}

	public void testXlintMessageForImproperAnnotationType_pr115252_AND() {
		runTest("xlint message for improper annotation type inside AND");
	}

	public void testXlintMessageForImproperAnnotationType_pr115252_Return() {
		runTest("xlint message for improper annotated return type");
	}

	public void testXlintMessageForImproperAnnotationType_pr115252_Declaring() {
		runTest("xlint message for improper annotated declaring type");
	}

	public void testXlintMessageForImproperAnnotationType_pr115252_Parameter() {
		runTest("xlint message for improper annotated parameter type");
	}

	public void testXlintMessageForImproperAnnotationType_pr115252_Throws() {
		runTest("xlint message for improper annotated throws pattern");
	}

	public void testXlintMessageForImproperAnnotationType_pr115252_MoreThanOne() {
		runTest("xlint message for more than one improper annotated parameter type");
	}

	public void testNoNPEWhenInaccessibleMethodIsCalledWithinITD_pr119019() {
		runTest("no NPE when inaccessible method is called within itd");
	}

	public void testNoNPEWithOrPointcutAndMoreThanOneArgs_pr118149() {
		runTest("no NPE with or pointcut and more than one args");
	}

	public void testNoSOBWithGenericInnerAspects_pr119543() {
		runTest("no StringOutOfBoundsException with generic inner aspects");
	}

	public void testIllegalAccessErrorWithAroundAdvice_pr119657() {
		runTest("IllegalAccessError with around advice on interface method call");
	}

	public void testIllegalAccessErrorWithAroundAdviceNotSelf_pr119657() {
		runTest("IllegalAccessError with around advice on interface method call not self");
	}

	public void testIllegalAccessErrorWithAroundAdviceTerminateAfterCompilationLTW_pr119657() {
		runTest("IllegalAccessError with around advice on interface method call using -XterminateAfterCompilation and LTW");
	}

	public void testIllegalAccessErrorWithAroundAdviceLTW_pr119657() {
		runTest("IllegalAccessError with around advice on interface method call using LTW");
	}

	public void testIllegalAccessErrorWithAroundAdviceNotSelfLTW_pr119657() {
		runTest("IllegalAccessError with around advice on interface method call not self using LTW");
	}

	public void testIllegalAccessErrorWithAroundAdviceSelfAndNotSelfLTW_pr119657() {
		runTest("IllegalAccessError with around advice on interface method call self and not self using LTW");
	}

	public void testIllegalAccessErrorWithAroundAdviceLTWNoInline_pr119657() {
		runTest("IllegalAccessError with around advice on interface method call using LTW and -XnoInline");
	}

	public void testReflectOnCodeStyleITDs() {
		runTest("reflection on itds");
	}

	public void testReflectOnAtAspectJDecP() {
		runTest("reflection on @DeclareParents");
	}

	public void testModifierOverrides() {
		runTest("modifier overrides");
	}

	public void testAbstractPerThisInAtAspectJ() {
		runTest("abstract perthis in @AspectJ");
	}

	public void testNPEInBcelAdviceWithConcreteAspect_pr121385() {
		runTest("override protected pointcut in aop.xml concrete aspect");
	}

}