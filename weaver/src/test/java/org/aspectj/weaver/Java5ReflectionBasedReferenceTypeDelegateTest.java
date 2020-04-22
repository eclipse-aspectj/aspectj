/* *******************************************************************
 * Copyright (c) 2005-2017 Contributors.
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import org.aspectj.weaver.reflect.ReflectionBasedReferenceTypeDelegateTest;

public class Java5ReflectionBasedReferenceTypeDelegateTest extends ReflectionBasedReferenceTypeDelegateTest {

	/**
	 * Let's play about with a generic type and ensure we can work with it in a reflective world.
	 */
	public void testResolveGeneric() {
		UnresolvedType collectionType = UnresolvedType.forName("java.util.Collection");
		world.resolve(collectionType).getRawType().resolve(world);
		ResolvedMember[] methods = world.resolve(collectionType).getDeclaredMethods();
		int i = -1;
		for (int j=0;j<methods.length;j++) {
			ResolvedMember method = methods[j];
			if (method.getName().equals("toArray") && method.getParameterSignature().equals("([TT;)")) {
				i = j;
			}
		}
		assertTrue("Couldn't find 'toArray' in the set of methods? ", i != -1);
		// String expectedSignature = "java.lang.Object[] java.util.Collection.toArray(java.lang.Object[])";
		String expectedSignature = "([Ljava/lang/Object;)[Ljava/lang/Object;";
		
		assertTrue("Expected signature of '" + expectedSignature + "' but it was '" + methods[i].getSignatureErased(), methods[i]
				.getSignatureErased().equals(expectedSignature));
	}

	/**
	 * Can we resolve the dreaded Enum type...
	 */
	public void testResolveEnum() {
		ResolvedType enumType = world.resolve("java.lang.Enum");
		assertTrue("Should be the raw type but is " + enumType.typeKind, enumType.isRawType());
		ResolvedType theGenericEnumType = enumType.getGenericType();
		assertTrue("Should have a type variable ", theGenericEnumType.getTypeVariables().length > 0);
		TypeVariable tv = theGenericEnumType.getTypeVariables()[0];
		String expected = "TypeVar E extends java.lang.Enum<E>";
		assertTrue("Type variable should be '" + expected + "' but is '" + tv + "'", tv.toString().equals(expected));
	}

	public void testResolveClass() {
		world.resolve("java.lang.Class").getGenericType();
	}

	public void testGenericInterfaceSuperclass_ReflectionWorldResolution() {

		UnresolvedType javaUtilMap = UnresolvedType.forName("java.util.Map");

		ReferenceType rawType = (ReferenceType) world.resolve(javaUtilMap);
		assertTrue("Should be the raw type ?!? " + rawType.getTypekind(), rawType.isRawType());

		ReferenceType genericType = rawType.getGenericType();
		assertTrue("Should be the generic type ?!? " + genericType.getTypekind(), genericType.isGenericType());

		ResolvedType rt = rawType.getSuperclass();
		assertTrue("Superclass for Map raw type should be Object but was " + rt, rt.equals(UnresolvedType.OBJECT));

		ResolvedType rt2 = genericType.getSuperclass();
		assertTrue("Superclass for Map generic type should be Object but was " + rt2, rt2.equals(UnresolvedType.OBJECT));
	}

	/**
	 * This is testing the optimization in the reflective annotation finder to verify that if you only want runtime
	 * annotation info then we use reflection and don't go digging through the classfile bytes.
	 */
	public void testAnnotationFinderClassRetention() throws Exception {
		ResolvedType type = world.resolve(AnnoTesting.class.getName());
		ResolvedMember[] ms = type.getDeclaredMethods();
		
		ResolvedMember methodWithOnlyClassLevelAnnotation = ms[findMethod("a", ms)];
		ResolvedMember methodWithOnlyRuntimeLevelAnnotation = ms[findMethod("b", ms)];
		ResolvedMember methodWithClassAndRuntimeLevelAnnotations = ms[findMethod("c", ms)];
		ResolvedMember methodWithClassAndRuntimeLevelAnnotations2 = ms[findMethod("d", ms)];
		
		assertTrue(methodWithOnlyClassLevelAnnotation.hasAnnotation(world.resolve(AnnoClass.class.getName())));
		assertTrue(methodWithOnlyRuntimeLevelAnnotation.hasAnnotation(world.resolve(AnnoRuntime.class.getName())));
		
		// This is the tricky scenario.
		
		// When asking about the runtime level annotations it should not go digging into bcel
		assertTrue(methodWithClassAndRuntimeLevelAnnotations.hasAnnotation(world.resolve(AnnoRuntime.class.getName())));
		
		Field annotationsField = ResolvedMemberImpl.class.getDeclaredField("annotationTypes");
		annotationsField.setAccessible(true);
		ResolvedType[] annoTypes = (ResolvedType[])annotationsField.get(methodWithClassAndRuntimeLevelAnnotations);

		// Should only be the runtime one here
		assertEquals(1, annoTypes.length);
		
		// But when you do ask again and this time for class level, it should redo the unpack and pull both runtime and class out
		assertTrue(methodWithClassAndRuntimeLevelAnnotations.hasAnnotation(world.resolve(AnnoClass.class.getName())));

		annotationsField.setAccessible(true);
		annoTypes = (ResolvedType[])annotationsField.get(methodWithClassAndRuntimeLevelAnnotations);

		// Now both should be there
		assertEquals(2, annoTypes.length);

		assertTrue(methodWithClassAndRuntimeLevelAnnotations2.hasAnnotation(world.resolve(AnnoRuntime.class.getName())));
		// now ask for 'all annotations' via another route, this should reunpack and get them all
		ResolvedType[] annotations = methodWithClassAndRuntimeLevelAnnotations2.getAnnotationTypes();
		assertEquals(2,annotations.length);
	}
	
	@Retention(RetentionPolicy.CLASS)
	@interface AnnoClass {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@interface AnnoRuntime {}
	
	class AnnoTesting {
		
		@AnnoClass
		public void a() {}
		
		@AnnoRuntime
		public void b() {}
		
		@AnnoClass @AnnoRuntime
		public void c() {}
		
		@AnnoClass @AnnoRuntime
		public void d() {}

	}

}