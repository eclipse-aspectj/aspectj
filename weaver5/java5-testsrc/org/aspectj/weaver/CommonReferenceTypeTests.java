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

import junit.framework.TestCase;

// test cases for Adrian's additions to ReferenceType
// XXX - couldn't find any unit test cases for the rest of the ReferenceType class
public abstract class CommonReferenceTypeTests extends TestCase {

	private World world;

	public abstract World getWorld();

	public void setUp() {
		world = getWorld();
	}

	public void testUnresolvedTypeSignatureProcessing() {
		world.setBehaveInJava5Way(true);
		UnresolvedType ut = null;
		ut = UnresolvedType.forName("java.util.List<java.util.List<java.lang.String>>[]").resolve(world);
		ut = UnresolvedType.forSignature("[Pjava/util/List<Pjava/util/List<Ljava/lang/String;>;>;").resolve(world);
		assertEquals("Signatures not equal ", "[Pjava/util/List<Pjava/util/List<Ljava/lang/String;>;>;", ut.getSignature());
		assertEquals("Names not equal ", "java.util.List<java.util.List<java.lang.String>>[]", ut.getName());
	}
	
	public void testArrays() {
		world.setBehaveInJava5Way(true);
		UnresolvedType ut = null;
		ut = UnresolvedType.forName("[Ljava.lang.String;");
		assertEquals("[Ljava/lang/String;",ut.getSignature());
		UnresolvedType reified = UnresolvedType.forSignature(ut.getSignature());
		ResolvedType rt = world.resolve(reified);
		assertEquals("[Ljava/lang/String;",rt.getSignature());
		assertEquals("java.lang.String[]",rt.getName());
		assertFalse(rt.isMissing());
		
		ut = UnresolvedType.forName("[[[[Ljava.lang.String;");
		assertEquals("[[[[Ljava/lang/String;",ut.getSignature());
		reified = UnresolvedType.forSignature(ut.getSignature());
		rt = world.resolve(reified);
		assertEquals("[[[[Ljava/lang/String;",rt.getSignature());
		assertEquals("java.lang.String[][][][]",rt.getName());
		assertTrue(rt.isArray());
		assertTrue(rt.getComponentType().isArray());
		assertFalse(rt.isMissing());
	}

	public void testIsRawTrue() {
		world.setBehaveInJava5Way(true);
		UnresolvedType javaLangClass = UnresolvedType.forName("java.lang.Class");
		ResolvedType rtx = world.resolve(javaLangClass);
		assertTrue("Resolves to reference type", (rtx instanceof ReferenceType));
		ReferenceType rt = (ReferenceType) rtx;
		assertTrue("java.lang.Class is raw", rt.isRawType());
	}

	public void testIsRawFalse() {
		world.setBehaveInJava5Way(true);
		UnresolvedType javaLangObject = UnresolvedType.forName("java.lang.Object");
		ResolvedType rtx = world.resolve(javaLangObject);
		assertTrue("Resolves to reference type", (rtx instanceof ReferenceType));
		ReferenceType rt = (ReferenceType) rtx;
		assertFalse("java.lang.Object is  not raw", rt.isRawType());
	}

	public void testIsGenericTrue() {
		world.setBehaveInJava5Way(true);
		UnresolvedType javaLangClass = UnresolvedType.forName("java.lang.Class");
		ResolvedType rtx = world.resolve(javaLangClass);
		assertTrue("java.lang.Class has underpinning generic type", rtx.getGenericType().isGenericType());
	}

	public void testIsGenericFalse() {
		world.setBehaveInJava5Way(true);
		UnresolvedType javaLangObject = UnresolvedType.forName("java.lang.Object");
		ResolvedType rtx = world.resolve(javaLangObject);
		assertFalse(rtx.isGenericType());
	}

}
