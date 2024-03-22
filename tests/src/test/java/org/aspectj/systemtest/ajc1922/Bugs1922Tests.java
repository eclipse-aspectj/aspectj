/*******************************************************************************
 * Copyright (c) 2024 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1922;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Bugs1922Tests extends XMLBasedAjcTestCase {

  public void testDummy() {
    //runTest("dummy");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs1922Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1922.xml");
  }

}
