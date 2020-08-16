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


public class VarargsTest extends BcelTestCase {
	

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	
	public void testVarargs() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("VarargsClass");
		
		checkMarkedVarargs(clazz,"foo",true);
		checkMarkedVarargs(clazz,"goo",true);
		checkMarkedVarargs(clazz,"hoo",false);
	}
	
	public void testVarargsReadWrite() throws ClassNotFoundException,IOException {
		JavaClass clazz = getClassFromJar("VarargsClass");
		
		checkMarkedVarargs(clazz,"foo",true);
		checkMarkedVarargs(clazz,"goo",true);
		checkMarkedVarargs(clazz,"hoo",false);
		
		//	 Write it out
		File tfile = createTestdataFile("VarargsClass.class");
		clazz.dump(tfile);
		
		SyntheticRepository repos2 = createRepos(".");
		JavaClass           clazz2 = repos2.loadClass("VarargsClass");
		
		checkMarkedVarargs(clazz,"foo",true);
		checkMarkedVarargs(clazz,"goo",true);
		checkMarkedVarargs(clazz,"hoo",false);

		assertTrue(tfile.delete());
	}
	
	// helper methods
		
	public void checkMarkedVarargs(JavaClass clazz,String methodname,boolean shouldBeMarked) {
		Method[] methods = clazz.getMethods();

		for (Method m : methods) {
			if (m.getName().equals(methodname)) {
				assertTrue("Method '" + methodname + "' should answer varargs=" + shouldBeMarked,
						m.isVarargs() == shouldBeMarked);
			}
		}
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
