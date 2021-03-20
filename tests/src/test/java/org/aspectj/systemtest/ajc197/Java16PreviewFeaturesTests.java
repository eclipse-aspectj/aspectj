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
import org.aspectj.testing.XMLBasedAjcTestCaseForJava16Only;

/**
 * @author Alexander Kriegisch
 */
public class Java16PreviewFeaturesTests extends XMLBasedAjcTestCaseForJava16Only {

  public void testSealedClassWithLegalSubclasses() {
    runTest("sealed class with legal subclasses");
    checkVersion("Employee", Constants.MAJOR_16, Constants.PREVIEW_MINOR_VERSION);
    checkVersion("Manager", Constants.MAJOR_16, Constants.PREVIEW_MINOR_VERSION);
  }

  public void testSealedClassWithIllegalSubclass() {
    runTest("sealed class with illegal subclass");
    checkVersion("Person", Constants.MAJOR_16, Constants.PREVIEW_MINOR_VERSION);
  }

  public void testWeaveSealedClass() {
    runTest("weave sealed class");
    checkVersion("PersonAspect", Constants.MAJOR_16, Constants.PREVIEW_MINOR_VERSION);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java16PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc197.xml");
  }

}
