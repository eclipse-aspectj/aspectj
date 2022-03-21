/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc199;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava18OrLater;

/**
 * @author Alexander Kriegisch
 */
public class Ajc199TestsJava extends XMLBasedAjcTestCaseForJava18OrLater {

  public void testDummyJava18() {
    runTest("dummy Java 18");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc199TestsJava.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc199.xml");
  }

}
