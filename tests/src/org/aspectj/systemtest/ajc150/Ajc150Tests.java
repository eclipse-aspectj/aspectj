/*******************************************************************************
 * Copyright (c) 2004 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.asm.AsmManager;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.util.LangUtil;

/**
 * These are tests that will run on Java 1.4 and use the old harness format for test specification.
 */
public class Ajc150Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	  
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc150Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc150/ajc150.xml");
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
    JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"PR82570_1");
    Method[] meths = jc.getMethods();
    for (int i = 0; i < meths.length; i++) {
		Method method = meths[i];
		if (f) System.err.println("Line number table for "+method.getName()+method.getSignature()+" = "+method.getLineNumberTable());
		assertTrue("Didn't find a line number table for method "+method.getName()+method.getSignature(),
				method.getLineNumberTable()!=null);
    }

    // This test would determine the info isn't there if you pass -g:none ...
//    cR = ajc(baseDir,new String[]{"PR82570_1.java","-g:none"});
//    assertTrue("Expected no compile problem:"+cR,!cR.hasErrorMessages());
//    System.err.println(cR.getStandardError());
//    jc = getClassFrom(ajc.getSandboxDirectory(),"PR82570_1");
//    meths = jc.getMethods();
//    for (int i = 0; i < meths.length; i++) {
//		Method method = meths[i];
//		assertTrue("Found a line number table for method "+method.getName(),
//				method.getLineNumberTable()==null);
//    }
  }

  
  public void testCanOverrideProtectedMethodsViaITDandDecp_pr83303() {
  	runTest("compiler error when mixing inheritance, overriding and polymorphism");
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
  	
  	if (LangUtil.is15VMOrGreater()) {
	  	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  	PrintWriter pw = new PrintWriter(baos);
	  	AsmManager.dumptree(pw,AsmManager.getDefault().getHierarchy().getRoot(),0);
	  	pw.flush();
	  	String tree = baos.toString();
	  	assertTrue("Expected 'Red [enumvalue]' somewhere in here:"+tree,tree.indexOf("Red  [enumvalue]")!=-1);
  	}
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

  /**
   * IfPointcut.findResidueInternal() was modified to make this test complete in a short amount
   * of time - if you see it hanging, someone has messed with the optimization.
   */
  public void testIfEvaluationExplosion_pr94086() {
	  runTest("Exploding compile time with if() statements in pointcut");
  }
  
  public void testReflectNPE_pr94167() {runTest("NPE in reflect implementation");}
  
  public void testStaticImports_pr84260() {runTest("static import failures");} 
  
  public void testGenerics_pr99089() {runTest("ArrayIndexOutOfBoundsException - Generics in privileged aspects");}
  public void testGenerics_pr95993() {runTest("NPE at ClassScope.java:660 when compiling generic class");}
  
  public void testItdGenerics_pr99228()  {runTest("ITD of a field into a generic class");}
  public void testItdGenerics_pr98320()  {runTest("intertype with nested generic type");}
  public void testItdGenerics_pr100227() {runTest("inner class with generic enclosing class");}
  public void testItdGenerics_pr100260() {runTest("methods inherited from a generic parent");}
  
  public void testSyntaxErrorNPE_pr103266() {runTest("NPE on syntax error");}
  
  public void testIllegalStateExceptionOnNestedParameterizedType_pr106634() { 
	  runTest("IllegalStateException unpacking signature of nested parameterized type");
  }
  
  public void testParseErrorOnAnnotationStarPlusPattern() {
	  runTest("(@Foo *)+ type pattern parse error");
  }
  
  public void test_pr106130_tooManyLocals() {
	  runTest("test weaving with > 256 locals");
  }
  
  public void testMissingNamePattern_pr106461() { runTest("missing name pattern"); }
  
  public void testMissingNamePattern_pr107059() {
	  runTest("parser crashes on call(void (@a *)(..)");
  }
  
  public void testIntermediateAnnotationMatching() {
	  runTest("intermediate annotation matching");
  }
  
  public void testBadRuntimeTestGeneration() {
	  runTest("target(@Foo *)");
  }
  
  public void testErrorMessageOnITDWithTypePatterns() {
	  runTest("clear error message on itd with type pattern");
  }
  
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
  
  // helper methods.....
  
  public SyntheticRepository createRepos(File cpentry) {
	ClassPath cp = new ClassPath(cpentry+File.pathSeparator+System.getProperty("java.class.path"));
	return SyntheticRepository.getInstance(cp);
  }
  
  protected JavaClass getClassFrom(File where,String clazzname) throws ClassNotFoundException {
	SyntheticRepository repos = createRepos(where);
	return repos.loadClass(clazzname);
  }

}