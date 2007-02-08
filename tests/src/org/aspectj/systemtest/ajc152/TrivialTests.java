/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc152;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * 
 */
public class TrivialTests extends XMLBasedAjcTestCase {

  public void testBasic() { runTest("basic"); }
  public void testBasic2() { runTest("basic2"); }
  public void testErrorCases() { runTest("errors"); }
  public void testMorePointcuts_1() { runTest("pointcuts - 1");}
  public void testLTW() { runTest("trivial and ltw");}
  public void testLTW_2() { runTest("trivial and ltw - 2");}
  
  // ---
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(TrivialTests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc152/trivial.xml");
  }
	
}
