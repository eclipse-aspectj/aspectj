/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.aspectj.systemtest.ajc1926;

import org.aspectj.tools.ant.taskdefs.AjcTask;
import org.aspectj.util.LangUtil;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andy Clement
 */
public class AllTestsAspectJ1926 {

	private static final int JAVA_VERSION = 26;

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.26 tests");
		suite.addTest(Bugs1926Tests.suite());
		if (LangUtil.isVMGreaterOrEqual(JAVA_VERSION)) {
			suite.addTest(SanityTestsJava26.suite());
			suite.addTest(Ajc1926TestsJava.suite());
		}
		// Do not run tests using a previous compiler's preview features anymore. They
		// would all fail.
		if (AjcTask.JAVA_VERSION_MAX == JAVA_VERSION) {
			if (LangUtil.isVMGreaterOrEqual(JAVA_VERSION) && LangUtil.isVMLessOrEqual(JAVA_VERSION)) {
				suite.addTest(Java26PreviewFeaturesTests.suite());
			}
		}
		return suite;
	}

}
