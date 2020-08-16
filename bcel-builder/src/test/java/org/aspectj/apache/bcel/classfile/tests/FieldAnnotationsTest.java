/* *******************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement -     initial implementation 
 * ******************************************************************/

package org.aspectj.apache.bcel.classfile.tests;

import java.io.File;
import java.io.IOException;

import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.util.SyntheticRepository;


public class FieldAnnotationsTest extends BcelTestCase {
	

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	
	/**
	 * Check field annotations are retrievable.
	 */
	public void testFieldAnnotations() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("AnnotatedFields");
		
		checkAnnotatedField(clazz,"i","SimpleAnnotation","id","1");
		checkAnnotatedField(clazz,"s","SimpleAnnotation","id","2");

	}

	/**
	 * Check field annotations (de)serialize ok.
	 */
	public void testFieldAnnotationsReadWrite() throws ClassNotFoundException,IOException {
		JavaClass clazz = getClassFromJar("AnnotatedFields");
		
		checkAnnotatedField(clazz,"i","SimpleAnnotation","id","1");
		checkAnnotatedField(clazz,"s","SimpleAnnotation","id","2");
		
		//	 Write it out
		File tfile = createTestdataFile("AnnotatedFields.class");
		clazz.dump(tfile);
		
		SyntheticRepository repos2 = createRepos(".");
		JavaClass           clazz2 = repos2.loadClass("AnnotatedFields");
		
		checkAnnotatedField(clazz,"i","SimpleAnnotation","id","1");
		checkAnnotatedField(clazz,"s","SimpleAnnotation","id","2");

		assertTrue(tfile.delete());
	}

	/**
	 * Check we can load in a class, modify its field annotations, save it, reload it and
	 * everything is correct.
	 */
	public void testFieldAnnotationsModification() throws ClassNotFoundException, IOException {
		boolean dbg = false;
		JavaClass clazz = getClassFromJar("AnnotatedFields");
		
		ClassGen clg = new ClassGen(clazz);
		Field f = clg.getFields()[0];
		if (dbg) System.err.println("Field in freshly constructed class is: "+f);
		if (dbg) System.err.println("Annotations on field are: "+dumpAnnotations(f.getAnnotations()));
		AnnotationGen fruitBasedAnnotation = createFruitAnnotation(clg.getConstantPool(),"Tomato",false);
		FieldGen fg = new FieldGen(f,clg.getConstantPool());
		if (dbg) System.err.println("Adding annotation to the field");
		fg.addAnnotation(fruitBasedAnnotation);
		if (dbg) System.err.println("FieldGen (mutable field) is "+fg);
		if (dbg) System.err.println("with annotations: "+dumpAnnotations(fg.getAnnotations()));

		if (dbg) System.err.println("Replacing original field with new field that has extra annotation");
		clg.removeField(f);
		clg.addField(fg.getField());
		
		f = clg.getFields()[1]; // there are two fields in the class, removing and readding has changed the order
		                        // so this time index [1] is the 'int i' field
		if (dbg) System.err.println("Field now looks like this: "+f);
		if (dbg) System.err.println("With annotations: "+dumpAnnotations(f.getAnnotations()));
		assertTrue("Should be 2 annotations on this field, but there are "+f.getAnnotations().length,f.getAnnotations().length==2);
	}
	
	// helper methods
		
	public void checkAnnotatedField(JavaClass clazz,String fieldname,
			String annotationName,String annotationElementName,String annotationElementValue) {
		Field[] fields = clazz.getFields();

		for (Field f : fields) {
			AnnotationGen[] fieldAnnotations = f.getAnnotations();
			if (f.getName().equals(fieldname)) {
				checkAnnotation(fieldAnnotations[0], annotationName, annotationElementName, annotationElementValue);

			}
		}
	}
	
	private void checkAnnotation(AnnotationGen a,String name,String elementname,String elementvalue) {
		assertTrue("Expected annotation to have name "+name+" but it had name "+a.getTypeName(),
				a.getTypeName().equals(name));
		assertTrue("Expected annotation to have one element but it had "+a.getValues().size(),a.getValues().size()==1);
		NameValuePair envp = a.getValues().get(0);
		assertTrue("Expected element name "+elementname+" but was "+envp.getNameString(),
				elementname.equals(envp.getNameString()));
		assertTrue("Expected element value "+elementvalue+" but was "+envp.getValue().stringifyValue(),
				elementvalue.equals(envp.getValue().stringifyValue()));
	}
	

	// helper methods
	
	public void checkValue(AnnotationGen a,String name,String tostring) {
		for (NameValuePair element : a.getValues()) {
			if (element.getNameString().equals(name)) {
				if (!element.getValue().stringifyValue().equals(tostring)) {
					fail("Expected element " + name + " to have value " + tostring + " but it had value " + element.getValue().stringifyValue());
				}
				return;
			}
		}
		fail("Didnt find named element "+name);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
}
