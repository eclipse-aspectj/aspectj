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


// default package

import junit.framework.*;

public class RuntimeModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(RuntimeModuleTests.class.getName());
        suite.addTestSuite(RuntimeModuleTests.class); // minimum 1 test (testNothing)
        return suite;
    }

    public RuntimeModuleTests(String name) { super(name); }

    public void testNothing() {}
}  
