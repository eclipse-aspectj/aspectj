/*******************************************************************************
 * Copyright (c) 2020 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc196;

import org.aspectj.util.LangUtil;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andy Clement
 */
public class AllTestsAspectJ196 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.6 tests");
		if (LangUtil.is14VMOrGreater()) {
			suite.addTest(Ajc196Tests.suite());
			suite.addTest(SanityTestsJava14.suite());
		}
		// Do not run tests using a previous compiler's preview features anymore. They would all fail.
/*
		if (LangUtil.is14VMOrGreater() && !LangUtil.is15VMOrGreater()) {
			suite.addTest(Ajc196PreviewFeaturesTests.suite());
		}
*/
		return suite;
	}
}
