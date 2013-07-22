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
package org.aspectj.systemtest.ajc174;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ174 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.7.4 tests");
		// $JUnit-BEGIN$
		suite.addTest(Ajc174Tests.suite());
		// $JUnit-END$
		return suite;
	}
}
