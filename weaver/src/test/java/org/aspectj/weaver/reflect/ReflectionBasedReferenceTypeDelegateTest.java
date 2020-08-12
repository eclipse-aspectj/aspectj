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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.TestCase;

public abstract class ReflectionBasedReferenceTypeDelegateTest extends TestCase {

	protected ReflectionWorld world;
	private ResolvedType objectType;
	private ResolvedType classType;

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
		assertEquals("no entries", 0, objectType.getAnnotations().length);
	}

	public void testGetAnnotationTypes() {
		assertEquals("no entries", 0, objectType.getAnnotationTypes().length);
	}

	public void testGetTypeVariables() {
		assertEquals("no entries", 0, objectType.getTypeVariables().length);
	}

	public void testGetPerClause() {
		assertNull(objectType.getPerClause());
	}

	public void testGetModifiers() {
		assertEquals(Object.class.getModifiers(), objectType.getModifiers());
	}

	public void testGetSuperclass() {
		assertTrue("Superclass of object should be null, but it is: " + objectType.getSuperclass(),
				objectType.getSuperclass() == null);
		assertEquals(objectType, world.resolve("java.lang.Class").getSuperclass());
		ResolvedType d = world.resolve("reflect.tests.D");
		assertEquals(world.resolve("reflect.tests.C"), d.getSuperclass());
	}

	protected int findMethod(String name, ResolvedMember[] methods) {
		for (int i = 0; i < methods.length; i++) {
			if (name.equals(methods[i].getName())) {
				return i;
			}
		}
		return -1;
	}

	protected int findMethod(String name, int numArgs, ResolvedMember[] methods) {
		for (int i = 0; i < methods.length; i++) {
			if (name.equals(methods[i].getName()) && (methods[i].getParameterTypes().length == numArgs)) {
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
		assertEquals(3, methods.length);
		int idx = findMethod("foo", methods);
		assertTrue(idx > -1);

		assertEquals(world.resolve("java.lang.String"), methods[idx].getReturnType());
		assertEquals(1, methods[idx].getParameterTypes().length);
		assertEquals(objectType, methods[idx].getParameterTypes()[0]);
		assertEquals(1, methods[idx].getExceptions().length);
		assertEquals(world.resolve("java.lang.Exception"), methods[idx].getExceptions()[0]);
		int baridx = findMethod("bar", methods);
		int initidx = findMethod("<init>", methods);
		assertTrue(baridx > -1);
		assertTrue(initidx > -1);
		assertTrue(baridx != initidx && baridx != idx && idx <= 2 && initidx <= 2 && baridx <= 2);

		ResolvedType d = world.resolve("reflect.tests.D");
		methods = d.getDeclaredMethods();
		assertEquals(2, methods.length);

		classType = world.resolve("java.lang.Class");
		methods = classType.getDeclaredMethods();
		assertEquals(Class.class.getDeclaredMethods().length + Class.class.getDeclaredConstructors().length, methods.length);
	}

	public void testGetDeclaredFields() {
		ResolvedMember[] fields = objectType.getDeclaredFields();
		assertEquals(0, fields.length);

		ResolvedType c = world.resolve("reflect.tests.C");
		fields = c.getDeclaredFields();

		assertEquals(2, fields.length);
		assertEquals("f", fields[0].getName());
		assertEquals("s", fields[1].getName());
		assertEquals(UnresolvedType.INT, fields[0].getReturnType());
		assertEquals(world.resolve("java.lang.String"), fields[1].getReturnType());
	}

	public void testGetDeclaredInterfaces() {
		ResolvedType[] interfaces = objectType.getDeclaredInterfaces();
		assertEquals(0, interfaces.length);

		ResolvedType d = world.resolve("reflect.tests.D");
		interfaces = d.getDeclaredInterfaces();
		assertEquals(1, interfaces.length);
		assertEquals(world.resolve("java.io.Serializable"), interfaces[0]);
	}

	public void testGetDeclaredPointcuts() {
		ResolvedMember[] pointcuts = objectType.getDeclaredPointcuts();
		assertEquals(0, pointcuts.length);
	}

	public void testSerializableSuperclass() {
		ResolvedType serializableType = world.resolve("java.io.Serializable");
		ResolvedType superType = serializableType.getSuperclass();
		assertTrue("Superclass of serializable should be Object but was " + superType, superType.equals(UnresolvedType.OBJECT));

		BcelWorld bcelworld = new BcelWorld();
		bcelworld.setBehaveInJava5Way(true);
		ResolvedType bcelSupertype = bcelworld.resolve(UnresolvedType.SERIALIZABLE).getSuperclass();
		assertTrue("Should be null but is " + bcelSupertype, bcelSupertype.equals(UnresolvedType.OBJECT));
	}

	public void testSubinterfaceSuperclass() {
		ResolvedType ifaceType = world.resolve("java.security.Key");
		ResolvedType superType = ifaceType.getSuperclass();
		assertTrue("Superclass should be Object but was " + superType, superType.equals(UnresolvedType.OBJECT));

		BcelWorld bcelworld = new BcelWorld();
		bcelworld.setBehaveInJava5Way(true);
		ResolvedType bcelSupertype = bcelworld.resolve("java.security.Key").getSuperclass();
		assertTrue("Should be null but is " + bcelSupertype, bcelSupertype.equals(UnresolvedType.OBJECT));
	}

	public void testVoidSuperclass() {
		ResolvedType voidType = world.resolve(Void.TYPE);
		ResolvedType superType = voidType.getSuperclass();
		assertNull(superType);

		BcelWorld bcelworld = new BcelWorld();
		bcelworld.setBehaveInJava5Way(true);
		ResolvedType bcelSupertype = bcelworld.resolve("void").getSuperclass();
		assertTrue("Should be null but is " + bcelSupertype, bcelSupertype == null);
	}

	public void testIntSuperclass() {
		ResolvedType voidType = world.resolve(Integer.TYPE);
		ResolvedType superType = voidType.getSuperclass();
		assertNull(superType);

		BcelWorld bcelworld = new BcelWorld();
		bcelworld.setBehaveInJava5Way(true);
		ResolvedType bcelSupertype = bcelworld.resolve("int").getSuperclass();
		assertTrue("Should be null but is " + bcelSupertype, bcelSupertype == null);
	}

	public void testGenericInterfaceSuperclass_BcelWorldResolution() {
		BcelWorld bcelworld = new BcelWorld();
		bcelworld.setBehaveInJava5Way(true);

		UnresolvedType javaUtilMap = UnresolvedType.forName("java.util.Map");

		ReferenceType rawType = (ReferenceType) bcelworld.resolve(javaUtilMap);
		assertTrue("Should be the raw type ?!? " + rawType.getTypekind(), rawType.isRawType());

		ReferenceType genericType = (ReferenceType) rawType.getGenericType();
		assertTrue("Should be the generic type ?!? " + genericType.getTypekind(), genericType.isGenericType());

		ResolvedType rt = rawType.getSuperclass();
		assertTrue("Superclass for Map raw type should be Object but was " + rt, rt.equals(UnresolvedType.OBJECT));

		ResolvedType rt2 = genericType.getSuperclass();
		assertTrue("Superclass for Map generic type should be Object but was " + rt2, rt2.equals(UnresolvedType.OBJECT));
	}

	// FIXME asc maybe. The reflection list of methods returned doesn't include <clinit> (the static initializer) ... is that really
	// a problem.
	public void testCompareSubclassDelegates() {

		boolean barfIfClinitMissing = false;
		world.setBehaveInJava5Way(true);

		BcelWorld bcelWorld = new BcelWorld(getClass().getClassLoader(), IMessageHandler.THROW, null);
		bcelWorld.setBehaveInJava5Way(true);
		UnresolvedType javaUtilHashMap = UnresolvedType.forName("java.util.HashMap");
		ReferenceType rawType = (ReferenceType) bcelWorld.resolve(javaUtilHashMap);

		ReferenceType rawReflectType = (ReferenceType) world.resolve(javaUtilHashMap);
		ResolvedMember[] rms1 = rawType.getDelegate().getDeclaredMethods();
		ResolvedMember[] rms2 = rawReflectType.getDelegate().getDeclaredMethods();
		StringBuffer errors = new StringBuffer();
		Set one = new HashSet();
		for (ResolvedMember item : rms1) {
			one.add(item.toString());
		}
		Set two = new HashSet();
		for (ResolvedMember value : rms2) {
			two.add(value.toString());
		}
		for (ResolvedMember member : rms2) {
			if (!one.contains(member.toString())) {
				errors.append("Couldn't find " + member.toString() + " in the bcel set\n");
			}
		}
		for (ResolvedMember resolvedMember : rms1) {
			if (!two.contains(resolvedMember.toString())) {
				if (!barfIfClinitMissing && resolvedMember.getName().equals("<clinit>"))
					continue;
				errors.append("Couldn't find " + resolvedMember.toString() + " in the reflection set\n");
			}
		}
		assertTrue("Errors:" + errors.toString(), errors.length() == 0);

		// the good old ibm vm seems to offer clinit through its reflection support (see pr145322)
		if (rms1.length == rms2.length)
			return;
		if (barfIfClinitMissing) {
			// the numbers must be exact
			assertEquals(rms1.length, rms2.length);
		} else {
			// the numbers can be out by one in favour of bcel
			if (rms1.length != (rms2.length + 1)) {
				for (int i = 0; i < rms1.length; i++) {
					System.err.println("bcel" + i + " is " + rms1[i]);
				}
				for (int i = 0; i < rms2.length; i++) {
					System.err.println("refl" + i + " is " + rms2[i]);
				}
			}
			assertTrue("Should be one extra (clinit) in BCEL case, but bcel=" + rms1.length + " reflect=" + rms2.length,
					rms1.length == rms2.length + 1);
		}
	}

	public void testArrayArgsSig() throws Exception {
		Method invokeMethod = Method.class.getMethod("invoke", new Class[] { Object.class, Object[].class });
		ResolvedMember reflectionMethod = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMethod(invokeMethod, world);
		String exp = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;";
		assertTrue("Expected: \n" + exp + "\n but got:\n" + reflectionMethod.getSignature(), reflectionMethod.getSignature()
				.equals(exp));
	}

	// todo: array of int

	protected void setUp() throws Exception {
		world = new ReflectionWorld(getClass().getClassLoader());
		objectType = world.resolve("java.lang.Object");
	}
}
