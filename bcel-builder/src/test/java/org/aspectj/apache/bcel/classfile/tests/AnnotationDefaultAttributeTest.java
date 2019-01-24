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

import org.aspectj.apache.bcel.classfile.AnnotationDefault;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValue;

public class AnnotationDefaultAttributeTest extends BcelTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	
	/**
	 * For values in an annotation that have default values, we should be able to
	 * query the AnnotationDefault attribute against the method to discover the
	 * default value that was originally declared.
	 */
	public void testMethodAnnotations() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("SimpleAnnotation");
		
		Method m = getMethod(clazz,"fruit");
		AnnotationDefault a = (AnnotationDefault) findAttribute("AnnotationDefault",m.getAttributes());
		SimpleElementValue val = (SimpleElementValue) a.getElementValue();
		assertTrue("Should be STRING but is "+val.getElementValueType(),
				val.getElementValueType()==ElementValue.STRING);
		assertTrue("Should have default of bananas but default is "+val.getValueString(),
				val.getValueString().equals("bananas"));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
}
