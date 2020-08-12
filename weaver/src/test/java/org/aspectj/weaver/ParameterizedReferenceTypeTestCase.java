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
		for (ResolvedMember resolvedMember : methods) {
			if (resolvedMember.getName().equals("add")) {
				if (resolvedMember.getParameterTypes().length == 1) {
					add = resolvedMember;
					System.out.println(add);
//					j8: boolean java.util.List<java.lang.String>.add(java.lang.Object)
//					break;
				}
			}
		}
		UnresolvedType parameterType = add.getParameterTypes()[0];
		assertEquals("Ljava/lang/String;",parameterType.getSignature());
		
		ResolvedMember get = null;
		for (ResolvedMember method : methods) {
			if (method.getName().equals("get")) {
				if (method.getParameterTypes().length == 1) {
					get = method;
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
		for (ResolvedMember method : methods) {
			if (method.getName().equals("iterator")) {
				iterator = method;
				break;
			}
		}
		UnresolvedType returnType = iterator.getReturnType();
		assertEquals("Pjava/util/Iterator<Ljava/lang/String;>;",returnType.getSignature());
		
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		world = new BcelWorld();
		world.setBehaveInJava5Way(true);
		listOfString = (ReferenceType)
			TypeFactory.createTypeFromSignature("Pjava/util/List<Ljava/lang/String;>;").resolve(world);
	}
}
