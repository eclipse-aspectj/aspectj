/*******************************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1925;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Bugs1925Tests extends XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs1925Tests.class);
  }

  public void testGh336_ProceedCodeGenProblem() {
	  runTest("proceed code gen problem 1");
  }

  public void testGh337_ProceedCodeGenProblem() {
	  runTest("proceed code gen problem 2");
  }
  
  public void testGh350_ITDCodeGenProblem() {
	  runTest("itd code gen problem");
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1925.xml");
  }

}
