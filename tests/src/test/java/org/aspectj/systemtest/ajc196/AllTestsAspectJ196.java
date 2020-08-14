/*******************************************************************************
 * Copyright (c) 2020 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		return suite;
	}
}
