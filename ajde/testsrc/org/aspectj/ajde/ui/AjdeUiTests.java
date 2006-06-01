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


package org.aspectj.ajde.ui;
 
import junit.framework.*;

public class AjdeUiTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(AjdeUiTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(StructureModelUtilTest.class); 
        suite.addTestSuite(StructureSearchManagerTest.class); 
        suite.addTestSuite(StructureViewManagerTest.class); 
        //$JUnit-END$
        return suite;
    }

    public AjdeUiTests(String name) { super(name); }

}  
