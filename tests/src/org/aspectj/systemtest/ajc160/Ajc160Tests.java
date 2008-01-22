/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc160;

import java.io.File;

import org.aspectj.testing.XMLBasedAjcTestCase;
import junit.framework.Test;

/**
 * These are tests for AspectJ1.6.0
 */
public class Ajc160Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	public void testGenericsSuperITD_pr206911() { runTest("generics super itd"); }
	public void testGenericsSuperITD_pr206911_2() { runTest("generics super itd - 2"); }

  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc160Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc160/ajc160.xml");
  }
  
}