/*******************************************************************************
 * Copyright (c) 2018-2019 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc195;

import org.aspectj.util.LangUtil;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ195 {
	private static final int JAVA_VERSION = 13;

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.5 tests");
		suite.addTest(Ajc195Tests.suite());
		if (LangUtil.isVMGreaterOrEqual(JAVA_VERSION)) {
			suite.addTest(SanityTestsJava13.suite());
		}
		return suite;
	}
}
