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


package org.aspectj.ajde;

import junit.framework.*;
 
public class AjdeTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(AjdeTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(BuildOptionsTest.class); 
        suite.addTestSuite(BuildConfigurationTests.class);
        suite.addTestSuite(StructureModelRegressionTest.class); 
        suite.addTestSuite(StructureModelTest.class); 
        suite.addTestSuite(VersionTest.class); 
		suite.addTestSuite(CompilerMessagesTest.class);
		suite.addTestSuite(AsmDeclarationsTest.class);
		suite.addTestSuite(AsmRelationshipsTest.class);
//		suite.addTestSuite(ResourceCopyTestCase.class);
		
        //$JUnit-END$
        return suite;
    }

    public AjdeTests(String name) { super(name); }

}  
