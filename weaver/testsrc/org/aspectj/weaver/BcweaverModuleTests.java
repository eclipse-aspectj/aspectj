package org.aspectj.weaver;
/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

// default package

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aspectj.weaver.tools.ToolsTests;

public class BcweaverModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(BcweaverModuleTests.class.getName());
        suite.addTest(org.aspectj.weaver.bcel.BcelTests.suite()); 
        suite.addTest(org.aspectj.weaver.BcweaverTests.suite()); 
		suite.addTest(org.aspectj.weaver.patterns.PatternsTests.suite()); 
        suite.addTestSuite(LocaleTest.class);
        suite.addTest(ToolsTests.suite());
        return suite;
    }

    public BcweaverModuleTests(String name) { super(name); }

}  
