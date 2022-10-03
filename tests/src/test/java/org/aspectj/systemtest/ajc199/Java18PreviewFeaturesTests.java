/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc199;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava18Only;

/**
 * @author Alexander Kriegisch
 */
public class Java18PreviewFeaturesTests extends XMLBasedAjcTestCaseForJava18Only {

  public void testSwitchPatternMatchingCaseLabelDominatedByPrecedingError() {
    runTest("switch pattern matching error");
  }

  public void testSwitchPatternMatchingJava() {
    runTest("switch pattern matching java");
    checkVersion("SwitchPatternOK", Constants.MAJOR_18, Constants.PREVIEW_MINOR_VERSION);
  }

  public void testSwitchPatternMatchingAspect() {
    runTest("switch pattern matching aspect");
    checkVersion("SwitchPatternAspect", Constants.MAJOR_18, Constants.PREVIEW_MINOR_VERSION);
    checkVersion("Application", Constants.MAJOR_18, Constants.PREVIEW_MINOR_VERSION);
    checkVersion("Shape", Constants.MAJOR_18, Constants.PREVIEW_MINOR_VERSION);
    checkVersion("S", Constants.MAJOR_18, Constants.PREVIEW_MINOR_VERSION);
  }

  public void testSwitchPatternMatchingPreview2Error1() {
    runTest("switch pattern matching preview 2 error 1");
  }

  public void testSwitchPatternMatchingPreview2Error2() {
    runTest("switch pattern matching preview 2 error 2");
  }

  public void testSwitchPatternMatchingPreview2Java() {
    runTest("switch pattern matching preview 2 java");
    checkVersion("SwitchPatternPreview2OK", Constants.MAJOR_18, Constants.PREVIEW_MINOR_VERSION);
  }

  public void testSwitchPatternMatchingPreview2Aspect() {
    runTest("switch pattern matching preview 2 aspect");
    checkVersion("SwitchPatternPreview2Aspect", Constants.MAJOR_18, Constants.PREVIEW_MINOR_VERSION);
    checkVersion("Application", Constants.MAJOR_18, Constants.PREVIEW_MINOR_VERSION);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java18PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc199.xml");
  }

}
