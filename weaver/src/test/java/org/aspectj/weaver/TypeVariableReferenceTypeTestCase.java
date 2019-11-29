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

/**
 * @author colyer
 * 
 */
public class TypeVariableReferenceTypeTestCase extends TestCase {

	ReferenceType javaLangClass;
	ReferenceType jlNumber;
	ReferenceType javaLangObject;
	BoundedReferenceType extendsClass;
	BoundedReferenceType superClass;
	BoundedReferenceType extendsWithExtras;
	BcelWorld world;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		world = new BcelWorld("../bin");
		world.setBehaveInJava5Way(true);
		javaLangClass = (ReferenceType) world.resolve(UnresolvedType.forName("java/lang/Class"));
		jlNumber = (ReferenceType) world.resolve(UnresolvedType.forSignature("Ljava/lang/Number;"));
		javaLangObject = (ReferenceType) world.resolve(UnresolvedType.OBJECT);
		extendsClass = new BoundedReferenceType(javaLangClass, true, world);
		superClass = new BoundedReferenceType(javaLangClass, false, world);
		extendsWithExtras = new BoundedReferenceType(javaLangClass, true, world, new ReferenceType[] { (ReferenceType) world
				.resolve(UnresolvedType.forName("java/util/List")) });
	}

	public void testConstructionByNameAndVariable() {
		TypeVariable tv = new TypeVariable("T", javaLangClass);
		TypeVariableReferenceType tvrt = new TypeVariableReferenceType(tv, world);
		assertEquals("T", tvrt.getTypeVariable().getName());
		assertEquals(javaLangClass, tvrt.getTypeVariable().getUpperBound());
	}

	public void testBounds() {
		// Load up the testclass from below
		ResolvedType testerClass = world.resolve(Tester1.class.getName());
		ResolvedType genericTesterClass = testerClass.getGenericType();

		// Check the declaration type variable
		TypeVariable[] typevars = genericTesterClass.getTypeVariables();
		TypeVariable typevar = typevars[0];
		assertEquals(jlNumber, typevar.getUpperBound());
		assertEquals("T", typevar.getName());
		ResolvedMember member = genericTesterClass.getDeclaredMethods()[1];

		// getParameterTypes() returning the erased parameter
		UnresolvedType param = member.getParameterTypes()[0];
		assertEquals(jlNumber, param);

		// Check the type variable reference
		TypeVariableReferenceType tvReference = (TypeVariableReferenceType) member.getGenericParameterTypes()[0];
		assertEquals("T", tvReference.getTypeVariableName());
		assertEquals(jlNumber, tvReference.getUpperBound());
		assertEquals(jlNumber, tvReference.getDelegate().getResolvedTypeX());
	}

	class Tester1<T extends Number> {
		public void method(T t) {
		}
	}

}
