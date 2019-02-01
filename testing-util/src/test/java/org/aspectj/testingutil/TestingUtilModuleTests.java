package org.aspectj.testingutil;
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


// default package

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestingUtilModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(TestingUtilModuleTests.class.getName());
        suite.addTestSuite(TestUtilTest.class);
        return suite;
    }

    public TestingUtilModuleTests(String name) { super(name); }
}  
