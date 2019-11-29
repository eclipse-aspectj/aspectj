/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.xlint;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.bcel.BcelShadow;

import junit.framework.Test;

public class XLintTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(XLintTests.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("xlint.xml");
  }


  public void test001(){
    runTest("options -Xlint args()");
  }

  public void test002(){
    runTest("options declare field on bad type");
  }

  public void test003(){
    runTest("options declare method on bad type");
  }

  public void test004(){
    runTest("options -Xlint declare parent");
  }

  public void test005(){
    runTest("options -Xlint target()");
  }

  public void test006(){
    runTest("options -Xlint this()");
  }

  public void test007(){
    runTest("options negative -Xlint args()");
  }

  public void test008(){
    runTest("options negative -Xlint declare parent");
  }

  public void test009(){
    runTest("options negative -Xlint target()");
  }

  public void test010(){
    runTest("options negative -Xlint this()");
  }

  public void test011(){
    runTest("unmatched type name in a declare parents should result in a warning in -Xlint mode");
  }

  public void test012(){
    runTest("privileged access to code outside the control of the compiler");
  }

  public void test013(){
    runTest("Unexpected Xlint:unresolvableMember warning with withincode");
  }

  public void test014(){
    runTest("valid XLintWarningTest file, default level of warning");
  }

  public void test015(){
    runTest("XLint:ignore suppresses XLint warnings");
  }

  public void test016(){
    runTest("XLint:error promotes XLint warnings to error");
  }

  public void test017(){
    runTest("alias getCause for getWrappedThrowable in SoftException");
  }

//     public void test018(){
//     runTest("XLint warning for call PCD's using subtype of defining type");
//     }

  public void test019(){
    runTest("XLint warning for call PCD's using subtype of defining type (-1.3 -Xlint:ignore)");
  }
  

  // the following five tests check various scenarios around the lazyTjp XLint message
  public void test020(){
     runTest("no XLint warning: thisJoinPoint potentially lazy and nothing stopping it");
     assertTrue("Something prevented the lazytjp optimization from working??",BcelShadow.appliedLazyTjpOptimization);
  }
  
  public void test021(){
     runTest("XLint warning: thisJoinPoint potentially lazy but stopped by around advice which doesn't use tjp");
     assertFalse("lazytjp optimization should have failed to be applied because of around advice at the jp",
    		 BcelShadow.appliedLazyTjpOptimization);
  }
  
  public void test022(){
    runTest("no XLint warning: thisJoinPoint not lazy (no if PCD) but would have been stopped anyway by around advice");
    assertFalse("lazytjp optimization should have failed to be applied because of around advice *and* before advice has no if() at the jp",
   		 BcelShadow.appliedLazyTjpOptimization);
  }
  
  public void test023(){
    runTest("no XLint warning: thisJoinPoint cannot be built lazily");
    assertFalse("lazytjp optimization should have failed to be applied because before advice has no if() at the jp",
      		 BcelShadow.appliedLazyTjpOptimization);
  }  
   
  public void test024(){
    runTest("XLint warning: thisJoinPoint potentially lazy but stopped by around advice which uses tjp");
    assertFalse("lazytjp optimization should have failed to be applied because around advice uses tjp",
     		 BcelShadow.appliedLazyTjpOptimization);
  }
    
  public void test025(){
	runTest("check for xlazytjp warning if actually supplied");
    assertTrue("Something prevented the lazytjp optimization from working??",BcelShadow.appliedLazyTjpOptimization);
  }
  
  public void test026(){
	runTest("lazytjp: warning when around advice uses tjp");
  }
  
  public void test027() {
		runTest("lazytjp: warning when if missing on before advice");
  }
  
  public void test028() {
		runTest("lazytjp: warning when if missing on after advice");
  }
  
  public void test029() {
		runTest("lazytjp: multiple clashing advice preventing lazytjp");
  }
  
  public void test030() {
		runTest("lazytjp: interfering before and around");
  }
  
  // FIXME asc put this back in !
//  public void test031() {
//	 if (is15VMOrGreater)
//       runTest("7 lint warnings");
//  }

}

