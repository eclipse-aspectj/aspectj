/* *******************************************************************
 * Copyright (c) 2003 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Wes Isberg     initial implementation
 * ******************************************************************/

package org.aspectj.testing.drivers;

import org.aspectj.ajdt.internal.core.builder.AjState;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * Run harness tests as JUnit test suites.
 */
public class AjcHarnessTestsUsingJUnit extends TestCase {

    /**
     * Create TestSuite with harness validation tests.
     * @return Test with all TestSuites and TestCases
     *         for the harness itself.
     */
    public static TestSuite suite() {
        TestSuite result = HarnessJUnitUtil.suite(null, null, null);
        result.addTest(
            HarnessJUnitUtil.suite("harness",
                new String[] {"../tests/ajcHarnessTests.xml"},
                null
                ));
        result.addTest(
            HarnessJUnitUtil.suite("harness selection tests",
                new String[] {"testdata/incremental/harness/selectionTest.xml"},
                null
                ));
        return result;
    }

	public AjcHarnessTestsUsingJUnit(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		AjState.FORCE_INCREMENTAL_DURING_TESTING = true;
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		AjState.FORCE_INCREMENTAL_DURING_TESTING = false;
	}
}
