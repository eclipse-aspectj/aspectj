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

import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTypeTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Utility;


public class LocalVariableTypeTableTest extends BcelTestCase {
	

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/**
	 * Check the local variable type table includes information about generic signatures.
	 */
	public void testLocalVariableTypeTableAttribute() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("SimpleGenericsProgram");
		
		Method mainMethod = getMethod(clazz,"main");
		Code codeAttr = (Code) findAttribute("Code",mainMethod.getAttributes());
		LocalVariableTypeTable localVariableTypeTable = 
			(LocalVariableTypeTable) findAttribute("LocalVariableTypeTable",codeAttr.getAttributes());
		
		assertTrue("Should be two entries in the LocalVariableTypeTable but found "+localVariableTypeTable.getTableLength(),
				localVariableTypeTable.getTableLength()==2);

		LocalVariable[] lvtable = localVariableTypeTable.getLocalVariableTypeTable();
		boolean tc1OK = false;
		boolean tc2OK = false;
		String errormessage = null;
		for (LocalVariable localVariable : lvtable) {
			String sig = Utility.signatureToString(localVariable.getSignature());
			if (localVariable.getName().equals("tc1")) {
				if (!sig.equals("TreasureChest<String>")) {
					errormessage = "Expected signature of 'TreasureChest<String>' for tc1 but got " + sig;
				} else {
					tc1OK = true;
				}
			}
			if (localVariable.getName().equals("tc2")) {
				if (!sig.equals("TreasureChest<Integer>")) {
					errormessage = "Expected signature of 'TreasureChest<Integer>' for tc2 but got " + sig;
				} else {
					tc2OK = true;
				}
			}
		}
		if (!tc1OK || !tc2OK) fail(errormessage);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
}
