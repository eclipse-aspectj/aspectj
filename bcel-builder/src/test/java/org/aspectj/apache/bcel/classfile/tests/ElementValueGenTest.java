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
import java.util.Collections;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationElementValue;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ClassElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.EnumElementValue;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValue;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.ObjectType;

public class ElementValueGenTest extends BcelTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	private ClassGen createClassGen(String classname) {
		return new ClassGen(classname, "java.lang.Object", "<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER, null);
	}

	// //
	// Create primitive element values

	public void testCreateIntegerElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		SimpleElementValue evg = new SimpleElementValue(ElementValue.PRIMITIVE_INT, cp, 555);
		// Creation of an element like that should leave a new entry in the cpool
		assertTrue("Should have the same index in the constantpool but " + evg.getIndex() + "!=" + cp.lookupInteger(555),
				evg.getIndex() == cp.lookupInteger(555));
		checkSerialize(evg, cp);
	}

	public void testCreateFloatElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		SimpleElementValue evg = new SimpleElementValue(ElementValue.PRIMITIVE_FLOAT, cp, 111.222f);
		// Creation of an element like that should leave a new entry in the cpool
		assertTrue("Should have the same index in the constantpool but " + evg.getIndex() + "!=" + cp.lookupFloat(111.222f),
				evg.getIndex() == cp.lookupFloat(111.222f));
		checkSerialize(evg, cp);
	}

	public void testCreateDoubleElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		SimpleElementValue evg = new SimpleElementValue(ElementValue.PRIMITIVE_DOUBLE, cp, 333.44);
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupDouble(333.44);
		assertTrue("Should have the same index in the constantpool but " + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
		checkSerialize(evg, cp);
	}

	public void testCreateLongElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		SimpleElementValue evg = new SimpleElementValue(ElementValue.PRIMITIVE_LONG, cp, 3334455L);
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupLong(3334455L);
		assertTrue("Should have the same index in the constantpool but " + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
		checkSerialize(evg, cp);
	}

	public void testCreateCharElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		SimpleElementValue evg = new SimpleElementValue(ElementValue.PRIMITIVE_CHAR, cp, (char) 't');
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupInteger((char) 't');
		assertTrue("Should have the same index in the constantpool but " + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
		checkSerialize(evg, cp);
	}

	public void testCreateByteElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		SimpleElementValue evg = new SimpleElementValue(ElementValue.PRIMITIVE_CHAR, cp, (byte) 'z');
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupInteger((byte) 'z');
		assertTrue("Should have the same index in the constantpool but " + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
		checkSerialize(evg, cp);
	}

	public void testCreateBooleanElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		SimpleElementValue evg = new SimpleElementValue(ElementValue.PRIMITIVE_BOOLEAN, cp, true);
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupInteger(1); // 1 == true
		assertTrue("Should have the same index in the constantpool but " + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
		checkSerialize(evg, cp);
	}

	public void testCreateShortElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		SimpleElementValue evg = new SimpleElementValue(ElementValue.PRIMITIVE_SHORT, cp, (short) 42);
		// Creation of an element like that should leave a new entry in the cpool
		int idx = cp.lookupInteger(42);
		assertTrue("Should have the same index in the constantpool but " + evg.getIndex() + "!=" + idx, evg.getIndex() == idx);
		checkSerialize(evg, cp);
	}

	// //
	// Create string element values

	public void testCreateStringElementValue() {

		// Create HelloWorld
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		SimpleElementValue evg = new SimpleElementValue(ElementValue.STRING, cp, "hello");
		// Creation of an element like that should leave a new entry in the cpool
		assertTrue("Should have the same index in the constantpool but " + evg.getIndex() + "!=" + cp.lookupUtf8("hello"),
				evg.getIndex() == cp.lookupUtf8("hello"));
		checkSerialize(evg, cp);
	}

	// //
	// Create enum element value

	public void testCreateEnumElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		ObjectType enumType = new ObjectType("SimpleEnum"); // Supports rainbow :)

		EnumElementValue evg = new EnumElementValue(enumType, "Red", cp);
		// Creation of an element like that should leave a new entry in the cpool
		assertTrue("The new ElementValue value index should match the contents of the constantpool but " + evg.getValueIndex()
				+ "!=" + cp.lookupUtf8("Red"), evg.getValueIndex() == cp.lookupUtf8("Red"));
		// BCELBUG: Should the class signature or class name be in the constant pool? (see note in ConstantPool)
		// assertTrue("The new ElementValue type index should match the contents of the constantpool but "+
		// evg.getTypeIndex()+"!="+cp.lookupClass(enumType.getSignature()),
		// evg.getTypeIndex()==cp.lookupClass(enumType.getSignature()));

		checkSerialize(evg, cp);
	}

	public void testCreateMarkerAnnotationElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();
		ObjectType annoType = new ObjectType("SimpleMarkerAnnotation");
		AnnotationGen annoGen = new AnnotationGen(annoType, Collections.<NameValuePair> emptyList(), true, cp);
		AnnotationElementValue evg = new AnnotationElementValue(annoGen, cp);
		checkSerialize(evg, cp);
	}

	// //
	// Create class element value

	public void testCreateClassElementValue() {
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		ObjectType classType = new ObjectType("java.lang.Integer");

		ClassElementValue evg = new ClassElementValue(classType, cp);

		assertTrue("Unexpected value for contained class: '" + evg.getClassString() + "'",
				evg.getClassString().contains("Integer"));

		checkSerialize(evg, cp);
	}

	// //
	// Helper methods

	private void checkSerialize(ElementValue evgBefore, ConstantPool cpg) {
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
			ElementValue evgAfter = ElementValue.readElementValue(dis, cpg);

			dis.close();
			String afterValue = evgAfter.stringifyValue();

			if (!beforeValue.equals(afterValue)) {
				fail("Deserialization failed: before='" + beforeValue + "' after='" + afterValue + "'");
			}

		} catch (IOException ioe) {
			fail("Unexpected exception whilst checking serialization: " + ioe);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}