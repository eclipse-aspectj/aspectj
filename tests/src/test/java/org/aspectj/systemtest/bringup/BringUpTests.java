/*******************************************************************************
 * Copyright (c) 2004 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.bringup;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * These are tests that will run on Java 1.4 and use the old harness format for test specification.
 */
public class BringUpTests extends org.aspectj.testing.XMLBasedAjcTestCase {
	  
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(BringUpTests.class);
  }

  protected java.net.URL getSpecFile() {
	  return getClassResource("bringup.xml");
//    return new File("../tests/src/org/aspectj/systemtest/bringup/bringup.xml");
  }

  public void testEmptyClass() {
	  runTest("empty class");
  }
  
  public void testEmptyAspect() {
	  runTest("empty aspect");
  }
}