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

public class EnumAccessFlagTest extends TestCase {
	
	private boolean verbose = false;

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/**
	 * An enumerated type, once compiled, should result in a class file that
	 * is marked such that we can determine from the access flags (through BCEL) that
	 * it was originally an enum type declaration.
	 */
	public void testEnumClassSaysItIs() throws ClassNotFoundException {
		ClassPath cp = 
			new ClassPath("testdata"+File.separator+"testcode.jar"+File.pathSeparator+System.getProperty("java.class.path"));
		SyntheticRepository repos = SyntheticRepository.getInstance(cp);
		JavaClass clazz = repos.loadClass("SimpleEnum");
		ConstantPool pool = clazz.getConstantPool();
		assertTrue("Expected SimpleEnum class to say it was an enum - but it didn't !",
				clazz.isEnum());
		clazz = repos.loadClass("SimpleClass");
		assertTrue("Expected SimpleClass class to say it was not an enum - but it didn't !",
				!clazz.isEnum());
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	

}
