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

public class BoundedReferenceTypeTestCase extends TestCase {

	ReferenceType javaLangClass;
	ReferenceType javaLangObject;
	BoundedReferenceType extendsClass;
	BoundedReferenceType superClass;
	BoundedReferenceType extendsWithExtras;

	public void testSignature() {
		String extendsSig = extendsClass.getSignature();
		assertEquals("+Ljava/lang/Class;", extendsSig);
		assertEquals("-Ljava/lang/Class;", superClass.getSignature());
	}

	public void testExtendsBounds() {
		assertFalse("has no lower bound", extendsClass.hasLowerBound());
		assertNull("no lower bound", extendsClass.getLowerBound());
		assertEquals(javaLangClass, extendsClass.getUpperBound());
		assertEquals("no interface bounds", 0, extendsClass.getAdditionalBounds().length);
	}

	public void testSuperBounds() {
		assertTrue("has lower bound", superClass.hasLowerBound());
		assertEquals(javaLangClass, superClass.getLowerBound());
		assertEquals("Ljava/lang/Object;", superClass.getUpperBound().getSignature());
		assertEquals("no interface bounds", 0, superClass.getAdditionalBounds().length);
	}

	public void testIsExtends() {
		assertTrue(extendsClass.kind == BoundedReferenceType.EXTENDS);
		assertFalse(superClass.kind == BoundedReferenceType.EXTENDS);
	}

	public void testIsSuper() {
		assertTrue(superClass.kind == BoundedReferenceType.SUPER);
		assertFalse(extendsClass.kind == BoundedReferenceType.SUPER);
	}

	public void testGetDeclaredInterfacesNoAdditions() {
		ResolvedType[] rt1 = extendsClass.getDeclaredInterfaces();
		ResolvedType[] rt2 = javaLangClass.getDeclaredInterfaces();
		assertEquals("same length", rt1.length, rt2.length);
		for (int i = 0; i < rt2.length; i++) {
			assertEquals("same methods", rt1[i], rt2[i]);
		}
	}

	public void testGetDeclaredInterfacesWithInterfaceBounds() {
		ResolvedType[] rt1 = extendsWithExtras.getDeclaredInterfaces();
		ResolvedType[] rt2 = javaLangClass.getDeclaredInterfaces();
		assertEquals("one extra interface", rt1.length, rt2.length + 1);
		for (int i = 0; i < rt2.length; i++) {
			assertEquals("same methods", rt1[i], rt2[i]);
		}
		assertEquals("Ljava/util/List;", rt1[rt1.length - 1].getSignature());
	}

	// all other methods in signature are delegated to upper bound...
	// representative test
	public void testGetDeclaredMethodsExtends() {
		ResolvedMember[] rm1 = extendsClass.getDeclaredMethods();
		ResolvedMember[] rm2 = javaLangClass.getDeclaredMethods();
		assertEquals("same length", rm1.length, rm2.length);
		for (int i = 0; i < rm2.length; i++) {
			assertEquals("same methods", rm1[i], rm2[i]);
		}
	}

	public void testGetDeclaredMethodsSuper() {
		ResolvedMember[] rm1 = superClass.getDeclaredMethods();
		ResolvedMember[] rm2 = javaLangObject.getDeclaredMethods();
		assertEquals("same length", rm1.length, rm2.length);
		for (int i = 0; i < rm2.length; i++) {
			assertEquals("same methods", rm1[i], rm2[i]);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BcelWorld world = new BcelWorld();
		javaLangClass = (ReferenceType) world.resolve(UnresolvedType.forName("java/lang/Class"));
		javaLangObject = (ReferenceType) world.resolve(UnresolvedType.OBJECT);
		extendsClass = new BoundedReferenceType(javaLangClass, true, world);
		superClass = new BoundedReferenceType(javaLangClass, false, world);
		extendsWithExtras = new BoundedReferenceType(javaLangClass, true, world, new ReferenceType[] { (ReferenceType) world
				.resolve(UnresolvedType.forName("java/util/List")) });
	}
}
