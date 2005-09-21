/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.apache.bcel.classfile.tests;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassLoaderRepository;
import org.aspectj.apache.bcel.util.Repository;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 */
public class GetReflectMembersTest extends TestCase {

  private Repository bcelRepository;
  private JavaClass jc;
	
  public void testGetMethod() throws Exception {
	  assertNotNull(jc.getMethod(GetMe.class.getMethod("foo",new Class[] {String.class})));
  }
  
  public void testGetConstructor() throws Exception {
	  assertNotNull(jc.getMethod(GetMe.class.getConstructor(new Class[] {int.class})));	  
  }
  
  public void testGetField() throws Exception {
	  assertNotNull(jc.getField(GetMe.class.getDeclaredField("x")));
  }
  
  protected void setUp() throws Exception {
	super.setUp();
	this.bcelRepository = new ClassLoaderRepository(getClass().getClassLoader());
	this.jc = bcelRepository.loadClass(GetMe.class);
  }
  
  protected void tearDown() throws Exception {
	super.tearDown();
	this.bcelRepository.clear();
  }
  
  private static class GetMe {
	 
	  private int x;
	  
	  public GetMe(int x) { this.x = x;}
	  
	  public void foo(String s) {};
	  
  }
}
