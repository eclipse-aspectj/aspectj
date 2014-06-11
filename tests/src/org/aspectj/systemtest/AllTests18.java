/*******************************************************************************
 * Copyright (c) 2013 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.systemtest.ajc180.AllTestsAspectJ180;
import org.aspectj.systemtest.ajc181.AllTestsAspectJ181;

public class AllTests18 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - 1.8");
		// $JUnit-BEGIN$ 
		suite.addTest(AllTestsAspectJ181.suite()); 
		suite.addTest(AllTestsAspectJ180.suite()); 
		suite.addTest(AllTests17.suite());
//		suite.addTest(AllTests16.suite());
//		suite.addTest(AllTests15.suite());
		// $JUnit-END$
		return suite;
	}
}
