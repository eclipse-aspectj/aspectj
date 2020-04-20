/*******************************************************************************
 * Copyright (c) 2013, 2019 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest;

import org.aspectj.systemtest.ajc190.AllTestsAspectJ190;
import org.aspectj.systemtest.ajc191.AllTestsAspectJ191;
import org.aspectj.systemtest.ajc192.AllTestsAspectJ192;
import org.aspectj.systemtest.ajc193.AllTestsAspectJ193;
import org.aspectj.systemtest.ajc195.AllTestsAspectJ195;
import org.aspectj.systemtest.ajc196.AllTestsAspectJ196;

import junit.framework.Test;
import junit.framework.TestSuite;

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
		suite.addTest(AllTests18.suite());
		// $JUnit-END$
		return suite;
	}
}
