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
package org.aspectj.systemtest.ajc153;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ153 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.5.3 tests");
		//$JUnit-BEGIN$
		suite.addTest(Ajc153Tests.suite());
		suite.addTest(JDTLikeHandleProviderTests.suite());
		suite.addTest(PipeliningTests.suite());
		suite.addTest(LTWServer153Tests.suite());
        //$JUnit-END$
		return suite;
	}
}
