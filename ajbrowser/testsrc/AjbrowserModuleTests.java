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
import org.aspectj.tools.ajbrowser.InteractiveBrowserTest;

//FIXME: obviously does nothing
public class AjbrowserModuleTests extends TestCase {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite(AjbrowserModuleTests.class.getName());
        suite.addTest(org.aspectj.tools.ajbrowser.AjbrowserTests.suite());
        //suite.addTestSuite(InteractiveBrowserTest.class);
        return suite;
    }

    public AjbrowserModuleTests(String name) { super(name); }

}  
