/*******************************************************************************
 * Copyright (c) 2004 IBM All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Andy Clement - initial implementation
 ******************************************************************************/

package org.aspectj.apache.bcel.classfile.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.annotation.ClassElementValueGen;
import org.aspectj.apache.bcel.generic.annotation.ElementValueGen;
import org.aspectj.apache.bcel.generic.annotation.EnumElementValueGen;
import org.aspectj.apache.bcel.generic.annotation.SimpleElementValueGen;

public class ElementValueGenTest extends BcelTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	private ClassGen createClassGen(String classname) {
		return new ClassGen(classname, "java.lang.Object",
				"<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER, null);
	}

	////
	// Create primitive element values

	public void testCreateIntegerElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		SimpleElementValueGen evg = new SimpleElementValueGen(ElementValueGen.PRIMITIVE_INT,cp,555);
		// Creation of an element like that should leave a new entry in the cpool
		assertTrue("Should have the same index in the constantpool but "+evg.getIndex()+"!="+cp.lookupInteger(555),
				evg.getIndex()==cp.lookupInteger(555));
		checkSerialize(evg,cp);
	}

	public void testCreateFloatElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		SimpleElementValueGen evg = new SimpleElementValueGen(ElementValueGen.PRIMITIVE_FLOAT,cp,111.222f);
		// Creation of an element like that should leave a new entry in the cpool
		assertTrue("Should have the same index in the constantpool but "+evg.getIndex()+"!="+cp.lookupFloat(111.222f),
				evg.getIndex()==cp.lookupFloat(111.222f));
		checkSerialize(evg,cp);
	}
	
	public void testCreateDoubleElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		SimpleElementValueGen evg = new SimpleElementValueGen(ElementValueGen.PRIMITIVE_DOUBLE,cp,333.44);
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupDouble(333.44);
		assertTrue("Should have the same index in the constantpool but "+evg.getIndex()+"!="+idx,
				evg.getIndex()==idx);
		checkSerialize(evg,cp);
	}
	
	public void testCreateLongElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		SimpleElementValueGen evg = new SimpleElementValueGen(ElementValueGen.PRIMITIVE_LONG,cp,3334455L);
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupLong(3334455L);
		assertTrue("Should have the same index in the constantpool but "+evg.getIndex()+"!="+idx,
				evg.getIndex()==idx);
		checkSerialize(evg,cp);
	}

	public void testCreateCharElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		SimpleElementValueGen evg = new SimpleElementValueGen(ElementValueGen.PRIMITIVE_CHAR,cp,(char)'t');
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupInteger((char)'t');
		assertTrue("Should have the same index in the constantpool but "+evg.getIndex()+"!="+idx,
				evg.getIndex()==idx);
		checkSerialize(evg,cp);
	}
	
	public void testCreateByteElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		SimpleElementValueGen evg = new SimpleElementValueGen(ElementValueGen.PRIMITIVE_CHAR,cp,(byte)'z');
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupInteger((byte)'z');
		assertTrue("Should have the same index in the constantpool but "+evg.getIndex()+"!="+idx,
				evg.getIndex()==idx);
		checkSerialize(evg,cp);
	}

	public void testCreateBooleanElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		SimpleElementValueGen evg = new SimpleElementValueGen(ElementValueGen.PRIMITIVE_BOOLEAN,cp,true);
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupInteger(1); // 1 == true
		assertTrue("Should have the same index in the constantpool but "+evg.getIndex()+"!="+idx,
				evg.getIndex()==idx);
		checkSerialize(evg,cp);
	}

	public void testCreateShortElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		SimpleElementValueGen evg = new SimpleElementValueGen(ElementValueGen.PRIMITIVE_SHORT,cp,(short)42);
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupInteger(42); 
		assertTrue("Should have the same index in the constantpool but "+evg.getIndex()+"!="+idx,
				evg.getIndex()==idx);
		checkSerialize(evg,cp);
	}
	
	////
	// Create string element values

	public void testCreateStringElementValue() {

		// Create HelloWorld
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		SimpleElementValueGen evg = new SimpleElementValueGen(ElementValueGen.STRING,cp,"hello");
		// Creation of an element like that should leave a new entry in the cpool
		assertTrue("Should have the same index in the constantpool but "+evg.getIndex()+"!="+cp.lookupUtf8("hello"),
				evg.getIndex()==cp.lookupUtf8("hello"));
		checkSerialize(evg,cp);
	}
	
	////
	// Create enum element value
	
	public void testCreateEnumElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		

		ObjectType enumType = new ObjectType("SimpleEnum"); // Supports rainbow :)
		
		EnumElementValueGen evg = new EnumElementValueGen(enumType,"Red",cp);
		// Creation of an element like that should leave a new entry in the cpool
		assertTrue("The new ElementValue value index should match the contents of the constantpool but "+
				evg.getValueIndex()+"!="+cp.lookupUtf8("Red"),
				evg.getValueIndex()==cp.lookupUtf8("Red"));
		//BCELBUG: Should the class signature or class name be in the constant pool? (see note in ConstantPool)
//		assertTrue("The new ElementValue type index should match the contents of the constantpool but "+
//				evg.getTypeIndex()+"!="+cp.lookupClass(enumType.getSignature()),
//				evg.getTypeIndex()==cp.lookupClass(enumType.getSignature()));

		checkSerialize(evg,cp);
	}
	
	////
	// Create class element value
	
	public void testCreateClassElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		ObjectType classType = new ObjectType("java.lang.Integer");
		
		ClassElementValueGen evg = new ClassElementValueGen(classType,cp);
		
		assertTrue("Unexpected value for contained class: '"+evg.getClassString()+"'",
				evg.getClassString().indexOf("Integer")!=-1);

		checkSerialize(evg,cp);
	}
	
	
	////
	// Helper methods
	
	private void checkSerialize(ElementValueGen evgBefore,ConstantPoolGen cpg) {
		try {
		  String beforeValue = evgBefore.stringifyValue();
		  ByteArrayOutputStream baos = new ByteArrayOutputStream();
		  DataOutputStream dos = new DataOutputStream(baos);
		  evgBefore.dump(dos);
		  dos.flush();
		  dos.close();
		  
		  byte[] bs = baos.toByteArray();
		  
		  ByteArrayInputStream bais = new ByteArrayInputStream(bs);
		  DataInputStream dis = new DataInputStream(bais);
		  ElementValueGen evgAfter = ElementValueGen.readElementValue(dis,cpg);
		  
		  dis.close();
		  String afterValue = evgAfter.stringifyValue();
		  
		  if (!beforeValue.equals(afterValue)) {
		  	fail("Deserialization failed: before='"+beforeValue+"' after='"+afterValue+"'");
		  }
		  
		} catch (IOException ioe) {
			fail("Unexpected exception whilst checking serialization: "+ioe);
		}
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}