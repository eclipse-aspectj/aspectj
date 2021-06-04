/*******************************************************************************
 * Copyright (c) 2006 IBM
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc160;


import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ160 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.6.0 tests");
		//$JUnit-BEGIN$
		suite.addTest(ParameterAnnotationMatchingTests.suite());
		suite.addTest(AnnotationValueMatchingTests.suite());
		suite.addTest(SanityTests.suite());
		suite.addTest(NewFeatures.suite());
		suite.addTest(Ajc160Tests.suite());
        //$JUnit-END$
		return suite;
	}
}
