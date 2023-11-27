/*******************************************************************************
 * Copyright (c) 2013, 2019 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest;

import org.aspectj.systemtest.ajc190.AllTestsAspectJ190;
import org.aspectj.systemtest.ajc191.AllTestsAspectJ191;
import org.aspectj.systemtest.ajc1919.AllTestsAspectJ1919;
import org.aspectj.systemtest.ajc192.AllTestsAspectJ192;
import org.aspectj.systemtest.ajc1920.AllTestsAspectJ1920;
import org.aspectj.systemtest.ajc1921.AllTestsAspectJ1921;
import org.aspectj.systemtest.ajc193.AllTestsAspectJ193;
import org.aspectj.systemtest.ajc195.AllTestsAspectJ195;
import org.aspectj.systemtest.ajc196.AllTestsAspectJ196;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.aspectj.systemtest.ajc197.AllTestsAspectJ197;
import org.aspectj.systemtest.ajc198.AllTestsAspectJ198;
import org.aspectj.systemtest.ajc199.AllTestsAspectJ199;

/**
 * @author Andy Clement
 */
public class AllTests19 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - 1.9");
		// $JUnit-BEGIN$
		suite.addTest(AllTestsAspectJ190.suite());
		suite.addTest(AllTestsAspectJ191.suite());
		suite.addTest(AllTestsAspectJ192.suite());
		suite.addTest(AllTestsAspectJ193.suite());
		// there were no new tests for 1.9.4
		suite.addTest(AllTestsAspectJ195.suite());
		suite.addTest(AllTestsAspectJ196.suite());
		suite.addTest(AllTestsAspectJ197.suite());
		suite.addTest(AllTestsAspectJ198.suite());
		suite.addTest(AllTestsAspectJ199.suite());
		suite.addTest(AllTestsAspectJ1919.suite());
		suite.addTest(AllTestsAspectJ1920.suite());
		suite.addTest(AllTestsAspectJ1921.suite());
		suite.addTest(AllTests18.suite());
		// $JUnit-END$
		return suite;
	}
}
