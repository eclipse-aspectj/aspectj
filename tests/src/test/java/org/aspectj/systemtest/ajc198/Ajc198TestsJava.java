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
import org.aspectj.testing.XMLBasedAjcTestCaseForJava17OrLater;

/**
 * @author Alexander Kriegisch
 */
public class Ajc198TestsJava extends XMLBasedAjcTestCaseForJava17OrLater {

  public void testSealedClassWithLegalSubclasses() {
    runTest("sealed class with legal subclasses");
    checkVersion("Employee", Constants.MAJOR_17, Constants.MINOR_17);
    checkVersion("Manager", Constants.MAJOR_17, Constants.MINOR_17);
  }

  public void testSealedClassWithIllegalSubclass() {
    runTest("sealed class with illegal subclass");
    checkVersion("Person", Constants.MAJOR_17, Constants.MINOR_17);
  }

  public void testWeaveSealedClass() {
    runTest("weave sealed class");
    checkVersion("PersonAspect", Constants.MAJOR_17, Constants.MINOR_17);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc198TestsJava.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc198.xml");
  }

}
