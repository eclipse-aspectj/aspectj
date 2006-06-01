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


package org.aspectj.bridge;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BridgeTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(BridgeTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(CountingMessageHandlerTest.class); 
        suite.addTestSuite(MessageTest.class); 
        suite.addTestSuite(VersionTest.class); 
        //$JUnit-END$
        return suite;
    }

    public BridgeTests(String name) { super(name); }

}  
