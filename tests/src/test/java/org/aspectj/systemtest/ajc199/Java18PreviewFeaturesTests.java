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
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Java18PreviewFeaturesTests extends JavaVersionSpecificXMLBasedAjcTestCase {
  private static final Constants.ClassFileVersion classFileVersion = Constants.ClassFileVersion.of(18);

  public Java18PreviewFeaturesTests() {
    super(18, 18);
  }

  public void testSwitchPatternMatchingCaseLabelDominatedByPrecedingError() {
    runTest("switch pattern matching error");
  }

  public void testSwitchPatternMatchingJava() {
    runTest("switch pattern matching java");
    checkVersion("SwitchPatternOK", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
  }

  public void testSwitchPatternMatchingAspect() {
    runTest("switch pattern matching aspect");
    checkVersion("SwitchPatternAspect", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
    checkVersion("Application", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
    checkVersion("Shape", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
    checkVersion("S", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
  }

  public void testSwitchPatternMatchingPreview2Error1() {
    runTest("switch pattern matching preview 2 error 1");
  }

  public void testSwitchPatternMatchingPreview2Error2() {
    runTest("switch pattern matching preview 2 error 2");
  }

  public void testSwitchPatternMatchingPreview2Java() {
    runTest("switch pattern matching preview 2 java");
    checkVersion("SwitchPatternPreview2OK", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
  }

  public void testSwitchPatternMatchingPreview2Aspect() {
    runTest("switch pattern matching preview 2 aspect");
    checkVersion("SwitchPatternPreview2Aspect", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
    checkVersion("Application", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java18PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc199.xml");
  }

}
