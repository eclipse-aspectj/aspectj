/* *******************************************************************
 * Copyright (c) 2013 VMware
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

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.MethodParameters;

public class MethodParametersTest extends BcelTestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testMethodParameters1() throws Exception {
		JavaClass jc = getClassFromJava8Jar("Parameters");
		Method m = getMethod(jc, "foo");
		MethodParameters mp = (MethodParameters)getAttribute(m.getAttributes(),Constants.ATTR_METHOD_PARAMETERS);
		assertEquals(3,mp.getParametersCount());
		assertEquals("abc",mp.getParameterName(0));
		assertEquals("def",mp.getParameterName(1));
		assertEquals("ghi",mp.getParameterName(2));
		assertFalse(mp.isFinal(0));
		assertFalse(mp.isSynthetic(0));
		assertFalse(mp.isMandated(0));
	}
	
	// this method specifies the receiver
	public void testMethodParameters2() throws Exception {
		JavaClass jc = getClassFromJava8Jar("Parameters");
		Method m = getMethod(jc, "bar");
		MethodParameters mp = (MethodParameters)getAttribute(m.getAttributes(),Constants.ATTR_METHOD_PARAMETERS);
		assertEquals(1,mp.getParametersCount());
		assertEquals("abc",mp.getParameterName(0));
		assertFalse(mp.isFinal(0));
		assertFalse(mp.isSynthetic(0));
		assertFalse(mp.isMandated(0));
	}

	// access flags
	public void testMethodParameters3() throws Exception {
		JavaClass jc = getClassFromJava8Jar("Parameters$Inner");
		Method m = getMethod(jc, "<init>");
		MethodParameters mp = (MethodParameters)getAttribute(m.getAttributes(),Constants.ATTR_METHOD_PARAMETERS);
		assertEquals(2,mp.getParametersCount());
		
		assertEquals("this$0",mp.getParameterName(0));
		assertTrue(mp.isFinal(0));
		assertFalse(mp.isSynthetic(0));
		assertTrue(mp.isMandated(0));
		
		assertEquals("x",mp.getParameterName(1));
		assertFalse(mp.isFinal(1));
		assertFalse(mp.isSynthetic(1));
		assertFalse(mp.isMandated(1));
	}
	
	// access flags
	public void testMethodParameters4() throws Exception {
		JavaClass jc = getClassFromJava8Jar("Parameters$Color");
		Method m = getMethod(jc, "<init>");
		MethodParameters mp = (MethodParameters)getAttribute(m.getAttributes(),Constants.ATTR_METHOD_PARAMETERS);
		assertEquals(2,mp.getParametersCount());
		
		assertEquals("$enum$name",mp.getParameterName(0));
		assertFalse(mp.isFinal(0));
		assertTrue(mp.isSynthetic(0));
		assertFalse(mp.isMandated(0));
		
		assertEquals("$enum$ordinal",mp.getParameterName(1));
		assertFalse(mp.isFinal(1));
		assertTrue(mp.isSynthetic(1));
		assertFalse(mp.isMandated(1));
	}
	
}