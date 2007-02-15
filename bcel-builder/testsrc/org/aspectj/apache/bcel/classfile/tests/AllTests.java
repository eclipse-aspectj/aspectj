/* *******************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement -     initial implementation {date}
 * ******************************************************************/

package org.aspectj.apache.bcel.classfile.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.apache.bcel.classfile.tests.AnnotationAccessFlagTest;
import org.aspectj.apache.bcel.classfile.tests.AnnotationDefaultAttributeTest;
import org.aspectj.apache.bcel.classfile.tests.ElementValueGenTest;
import org.aspectj.apache.bcel.classfile.tests.EnclosingMethodAttributeTest;
import org.aspectj.apache.bcel.classfile.tests.EnumAccessFlagTest;
import org.aspectj.apache.bcel.classfile.tests.FieldAnnotationsTest;
import org.aspectj.apache.bcel.classfile.tests.GeneratingAnnotatedClassesTest;
import org.aspectj.apache.bcel.classfile.tests.LocalVariableTypeTableTest;
import org.aspectj.apache.bcel.classfile.tests.MethodAnnotationsTest;
import org.aspectj.apache.bcel.classfile.tests.RuntimeVisibleAnnotationAttributeTest;
import org.aspectj.apache.bcel.classfile.tests.RuntimeVisibleParameterAnnotationAttributeTest;
import org.aspectj.apache.bcel.classfile.tests.VarargsTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for BCEL Java5 support");
		//$JUnit-BEGIN$
		suite.addTestSuite(RuntimeVisibleParameterAnnotationAttributeTest.class);
		suite.addTestSuite(AnnotationDefaultAttributeTest.class);
		suite.addTestSuite(EnclosingMethodAttributeTest.class);
		suite.addTestSuite(MethodAnnotationsTest.class);
		suite.addTestSuite(RuntimeVisibleAnnotationAttributeTest.class);
		suite.addTestSuite(ClassloaderRepositoryTest.class);
		suite.addTestSuite(EnumAccessFlagTest.class);
		suite.addTestSuite(LocalVariableTypeTableTest.class);
		suite.addTestSuite(VarargsTest.class);
		suite.addTestSuite(AnnotationAccessFlagTest.class);
		suite.addTestSuite(ElementValueGenTest.class);
		suite.addTestSuite(FieldAnnotationsTest.class);
		suite.addTestSuite(AnnotationGenTest.class);
		suite.addTestSuite(ParameterAnnotationsTest.class);
		suite.addTestSuite(GeneratingAnnotatedClassesTest.class);
		suite.addTestSuite(TypeAnnotationsTest.class);
		suite.addTestSuite(UtilTests.class);
		suite.addTestSuite(GenericSignatureParsingTest.class);
		suite.addTestSuite(GenericsErasureTesting.class);
		suite.addTestSuite(AnonymousClassTest.class);
		//$JUnit-END$
		return suite;
	}
}