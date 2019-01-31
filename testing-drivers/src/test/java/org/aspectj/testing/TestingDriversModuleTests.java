package org.aspectj.testing;
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

import org.aspectj.testing.drivers.DriversTests;

import junit.framework.*;
import junit.framework.Test;

/**
 * TODO weaver dumping ajcore files when using this interface
 */
public class TestingDriversModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(TestingDriversModuleTests.class.getName());
        suite.addTest(DriversTests.suite()); 
        return suite;
    }

    public TestingDriversModuleTests(String name) { super(name); }

}  
