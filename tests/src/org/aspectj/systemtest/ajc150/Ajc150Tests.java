/*******************************************************************************
 * Copyright (c) 2004 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * These are tests that will run on Java 1.4 and use the old harness format for test specification.
 */
public class Ajc150Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	  
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc150Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc150/ajc150.xml");
  }

  public void test_typeProcessingOrderWhenDeclareParents() {
	try {
  		runTest("Order of types passed to compiler determines weaving behavior");
	} finally {
  		System.err.println(ajc.getLastCompilationResult().getStandardError());
	}
  }
  
  public void test_ambiguousBindingsDetection() {
  	runTest("Various kinds of ambiguous bindings");
  }
  
  public void test_ambiguousArgsDetection() {
  	runTest("ambiguous args");
  }
}