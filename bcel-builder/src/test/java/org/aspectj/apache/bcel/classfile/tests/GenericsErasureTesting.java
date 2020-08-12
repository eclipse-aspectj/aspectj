/* *******************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement (IBM)     initial implementation 
 * ******************************************************************/
package org.aspectj.apache.bcel.classfile.tests;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Signature;

/**
 * Should be possible to recover original declared signatures after erasure by using
 * the signature attribute.
 */
public class GenericsErasureTesting extends BcelTestCase {
	
	
	public void testLoadingGenerics() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("ErasureTestData");
		Method m = getMethod(clazz,"getData");
		String sig = m.getDeclaredSignature();
		System.err.println(getSignatureAttribute(clazz,"getData"));
		System.err.println(sig);
		assertTrue("Incorrect: "+sig,sig.equals("()Ljava/util/Vector<Ljava/lang/String;>;"));
	}
	
	
	// helper methods below
	
	public Signature getSignatureAttribute(JavaClass clazz,String name) {
		Method m = getMethod(clazz,name);
		Attribute[] as = m.getAttributes();
		for (Attribute attribute : as) {
			if (attribute.getName().equals("Signature")) {
				return (Signature) attribute;
			}
		}
		return null;
	}
	
}
