/*******************************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc164;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ164 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.6.4 tests");
		// $JUnit-BEGIN$
		suite.addTest(Ajc164Tests.suite());
		suite.addTest(DeclareMixinTests.suite());
		suite.addTest(JointpointIdTests.suite());
		// $JUnit-END$
		return suite;
	}
}
