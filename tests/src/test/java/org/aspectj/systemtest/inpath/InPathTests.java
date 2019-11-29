/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.inpath;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class InPathTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(InPathTests.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("inpath.xml");
  }


  public void test001(){
    runTest("source for options -injars");
  }

  public void test002(){
    runTest("options -injars");
  }

  public void test003(){
    runTest("options -injars checking declare parents interactions");
  }

// test removed from suite since aspects are *always* generated reweavable
//  public void test004(){
//    runTest("The compiler crashes when using aspect libraries created without using -terminateAfterCompilation");
//  }

  public void test005(){
    runTest("declare warnings on main");
  }

  public void test006(){
    runTest("declare warnings on binary javac 1.4 classes");
  }

  public void test007(){
    runTest("declare warnings on binary ajc 1.1 classes");
  }

  public void test008(){
    runTest("Weaving rt.jar results in stack overflow");
  }

  public void test009(){
    runTest("Weaving failure when using injars (no jars)");
  }

  public void test010(){
    runTest("Weaving failure when using injars (on aspectpath)");
  }

  public void test011(){
    runTest("Weaving failure when using injars (on classpath)");
  }

  public void test012(){
    runTest("Weaving failure when using injars (actual injars)");
  }

  public void test013(){
    runTest("Introduced abstract method on interface not implemented by subtype (injars)");
  }

  public void test014(){
    runTest("error Type mismatch: cannot convert from java.lang.String to java.lang.String");
  }

  public void test015(){
    runTest("declare error binary-weaving NPE");
  }

  public void test016(){
    runTest("declare error binary-weaving NPE");
  }

  public void test017(){
    runTest("Regression from 1.1: NPE in CompilationResult");
  }

}

