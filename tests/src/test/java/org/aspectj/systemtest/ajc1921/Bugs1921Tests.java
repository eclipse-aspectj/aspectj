/*******************************************************************************
 * Copyright (c) 2023 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1921;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Bugs1921Tests extends XMLBasedAjcTestCase {

  public void testGitHub_279() {
    runTest("same class woven concurrently in parallel-capable classloader");
  }

  public void testGitHub_285() {
    runTest("shared cache negative test");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs1921Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1921.xml");
  }

}
