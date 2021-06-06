/* *******************************************************************
 * Copyright (c) 2002-2008 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/
package org.aspectj.matcher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aspectj.matcher.tools.ReflectionWorldPointcutExpressionTest;
import org.aspectj.weaver.TypeFactoryTest;
import org.aspectj.weaver.patterns.*;
import org.aspectj.weaver.reflect.ReflectionWorldBasicTest;
import org.aspectj.weaver.reflect.ReflectionWorldSpecificTest;

public class MatcherModuleTests extends TestCase {

	public MatcherModuleTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(MatcherModuleTests.class.getName());
		suite.addTestSuite(ReflectionWorldPointcutExpressionTest.class);
		suite.addTestSuite(TypeFactoryTest.class);
		suite.addTestSuite(ThisOrTargetTestCase.class);
		suite.addTestSuite(ParserTestCase.class);
		suite.addTestSuite(SignaturePatternTestCase.class);
		suite.addTestSuite(DeclareErrorOrWarningTestCase.class);
		suite.addTestSuite(AndOrNotTestCase.class);
		suite.addTestSuite(NamePatternParserTestCase.class);
		suite.addTestSuite(WithinTestCase.class);
		suite.addTestSuite(ArgsTestCase.class);
		suite.addTestSuite(SignaturePatternMatchSpeedTestCase.class);
		suite.addTestSuite(PointcutRewriterTest.class);
		suite.addTestSuite(NamePatternTestCase.class);
		suite.addTestSuite(VisitorTestCase.class);
		suite.addTestSuite(TypePatternTestCase.class);
		suite.addTestSuite(SimpleScopeTest.class);
		suite.addTestSuite(BindingTestCase.class);
		suite.addTestSuite(ModifiersPatternTestCase.class);
		suite.addTestSuite(TypePatternListTestCase.class);

		suite.addTestSuite(ReflectionWorldSpecificTest.class);
		suite.addTestSuite(ReflectionWorldBasicTest.class);
		return suite;
	}
}
