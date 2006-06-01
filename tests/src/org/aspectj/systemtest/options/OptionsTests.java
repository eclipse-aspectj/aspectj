/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.options;

import java.io.File;
import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class OptionsTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(OptionsTests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/options/options.xml");
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

