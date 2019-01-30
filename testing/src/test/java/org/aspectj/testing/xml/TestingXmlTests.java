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


package org.aspectj.testing.xml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestingXmlTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(TestingXmlTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(AjcSpecXmlReaderTest.class); 
        suite.addTestSuite(MessageListXmlReaderTest.class); 
        suite.addTestSuite(XMLWriterTest.class); 
        //$JUnit-END$
        return suite;
    }

    public TestingXmlTests(String name) { super(name); }

}  
