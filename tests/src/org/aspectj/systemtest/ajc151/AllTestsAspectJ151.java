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
package org.aspectj.systemtest.ajc151;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ151 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.5.1 tests");
		//$JUnit-BEGIN$
		suite.addTest(Ajc151Tests.suite());
		suite.addTest(NewarrayJoinpointTests.suite());
		suite.addTest(AtAroundTests.suite());
		suite.addTest(SerialVersionUIDTests.suite());
        //$JUnit-END$
		return suite;
	}
}
