/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1919;

import junit.framework.Test;
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Ajc1919TestsJava extends JavaVersionSpecificXMLBasedAjcTestCase {
  public Ajc1919TestsJava() {
    super(19);
  }

  public void testDummyJava19() {
    //runTest("dummy Java 19");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc1919TestsJava.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1919.xml");
  }

}
