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


package org.aspectj.testing.harness.bridge;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestingBridgeTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(TestingBridgeTests.class.getName());
        // XXX disabled DirChangesTest pending implementation
        //$JUnit-BEGIN$
        suite.addTestSuite(AbstractRunSpecTest.class); 
        suite.addTestSuite(AjcSpecTest.class); 
        suite.addTestSuite(CompilerRunTest.class); 
        suite.addTestSuite(CompilerRunSpecTest.class); 
        suite.addTestSuite(ParseTestCase.class); 
        //$JUnit-END$
        return suite;
    }

    public TestingBridgeTests(String name) { super(name); }

}  
