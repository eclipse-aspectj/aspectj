/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc152;

import java.io.File;
import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc152Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
  public void testVerifyErrorForComplexCflow_pr136026() { runTest("verifyerror");}
  public void testVerifyErrorForComplexCflow_pr136026_2() { runTest("verifyerror - 2");}
  public void testAnnotationsAndGenericsBCException_pr129704() { runTest("annotations and generics leading to BCException");}
  public void testMethodTooBigAfterWeaving_pr138384() { runTest("method too big"); }
  public void testNotAtWithincode_pr138158_1() { runTest("not at withincode - 1");}
  public void testNotAtWithincode_pr138158_2() { runTest("not at withincode - 2");}
  public void testNotAtWithincode_pr138158_3() { runTest("not at within - 3");}
//  public void testComplexGenericDecl_pr137568() { runTest("complicated generics declaration");}
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

  // known failures, uncomment when working.
  public void testReferencePCutInDeclareWarning_pr138215() { runTest("Reference pointcut fails inside @DeclareWarning");}
//  public void testReferencePCutInPerClause_pr138219() { runTest("Can't use a FQ Reference pointcut in any pointcut expression referenced by a per-clause");}
//  public void testDoubleAnnotationMatching_pr138223() { runTest("Double at annotation matching (no binding)");}
  public void testSuperCallsInAtAspectJAdvice_pr139749() { runTest("Super calls in @AspectJ advice");}

  public void testNoClassCastExceptionWithPerThis_pr138286() { runTest("No ClassCastException with perThis");}
  
// this next one reported as a bug by Rob Harrop, but I can't reproduce the failure yet...
//public void testAtAspectWithReferencePCPerClause_pr138220() { runTest("@Aspect with reference pointcut in perclause");}  

  public void testJarChecking_pr137235_1() { runTest("directory with .jar extension: source and outjar"); }	    
  public void testJarChecking_pr137235_2() { runTest("directory with .jar extension"); }
  public void testMakePreMethodNPE_pr136393() { runTest("NPE in makePreMethod");}
  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc152Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc152/ajc152.xml");
  }

  
}