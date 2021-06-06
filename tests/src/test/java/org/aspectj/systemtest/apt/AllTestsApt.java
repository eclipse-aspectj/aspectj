/*******************************************************************************
 * Copyright (c) 2014 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.apt;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsApt {

	public static Test suite() {
		TestSuite suite = new TestSuite("Annotation processing tests");
		// $JUnit-BEGIN$
		suite.addTest(AptTests.suite());
		// $JUnit-END$
		return suite;
	}
}
