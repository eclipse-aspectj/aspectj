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

import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

import junit.framework.TestCase;

public class ReflectionBasedReferenceTypeDelegateTest extends TestCase {

	private World world;
	private ResolvedType objectType;
	
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
		assertNull(objectType.getSuperclass());
		assertEquals(objectType,world.resolve("java.lang.Class").getSuperclass());
		ResolvedType d = world.resolve("reflect.tests.D");
		assertEquals(world.resolve("reflect.tests.C"),d.getSuperclass());
	}
	
	public void testGetDeclaredMethods() {
		ResolvedMember[] methods = objectType.getDeclaredMethods();
		assertEquals(13,methods.length);
		
		ResolvedType c = world.resolve("reflect.tests.C");
		methods = c.getDeclaredMethods();
		assertEquals(3,methods.length);
		assertEquals("foo",methods[0].getName());
		assertEquals(world.resolve("java.lang.String"),methods[0].getReturnType());
		assertEquals(1, methods[0].getParameterTypes().length);
		assertEquals(objectType,methods[0].getParameterTypes()[0]);
		assertEquals(1,methods[0].getExceptions().length);
		assertEquals(world.resolve("java.lang.Exception"),methods[0].getExceptions()[0]);
		assertEquals("bar",methods[1].getName());
		assertEquals("init",methods[2].getName());
		
		ResolvedType d = world.resolve("reflect.tests.D");
		methods = d.getDeclaredMethods();
		assertEquals(2,methods.length);
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
