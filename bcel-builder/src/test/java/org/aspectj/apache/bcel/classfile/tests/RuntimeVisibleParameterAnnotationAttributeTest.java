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

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisParamAnnos;
import org.aspectj.apache.bcel.util.SyntheticRepository;


public class RuntimeVisibleParameterAnnotationAttributeTest extends BcelTestCase {
	

	protected void setUp() throws Exception {
		super.setUp();
	}
	

	public void testAccessingRuntimeVisibleParameterAnnotations() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("AnnotatedParameters");
		Attribute[] rvaAttr = findAttribute("RuntimeVisibleParameterAnnotations",clazz);
		Method[] methods = clazz.getMethods();

		for (Method m : methods) {
			if (m.getName().equals("foo")) {
				RuntimeVisParamAnnos paramAnns =
						(RuntimeVisParamAnnos) findAttribute("RuntimeVisibleParameterAnnotations", m.getAttributes());
				assertTrue("foo takes two parameters, not " + paramAnns.getParameterAnnotations().size(),
						paramAnns.getParameterAnnotations().size() == 2);

				AnnotationGen[] firstParamAnnotations = paramAnns.getAnnotationsOnParameter(0);
				checkAnnotation(firstParamAnnotations[0], "SimpleAnnotation", "id", "2");

				AnnotationGen[] secondParamAnnotations = paramAnns.getAnnotationsOnParameter(1);
				checkAnnotation(secondParamAnnotations[0], "SimpleAnnotation", "id", "3");
				checkAnnotation(secondParamAnnotations[1], "AnnotationEnumElement", "enumval", "LSimpleEnum;Red");

			}
			if (m.getName().equals("main")) {
				RuntimeVisParamAnnos paramAnns =
						(RuntimeVisParamAnnos) findAttribute("RuntimeVisibleParameterAnnotations", m.getAttributes());
				assertTrue("main takes one parameter, not " + paramAnns.getParameterAnnotations().size(),
						paramAnns.getParameterAnnotations().size() == 1);

				AnnotationGen[] firstParamAnnotations = paramAnns.getAnnotationsOnParameter(0);
				checkAnnotation(firstParamAnnotations[0], "SimpleAnnotation", "id", "1");
			}
		}
	}
	
	public void testAccessingParameterAnnotationsThroughGetAnnotations() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("AnnotatedParameters");
		Attribute[] rvaAttr = findAttribute("RuntimeVisibleParameterAnnotations",clazz);
		
		checkFooMethod(clazz);
	}
	
	public void testParameterAnnotationsReadWrite() throws ClassNotFoundException,IOException {
		JavaClass clazz = getClassFromJar("AnnotatedParameters");
		
		checkFooMethod(clazz);

		//	 Write it out
		File tfile = createTestdataFile("AnnotatedParameters.class");
		clazz.dump(tfile);
		
		SyntheticRepository repos2 = createRepos(".");
		JavaClass           clazz2 = repos2.loadClass("AnnotatedParameters");
		
		checkFooMethod(clazz);

		assertTrue(tfile.delete());
	}
		
		
	public void checkFooMethod(JavaClass clazz) {
		Method[] methods = clazz.getMethods();

		for (Method m : methods) {
			if (m.getName().equals("foo")) {

				AnnotationGen[] firstParamAnnotations = m.getAnnotationsOnParameter(0);
				checkAnnotation(firstParamAnnotations[0], "SimpleAnnotation", "id", "2");

				AnnotationGen[] secondParamAnnotations = m.getAnnotationsOnParameter(1);
				checkAnnotation(secondParamAnnotations[0], "SimpleAnnotation", "id", "3");
				checkAnnotation(secondParamAnnotations[1], "AnnotationEnumElement", "enumval", "LSimpleEnum;Red");

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
