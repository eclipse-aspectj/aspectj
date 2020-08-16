/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.asm;

import org.aspectj.asm.internal.CharOperation;
import org.aspectj.asm.internal.NameConvertor;

import junit.framework.TestCase;

public class NameConvertorTest extends TestCase {

	public void testBoolean() {
		checkConversion("Z", "boolean");
	}

	public void testByte() {
		checkConversion("B", "byte");
	}

	public void testChar() {
		checkConversion("C", "char");
	}

	public void testDouble() {
		checkConversion("D", "double");
	}

	public void testFloat() {
		checkConversion("F", "float");
	}

	public void testInt() {
		checkConversion("I", "int");
	}

	public void testLong() {
		checkConversion("J", "long");
	}

	public void testShort() {
		checkConversion("S", "short");
	}

	public void testString() {
		checkConversion("Ljava/lang/String;", "java.lang.String");
	}

	public void testType() {
		checkConversion("LMyClass;", "MyClass");
	}

	public void testListPameterizedWithString() {
		checkConversion("Pjava/util/List<Ljava/lang/String;>;", "java.util.List<java.lang.String>");
	}

	public void testClassParameterizedWithStringAndType() {
		checkConversion("PMyGenericClass<Ljava/lang/String;LMyClass;>;", "MyGenericClass<java.lang.String,MyClass>");
	}

	public void testStringArray() {
		checkConversion("[Ljava/lang/String;", "java.lang.String[]");
	}

	public void testTwoDimensionalStringArray() {
		checkConversion("[[Ljava/lang/String;", "java.lang.String[][]");
	}

	public void testIntArray() {
		checkConversion("[I", "int[]");
	}

	private void checkConversion(String signature, String expected) {
		char[] c = NameConvertor.convertFromSignature(signature.toCharArray());
		assertTrue("converting " + signature + ", expected " + expected + "," + "but found " + String.valueOf(c), CharOperation
				.equals(c, expected.toCharArray()));
	}

}
