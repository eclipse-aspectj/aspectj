/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/
 package org.aspectj.tools.ajdoc;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Mik Kersten
 */
public class AjdocModuleTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.aspectj.tools.ajdoc");
		//$JUnit-BEGIN$
		suite.addTestSuite(SpacewarTestCase.class);
		suite.addTestSuite(ExecutionTestCase.class);
		suite.addTestSuite(MainTestCase.class); // !!! must be last because it exists
		//$JUnit-END$
		return suite;
	}
}
