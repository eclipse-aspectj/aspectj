/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc196;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava14Only;

import junit.framework.Test;

/**
 * @author Alexander Kriegisch
 */
public class Java14PreviewFeaturesTests extends XMLBasedAjcTestCaseForJava14Only {

  public void testRecords() {
    runTest("simple record");
    checkVersion("Person", Constants.MAJOR_14, Constants.PREVIEW_MINOR_VERSION);
  }

  public void testRecords2() {
    runTest("using a record");
  }

  public void testInstanceofPatterns() {
    runTest("instanceof patterns");
  }

  public void testAdvisingRecords() {
    runTest("advising records");
  }

  public void testTextBlock1() {
    runTest("textblock 1");
  }

  public void testTextBlock2() {
    runTest("textblock 2");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java14PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc196.xml");
  }

}
