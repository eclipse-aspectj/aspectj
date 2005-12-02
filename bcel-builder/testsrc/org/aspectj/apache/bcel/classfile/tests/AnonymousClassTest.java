/**
 * Copyright (c) 2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer -     initial implementation 
 */
package org.aspectj.apache.bcel.classfile.tests;

import java.io.File;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;

import junit.framework.TestCase;

/**
 * @author adrian colyer
 *
 */
public class AnonymousClassTest extends TestCase {

	private SyntheticRepository repos;
	
	public void testRegularClassIsNotAnonymous() throws ClassNotFoundException {
		JavaClass clazz = repos.loadClass("AnonymousClassTest");
		assertFalse("regular outer classes are not anonymous",clazz.isAnonymous());
		assertFalse("regular outer classes are not nested",clazz.isNested());
	}
	
	public void testNamedInnerClassIsNotAnonymous() throws ClassNotFoundException {
		JavaClass clazz = repos.loadClass("AnonymousClassTest$X");
		assertFalse("regular inner classes are not anonymous",clazz.isAnonymous());		
		assertTrue("regular inner classes are nested",clazz.isNested());		
	}
	
	public void testStaticInnerClassIsNotAnonymous() throws ClassNotFoundException {
		JavaClass clazz = repos.loadClass("AnonymousClassTest$Y");
		assertFalse("regular static inner classes are not anonymous",clazz.isAnonymous());				
		assertTrue("regular static inner classes are nested",clazz.isNested());				
	}
	
	public void testAnonymousInnerClassIsAnonymous() throws ClassNotFoundException {
		JavaClass clazz = repos.loadClass("AnonymousClassTest$1");
		assertTrue("anonymous inner classes are anonymous",clazz.isAnonymous());		
		assertTrue("anonymous inner classes are anonymous",clazz.isNested());				
	}
	
	protected void setUp() throws Exception {
		ClassPath cp = 
			new ClassPath("testdata"+File.separator+"testcode.jar"+File.pathSeparator+System.getProperty("java.class.path"));
		repos = SyntheticRepository.getInstance(cp);
	}
	
}
