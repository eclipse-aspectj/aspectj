/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import junit.framework.*;

public class BcweaverTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(BcweaverTests.class.getName());
        // abstract
        //suite.addTestSuite(AbstractWorldTestCase.class); 
        //$JUnit-BEGIN$
        suite.addTestSuite(MemberTestCase.class); 
        suite.addTestSuite(TypeXTestCase.class); 
        //$JUnit-END$
        return suite;
    }

    public BcweaverTests(String name) { super(name); }

}  
