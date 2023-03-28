/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1920;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.aspectj.util.LangUtil;

/**
 * @author Alexander Kriegisch
 */
public class AllTestsAspectJ1920 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.20 tests");
		suite.addTest(Bugs1920Tests.suite());
		if (LangUtil.is20VMOrGreater()) {
			suite.addTest(SanityTestsJava20.suite());
			suite.addTest(Ajc1920TestsJava.suite());
		}
		// Do not run tests using a previous compiler's preview features anymore. They would all fail.
		// TODO: Comment out the following block when upgrading JDT Core to Java 20
		if (LangUtil.is20VMOrGreater() && !LangUtil.is21VMOrGreater()) {
			suite.addTest(Java20PreviewFeaturesTests.suite());
		}
		return suite;
	}
}
