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
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Java17PreviewFeaturesTests extends JavaVersionSpecificXMLBasedAjcTestCase {
  private static final Constants.ClassFileVersion classFileVersion = Constants.ClassFileVersion.of(17);

  public Java17PreviewFeaturesTests() {
    super(17, 17);
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

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java17PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc198.xml");
  }

}
