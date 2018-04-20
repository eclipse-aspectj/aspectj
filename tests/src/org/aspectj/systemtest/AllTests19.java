/*******************************************************************************
 * Copyright (c) 2013, 2014 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest;

import org.aspectj.systemtest.ajc190.AllTestsAspectJ190;
import org.aspectj.systemtest.ajc191.AllTestsAspectJ191;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests19 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - 1.9");
		// $JUnit-BEGIN$ 
		suite.addTest(AllTestsAspectJ190.suite()); 
		suite.addTest(AllTestsAspectJ191.suite()); 
		suite.addTest(AllTests18.suite());
		// $JUnit-END$
		return suite;
	}
}
