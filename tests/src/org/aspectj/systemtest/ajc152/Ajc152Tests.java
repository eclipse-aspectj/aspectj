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
package org.aspectj.systemtest.ajc152;

import java.io.File;
import java.util.List;

import junit.framework.Test;

//import org.aspectj.systemtest.ajc150.GenericsTests;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc152Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public void testNoUnnecessaryDeclarationOfThrownExcp_pr129282_1() {runTest("no unnecessary declaration of thrown exception warning - 1");}
  public void testNoUnnecessaryDeclarationOfThrownExcp_pr129282_2() {runTest("no unnecessary declaration of thrown exception warning - 2");}
  public void testNoUnnecessaryDeclarationOfThrownExcp_pr129282_3() {runTest("no unnecessary declaration of thrown exception warning - 3");}
  public void testNoUnnecessaryDeclarationOfThrownExcp_pr129282_4() {runTest("no unnecessary declaration of thrown exception warning - 4");}
  public void testNoUnnecessaryDeclarationOfThrownExcp_pr129282_5() {runTest("no unnecessary declaration of thrown exception warning - 5");}
  public void testNoUnnecessaryDeclarationOfThrownExcp_pr129282_6() {runTest("no unnecessary declaration of thrown exception warning - 6");}
  public void testNoUnnecessaryDeclarationOfThrownExcp_pr129282_7() {runTest("no unnecessary declaration of thrown exception warning - 7");}
  public void testNoUnnecessaryDeclarationOfThrownExcp_pr129282_8() {runTest("no unnecessary declaration of thrown exception warning - 8");}
  public void testNoUnnecessaryDeclarationOfThrownExcp_pr129282_9() {runTest("no unnecessary declaration of thrown exception warning - 9");}
  public void testNoUnnecessaryDeclarationOfThrownExcp_pr129282_10() {runTest("no unnecessary declaration of thrown exception warning - 10");}  
  public void testAtAJVerificationError_pr144602() { runTest("atAJ perthis aspect verification error");}
  public void testLTWAndGeneratingSUID_pr144465() { runTest("ltw with serialversionUID creation"); }
  public void testAspects14PerSingleton_pr122253() { runTest("aspects14 - persingleton");}
  public void testAspects14PerCflow_pr122253() { runTest("aspects14 - percflow");}
  public void testAspects14PerThis_pr122253() { runTest("aspects14 - perthis");}
  public void testAspects14PerTypeWithin_pr122253() { runTest("aspects14 - pertypewithin");}
  public void testFunkyGenericErrorWithITDs_pr126355() { runTest("bizarre generic error with itds");}
  public void testConcretizingAbstractMethods_pr142466() { runTest("aop.xml aspect inheriting but not concretizing abstract method");}
  public void testConcretizingAbstractMethods_pr142466_2() { runTest("aop.xml aspect inheriting but not concretizing abstract method - 2");}
  public void testComplexGenericDecl_pr137568() { runTest("complicated generics declaration");}
  public void testItdOnInnerTypeOfGenericType_pr132349() { runTest("ITD on inner type of generic type");}
  public void testItdOnInnerTypeOfGenericType_pr132349_2() { runTest("ITD on inner type of generic type - 2");}
  public void testItdOnInnerTypeOfGenericType_pr132349_3() { runTest("ITD on inner type of generic type - 3");}
