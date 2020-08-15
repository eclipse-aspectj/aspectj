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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValue;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.ObjectType;

public class AnnotationGenTest extends BcelTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	private ClassGen createClassGen(String classname) {
		return new ClassGen(classname, "java.lang.Object", "<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER, null);
	}

	/**
	 * Programmatically construct an mutable annotation (AnnotationGen) object.
	 */
	public void testConstructMutableAnnotation() {

		// Create the containing class
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		// Create the simple primitive value '4' of type 'int'
		SimpleElementValue evg = new SimpleElementValue(ElementValue.PRIMITIVE_INT, cp, 4);

		// Give it a name, call it 'id'
		NameValuePair nvGen = new NameValuePair("id", evg, cp);

		// Check it looks right
		assertTrue("Should include string 'id=4' but says: " + nvGen.toString(), nvGen.toString().contains("id=4"));

		ObjectType t = new ObjectType("SimpleAnnotation");

		List<NameValuePair> elements = new ArrayList<>();
		elements.add(nvGen);

		// Build an annotation of type 'SimpleAnnotation' with 'id=4' as the only value :)
		AnnotationGen a = new AnnotationGen(t, elements, true, cp);

		// Check we can save and load it ok
		checkSerialize(a, cp);
	}

	public void testVisibleInvisibleAnnotationGen() {

		// Create the containing class
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPool cp = cg.getConstantPool();

		// Create the simple primitive value '4' of type 'int'
		SimpleElementValue evg = new SimpleElementValue(ElementValue.PRIMITIVE_INT, cp, 4);

		// Give it a name, call it 'id'
		NameValuePair nvGen = new NameValuePair("id", evg, cp);

		// Check it looks right
		assertTrue("Should include string 'id=4' but says: " + nvGen.toString(), nvGen.toString().contains("id=4"));

		ObjectType t = new ObjectType("SimpleAnnotation");

		List<NameValuePair> elements = new ArrayList<>();
		elements.add(nvGen);

		// Build a RV annotation of type 'SimpleAnnotation' with 'id=4' as the only value :)
		AnnotationGen a = new AnnotationGen(t, elements, true, cp);

		List<AnnotationGen> v = new Vector<>();
		v.add(a);
		Collection<RuntimeAnnos> attributes = Utility.getAnnotationAttributes(cp, v);
		boolean foundRV = false;
		for (Attribute attribute : attributes) {
			if (attribute instanceof RuntimeVisAnnos) {
				assertTrue(((RuntimeAnnos) attribute).areVisible());
				foundRV = true;

			}
		}
		assertTrue("Should have seen a RuntimeVisibleAnnotation", foundRV);

		// Build a RIV annotation of type 'SimpleAnnotation' with 'id=4' as the only value :)
		AnnotationGen a2 = new AnnotationGen(t, elements, false, cp);

		List<AnnotationGen> v2 = new Vector<>();
		v2.add(a2);
		Collection<RuntimeAnnos> attributes2 = Utility.getAnnotationAttributes(cp, v2);
		boolean foundRIV = false;
		for (Attribute attribute : attributes2) {
			// for (int i = 0; i < attributes2.length; i++) {
			// Attribute attribute = attributes2[i];
			if (attribute instanceof RuntimeInvisAnnos) {
				assertFalse(((RuntimeAnnos) attribute).areVisible());
				foundRIV = true;
			}
		}
		assertTrue("Should have seen a RuntimeInvisibleAnnotation", foundRIV);
	}

	// //
	// Helper methods

	private void checkSerialize(AnnotationGen a, ConstantPool cpg) {
		try {
			String beforeName = a.getTypeName();
			List<NameValuePair> beforeValues = a.getValues();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			a.dump(dos);
			dos.flush();
			dos.close();

			byte[] bs = baos.toByteArray();

			ByteArrayInputStream bais = new ByteArrayInputStream(bs);
			DataInputStream dis = new DataInputStream(bais);
			AnnotationGen annAfter = AnnotationGen.read(dis, cpg, a.isRuntimeVisible());

			dis.close();

			String afterName = annAfter.getTypeName();
			List<NameValuePair> afterValues = annAfter.getValues();

			if (!beforeName.equals(afterName)) {
				fail("Deserialization failed: before type='" + beforeName + "' after type='" + afterName + "'");
			}
			if (a.getValues().size() != annAfter.getValues().size()) {
				fail("Different numbers of element name value pairs?? " + a.getValues().size() + "!=" + annAfter.getValues().size());
			}
			for (int i = 0; i < a.getValues().size(); i++) {
				NameValuePair beforeElement = a.getValues().get(i);
				NameValuePair afterElement = annAfter.getValues().get(i);
				if (!beforeElement.getNameString().equals(afterElement.getNameString())) {
					fail("Different names?? " + beforeElement.getNameString() + "!=" + afterElement.getNameString());
				}
			}

		} catch (IOException ioe) {
			fail("Unexpected exception whilst checking serialization: " + ioe);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}