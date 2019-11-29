/*******************************************************************************
 * Copyright (c) 2016 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc1810;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc1810Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testBinding_508661() {
		runTest("various ltw");
	}
	
	public void testBinding_500035() {
		runTest("ataspectj binding");
	}

	public void testBinding_500035_2() {
		runTest("ataspectj binding 2");
	}

	public void testBinding_500035_3() {
		runTest("ataspectj binding 3 -XnoInline");
	}

	public void testBinding_500035_4() {
		runTest("ataspectj binding 4");
	}

	public void testGenericsException_501656() {
		runTest("generics exception");
	}
	
	public void testAIOOBE_502807() {
		runTest("unexpected aioobe");
	}
	
	public void testInvokeDynamic_490315() {
		runTest("indy");
	}

	public void testAmbigMessage17() throws Exception {
		runTest("ambiguous message - 17");
	}

	public void testAmbigMessage18() throws Exception {
		runTest("ambiguous message - 18");
	}

	// http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.6
	public void testInnerClassesAttributeStructure_493554() throws Exception {
		runTest("pertarget");
		
		// Testcode commented out below is for full analysis of the inner class attribute but under
		// 493554 we are going to remove that attribute for this class
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "example/aspect/FooAspect$ajcMightHaveAspect");
		assertNotNull(jc);
		assertEquals(Constants.ACC_PUBLIC | Constants.ACC_INTERFACE | Constants.ACC_ABSTRACT,jc.getModifiers());
		Attribute[] attributes = jc.getAttributes();
		for (Attribute attribute: attributes) {
			if (attribute.getName().equals("InnerClasses")) {
				fail("Did not expect to find InnerClasses attribute");
			}
		}

//		// Is InnerClasses attribute well formed for the pertarget interface?
//		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "example/aspect/FooAspect$ajcMightHaveAspect");
//		assertNotNull(jc);
//		assertEquals(Constants.ACC_PUBLIC | Constants.ACC_INTERFACE | Constants.ACC_ABSTRACT,jc.getModifiers());
//		Attribute attr = getAttributeStartsWith(jc.getAttributes(), "InnerClasses");
//		assertNotNull(attr);
//		InnerClasses innerClasses = (InnerClasses)attr;
//		InnerClass[] innerClassArray = innerClasses.getInnerClasses();
//		assertEquals(1,innerClassArray.length);
//		InnerClass innerClass = innerClassArray[0];
//		ConstantPool cp = jc.getConstantPool();
//
//		// The value of the inner_class_info_index item must be a valid index into the 
//		// constant_pool table. The constant_pool entry at that index must be a CONSTANT_Class_info 
//		// structure representing C.
//		int innerClassIndex = innerClass.getInnerClassIndex();
//		ConstantClass cc = (ConstantClass)cp.getConstant(innerClassIndex);
//		ConstantUtf8 utf8 = cp.getConstantUtf8(cc.getNameIndex());
//		assertEquals("example/aspect/FooAspect$ajcMightHaveAspect",utf8.getStringValue());
//		
//		// The remaining items in the classes array entry give information about C.
//		// The value of the outer_class_info_index item must be a valid index into the 
//		// constant_pool table, and the entry at that index must be a CONSTANT_Class_info
//		// structure representing the class or interface of which C is a member.
//		int outerClassIndex = innerClass.getOuterClassIndex();
//		cc = (ConstantClass)cp.getConstant(outerClassIndex);
//		utf8 = cp.getConstantUtf8(cc.getNameIndex());
//		assertEquals("example/aspect/FooAspect",utf8.getStringValue());
//		
//		// The value of the inner_name_index item must be a valid index into the constant_pool table, 
//		// and the entry at that index must be a CONSTANT_Utf8_info structure (§4.4.7) that represents 
//		// the original simple name of C, as given in the source code from which this class file was compiled.
//		int innerNameIndex = innerClass.getInnerNameIndex();
//		utf8 = cp.getConstantUtf8(innerNameIndex);
//		assertEquals("ajcMightHaveAspect",utf8.getStringValue());
//		
//		int innerAccessFlags = innerClass.getInnerAccessFlags();
//		assertEquals(Constants.ACC_PUBLIC | Constants.ACC_ABSTRACT | Constants.ACC_INTERFACE | Constants.ACC_STATIC,innerAccessFlags);
//		
//		// Is InnerClasses attribute well formed for the containing type?
//		jc = getClassFrom(ajc.getSandboxDirectory(), "example/aspect/FooAspect");
//		assertNotNull(jc);
//		attr = getAttributeStartsWith(jc.getAttributes(), "InnerClasses");
//		assertNotNull(attr);
//		innerClasses = (InnerClasses)attr;
//		innerClassArray = innerClasses.getInnerClasses();
//		assertEquals(1,innerClassArray.length);
//		innerClass = innerClassArray[0];
//		cp = jc.getConstantPool();
//		System.out.println(innerClass);
//		
//		// inner class name
//		innerClassIndex = innerClass.getInnerClassIndex();
//		cc = (ConstantClass)cp.getConstant(innerClassIndex);
//		utf8 = cp.getConstantUtf8(cc.getNameIndex());
//		assertEquals("example/aspect/FooAspect$ajcMightHaveAspect",utf8.getStringValue());
//		
//		// outer class name
//		outerClassIndex = innerClass.getOuterClassIndex();
//		cc = (ConstantClass)cp.getConstant(outerClassIndex);
//		utf8 = cp.getConstantUtf8(cc.getNameIndex());
//		assertEquals("example/aspect/FooAspect",utf8.getStringValue());
//
//		// Simple name
//		innerNameIndex = innerClass.getInnerNameIndex();
//		utf8 = cp.getConstantUtf8(innerNameIndex);
//		assertEquals("ajcMightHaveAspect",utf8.getStringValue());
//		
//		// inner modifiers
//		innerAccessFlags = innerClass.getInnerAccessFlags();
//		assertEquals(Constants.ACC_ABSTRACT | Constants.ACC_INTERFACE | Constants.ACC_STATIC,innerAccessFlags);
//				
//		// Reflection work getDeclaredClasses?
//		
//		// What about other interfaces?
	}
	
	
	
//	public void testOverweaving_352389() throws Exception {
//		runTest("overweaving");
//	}
	
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1810Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc1810.xml");
	}

}
