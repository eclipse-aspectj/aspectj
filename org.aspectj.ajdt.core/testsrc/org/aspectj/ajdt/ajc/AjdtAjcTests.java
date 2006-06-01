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
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.ajc;

import java.io.File;

import junit.framework.*;

public class AjdtAjcTests extends TestCase {

    public static final String TESTDATA_PATH 
                                = "../org.aspectj.ajdt.core/testdata";
    public static final File TESTDATA_DIR = new File(TESTDATA_PATH);

    public static Test suite() { 
        TestSuite suite = new TestSuite(AjdtAjcTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(AjdtCommandTestCase.class); 
        suite.addTestSuite(BuildArgParserTestCase.class); 
        suite.addTestSuite(ConsoleMessageHandlerTestCase.class); 
        //$JUnit-END$
        return suite;  
    }

    public AjdtAjcTests(String name) { super(name); }

}  
