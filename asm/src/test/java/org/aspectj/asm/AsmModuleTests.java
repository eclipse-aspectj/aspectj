package org.aspectj.asm;
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


import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AsmModuleTests extends TestCase {

    public static TestSuite suite() { 
        TestSuite suite = new TestSuite(AsmModuleTests.class.getName());
        suite.addTestSuite(NameConvertorTest.class);
        return suite;
    }

    public AsmModuleTests(String name) { super(name); }

    public void testNothing() {}
}  
