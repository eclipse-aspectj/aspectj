/*******************************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1923;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.aspectj.tools.ant.taskdefs.AjcTask;
import org.aspectj.util.LangUtil;

/**
 * @author Alexander Kriegisch
 * @author Andy Clement
 */
public class AllTestsAspectJ1923 {

	private static final int JAVA_VERSION = 23;

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.23 tests");
		suite.addTest(Bugs1923Tests.suite());
		if (LangUtil.isVMGreaterOrEqual(JAVA_VERSION)) {
			suite.addTest(SanityTestsJava23.suite());
			suite.addTest(Ajc1923TestsJava.suite());
		}
		// Do not run tests using a previous compiler's preview features anymore. They
		// would all fail.
		if (AjcTask.JAVA_VERSION_MAX == JAVA_VERSION) {
			if (LangUtil.isVMGreaterOrEqual(JAVA_VERSION) && LangUtil.isVMLessOrEqual(JAVA_VERSION)) {
				suite.addTest(Java23PreviewFeaturesTests.suite());
			}
		}
		return suite;
	}

}
