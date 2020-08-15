package org.aspectj.tests;

import org.aspectj.systemtest.AllTests18;
import org.aspectj.systemtest.AllTests19;
import org.aspectj.util.LangUtil;

/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wes Isberg       initial implementation
 * ******************************************************************/

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestsModuleTests extends TestCase {

	public static Test suite() {
		String name = TestsModuleTests.class.getName();
		TestSuite suite = new TestSuite(name);
		// compiler tests, wrapped for JUnit
		if (LangUtil.is19VMOrGreater()) {
			suite.addTest(AllTests19.suite());
		} else {
			suite.addTest(AllTests18.suite());
		}
		return suite;
	}
}
