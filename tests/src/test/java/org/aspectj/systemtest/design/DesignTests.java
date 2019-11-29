/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.design;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class DesignTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(DesignTests.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("design.xml");
  }


  public void test001(){
    runTest("initial tests for new introduction style");
  }

  public void test002(){
    runTest("overriding of introduced methods and accessibility");
  }

  public void test003(){
    runTest("within and introductions behaves correctly");
  }

  public void test004(){
    runTest("correct inheritance of multiple concrete methods");
  }

  public void test005(){
    runTest("errors in inheritance of multiple concrete methods");
  }

  public void test006(){
    runTest("declared exceptions are checked correctly on intros (errors)");
  }

  public void test007(){
    runTest("declared exceptions are checked correctly on intros");
  }

  public void test008(){
    runTest("Joinpoint is not created for foo(String) when before() advice is present.");
  }

  public void test009(){
    runTest("more tests of eachobject with some difficult typing issues");
  }

  public void test010(){
    runTest("eachobject: eachobject(receptions(...)) [eachobject]");
  }

  public void test011(){
    runTest("Checking new joinpoints");
  }

  public void test012(){
    runTest("eachobject: simple test [eachobject] (still)");
  }

  public void test013(){
    runTest("scope issues with introduction (needs more work)");
  }

// uncomment this test if we implement 42743
//  public void test014_DeclareSoft(){
//      runTest("declare soft limitation");
//    }
}

