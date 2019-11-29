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

import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;

import junit.framework.TestCase;

public class AnnotationAccessFlagTest extends TestCase {
	
	private boolean verbose = false;

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/**
	 * If you write an annotation and compile it, the class file generated should be
	 * marked as an annotation type - which is detectable through BCEL.
	 */
	public void testAnnotationClassSaysItIs() throws ClassNotFoundException {
		ClassPath cp = 
			new ClassPath("testdata"+File.separator+"testcode.jar"+File.pathSeparator+System.getProperty("java.class.path"));
		SyntheticRepository repos = SyntheticRepository.getInstance(cp);
		JavaClass clazz = repos.loadClass("SimpleAnnotation");
		ConstantPool pool = clazz.getConstantPool();
		assertTrue("Expected SimpleAnnotation class to say it was an annotation - but it didn't !",
				clazz.isAnnotation());
		clazz = repos.loadClass("SimpleClass");
		assertTrue("Expected SimpleClass class to say it was not an annotation - but it didn't !",
				!clazz.isAnnotation());
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	

}
