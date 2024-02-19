/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1919;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.aspectj.util.LangUtil;

/**
 * @author Alexander Kriegisch
 */
public class AllTestsAspectJ1919 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.19 tests");
		suite.addTest(Bugs1919Tests.suite());
		if (LangUtil.isVMGreaterOrEqual(19)) {
			suite.addTest(SanityTestsJava19.suite());
			suite.addTest(Ajc1919TestsJava.suite());
		}
		// Do not run tests using a previous compiler's preview features anymore. They would all fail.
		/*
		if (LangUtil.isVMGreaterOrEqual(19) && !LangUtil.isVMGreaterOrEqual(20)) {
			suite.addTest(Java19PreviewFeaturesTests.suite());
		}
		*/
		return suite;
	}
}
