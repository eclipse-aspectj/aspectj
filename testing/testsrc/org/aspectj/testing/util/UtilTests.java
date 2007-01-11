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


package org.aspectj.testing.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UtilTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(UtilTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(BridgeUtilTest.class); 
        suite.addTestSuite(FileUtilTest.class); 
        suite.addTestSuite(IteratorWrapperTest.class); 
        suite.addTestSuite(LangUtilTest.class); 
        suite.addTestSuite(MessageUtilTest.class); 
        suite.addTestSuite(StreamGrabberTest.class); 
        suite.addTestSuite(StructureModelUtilTest.class);
        //$JUnit-END$
        return suite;
    }

    public UtilTests(String name) { super(name); }

}  
