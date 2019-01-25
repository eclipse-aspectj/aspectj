//package org.aspectj.weaver;
//
///* *******************************************************************
// * Copyright (c) 2005 Contributors.
// * All rights reserved. 
// * This program and the accompanying materials are made available 
// * under the terms of the Eclipse Public License v1.0 
// * which accompanies this distribution and is available at 
// * http://eclipse.org/legal/epl-v10.html 
// *  
// * Contributors: 
// *   Adrian Colyer			Initial implementation
// * ******************************************************************/
//import junit.framework.Test;
//import junit.framework.TestCase;
//import junit.framework.TestSuite;
//
//import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXTestCase;
//import org.aspectj.weaver.bcel.BcelWorldReferenceTypeTest;
//import org.aspectj.weaver.patterns.WildTypePatternResolutionTestCase;
//import org.aspectj.weaver.tools.Java15PointcutExpressionTest;
//
//public class BcweaverModule15Test extends TestCase {
//	public static Test suite() {
//		TestSuite suite = new TestSuite(BcweaverModule15Test.class.getName());
//		suite.addTestSuite(BcelGenericSignatureToTypeXTestCase.class);
//		suite.addTestSuite(BoundedReferenceTypeTestCase.class);
//		suite.addTest(Java15PointcutExpressionTest.suite());
//		suite.addTestSuite(Member15Test.class);
//		suite.addTestSuite(BcelWorldReferenceTypeTest.class);
//		suite.addTest(Java5ReflectionBasedReferenceTypeDelegateTest.suite());
//		suite.addTestSuite(TypeVariableTestCase.class);
//		suite.addTestSuite(TypeVariableReferenceTypeTestCase.class);
//		suite.addTestSuite(WildTypePatternResolutionTestCase.class);
//		return suite;
//	}
//
//	public BcweaverModule15Test(String name) {
//		super(name);
//	}
//}
