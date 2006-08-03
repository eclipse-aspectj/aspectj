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
 * ******************************************************************/

import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.weaver.AllTracing5Tests;
import org.aspectj.weaver.TestJava5ReflectionBasedReferenceTypeDelegate;
import org.aspectj.weaver.patterns.ArgsTestCase;
import org.aspectj.weaver.patterns.ThisOrTargetTestCase;
import org.aspectj.weaver.tools.Java15PointcutExpressionTest;
import org.aspectj.weaver.tools.PointcutExpressionTest;
import org.aspectj.weaver.tools.PointcutParserTest;
import org.aspectj.weaver.tools.TypePatternMatcherTest;

public class AllWeaver5Tests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for default package");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestJava5ReflectionBasedReferenceTypeDelegate.class);
		suite.addTestSuite(Java15PointcutExpressionTest.class);
		suite.addTestSuite(ArgsTestCase.class);
		suite.addTestSuite(ThisOrTargetTestCase.class);
		suite.addTestSuite(PointcutExpressionTest.class);
		suite.addTestSuite(PointcutParserTest.class);
		suite.addTestSuite(TypePatternMatcherTest.class);
        suite.addTest(AllTracing5Tests.suite());
		//$JUnit-END$
		return suite;
	}

}
