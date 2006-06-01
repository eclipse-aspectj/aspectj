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


package org.aspectj.tools.ajbrowser;

import junit.framework.*;

public class AjbrowserTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(AjbrowserTests.class.getName());
        suite.addTestSuite(AjbrowserTests.class); 
        //$JUnit-BEGIN$
        //suite.addTestSuite(BrowserManagerTest.class); 
        //$JUnit-END$
        return suite;
    }

    public AjbrowserTests(String name) { super(name); }
    
	// AjBrowser is waiting for some tests - have to have a placeholder to 
	// keep JUnit happy...
	public void testNothing() {}
}  
