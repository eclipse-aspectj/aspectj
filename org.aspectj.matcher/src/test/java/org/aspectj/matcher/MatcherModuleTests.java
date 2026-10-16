/* *******************************************************************
 * Copyright (c) 2002-2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 * ******************************************************************/
package org.aspectj.matcher;

import org.aspectj.matcher.tools.ReflectionWorldPointcutExpressionTest;
import org.aspectj.weaver.AjAttributeReadLimitsTest;
import org.aspectj.weaver.TypeFactoryTest;
import org.aspectj.weaver.patterns.AndOrNotTestCase;
import org.aspectj.weaver.patterns.ArgsTestCase;
import org.aspectj.weaver.patterns.BindingTestCase;
import org.aspectj.weaver.patterns.DeclareErrorOrWarningTestCase;
import org.aspectj.weaver.patterns.ModifiersPatternTestCase;
import org.aspectj.weaver.patterns.NamePatternParserTestCase;
import org.aspectj.weaver.patterns.NamePatternTestCase;
import org.aspectj.weaver.patterns.ParserTestCase;
import org.aspectj.weaver.patterns.PatternDeserializationLimitsTest;
import org.aspectj.weaver.patterns.PointcutRewriterTest;
import org.aspectj.weaver.patterns.SignaturePatternMatchSpeedTestCase;
import org.aspectj.weaver.patterns.SignaturePatternTestCase;
import org.aspectj.weaver.patterns.SimpleScopeTest;
import org.aspectj.weaver.patterns.ThisOrTargetTestCase;
import org.aspectj.weaver.patterns.TypePatternListTestCase;
import org.aspectj.weaver.patterns.TypePatternTestCase;
import org.aspectj.weaver.patterns.VisitorTestCase;
import org.aspectj.weaver.patterns.WithinTestCase;
import org.aspectj.weaver.reflect.ReflectionWorldBasicTest;
import org.aspectj.weaver.reflect.ReflectionWorldSpecificTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
		suite.addTestSuite(PatternDeserializationLimitsTest.class);
		suite.addTestSuite(AjAttributeReadLimitsTest.class);

		suite.addTestSuite(ReflectionWorldSpecificTest.class);
		suite.addTestSuite(ReflectionWorldBasicTest.class);
		return suite;
	}
}
