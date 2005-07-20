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
 * For a parameterized reference type, the methods that return members
 *  - getDeclaredFields
 *  - getDeclaredMethods
 *  - getDeclaredInterfaces
 *  - getDeclaredPointcuts
 *  should have any type variables substituted by the given type parameter before
 *  being returned.
 */
public class ParameterizedReferenceTypeTestCase extends TestCase {

	BcelWorld world;
	ReferenceType listOfString;
	
	public void testDeclaredMethodWithParameter() {
		ResolvedMember[] methods = listOfString.getDeclaredMethods();
		ResolvedMember add = null;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals("add")) {
				if (methods[i].getParameterTypes().length == 1) {
					add = methods[i];
					break;
				}
			}
		}
		UnresolvedType parameterType = add.getParameterTypes()[0];
		assertEquals("Ljava/lang/String;",parameterType.getSignature());
		
		ResolvedMember get = null;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals("get")) {
				if (methods[i].getParameterTypes().length == 1) {
					get = methods[i];
					break;
				}
			}
		}
		UnresolvedType returnType = get.getReturnType();
		assertEquals("Ljava/lang/String;",returnType.getSignature());
		
	}
	
	public void testDeclaredMethodWithParameterizedReturnType() {
		ResolvedMember[] methods = listOfString.getDeclaredMethods();
		ResolvedMember iterator = null;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals("iterator")) {
				iterator = methods[i];
				break;
			}
		}
		UnresolvedType returnType = iterator.getReturnType();
		assertEquals("Pjava/util/Iterator<Ljava/lang/String;>;",returnType.getSignature());
		
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		world = new BcelWorld();
		listOfString = (ReferenceType)
			TypeFactory.createTypeFromSignature("Pjava/util/List<Ljava/lang/String;>;").resolve(world);
	}
}
