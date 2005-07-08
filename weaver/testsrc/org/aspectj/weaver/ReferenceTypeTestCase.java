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
package org.aspectj.weaver;

import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.TestCase;

// test cases for Adrian's additions to ReferenceType
// XXX - couldn't find any unit test cases for the rest of the ReferenceType class
public class ReferenceTypeTestCase extends TestCase {

	public void testIsGenericTrue() {
		BcelWorld world = new BcelWorld();
		TypeX javaLangClass = TypeX.forName("java/lang/Class");
		ResolvedTypeX rtx = world.resolve(javaLangClass);
		assertTrue("Resolves to reference type",(rtx instanceof ReferenceType));
		ReferenceType rt = (ReferenceType) rtx;
		assertTrue("java.lang.Class is generic",rt.isGeneric());
	}
	
	public void testIsGenericFalse() {
		BcelWorld world = new BcelWorld();
		TypeX javaLangObject = TypeX.forName("java/lang/Object");
		ResolvedTypeX rtx = world.resolve(javaLangObject);
		assertTrue("Resolves to reference type",(rtx instanceof ReferenceType));
		ReferenceType rt = (ReferenceType) rtx;
		assertFalse("java.lang.Object is  not generic",rt.isGeneric());		
	}
	
}
