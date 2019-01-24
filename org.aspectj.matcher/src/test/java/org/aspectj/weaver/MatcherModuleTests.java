/* *******************************************************************
 * Copyright (c) 2002-2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

// default package
package org.aspectj.weaver;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aspectj.matcher.tools.ReflectionWorldPointcutExpressionTests;
import org.aspectj.weaver.patterns.PatternsTests;
import org.aspectj.weaver.reflect.ReflectionWorldBasicTest;
import org.aspectj.weaver.reflect.ReflectionWorldSpecificTest;

public class MatcherModuleTests extends TestCase {

	public MatcherModuleTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(MatcherModuleTests.class.getName());
		suite.addTestSuite(ReflectionWorldSpecificTest.class);
		suite.addTestSuite(ReflectionWorldBasicTest.class);
		suite.addTestSuite(ReflectionWorldPointcutExpressionTests.class);
		suite.addTestSuite(TypeFactoryTests.class);
		suite.addTest(PatternsTests.suite());
		return suite;
	}
}
