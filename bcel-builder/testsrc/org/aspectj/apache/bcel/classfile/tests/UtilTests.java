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

import org.aspectj.apache.bcel.classfile.ClassFormatException;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.generic.Type;

import junit.framework.TestCase;

public class UtilTests extends TestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testUtilityClassSignatureManipulation1() {
	  String[] ss = UtilTests.methodSignatureArgumentTypes("(Ljava/lang/String;I[Ljava/lang/Integer;)");
	  assertTrue("should be 3 not "+ss.length,ss.length==3);
	  
      assertTrue("first should be 'String', not "+ss[0],ss[0].equals("String"));
      assertTrue("second should be 'int', not "+ss[1],ss[1].equals("int"));
      assertTrue("third should be 'Integer[]', not "+ss[2],ss[2].equals("Integer[]"));
	}
	
	public void testUtilityClassSignatureManipulation2() {
	  String s = Utility.methodSignatureToString("(Ljava/lang/String;[Z[[Ljava/lang/Integer;II)Z","hello","public");
	  String expected = "public boolean hello(String arg1, boolean[] arg2, Integer[][] arg3, int arg4, int arg5)";
	  assertTrue("Expected '"+expected+"' but got "+s,s.equals(expected));
	}
	
	public void testTypeUtilMethods1() {
	String s = Utility.toMethodSignature(Type.DOUBLE,new Type[]{Type.INT,Type.STRING,Type.SHORT});
	System.err.println(s);
	}
	
	public void testTypeUtilMethods2() {
	Type s = Type.getType("Ljava/lang/String;");
	System.err.println(s);
	s = Type.getType("Z");
	System.err.println(s);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	   * @param  signature    Method signature
	   * @return Array of argument types
	   * @throws  ClassFormatException  
	   */
	  public static final String[] methodSignatureArgumentTypes(String signature) throws ClassFormatException {
	    return GenericSignatureParsingTest.methodSignatureArgumentTypes(signature, true);
	  }
	

}
