/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
  private static final Constants.ClassFileVersion classFileVersion = Constants.ClassFileVersion.of(16);

  public void testSealedClassWithLegalSubclasses() {
    runTest("sealed class with legal subclasses");
    checkVersion("Employee", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
    checkVersion("Manager", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
  }

  public void testSealedClassWithIllegalSubclass() {
    runTest("sealed class with illegal subclass");
    checkVersion("Person", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
  }

  public void testWeaveSealedClass() {
    runTest("weave sealed class");
    checkVersion("PersonAspect", classFileVersion.MAJOR, classFileVersion.PREVIEW_MINOR);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java16PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc197.xml");
  }

}
