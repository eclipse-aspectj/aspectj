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
 *     Helen Hawkins  updated for bug 148190
 * ******************************************************************/
package org.aspectj.ajde;

import org.aspectj.ajde.internal.AspectJBuildManagerTest;
import org.aspectj.ajde.internal.LstBuildConfigManagerTest;
import org.aspectj.ajde.ui.StructureSearchManagerTest;
import org.aspectj.ajde.ui.StructureViewManagerTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
 
public class AjdeTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(AjdeTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(SymbolFileGenerationTest.class);
		suite.addTestSuite(ExtensionTests.class);
		suite.addTestSuite(AspectJBuildManagerTest.class); 
        suite.addTestSuite(LstBuildConfigManagerTest.class);
        suite.addTestSuite(StructureSearchManagerTest.class); 
        suite.addTestSuite(StructureViewManagerTest.class); 
        suite.addTestSuite(AjdeCompilerTests.class);
		
        //$JUnit-END$
        return suite;
    }

    public AjdeTests(String name) { super(name); }

}  
