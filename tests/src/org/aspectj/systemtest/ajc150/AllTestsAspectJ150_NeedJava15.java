/*******************************************************************************
 * Copyright (c) 2005 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is a superset of AllTestsAspectJ150 that includes tests that must be run on Java 1.5
 */
public class AllTestsAspectJ150_NeedJava15 {

	public static Test suite() {
		TestSuite suite = new TestSuite("Java5");
		//$JUnit-BEGIN$
		suite.addTestSuite(Ajc150TestsRequireJava15.class);
		suite.addTestSuite(Autoboxing.class);		
		
		//$JUnit-END$
		return suite;
	}
}
