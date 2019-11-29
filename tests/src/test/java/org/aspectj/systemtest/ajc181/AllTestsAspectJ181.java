/*******************************************************************************
 * Copyright (c) 2014 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc181;

import org.aspectj.systemtest.apt.AptTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ181 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.8.1 tests");
		// $JUnit-BEGIN$
		suite.addTest(Ajc181Tests.suite());
		suite.addTest(AptTests.suite());
		// $JUnit-END$
		return suite;
	}
}
