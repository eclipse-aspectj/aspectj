/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.testing.drivers;

import junit.framework.*;

public class DriversTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(DriversTests.class.getName());
        // for now, do not include SuiteTest because it would take 15 minutes
        //$JUnit-BEGIN$
        suite.addTestSuite(HarnessSelectionTest.class); 
        //$JUnit-END$
        return suite;
    }

    public DriversTests(String name) { super(name); }

}  
