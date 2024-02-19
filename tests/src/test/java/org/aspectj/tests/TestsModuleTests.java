package org.aspectj.tests;

import org.aspectj.systemtest.AllTests18;
import org.aspectj.systemtest.AllTests19;
import org.aspectj.util.LangUtil;

/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
		if (LangUtil.isVMGreaterOrEqual(9)) {
			suite.addTest(AllTests19.suite());
		} else {
			suite.addTest(AllTests18.suite());
		}
		return suite;
	}
}
