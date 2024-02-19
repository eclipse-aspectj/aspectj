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
package org.aspectj.systemtest.ajc190;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ190 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.0 tests");
		suite.addTest(Ajc190Tests.suite());
		suite.addTest(SanityTests19.suite());
		suite.addTest(EfficientTJPTests.suite());
		suite.addTest(ModuleTests.suite());
		suite.addTest(Annotations.suite());
		return suite;
	}
}
