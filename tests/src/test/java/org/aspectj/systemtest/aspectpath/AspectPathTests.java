/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.aspectpath;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class AspectPathTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(AspectPathTests.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("aspectpath.xml");
  }


  public void test001(){
    runTest("testing new options");
  }

  public void test002(){
    runTest("aspect-declared interface members in libraries - baseline");
  }

  public void test003(){
    runTest("aspect-declared interface members in libraries - interfaceOnly.jar");
  }

  public void test004(){
    runTest("aspect-declared interface members in libraries - aspectOnly.jar");
  }

  public void test005(){
    runTest("aspect-declared interface members in libraries - aspectedInterfaceOnly.jar");
  }

  public void test006(){
    runTest("aspect-declared interface members in libraries - aspectedInterfaceOnly.jar,aspectOnly.jar");
  }

  public void test007(){
    runTest("aspect-declared interface members in libraries - aspectedInterfaceOnlyBinary.jar,aspectOnly.jar");
  }

  public void test008(){
    runTest("aspect-declared interface members in libraries - aspectedInterfaceOnly.jar,aspectpath=aspectOnly.jar");
  }

  public void test009(){
    runTest("aspect-declared interface members in libraries - aspectedInterfaceOnly.jar,aspectpath=aspectOnly.jar");
  }

  public void test010(){
    runTest("exception clause for aspect-declared interface methods - positive");
  }

  public void test011(){
    runTest("exception clause for aspect-declared interface methods - negative");
  }

  public void test012(){
    runTest("exception clause for aspect-declared class methods - positive");
  }

  public void test013(){
    runTest("exception clause for aspect-declared class methods - negative");
  }

  public void test014(){
    runTest("exception clause for aspect-declared interface methods - positive binary");
  }

  public void test015(){
    runTest("exception clause for aspect-declared interface methods - negative binary");
  }

  public void test016(){
    runTest("exception clause for aspect-declared class methods - positive binary");
  }

  public void test017(){
    runTest("exception clause for aspect-declared class methods - negative binary");
  }

  public void test018(){
    runTest("percflow aspects compiled from jars share one instance for all entry points");
  }

  public void test019(){
    runTest("(using aspectpath) percflow aspects compiled from jars share one instance for all entry points");
  }

  public void test020(){
    runTest("Introduced abstract method on abstract class not implemented by subtype");
  }

  public void test021(){
    runTest("Introduced abstract method on interface not implemented by subtype (aspectpath)");
  }

}

