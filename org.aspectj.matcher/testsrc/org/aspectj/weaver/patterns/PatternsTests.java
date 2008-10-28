/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import org.aspectj.weaver.patterns.AndOrNotTestCase;
import org.aspectj.weaver.patterns.ArgsTestCase;
import org.aspectj.weaver.patterns.BindingTestCase;
import org.aspectj.weaver.patterns.DeclareErrorOrWarningTestCase;
import org.aspectj.weaver.patterns.ModifiersPatternTestCase;
import org.aspectj.weaver.patterns.NamePatternParserTestCase;
import org.aspectj.weaver.patterns.NamePatternTestCase;
import org.aspectj.weaver.patterns.ParserTestCase;
import org.aspectj.weaver.patterns.PatternsTests;
import org.aspectj.weaver.patterns.PointcutRewriterTest;
import org.aspectj.weaver.patterns.SignaturePatternTestCase;
import org.aspectj.weaver.patterns.ThisOrTargetTestCase;
import org.aspectj.weaver.patterns.TypePatternListTestCase;
import org.aspectj.weaver.patterns.TypePatternTestCase;
import org.aspectj.weaver.patterns.VisitorTestCase;
import org.aspectj.weaver.patterns.WithinTestCase;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PatternsTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(PatternsTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(AndOrNotTestCase.class);
		suite.addTestSuite(BindingTestCase.class);
		suite.addTestSuite(DeclareErrorOrWarningTestCase.class);
		suite.addTestSuite(ModifiersPatternTestCase.class);
		suite.addTestSuite(NamePatternParserTestCase.class);
		suite.addTestSuite(NamePatternTestCase.class);
		suite.addTestSuite(ParserTestCase.class);
		suite.addTestSuite(SignaturePatternTestCase.class);
		suite.addTestSuite(ThisOrTargetTestCase.class);
		suite.addTestSuite(TypePatternListTestCase.class);
		suite.addTestSuite(TypePatternTestCase.class);
		suite.addTestSuite(WithinTestCase.class);
		suite.addTestSuite(ArgsTestCase.class);
		// suite.addTestSuite(AnnotationPatternTestCase.class);
		// suite.addTestSuite(AnnotationPatternMatchingTestCase.class);
		suite.addTestSuite(PointcutRewriterTest.class);
		suite.addTestSuite(VisitorTestCase.class);
		// $JUnit-END$
		return suite;
	}

	public PatternsTests(String name) {
		super(name);
	}

}
