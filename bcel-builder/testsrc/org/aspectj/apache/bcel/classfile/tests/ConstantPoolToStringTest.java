/* *******************************************************************
 * Copyright (c) 2018 Contributors
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

import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.util.SyntheticRepository;

public class ConstantPoolToStringTest extends BcelTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testToStringLambdaElements() throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("lambda.jar");
		JavaClass clazz = repos.loadClass("Code");
		ConstantPool pool = clazz.getConstantPool();
		Method[] methods = clazz.getMethods();
		String codeString = methods[1].getCode().getCodeString();
		assertEquals("Code(max_stack = 1, max_locals = 2, code_length = 13)\n" + 
				"0:    invokedynamic	#0.run ()Ljava/lang/Runnable; (2)\n" + 
				"5:    astore_1\n" + 
				"6:    aload_1\n" + 
				"7:    invokeinterface	java.lang.Runnable.run ()V (3)	1	0\n" + 
				"12:   return\n",codeString);
		
		// #20 = MethodHandle       6:#32          // REF_invokeStatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
		String cts = pool.constantToString(pool.getConstant(20));
		assertEquals("6:java.lang.invoke.LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",cts);
		
		//		#21 = MethodType         #10            //  ()V
		cts = pool.constantToString(pool.getConstant(21));
		assertEquals("()V",cts);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
