/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.tools;

import org.aspectj.testing.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ToolsTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(ToolsTests.class.getName());
		//$JUnit-BEGIN$
		/* FIXME maw The CLASSPATH is wrong so run them in weaver5 instead */
		if (!TestUtil.is15VMOrGreater()) {
			suite.addTestSuite(PointcutExpressionTest.class);
        } else {
            suite.addTest(TestUtil.testNamed("run from weaver5 under 1.5"));
        }
		suite.addTestSuite(PointcutParserTest.class);
		suite.addTestSuite(TypePatternMatcherTest.class);
		suite.addTestSuite(PointcutDesignatorHandlerTests.class);
		//$JUnit-END$
		return suite;
	}
}
