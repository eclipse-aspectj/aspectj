/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc198;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.aspectj.util.LangUtil;

/**
 * @author Alexander Kriegisch
 */
public class AllTestsAspectJ198 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.8 tests");
		if (LangUtil.is17VMOrGreater()) {
			suite.addTest(SanityTestsJava17.suite());
			suite.addTest(Ajc198TestsJava.suite());
		}
		if (LangUtil.is17VMOrGreater() && !LangUtil.is18VMOrGreater()) {
			suite.addTest(Java17PreviewFeaturesTests.suite());
		}
		return suite;
	}
}
