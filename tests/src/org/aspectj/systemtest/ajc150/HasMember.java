/*******************************************************************************
 * Copyright (c) 2004 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Adrian Colyer - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class HasMember extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(HasMember.class);
	  }

	  protected File getSpecFile() {
	    return new File("../tests/src/org/aspectj/systemtest/ajc150/ajc150.xml");
	  }

	  public void testSimpleDecPHasMethod() {
		  runTest("declare parents : hasmethod(..) - 1");
	  }

	  public void testSimpleDecPHasMethodInherited() {
		  runTest("declare parents : hasmethod(..) - 2");
	  }	  

	  public void testSimpleDecPHasMethodInheritedPrivate() {
		  runTest("declare parents : hasmethod(..) - 3");
	  }	 
	  
	  // this test not passing yet, ITD integration not implemented
//	  public void testDecPHasMethodViaITD() {
//		  runTest("declare parents : hasmethod(..) - 4");
//	  }
	  
	  public void testSimpleDecPHasField() {
		  runTest("declare parents : hasfield(..) - 1");
	  }
	  
	  public void testSimpleDecPHasFieldInherited() {
		  runTest("declare parents : hasfield(..) - 2");
	  }

	  public void testSimpleDecPHasFieldInheritedPrivate() {
		  runTest("declare parents : hasfield(..) - 3");
	  }

}