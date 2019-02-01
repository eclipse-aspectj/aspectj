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
package org.aspectj.testing;


import org.aspectj.testing.server.TestServerTest;

import junit.framework.*;

public class TestingClientModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(TestingClientModuleTests.class.getName());
        suite.addTestSuite(TesterTest.class); 
        suite.addTestSuite(TestServerTest.class); 
        return suite;
    }

    public TestingClientModuleTests(String name) { super(name); }

}  
