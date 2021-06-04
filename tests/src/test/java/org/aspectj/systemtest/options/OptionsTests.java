/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * ******************************************************************/
package org.aspectj.systemtest.options;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class OptionsTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(OptionsTests.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("options.xml");
  }


  public void test001(){
    runTest("options -warn:deprecation");
  }

  public void test002(){
    runTest("options -warn:deprecation not enabled");
  }

  public void test003(){
    runTest("setting -warn:constructorName works");
  }

  public void test004(){
    runTest("-deprecation not working?");
  }

}

