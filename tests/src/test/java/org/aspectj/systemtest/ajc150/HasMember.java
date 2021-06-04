/*******************************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Adrian Colyer - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class HasMember extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(HasMember.class);
	  }

	  protected java.net.URL getSpecFile() {
	    return getClassResource("ajc150.xml");
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
