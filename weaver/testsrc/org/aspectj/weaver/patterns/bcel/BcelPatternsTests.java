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

package org.aspectj.weaver.patterns.bcel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aspectj.weaver.patterns.AnnotationPatternMatchingTestCase;
import org.aspectj.weaver.patterns.AnnotationPatternTestCase;

public class BcelPatternsTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(BcelPatternsTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(BcelAndOrNotTestCase.class);
		suite.addTestSuite(BcelBindingTestCase.class);
		suite.addTestSuite(BcelWithinTestCase.class);
		suite.addTestSuite(BcelModifiersPatternTestCase.class);
		suite.addTestSuite(BcelTypePatternListTestCase.class);
		suite.addTestSuite(BcelParserTestCase.class);
		suite.addTestSuite(BcelSignaturePatternTestCase.class);
		suite.addTestSuite(BcelTypePatternTestCase.class);

		suite.addTestSuite(AnnotationPatternTestCase.class);
		suite.addTestSuite(AnnotationPatternMatchingTestCase.class);
		// $JUnit-END$
		return suite;
	}

	public BcelPatternsTests(String name) {
		super(name);
	}

}
