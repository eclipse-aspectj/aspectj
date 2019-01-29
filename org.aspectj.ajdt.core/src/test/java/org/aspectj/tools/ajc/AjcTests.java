///* *******************************************************************
// * Copyright (c) 2005 Contributors.
// * All rights reserved. 
// * This program and the accompanying materials are made available 
// * under the terms of the Eclipse Public License v1.0 
// * which accompanies this distribution and is available at 
// * http://eclipse.org/legal/epl-v10.html 
// *  
// * Contributors: 
// *     Wes Isberg       initial implementation 
// * ******************************************************************/
//
//
//package org.aspectj.tools.ajc;
//
//import org.aspectj.testing.util.TestUtil;
//
//import junit.framework.Test;
//import junit.framework.TestCase;
//import junit.framework.TestSuite;
//
//public class AjcTests extends TestCase {
//    public static String aspectjrtClasspath() {
//        return TestUtil.aspectjrtPath().getPath();        
//    }
//    public static Test suite() { 
//        TestSuite suite = new TestSuite(AjcTests.class.getName());
//        suite.addTestSuite(org.aspectj.tools.ajc.MainTest.class);
//        suite.addTestSuite(ASTVisitorTest.class);
//        suite.addTestSuite(ASTitdTest.class);
//        suite.addTestSuite(AjASTTest.class);
//        suite.addTestSuite(AjNaiveASTFlattenerTest.class);
//        return suite;
//    }
//
//}
