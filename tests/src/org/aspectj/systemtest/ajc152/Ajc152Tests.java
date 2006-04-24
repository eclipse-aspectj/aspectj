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
	
  public void testNotAtWithincode_pr138158() { runTest("not at withincode");}
  //public void testComplexGenericDecl_pr137568() { runTest("complicated generics declaration");}
  public void testNpeOnDup_pr138143() { runTest("npe on duplicate method with ataj");}
  public void testPointcutsAndGenerics_pr137496_1() { runTest("pointcuts and generics - B");}
  public void testPointcutsAndGenerics_pr137496_2() { runTest("pointcuts and generics - D");}
  public void testPointcutsAndGenerics_pr137496_3() { runTest("pointcuts and generics - E");}
  public void testPointcutsAndGenerics_pr137496_4() { runTest("pointcuts and generics - F");}
  public void testPointcutsAndGenerics_pr137496_5() { runTest("pointcuts and generics - G");}
  public void testAspectLibrariesAndASM_pr135001() { runTest("aspect libraries and asm");}
  public void testStackOverflow_pr136258() { runTest("stack overflow");}
  public void testIncorrectOverridesEvaluation13() { runTest("incorrect overrides evaluation - 1.3"); }
  public void testIncorrectOverridesEvaluation15() { runTest("incorrect overrides evaluation - 1.5"); }
  
  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc152Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc152/ajc152.xml");
  }

  
}