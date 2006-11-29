package org.aspectj.tests;
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

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.systemtest.AllTests;
import org.aspectj.systemtest.AllTests14;
import org.aspectj.systemtest.AllTests16;
import org.aspectj.util.LangUtil;

public class TestsModuleTests extends TestCase {

    public static Test suite() {
        String name = TestsModuleTests.class.getName();
        TestSuite suite = new TestSuite(name);
        // compiler tests, wrapped for JUnit
        if (LangUtil.is15VMOrGreater()) {
//            suite.addTest(AllTests15.suite());
            suite.addTest(AllTests16.suite()); // there are currently (28/11/06) no tests specific to a 1.6 vm - so we can do this
        } else if (LangUtil.is14VMOrGreater()) {
            System.err.println("Skipping tests for 1.5");
            //suite.addTest(TestUtil.skipTest("for 1.5"));
            suite.addTest(AllTests14.suite());            
        } else {
            System.err.println("Skipping tests for 1.4 and 1.5");
            //suite.addTest(TestUtil.skipTest("for 1.4 and 1.5"));
	        suite.addTest(AllTests.suite());            
        }
        return suite;
    }
}
