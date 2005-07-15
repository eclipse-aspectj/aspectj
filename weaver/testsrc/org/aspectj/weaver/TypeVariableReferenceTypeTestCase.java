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
	ReferenceType javaLangObject;
	BoundedReferenceType extendsClass;
	BoundedReferenceType superClass;
	BoundedReferenceType extendsWithExtras;
	BcelWorld world;
	
	public void testConstructionByNameAndVariable() {
		TypeVariable tv = new TypeVariable("T",javaLangClass);
		TypeVariableReferenceType tvrt = new TypeVariableReferenceType(tv,world);
		assertEquals("T",tvrt.getTypeVariable().getName());
		assertEquals(javaLangClass,tvrt.getUpperBound());
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		world = new BcelWorld();
		javaLangClass = (ReferenceType) world.resolve(UnresolvedType.forName("java/lang/Class"));
		javaLangObject = (ReferenceType) world.resolve(UnresolvedType.OBJECT);
		extendsClass = new BoundedReferenceType(javaLangClass,true,world);
		superClass = new BoundedReferenceType(javaLangClass,false,world);
		extendsWithExtras = new BoundedReferenceType(javaLangClass,true,world,
				new ReferenceType[] {(ReferenceType)world.resolve(UnresolvedType.forName("java/util/List"))});
	}

}
