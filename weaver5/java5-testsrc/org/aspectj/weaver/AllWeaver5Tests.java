/* *******************************************************************
 * Copyright (c) 2005-2006 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer          Initial implementation
 *   Matthew Webster        Move from default package
 * ******************************************************************/
package org.aspectj.weaver;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.weaver.reflect.ReflectionWorldTest;
import org.aspectj.weaver.tools.PointcutExpressionTest;

public class AllWeaver5Tests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllWeaver5Tests.class.getName());
		//$JUnit-BEGIN$
        suite.addTest(AllTracing5Tests.suite());
        suite.addTest(BcweaverModuleTests15.suite());
		suite.addTestSuite(PointcutExpressionTest.class);
		suite.addTestSuite(ReflectionWorldTest.class);
		//$JUnit-END$
		return suite;
	}

}
