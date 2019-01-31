package org.aspectj.bridge;
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

import org.aspectj.bridge.context.CompilationAndWeavingContextTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BridgeModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(BridgeModuleTests.class.getName());
        suite.addTestSuite(CountingMessageHandlerTest.class); 
        suite.addTestSuite(MessageTest.class); 
        suite.addTestSuite(VersionTest.class); 
        suite.addTestSuite(CompilationAndWeavingContextTest.class);
        return suite;
    }

    public BridgeModuleTests(String name) { super(name); }

}  
