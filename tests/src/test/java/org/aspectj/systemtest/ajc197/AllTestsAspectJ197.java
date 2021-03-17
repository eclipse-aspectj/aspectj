/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc197;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.aspectj.util.LangUtil;

/**
 * @author Alexander Kriegisch
 */
public class AllTestsAspectJ197 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.7 tests");
		if (LangUtil.is15VMOrGreater()) {
			suite.addTest(Ajc197Tests.suite());
			suite.addTest(SanityTestsJava15.suite());
		}
		if (LangUtil.is15VMOrGreater() && !LangUtil.is16VMOrGreater()) {
			suite.addTest(Ajc197PreviewFeaturesTests.suite());
		}
		return suite;
	}
}
