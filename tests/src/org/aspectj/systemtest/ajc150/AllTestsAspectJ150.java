/*******************************************************************************
 * Copyright (c) 2004 IBM 
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
 * This pulls together tests we have written for AspectJ 1.5.0 that don't need Java 1.5 to run
 */
public class AllTestsAspectJ150 {

	public static Test suite() {
		TestSuite suite = new TestSuite("Java5 - binary weaving");
		//$JUnit-BEGIN$
		suite.addTestSuite(MigrationTests.class);
		suite.addTest(Ajc150Tests.suite());
		suite.addTestSuite(Ajc150TestsNoHarness.class); 
		
		// These are binary weaving tests
		suite.addTest(AccBridgeMethods.suite());
		suite.addTestSuite(CovarianceTests.class);
		suite.addTestSuite(Enums.class);
		suite.addTestSuite(Annotations.class);
		suite.addTestSuite(AnnotationPointcutsTests.class);
		suite.addTestSuite(VarargsTests.class);
		suite.addTestSuite(AnnotationRuntimeTests.class);
		suite.addTestSuite(PerTypeWithinTests.class);
		
		
		//$JUnit-END$
		return suite;
	}
}
