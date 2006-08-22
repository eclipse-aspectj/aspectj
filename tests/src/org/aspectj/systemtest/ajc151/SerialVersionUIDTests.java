/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc151;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;


public class SerialVersionUIDTests extends XMLBasedAjcTestCase {

  public void testTheBasics() { runTest("basic"); }
  public void testTheBasicsWithLint() { runTest("basic - lint"); }
  public void testHorrible() { runTest("horrible"); }
  
  public void testAbstractClass() { runTest("abstract class");}
  
  //
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(SerialVersionUIDTests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc151/serialversionuid.xml");
  }
	
}
