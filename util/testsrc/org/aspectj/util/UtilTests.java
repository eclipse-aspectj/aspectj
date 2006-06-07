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


package org.aspectj.util;

import junit.framework.*;

public class UtilTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(UtilTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(FileUtilTest.class); 
        suite.addTestSuite(LangUtilTest.class); 
        suite.addTestSuite(NameConvertorTest.class);
        //$JUnit-END$
        return suite;
    }

    public UtilTests(String name) { super(name); }

}  
