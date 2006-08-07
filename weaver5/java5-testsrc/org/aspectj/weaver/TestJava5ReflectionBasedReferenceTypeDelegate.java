/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Andrew Clement          Initial implementation
 * ******************************************************************/

package org.aspectj.weaver;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.weaver.reflect.ReflectionBasedReferenceTypeDelegateTest;


public class TestJava5ReflectionBasedReferenceTypeDelegate extends ReflectionBasedReferenceTypeDelegateTest {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestJava5ReflectionBasedReferenceTypeDelegate");
		suite.addTestSuite(TestJava5ReflectionBasedReferenceTypeDelegate.class);
		return suite;
	}
	
	
	/**
	 * Let's play about with a generic type and ensure we can work with it in a reflective world.
	 */
	public void testResolveGeneric() {
		UnresolvedType collectionType = UnresolvedType.forName("java.util.Collection");
		world.resolve(collectionType).getRawType().resolve(world);
		ResolvedMember[] methods = world.resolve(collectionType).getDeclaredMethods();
		int i = findMethod("toArray", 1, methods);
		assertTrue("Couldn't find 'toArray' in the set of methods? "+methods,i != -1);
		String expectedSignature = "java.lang.Object[] java.util.Collection.toArray(java.lang.Object[])";
		assertTrue("Expected signature of '"+expectedSignature+"' but it was '"+methods[i],methods[i].toString().equals(expectedSignature));
	}

	/**
	 * Can we resolve the dreaded Enum type...
	 */
	public void testResolveEnum() {
		ResolvedType enumType = world.resolve("java.lang.Enum");
		assertTrue("Should be the raw type but is "+enumType.typeKind,enumType.isRawType());
		ResolvedType theGenericEnumType = enumType.getGenericType();
		assertTrue("Should have a type variable ",theGenericEnumType.getTypeVariables().length>0);
		TypeVariable tv = theGenericEnumType.getTypeVariables()[0];
		String expected = "TypeVar E extends java.lang.Enum<E>";
		assertTrue("Type variable should be '"+expected+"' but is '"+tv+"'",tv.toString().equals(expected));
	}
	
	public void testResolveClass() {
		world.resolve("java.lang.Class").getGenericType();		
	}
	
    public void testGenericInterfaceSuperclass_ReflectionWorldResolution() {
        
        UnresolvedType javaUtilMap = UnresolvedType.forName("java.util.Map");
        
        ReferenceType rawType = (ReferenceType) world.resolve(javaUtilMap);
        assertTrue("Should be the raw type ?!? "+rawType.getTypekind(),rawType.isRawType());
        
        ReferenceType genericType = (ReferenceType)rawType.getGenericType();
        assertTrue("Should be the generic type ?!? "+genericType.getTypekind(),genericType.isGenericType());
        
        ResolvedType rt = rawType.getSuperclass();
        assertTrue("Superclass for Map raw type should be Object but was "+rt,rt.equals(UnresolvedType.OBJECT));     
        
        ResolvedType rt2 = genericType.getSuperclass();
        assertTrue("Superclass for Map generic type should be Object but was "+rt2,rt2.equals(UnresolvedType.OBJECT));       
    }
	
}