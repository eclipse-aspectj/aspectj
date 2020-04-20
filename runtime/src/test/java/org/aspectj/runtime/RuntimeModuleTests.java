package org.aspectj.runtime;
/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

import org.aspectj.internal.lang.reflect.AjTypeTest;
import org.aspectj.internal.lang.reflect.AjTypeWithAspectsTest;
import org.aspectj.internal.lang.reflect.InterTypeTest;
import org.aspectj.runtime.reflect.JoinPointImplTest;
import org.aspectj.runtime.reflect.SignatureTest;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RuntimeModuleTests extends TestCase {

	public static TestSuite suite() {
		TestSuite suite = new TestSuite(RuntimeModuleTests.class.getName());
		suite.addTestSuite(RuntimeTest.class);
		suite.addTestSuite(SignatureTest.class);
		suite.addTestSuite(JoinPointImplTest.class);
		// suite.addTestSuite(RuntimePerformanceTest.class);
		suite.addTestSuite(AjTypeTest.class);
		suite.addTestSuite(AjTypeWithAspectsTest.class);
		suite.addTestSuite(InterTypeTest.class);
		return suite;
	}

	public RuntimeModuleTests(String name) { super(name); }

}
