/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc197;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava15Only;

/**
 * @author Alexander Kriegisch
 */
public class Ajc197PreviewFeaturesTests extends XMLBasedAjcTestCaseForJava15Only {

  public void testRecords() {
    runTest("simple record");
    checkVersion("Person", Constants.MAJOR_15, Constants.PREVIEW_MINOR_VERSION);
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

  public void testSealedClassWithLegalSubclasses() {
    runTest("sealed class with legal subclasses");
  }

  public void testSealedClassWithIllegalSubclass() {
    runTest("sealed class with illegal subclass");
  }

  public void testWeaveSealedClass() {
    runTest("weave sealed class");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc197PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc197.xml");
  }

}
