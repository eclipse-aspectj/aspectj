/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.xlint;

import java.io.File;
import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class XLintTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(XLintTests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/xlint/xlint.xml");
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

  public void test018(){
    runTest("XLint warning for call PCD's using subtype of defining type");
  }

  public void test019(){
    runTest("XLint warning for call PCD's using subtype of defining type (-1.3 -Xlint:ignore)");
  }

}

