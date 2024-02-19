/*******************************************************************************
 * Copyright (c) 2020 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc196;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava14OrLater;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc196Tests extends XMLBasedAjcTestCaseForJava14OrLater {

  public void testNPE_558995() {
    runTest("early resolution of supporting interfaces");
  }

  public void testSwitch1() {
    runTest("switch 1");
    checkVersion("Switch1", Constants.ClassFileVersion.of(14).MAJOR, 0);
  }

  public void testSwitch2() {
    runTest("switch 2");
    checkVersion("Switch2", Constants.ClassFileVersion.of(14).MAJOR, 0);
  }

  public void testSwitch3() {
    runTest("switch 3");
    checkVersion("Switch3", Constants.ClassFileVersion.of(14).MAJOR, 0);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc196Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc196.xml");
  }

}
