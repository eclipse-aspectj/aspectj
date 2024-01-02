/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc199;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.aspectj.util.LangUtil;

/**
 * @author Alexander Kriegisch
 */
public class AllTestsAspectJ199 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.9 tests");
    suite.addTest(Bugs199Tests.suite());
		if (LangUtil.is18VMOrGreater()) {
			suite.addTest(SanityTestsJava18.suite());
			suite.addTest(Ajc199TestsJava.suite());
		}
		// Do not run tests using a previous compiler's preview features anymore. They would all fail.
		/*
		if (LangUtil.is18VMOrGreater() && !LangUtil.is19VMOrGreater()) {
			suite.addTest(Java18PreviewFeaturesTests.suite());
		}
		*/
		return suite;
	}
}
