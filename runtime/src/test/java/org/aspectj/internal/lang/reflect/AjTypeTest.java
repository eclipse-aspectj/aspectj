/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer       initial implementation 
 * ******************************************************************/
package org.aspectj.internal.lang.reflect;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import junit.framework.TestCase;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;

public class AjTypeTest extends TestCase {

	private AjType<String> stringType;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		stringType = AjTypeSystem.getAjType(String.class);
	}
	
	public void testCreateAjType() {
		assertNotNull("should find type",stringType);
	}
	
	public void testGetName() {
		assertEquals(String.class.getName(),stringType.getName());
	}
	
	public void testGetPackage() {
		assertEquals(String.class.getPackage(),stringType.getPackage());
	}
	
	public void testGetInterfaces() {
		Class[] i1 = String.class.getInterfaces();
		AjType<?>[] i2 = stringType.getInterfaces();
		assertEquals(i1.length,i2.length);
		for (int i = 0; i < i1.length; i++)
			assertEquals(i1[i],i2[i].getJavaClass());
	}
	
	public void testGetModifiers() {
		assertEquals(String.class.getModifiers(),stringType.getModifiers());
	}
	
	public void testGetSupertype() {
		Class<?> stringSuper = String.class.getSuperclass();
		AjType ajSuper = stringType.getSupertype();
		assertEquals(AjTypeSystem.getAjType(stringSuper),ajSuper);		
	}

	public void testObjectSupertype() {
		AjType<?> objectSuper = AjTypeSystem.getAjType(Object.class).getSupertype();
		assertNull(objectSuper);		
	}

	public void testInterfaceSupertype() {
		AjType<?> serializableSuper = AjTypeSystem.getAjType(Serializable.class).getSupertype();
		assertNull(serializableSuper);		
	}
	
	public void testGetGenericSupertype() {
		Type t  = AjTypeSystem.getAjType(Goo.class).getGenericSupertype();
		assertEquals(Foo.class,t);
	}
	
	public void testGetEnclosingMethod() {
		new Goo().foo();
	}

	public void testGetEnclosingConstructor() {
		new Goo();
	}

	public void testGetEnclosingType() {
		AjType t = AjTypeSystem.getAjType(Foo.Z.class);
		assertEquals("org.aspectj.internal.lang.reflect.Foo",t.getEnclosingType().getName());
	}
	
	public void testGetDeclaringType() {
		AjType t = AjTypeSystem.getAjType(Foo.Z.class);
		assertEquals("org.aspectj.internal.lang.reflect.Foo",t.getDeclaringType().getName());
	}
	
	public void testIsAnnotationPresent() {
		AjType<Foo> foo = AjTypeSystem.getAjType(Foo.class);
		AjType<Goo> goo = AjTypeSystem.getAjType(Goo.class);
		assertTrue(foo.isAnnotationPresent(SomeAnn.class));
		assertFalse(goo.isAnnotationPresent(SomeAnn.class));
	}
	
	public void testGetAnnotation() {
		AjType<Foo> foo = AjTypeSystem.getAjType(Foo.class);
		AjType<Goo> goo = AjTypeSystem.getAjType(Goo.class);
		assertNotNull(foo.getAnnotation(SomeAnn.class));
		assertNull(goo.getAnnotation(SomeAnn.class));
	}

	public void testGetAnnotations() {
		AjType<Foo> foo = AjTypeSystem.getAjType(Foo.class);
		AjType<Goo> goo = AjTypeSystem.getAjType(Goo.class);
		assertEquals(1,foo.getAnnotations().length);
		assertEquals(0,goo.getAnnotations().length);
	}

	public void testGetDeclaredAnnotations() {
		AjType<Foo> foo = AjTypeSystem.getAjType(Foo.class);
		AjType<Goo> goo = AjTypeSystem.getAjType(Goo.class);
		assertEquals(0,goo.getDeclaredAnnotations().length);
		assertEquals(1,foo.getDeclaredAnnotations().length);
	}
	
	public void testGetAjTypes() {
		AjType<Foo> foo = AjTypeSystem.getAjType(Foo.class);
		AjType[] fooTypes = foo.getAjTypes();
		assertEquals(1,fooTypes.length);
		assertEquals("org.aspectj.internal.lang.reflect.Foo$Z",fooTypes[0].getName());
	}
	
	public void testGetDeclaredAjTypes() {
		AjType<Foo> foo = AjTypeSystem.getAjType(Foo.class);
		AjType[] fooTypes = foo.getDeclaredAjTypes();
		assertEquals(2,fooTypes.length);
        // Alex -> Adrian: looks like you can not make assumption on the ordering
        String s = " " + fooTypes[0].getName() + " " + fooTypes[1].getName();
        assertTrue(s.contains(" org.aspectj.internal.lang.reflect.Foo$Z"));
		assertTrue(s.contains(" org.aspectj.internal.lang.reflect.Foo$XX"));
	}
	
	public void testGetConstructor() throws Exception {
		Constructor c1 = String.class.getConstructor(String.class);
		Constructor c2 = stringType.getConstructor(stringType);
		assertEquals(c1,c2);
	}
	
	public void testGetConstructors() {
		Constructor[] c1 = String.class.getConstructors();
		Constructor[] c2 = stringType.getConstructors();
		assertEquals(c1.length,c2.length);
		for (int i = 0; i < c1.length; i++)
			assertEquals(c1[i],c2[i]);
	}
	
	public void testGetDeclaredConstructor() throws Exception {
		Constructor c1 = String.class.getDeclaredConstructor(String.class);
		Constructor c2 = stringType.getDeclaredConstructor(stringType);
		assertEquals(c1,c2);
	}
	
	public void testGetDeclaredConstructors() {
		Constructor[] c1 = String.class.getDeclaredConstructors();
		Constructor[] c2 = stringType.getDeclaredConstructors();
		assertEquals(c1.length,c2.length);
		for (int i = 0; i < c1.length; i++)
			assertEquals(c1[i],c2[i]);
	}
	
	public void testGetDeclaredField() throws Exception {
		Field f1 = String.class.getDeclaredField("value");
		Field f2 = stringType.getDeclaredField("value");
		assertEquals(f1,f2);
	}
	
	public void testGetDeclaredFields() {
		Field[] f1 = String.class.getDeclaredFields();
		Field[] f2 = stringType.getDeclaredFields();
		assertEquals(f1.length,f2.length);
		for (int i = 0; i < f1.length; i++)
			assertEquals(f1[i],f2[i]);
	}
	
	public void testGetField() throws Exception {
		AjType<Goo> goo = AjTypeSystem.getAjType(Goo.class);
		assertEquals("g",goo.getField("g").getName());
	}
	
	public void testGetFields() {
		AjType<Goo> goo = AjTypeSystem.getAjType(Goo.class);
		Field[] fields = goo.getFields();
		assertEquals(1,fields.length);
		assertEquals("g",fields[0].getName());
		
	}
	
	public void testGetDeclaredMethod() throws Exception {
		Method m1 = String.class.getDeclaredMethod("toUpperCase");
		Method m2 = stringType.getDeclaredMethod("toUpperCase");
		assertEquals(m1,m2);
	}
	
	public void testGetMethod() throws Exception {
		Method m1 = String.class.getMethod("toUpperCase");
		Method m2 = stringType.getMethod("toUpperCase");
		assertEquals(m1,m2);		
	}
	
	public void testGetDeclaredMethods() {
		Method[] m1 = String.class.getDeclaredMethods();
		Method[] m2 = stringType.getDeclaredMethods();
		assertEquals(m1.length,m2.length);
		for (int i = 0; i < m1.length; i++)
			assertEquals(m1[i],m2[i]);
	}

	public void testGetMethods() {
		Method[] m1 = String.class.getMethods();
		Method[] m2 = stringType.getMethods();
		assertEquals(m1.length,m2.length);
		for (int i = 0; i < m1.length; i++)
			assertEquals(m1[i],m2[i]);
	}
	
	public void testGetEnumConstants() {
		AjType e = AjTypeSystem.getAjType(E.class);
		Object[] consts = e.getEnumConstants();
		assertEquals(3,consts.length);
	}
	
	public void testGetTypeParameters() {
		AjType<Foo> foo = AjTypeSystem.getAjType(Foo.class);
		TypeVariable<Class<Foo>>[] tvs = foo.getTypeParameters();
		assertEquals(1,tvs.length);
		assertEquals("T",tvs[0].getName());
	}
	
	public void testIsEnum() {
		assertFalse(stringType.isEnum());
	}
	
	public void testIsInstance() {
		assertTrue(stringType.isInstance("I am"));
	}
	
	public void testIsInterface() {
		assertFalse(stringType.isInterface());
		assertTrue(AjTypeSystem.getAjType(Serializable.class).isInterface());
	}
	
	public void testIsLocalClass() {
		assertFalse(stringType.isLocalClass());
	}
	
	public void testIsArray() {
		assertFalse(stringType.isArray());
		assertTrue(AjTypeSystem.getAjType(Integer[].class).isArray());
	}
	
	public void testIsPrimitive() {
		assertFalse(stringType.isPrimitive());
		assertTrue(AjTypeSystem.getAjType(boolean.class).isPrimitive());
	}
	
	public void testIsAspect() {
		assertFalse(stringType.isAspect());
	}
	
	public void testIsMemberAspect() {
		assertFalse(stringType.isMemberAspect());
	}
	
	public void testIsPrivileged() {
		assertFalse(stringType.isPrivileged());
	}
	
	public void testEquals() {
		AjType stringTypeTwo = AjTypeSystem.getAjType(String.class);
		assertTrue(stringType.equals(stringTypeTwo));
	}
	
	public void testHashCode() {
		AjType stringTypeTwo = AjTypeSystem.getAjType(String.class);
		assertEquals(stringType.hashCode(),stringTypeTwo.hashCode());		
	}

}

@Retention(RetentionPolicy.RUNTIME)
@interface SomeAnn {}

@SomeAnn
class Foo<T> {
	
	public Foo() {
		class Y { int y; }
		AjType t = AjTypeSystem.getAjType(Y.class);
		Constructor c = t.getEnclosingConstructor();
		if (!c.getName().equals("org.aspectj.internal.lang.reflect.Foo")) throw new RuntimeException("should have been Foo");
	}
	public void foo() {
		class X { int x; }
		AjType t = AjTypeSystem.getAjType(X.class);
		Method m = t.getEnclosingMethod();
		if (!m.getName().equals("foo")) throw new RuntimeException("should have been foo");
	}
	public class Z { int z; }
	class XX { int xx; }
}

class Goo extends Foo {
  @interface IX {}	
  
  public Goo() {
	  super();
  }

  public int g;
  int g2;

}

enum E { A,B,C; }
