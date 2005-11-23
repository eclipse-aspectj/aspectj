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
package org.aspectj.weaver.reflect;


import junit.framework.TestCase;

import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;

public class ReflectionBasedReferenceTypeDelegateTest extends TestCase {

	protected ReflectionWorld world;
	private ResolvedType objectType;
	private ResolvedType classType;
	private ResolvedType enumType;

	
	public void testIsAspect() {
		assertFalse(objectType.isAspect());
	}
	
	public void testIsAnnotationStyleAspect() {
		assertFalse(objectType.isAnnotationStyleAspect());
	}
	
	public void testIsInterface() {
		assertFalse(objectType.isInterface());
		assertTrue(world.resolve("java.io.Serializable").isInterface());
	}
	
	public void testIsEnum() {
		assertFalse(objectType.isEnum());
	}
	
	public void testIsAnnotation() {
		assertFalse(objectType.isAnnotation());
	}
	
	public void testIsAnnotationWithRuntimeRetention() {
		assertFalse(objectType.isAnnotationWithRuntimeRetention());
	}
	
	public void testIsClass() {
		assertTrue(objectType.isClass());
		assertFalse(world.resolve("java.io.Serializable").isClass());
	}
	
	public void testIsGeneric() {
		assertFalse(objectType.isGenericType());
	}
	
	public void testIsExposedToWeaver() {
		assertFalse(objectType.isExposedToWeaver());
	}
	
	public void testHasAnnotation() {
		assertFalse(objectType.hasAnnotation(UnresolvedType.forName("Foo")));
	}
	
	public void testGetAnnotations() {
		assertEquals("no entries",0,objectType.getAnnotations().length);
	}
	
	public void testGetAnnotationTypes() {
		assertEquals("no entries",0,objectType.getAnnotationTypes().length);
	}
	
	public void testGetTypeVariables() {
		assertEquals("no entries",0,objectType.getTypeVariables().length);
	}
	
	public void testGetPerClause() {
		assertNull(objectType.getPerClause());
	}
	
	public void testGetModifiers() {
		assertEquals(Object.class.getModifiers(),objectType.getModifiers());
	}
	
	public void testGetSuperclass() {
		assertTrue("Superclass of object should be null, but it is: "+objectType.getSuperclass(),objectType.getSuperclass()==null);
		assertEquals(objectType,world.resolve("java.lang.Class").getSuperclass());
		ResolvedType d = world.resolve("reflect.tests.D");
		assertEquals(world.resolve("reflect.tests.C"),d.getSuperclass());
	}
	

	protected int findMethod(String name, ResolvedMember[] methods) {
		for (int i=0; i<methods.length; i++) {
			if (name.equals(methods[i].getName())) {
				return i;
			}
		}
		return -1;
	}
	
	public void testGetDeclaredMethods() {
		ResolvedMember[] methods = objectType.getDeclaredMethods();
		assertEquals(Object.class.getDeclaredMethods().length + Object.class.getDeclaredConstructors().length, methods.length);
		
		ResolvedType c = world.resolve("reflect.tests.C");
		methods = c.getDeclaredMethods();
		assertEquals(3,methods.length);
		int idx = findMethod("foo", methods);
		assertTrue(idx > -1);
		
		assertEquals(world.resolve("java.lang.String"),methods[idx].getReturnType());
		assertEquals(1, methods[idx].getParameterTypes().length);
		assertEquals(objectType,methods[idx].getParameterTypes()[0]);
		assertEquals(1,methods[idx].getExceptions().length);
		assertEquals(world.resolve("java.lang.Exception"),methods[idx].getExceptions()[0]);
		int baridx = findMethod("bar", methods);
		int initidx = findMethod("init", methods);
		assertTrue(baridx > -1);
		assertTrue(initidx > -1);
		assertTrue(baridx != initidx && baridx != idx && idx <= 2 && initidx <= 2 && baridx <= 2);
		
		ResolvedType d = world.resolve("reflect.tests.D");
		methods = d.getDeclaredMethods();
		assertEquals(2,methods.length);

		classType = world.resolve("java.lang.Class");
		methods = classType.getDeclaredMethods();
		assertEquals(Class.class.getDeclaredMethods().length + Class.class.getDeclaredConstructors().length, methods.length); 
	}
	
	public void testGetDeclaredFields() {
		ResolvedMember[] fields = objectType.getDeclaredFields();
		assertEquals(0,fields.length);

		ResolvedType c = world.resolve("reflect.tests.C");
		fields = c.getDeclaredFields();
		
		assertEquals(2,fields.length);
		assertEquals("f",fields[0].getName());
		assertEquals("s",fields[1].getName());
		assertEquals(ResolvedType.INT,fields[0].getReturnType());
		assertEquals(world.resolve("java.lang.String"),fields[1].getReturnType());
	}
	
	public void testGetDeclaredInterfaces() {
		ResolvedType[] interfaces = objectType.getDeclaredInterfaces();
		assertEquals(0,interfaces.length);

		ResolvedType d = world.resolve("reflect.tests.D");
		interfaces = d.getDeclaredInterfaces();
		assertEquals(1,interfaces.length);
		assertEquals(world.resolve("java.io.Serializable"),interfaces[0]);
    }
	
	public void testGetDeclaredPointcuts() {
		ResolvedMember[] pointcuts = objectType.getDeclaredPointcuts();
		assertEquals(0,pointcuts.length);
	}
	
	protected void setUp() throws Exception {
		world = new ReflectionWorld();
		objectType = world.resolve("java.lang.Object");
	}
}