//  public void testCovarianceAndDecp_pr128443_1() { runTest("covariance and decp - 1"); }
  public void testLTWGeneratedAspectAbstractMethod_pr125480() { runTest("aop.xml aspect inheriting abstract method ");} 
  public void testLTWGeneratedAspectAbstractMethod_pr125480_2() { runTest("aop.xml aspect inheriting abstract method - code style");} 
  //public void testSuperITDExplosion_pr134425() { runTest("super ITDs");}
  //public void testMisbehavingDeclareAnnotation_pr135865() { runTest("misbehaving declare annotation");}
  //public void testMisbehavingDeclareAnnotation_pr135865_2() { runTest("misbehaving declare annotation - 2");}
  public void testCompletelyBrokenAopConcretization_pr142165_1() { runTest("broken concretization");}
  public void testCompletelyBrokenAopConcretization_pr142165_2() { runTest("broken concretization - 2");}
  public void testCompletelyBrokenAopConcretization_pr142165_3() { runTest("broken concretization - 3");}
  public void testVerifyErrorLTW_pr135068() { runTest("ltw verifyerror");}
  public void testVerifyErrorLTW_pr135068_2() { runTest("ltw verifyerror - 2");}
  public void testVerifyErrorLTW_pr135068_3() { runTest("ltw verifyerror - 3");}
  public void testVerifyErrorLTW_pr135068_4() { runTest("ltw verifyerror - 4");}
  public void testVerifyErrorForComplexCflow_pr136026() { runTest("verifyerror");}
  public void testVerifyErrorForComplexCflow_pr136026_2() { runTest("verifyerror - 2");}
  public void testAnnotationsAndGenericsBCException_pr129704() { runTest("annotations and generics leading to BCException");}
  public void testMethodTooBigAfterWeaving_pr138384() { runTest("method too big"); }
  public void testNotAtWithincode_pr138158_1() { runTest("not at withincode - 1");}
  public void testNotAtWithincode_pr138158_2() { runTest("not at withincode - 2");}
  public void testNotAtWithincode_pr138158_3() { runTest("not at within - 3");}
  public void testNpeOnDup_pr138143() { runTest("npe on duplicate method with ataj");}
  public void testPointcutsAndGenerics_pr137496_1() { runTest("pointcuts and generics - B");}
  public void testPointcutsAndGenerics_pr137496_2() { runTest("pointcuts and generics - D");}
  public void testPointcutsAndGenerics_pr137496_3() { runTest("pointcuts and generics - E");}
  public void testPointcutsAndGenerics_pr137496_4() { runTest("pointcuts and generics - F");}
  public void testPointcutsAndGenerics_pr137496_5() { runTest("pointcuts and generics - G");}
  public void testPointcutsAndGenerics_pr137496_6() { runTest("pointcuts and generics - H");}
  public void testAspectLibrariesAndASM_pr135001() { runTest("aspect libraries and asm");}
  public void testStackOverflow_pr136258() { runTest("stack overflow");}
  public void testIncorrectOverridesEvaluation13() { runTest("incorrect overrides evaluation - 1.3"); }
  public void testIncorrectOverridesEvaluation15() { runTest("incorrect overrides evaluation - 1.5"); }
  public void testAtWithinCodeBug_pr138798() { runTest("atWithinCodeBug"); }
  public void testReferencePCutInDeclareWarning_pr138215() { runTest("Reference pointcut fails inside @DeclareWarning");}
  public void testReferencePCutInPerClause_pr138219() { runTest("Can't use a FQ Reference pointcut in any pointcut expression referenced by a per-clause");}
  public void testReferencePCutInPerClause_pr130722() { runTest("FQ Reference pointcut from perclause ref pc"); }
  public void testDoubleAnnotationMatching_pr138223() { runTest("Double at annotation matching (no binding)");}
  public void testSuperCallsInAtAspectJAdvice_pr139749() { runTest("Super calls in @AspectJ advice");}
  public void testNoClassCastExceptionWithPerThis_pr138286() { runTest("No ClassCastException with perThis");}
  
  public void testDeclareAtMethodRelationship_pr143924() {
	  //AsmManager.setReporting("c:/debug.txt",true,true,true,true);
	  runTest("declare @method relationship");
	  IHierarchy top = AsmManager.getDefault().getHierarchy();
	  
  	  // get the IProgramElements corresponding to the different code entries
  	  IProgramElement decam = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.DECLARE_ANNOTATION_AT_METHOD,
  			  "declare @method: * debit(..) : @Secured(role = \"supervisor\")");  	   	 
  	  assertNotNull("Couldn't find 'declare @method' element in the tree",decam);
  	  IProgramElement method = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.METHOD,"debit(java.lang.String,long)");
  	  assertNotNull("Couldn't find the 'debit(String,long)' method element in the tree",method);

  	  List matches = AsmManager.getDefault().getRelationshipMap().get(decam);	
  	  assertNotNull("'declare @method' should have some relationships but does not",matches);
  	  assertTrue("'declare @method' should have one relationships but has " + matches.size(),matches.size()==1);
  	  List matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("'declare @method' should have one targets but has" + matchesTargets.size(),matchesTargets.size()==1);
  	  IProgramElement target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the 'debit(java.lang.String,long)' method but is IPE with label "
  			  + target.toLabelString(),method,target);
  	  
  	  // check that the debit method has an annotated by relationship with the declare @method
  	  matches = AsmManager.getDefault().getRelationshipMap().get(method);	
  	  assertNotNull("'debit(java.lang.String,long)' should have some relationships but does not",matches);
  	  assertTrue("'debit(java.lang.String,long)' should have one relationships but has " + matches.size(),matches.size()==1);
  	  matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("'debit(java.lang.String,long)' should have one targets but has" + matchesTargets.size(),matchesTargets.size()==1);
  	  target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the 'declare @method' ipe but is IPE with label "
  			  + target.toLabelString(),decam,target);
  	  
  }
  
// this next one reported as a bug by Rob Harrop, but I can't reproduce the failure yet...
//public void testAtAspectWithReferencePCPerClause_pr138220() { runTest("@Aspect with reference pointcut in perclause");}  

  public void testJarChecking_pr137235_1() { runTest("directory with .jar extension: source and outjar"); }	    
  public void testJarChecking_pr137235_2() { runTest("directory with .jar extension"); }
  public void testMakePreMethodNPE_pr136393() { runTest("NPE in makePreMethod");}

//  public void testFunkyGenericErrorWithITDs_pr126355_2() { 
//	  runTest("bizarre generic error with itds - 2");
//	  // public class Pair<F,S> affected by pertarget aspect
//	  GenericsTests.verifyClassSignature(ajc,"Pair","<F:Ljava/lang/Object;S:Ljava/lang/Object;>Ljava/lang/Object;LIdempotentCache$ajcMightHaveAspect;;");
//  }

  // tests that can't be included for some reason

  // Not valid whilst the ajc compiler forces debug on (ignores -g:none) - it will be green but is invalid, trust me
  // public void testLongWindedMessages_pr129408() { runTest("long winded ataj messages");}
  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc152Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc152/ajc152.xml");
  }

  
}