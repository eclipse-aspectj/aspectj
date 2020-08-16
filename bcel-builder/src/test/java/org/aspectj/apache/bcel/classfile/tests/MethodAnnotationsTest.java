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

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.util.SyntheticRepository;


public class MethodAnnotationsTest extends BcelTestCase {
	

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testMethodAnnotations() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("AnnotatedMethods");
		
		checkAnnotatedMethod(clazz,"method1","SimpleAnnotation","id","1");
		checkAnnotatedMethod(clazz,"method2","SimpleAnnotation","id","2");

	}
	
	public void testMethodAnnotationsReadWrite() throws ClassNotFoundException,IOException {
		JavaClass clazz = getClassFromJar("AnnotatedMethods");
		
		checkAnnotatedMethod(clazz,"method1","SimpleAnnotation","id","1");
		checkAnnotatedMethod(clazz,"method2","SimpleAnnotation","id","2");
		
		//	 Write it out
		File tfile = createTestdataFile("AnnotatedMethods.class");
		clazz.dump(tfile);
		
		SyntheticRepository repos2 = createRepos(".");
		JavaClass           clazz2 = repos2.loadClass("AnnotatedMethods");
		
		checkAnnotatedMethod(clazz,"method1","SimpleAnnotation","id","1");
		checkAnnotatedMethod(clazz,"method2","SimpleAnnotation","id","2");

		assertTrue(tfile.delete());
	}
	
	// helper methods
		
	public void checkAnnotatedMethod(JavaClass clazz,String methodname,
			String annotationName,String annotationElementName,String annotationElementValue) {
		Method[] methods = clazz.getMethods();

		for (Method m : methods) {
			AnnotationGen[] methodAnnotations = m.getAnnotations();
			if (m.getName().equals(methodname)) {
				checkAnnotation(methodAnnotations[0], annotationName, annotationElementName, annotationElementValue);

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
