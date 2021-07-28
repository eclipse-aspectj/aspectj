/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc198;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava17Only;

/**
 * @author Alexander Kriegisch
 */
public class Java17PreviewFeaturesTests extends XMLBasedAjcTestCaseForJava17Only {

  public void testSwitchPatternMatchingCaseLabelDominatedByPrecedingError() {
    // TODO: JDT Core does not support detecting type domination detection in the development version yet -> activate when available
    System.out.println("TODO: JDT Core does not support detecting type domination detection in the development version yet -> activate when available");
//    runTest("switch pattern matching error");
  }

  public void testSwitchPatternMatchingJava() {
    // TODO: JDT Core does not support sealed class coverage in the development version yet -> activate when available
    System.out.println("TODO: JDT Core does not support sealed class coverage in the development version yet -> activate when available");
//    runTest("switch pattern matching java");
//    checkVersion("SwitchPatternOK", Constants.MAJOR_17, Constants.PREVIEW_MINOR_VERSION);
  }

  public void testSwitchPatternMatchingAspect() {
    // TODO: JDT Core does not support sealed class coverage in the development version yet -> activate when available
    System.out.println("TODO: JDT Core does not support sealed class coverage in the development version yet -> activate when available");
//    runTest("switch pattern matching aspect");
//    checkVersion("SwitchPatternAspect", Constants.MAJOR_17, Constants.PREVIEW_MINOR_VERSION);
//    checkVersion("Application", Constants.MAJOR_17, Constants.PREVIEW_MINOR_VERSION);
//    checkVersion("Shape", Constants.MAJOR_17, Constants.PREVIEW_MINOR_VERSION);
//    checkVersion("S", Constants.MAJOR_17, Constants.PREVIEW_MINOR_VERSION);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java17PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc198.xml");
  }

}
