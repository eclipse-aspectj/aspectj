/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.serialVerUID;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class SUIDTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(SUIDTests.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("serialVerUID.xml");
  }


  public void test001(){
    runTest("SUID: Before execution advice");
  }

  public void test002(){
    runTest("SUID: Around execution advice");
  }

  public void test003(){
    runTest("SUID: Around closure execution advice (-Xlint:ignore)");
  }

  public void test004(){
    runTest("SUID: Around closure execution advice");
  }

  public void test005(){
    runTest("SUID: thisJoinPoint");
  }

  public void test006(){
    runTest("SUID: thisJoinPoint with clinit method");
  }

  public void test007(){
    runTest("SUID: After returning staticinitialization advice");
  }

  public void test008(){
    runTest("SUID: After returning staticinitialization advice with serialVersionUID field");
  }

  public void test009(){
    runTest("SUID: ITD field");
  }

  public void test010(){
    runTest("SUID: ITD method");
  }

  public void test011(){
    runTest("SUID: Declare extends");
  }

  public void test012(){
    runTest("SUID: Declare implements (compile)");
  }

  public void test013(){
    runTest("SUID: Declare implements non-Serializable (compile)");
  }

  public void test014(){
    runTest("SUID: Declare implements Serializable (compile)");
  }

  public void test015(){
    runTest("SUID: Declare implements (weave)");
  }

  public void test016(){
    runTest("SUID: Priviliged aspect");
  }

  public void test017(){
    runTest("SUID: Perthis aspect");
  }

}

