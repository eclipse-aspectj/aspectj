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
package org.aspectj.systemtest.ajc165;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ165 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.6.5 tests");
		// $JUnit-BEGIN$
		suite.addTest(Ajc165Tests.suite());
		// $JUnit-END$
		return suite;
	}
}
