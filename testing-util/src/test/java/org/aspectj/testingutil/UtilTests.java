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
package org.aspectj.testingutil;

import junit.framework.*;

public class UtilTests extends TestCase {

    public static final String TESTING_UTIL_PATH = "../testing-util";
    public static Test suite() { 
        TestSuite suite = new TestSuite(UtilTests.class.getName());
        // for now, do not include SuiteTest because it would take 15 minutes
        //$JUnit-BEGIN$
        suite.addTestSuite(TestUtilTest.class);
        //$JUnit-END$
        return suite;
    }

    public UtilTests(String name) { super(name); }

}  

