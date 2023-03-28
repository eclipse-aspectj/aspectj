/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1920;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Bugs1920Tests extends XMLBasedAjcTestCase {

  public void testDummyJava20() {
    //runTest("dummy Java 20");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs1920Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1920.xml");
  }

}
