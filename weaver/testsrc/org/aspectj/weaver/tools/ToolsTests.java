/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.tools;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ToolsTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.aspectj.weaver.tools");
		//$JUnit-BEGIN$
		suite.addTestSuite(PointcutParserTest.class);
		suite.addTestSuite(PointcutExpressionTest.class);
		//$JUnit-END$
		return suite;
	}
}
