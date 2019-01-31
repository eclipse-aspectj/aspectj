/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
