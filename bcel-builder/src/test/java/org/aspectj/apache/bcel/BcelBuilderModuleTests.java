/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.apache.bcel;

import org.aspectj.apache.bcel.classfile.tests.AnnotationAccessFlagTest;
import org.aspectj.apache.bcel.classfile.tests.AnnotationDefaultAttributeTest;
import org.aspectj.apache.bcel.classfile.tests.AnnotationGenTest;
import org.aspectj.apache.bcel.classfile.tests.AnonymousClassTest;
import org.aspectj.apache.bcel.classfile.tests.ClassloaderRepositoryTest;
import org.aspectj.apache.bcel.classfile.tests.ConstantPoolToStringTest;
import org.aspectj.apache.bcel.classfile.tests.ElementValueGenTest;
import org.aspectj.apache.bcel.classfile.tests.EnclosingMethodAttributeTest;
import org.aspectj.apache.bcel.classfile.tests.EnumAccessFlagTest;
import org.aspectj.apache.bcel.classfile.tests.FieldAnnotationsTest;
import org.aspectj.apache.bcel.classfile.tests.Fundamentals;
import org.aspectj.apache.bcel.classfile.tests.GeneratingAnnotatedClassesTest;
import org.aspectj.apache.bcel.classfile.tests.GenericSignatureParsingTest;
import org.aspectj.apache.bcel.classfile.tests.GenericsErasureTesting;
import org.aspectj.apache.bcel.classfile.tests.GetReflectMembersTest;
import org.aspectj.apache.bcel.classfile.tests.LineNumberTableConcurrencyTest;
import org.aspectj.apache.bcel.classfile.tests.LocalVariableTableConcurrencyTest;
import org.aspectj.apache.bcel.classfile.tests.LocalVariableTypeTableTest;
import org.aspectj.apache.bcel.classfile.tests.MethodAnnotationsTest;
import org.aspectj.apache.bcel.classfile.tests.MethodParametersTest;
import org.aspectj.apache.bcel.classfile.tests.ModuleTest;
import org.aspectj.apache.bcel.classfile.tests.NonCachingClassLoaderRepositoryTest;
import org.aspectj.apache.bcel.classfile.tests.ParameterAnnotationsTest;
import org.aspectj.apache.bcel.classfile.tests.RuntimeVisibleAnnotationAttributeTest;
import org.aspectj.apache.bcel.classfile.tests.RuntimeVisibleParameterAnnotationAttributeTest;
import org.aspectj.apache.bcel.classfile.tests.TypeAnnotationsTest;
import org.aspectj.apache.bcel.classfile.tests.UtilTests;
import org.aspectj.apache.bcel.classfile.tests.VarargsTest;
import org.aspectj.apache.bcel.util.ClassPathTests;
import org.aspectj.apache.bcel.util.SoftHashMapTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * All the tests of the bcel-builder module, so that RunTheseBeforeYouCommitTests can run them. Maven runs the module's tests by
 * itself and does not run this class (surefire excludes the *ModuleTests classes), so every test class needs to be listed here.
 */
public class BcelBuilderModuleTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(BcelBuilderModuleTests.class.getName());
		suite.addTestSuite(AnnotationAccessFlagTest.class);
		suite.addTestSuite(AnnotationDefaultAttributeTest.class);
		suite.addTestSuite(AnnotationGenTest.class);
		suite.addTestSuite(AnonymousClassTest.class);
		suite.addTestSuite(ClassloaderRepositoryTest.class);
		suite.addTestSuite(ConstantPoolToStringTest.class);
		suite.addTestSuite(ElementValueGenTest.class);
		suite.addTestSuite(EnclosingMethodAttributeTest.class);
		suite.addTestSuite(EnumAccessFlagTest.class);
		suite.addTestSuite(FieldAnnotationsTest.class);
		suite.addTestSuite(Fundamentals.class);
		suite.addTestSuite(GeneratingAnnotatedClassesTest.class);
		suite.addTestSuite(GenericSignatureParsingTest.class);
		suite.addTestSuite(GenericsErasureTesting.class);
		suite.addTestSuite(GetReflectMembersTest.class);
		suite.addTestSuite(LineNumberTableConcurrencyTest.class);
		suite.addTestSuite(LocalVariableTableConcurrencyTest.class);
		suite.addTestSuite(LocalVariableTypeTableTest.class);
		suite.addTestSuite(MethodAnnotationsTest.class);
		suite.addTestSuite(MethodParametersTest.class);
		suite.addTestSuite(ModuleTest.class);
		suite.addTestSuite(NonCachingClassLoaderRepositoryTest.class);
		suite.addTestSuite(ParameterAnnotationsTest.class);
		suite.addTestSuite(RuntimeVisibleAnnotationAttributeTest.class);
		suite.addTestSuite(RuntimeVisibleParameterAnnotationAttributeTest.class);
		suite.addTestSuite(TypeAnnotationsTest.class);
		suite.addTestSuite(UtilTests.class);
		suite.addTestSuite(VarargsTest.class);
		suite.addTestSuite(ClassPathTests.class);
		suite.addTestSuite(SoftHashMapTest.class);
		return suite;
	}

	public BcelBuilderModuleTests(String name) {
		super(name);
	}
}
