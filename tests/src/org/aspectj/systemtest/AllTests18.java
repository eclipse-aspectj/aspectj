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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.systemtest.ajc180.AllTestsAspectJ180;
import org.aspectj.systemtest.ajc181.AllTestsAspectJ181;
import org.aspectj.systemtest.ajc182.AllTestsAspectJ182; 
import org.aspectj.systemtest.ajc183.AllTestsAspectJ183;
import org.aspectj.systemtest.ajc184.AllTestsAspectJ184;
import org.aspectj.systemtest.ajc185.AllTestsAspectJ185;
import org.aspectj.systemtest.ajc186.AllTestsAspectJ186;
import org.aspectj.systemtest.ajc187.AllTestsAspectJ187;
import org.aspectj.systemtest.ajc188.AllTestsAspectJ188;

public class AllTests18 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - 1.8");
		// $JUnit-BEGIN$ 
		suite.addTest(AllTestsAspectJ188.suite()); 
		suite.addTest(AllTestsAspectJ187.suite()); 
		suite.addTest(AllTestsAspectJ186.suite()); 
		suite.addTest(AllTestsAspectJ185.suite()); 
		suite.addTest(AllTestsAspectJ184.suite()); 
		suite.addTest(AllTestsAspectJ183.suite()); 
		suite.addTest(AllTestsAspectJ182.suite()); 
		suite.addTest(AllTestsAspectJ181.suite()); 
		suite.addTest(AllTestsAspectJ180.suite()); 
		suite.addTest(AllTests17.suite());
//		suite.addTest(AllTests16.suite());
//		suite.addTest(AllTests15.suite());
		// $JUnit-END$
		return suite;
	}
}
