/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc197;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava15OrLater;

/**
 * @author Alexander Kriegisch
 */
public class Ajc197Tests extends XMLBasedAjcTestCaseForJava15OrLater {

  public void testHiddenClass() {
    runTest("hidden class");
  }

  public void testTextBlock1() {
    runTest("textblock 1");
  }

  public void testTextBlock2() {
    runTest("textblock 2");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc197Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc197.xml");
  }

}
