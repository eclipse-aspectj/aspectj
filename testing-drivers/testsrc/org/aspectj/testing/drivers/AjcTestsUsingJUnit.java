/* *******************************************************************
 * Copyright (c) 2003 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.drivers;

import junit.framework.*;

/**
 * Run ajc tests as JUnit test suites.
 * This class is named to avoid automatic inclusion in 
 * most JUnit test runs.
 */
public class AjcTestsUsingJUnit extends TestCase {
    private static final String[] SUITES = new String[]
        { "../tests/ajcTestsFailing.xml",
         "../tests/ajcTests.xml"
        };

    private static final String SKIPS = 
        "-ajctestSkipKeywords=purejava,knownLimitation";
    private static final String[][] OPTIONS = new String[][]
        { new String[] { SKIPS },
          new String[] { SKIPS, "-emacssym" }
        };
     
    /**
     * Create TestSuite with all SUITES running all OPTIONS.
     * @return Test with all TestSuites and TestCases
     *         specified in SUITES and OPTIONS.
     */
    public static Test suite() {
        String name = AjcTestsUsingJUnit.class.getName();
        return HarnessJUnitUtil.suite(name, SUITES, OPTIONS);
    }
        
	public AjcTestsUsingJUnit(String name) {
		super(name);
	}
}
