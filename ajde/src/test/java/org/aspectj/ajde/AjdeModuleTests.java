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
package org.aspectj.ajde;

import org.aspectj.ajde.internal.AspectJBuildManagerTest;
import org.aspectj.ajde.internal.LstBuildConfigManagerTest;
import org.aspectj.ajde.ui.StructureSearchManagerTest;
import org.aspectj.ajde.ui.StructureViewManagerTest;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AjdeModuleTests extends TestCase {

    public static TestSuite suite() { 
        TestSuite suite = new TestSuite(AjdeModuleTests.class.getName());
        suite.addTestSuite(SymbolFileGenerationTest.class);
		suite.addTestSuite(ExtensionTest.class);
		suite.addTestSuite(AspectJBuildManagerTest.class); 
        suite.addTestSuite(LstBuildConfigManagerTest.class);
        suite.addTestSuite(StructureSearchManagerTest.class); 
        suite.addTestSuite(StructureViewManagerTest.class); 
        suite.addTestSuite(AjdeCompilerTest.class);
        return suite;
    }

    public AjdeModuleTests(String name) { super(name); }

}  
