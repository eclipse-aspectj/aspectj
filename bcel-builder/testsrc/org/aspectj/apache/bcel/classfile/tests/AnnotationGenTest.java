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
import java.util.List;
import java.util.Vector;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisibleAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisibleAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnotations;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.annotation.AnnotationGen;
import org.aspectj.apache.bcel.generic.annotation.ElementNameValuePairGen;
import org.aspectj.apache.bcel.generic.annotation.ElementValueGen;
import org.aspectj.apache.bcel.generic.annotation.SimpleElementValueGen;

public class AnnotationGenTest extends BcelTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	private ClassGen createClassGen(String classname) {
		return new ClassGen(classname, "java.lang.Object",
				"<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER, null);
	}

	/**
	 * Programmatically construct an mutable annotation (AnnotationGen) object.
	 */
	public void testConstructMutableAnnotation() {
		
		// Create the containing class
		ClassGen cg = createClassGen("HelloWorld");
		ConstantPoolGen cp = cg.getConstantPool();
		
		// Create the simple primitive value '4' of type 'int'
		SimpleElementValueGen evg = 
			new SimpleElementValueGen(ElementValueGen.PRIMITIVE_INT,cp,4);
		
		// Give it a name, call it 'id'
		ElementNameValuePairGen nvGen = new ElementNameValuePairGen("id",evg,cp);
		
		// Check it looks right 
		assertTrue("Should include string 'id=4' but says: "+nvGen.toString(),
			nvGen.toString().indexOf("id=4")!=-1);
		
		
		ObjectType t = new ObjectType("SimpleAnnotation");
		
		List elements = new ArrayList();
		elements.add(nvGen);
		
		// Build an annotation of type 'SimpleAnnotation' with 'id=4' as the only value :)
		AnnotationGen a = new AnnotationGen(t,elements,true,cp);
		
		// Check we can save and load it ok
		checkSerialize(a,cp);
	}


    public void testVisibleInvisibleAnnotationGen() {

        // Create the containing class
        ClassGen cg = createClassGen("HelloWorld");
        ConstantPoolGen cp = cg.getConstantPool();

        // Create the simple primitive value '4' of type 'int'
        SimpleElementValueGen evg =
            new SimpleElementValueGen(ElementValueGen.PRIMITIVE_INT,cp,4);

        // Give it a name, call it 'id'
        ElementNameValuePairGen nvGen = new ElementNameValuePairGen("id",evg,cp);

        // Check it looks right
        assertTrue("Should include string 'id=4' but says: "+nvGen.toString(),
            nvGen.toString().indexOf("id=4")!=-1);


        ObjectType t = new ObjectType("SimpleAnnotation");

        List elements = new ArrayList();
        elements.add(nvGen);

        // Build a RV annotation of type 'SimpleAnnotation' with 'id=4' as the only value :)
        AnnotationGen a = new AnnotationGen(t,elements,true,cp);

        Vector v = new Vector();
        v.add(a);
        Attribute[] attributes = Utility.getAnnotationAttributes(cp, v);
        boolean foundRV = false;
        for (int i = 0; i < attributes.length; i++) {
            Attribute attribute = attributes[i];
            if (attribute instanceof RuntimeVisibleAnnotations) {
                assertTrue(((RuntimeAnnotations)attribute).areVisible());
                foundRV = true;

            }
        }
        assertTrue("Should have seen a RuntimeVisibleAnnotation", foundRV);


        // Build a RIV annotation of type 'SimpleAnnotation' with 'id=4' as the only value :)
        AnnotationGen a2 = new AnnotationGen(t,elements,false,cp);

        Vector v2 = new Vector();
        v2.add(a2);
        Attribute[] attributes2 = Utility.getAnnotationAttributes(cp, v2);
        boolean foundRIV = false;
        for (int i = 0; i < attributes2.length; i++) {
            Attribute attribute = attributes2[i];
            if (attribute instanceof RuntimeInvisibleAnnotations) {
                assertFalse(((RuntimeAnnotations)attribute).areVisible());
                foundRIV = true;
            }
        }
        assertTrue("Should have seen a RuntimeInvisibleAnnotation", foundRIV);
    }

	////
	// Helper methods
	
	private void checkSerialize(AnnotationGen a,ConstantPoolGen cpg) {
		try {
		  String beforeName = a.getTypeName();
		  List beforeValues = a.getValues();
		  ByteArrayOutputStream baos = new ByteArrayOutputStream();
		  DataOutputStream dos = new DataOutputStream(baos);
		  a.dump(dos);
		  dos.flush();
		  dos.close();
		  
		  byte[] bs = baos.toByteArray();
		  
		  ByteArrayInputStream bais = new ByteArrayInputStream(bs);
		  DataInputStream dis = new DataInputStream(bais);
		  AnnotationGen annAfter = AnnotationGen.read(dis,cpg,a.isRuntimeVisible());
		  
		  dis.close();
		  
		  String afterName = annAfter.getTypeName();
		  List afterValues = annAfter.getValues();
		  
		  if (!beforeName.equals(afterName)) {
		  	fail("Deserialization failed: before type='"+beforeName+"' after type='"+afterName+"'");
		  }
		  if (a.getValues().size()!=annAfter.getValues().size()) {
		  	fail("Different numbers of element name value pairs?? "+a.getValues().size()+"!="+annAfter.getValues().size());
		  }
		  for (int i=0;i<a.getValues().size();i++) {
			ElementNameValuePairGen beforeElement = (ElementNameValuePairGen) a.getValues().get(i);
			ElementNameValuePairGen afterElement = (ElementNameValuePairGen) annAfter.getValues().get(i);
			if (!beforeElement.getNameString().equals(afterElement.getNameString())) {
				fail("Different names?? "+beforeElement.getNameString()+"!="+afterElement.getNameString());
			}
		  }
		 
		  
		} catch (IOException ioe) {
			fail("Unexpected exception whilst checking serialization: "+ioe);
		}
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}