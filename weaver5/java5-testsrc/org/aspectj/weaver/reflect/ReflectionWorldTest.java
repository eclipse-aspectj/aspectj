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
import java.lang.reflect.Type;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.TestCase;

public class ReflectionWorldTest extends TestCase {

	public void testDelegateCreation() {
		World world = new ReflectionWorld(getClass().getClassLoader());
		ResolvedType rt = world.resolve("java.lang.Object");
		assertNotNull(rt);
		assertEquals("Ljava/lang/Object;", rt.getSignature());
	}

	public void testArrayTypes() {
		IReflectionWorld world = new ReflectionWorld(getClass().getClassLoader());
		String[] strArray = new String[1];
		ResolvedType rt = world.resolve(strArray.getClass());
		assertTrue(rt.isArray());
	}

	public void testPrimitiveTypes() {
		IReflectionWorld world = new ReflectionWorld(getClass().getClassLoader());
		assertEquals("int", UnresolvedType.INT, world.resolve(int.class));
		assertEquals("void", UnresolvedType.VOID, world.resolve(void.class));
	}
	
	public void testTypeConversions_509327() throws Exception {
		ReflectionWorld rWorld = new ReflectionWorld(getClass().getClassLoader());
		JavaLangTypeToResolvedTypeConverter converter = new JavaLangTypeToResolvedTypeConverter(rWorld);

		// Check basic conversion of String to String
		Method method = TestClass.class.getDeclaredMethod("m");
		Type stringType = method.getGenericReturnType();
		assertEquals("java.lang.String",stringType.getTypeName());
		ResolvedType stringResolvedType = converter.fromType(stringType);
		assertEquals("java.lang.String",stringResolvedType.getName());
		
		// public String m() { return ""; }
		method = TestClass2.class.getDeclaredMethod("m");
		stringType = method.getGenericReturnType();
		assertEquals("java.lang.String",stringType.getTypeName());
		stringResolvedType = converter.fromType(stringType);
		assertEquals("java.lang.String",stringResolvedType.getName());
		
		// Verify that the conversion process creates the same thing as the bcel unpacking

		// Here the return type is a non-static inner of a generic class
		// public Inner m2() { return null; }
		method = TestClass2.class.getDeclaredMethod("m2");
		Type innerType = method.getGenericReturnType();
		assertEquals("org.aspectj.weaver.reflect.ReflectionWorldTest.org.aspectj.weaver.reflect.ReflectionWorldTest$TestClass2<T>.Inner",innerType.getTypeName());
		ResolvedType rType_Inner = converter.fromType(innerType);
		assertEquals("Lorg/aspectj/weaver/reflect/ReflectionWorldTest$TestClass2$Inner;",rType_Inner.getSignature());
		assertEquals(UnresolvedType.TypeKind.SIMPLE,rType_Inner.getTypekind());
		ResolvedType rType_Outer = rType_Inner.getOuterClass();
		assertEquals("Lorg/aspectj/weaver/reflect/ReflectionWorldTest$TestClass2;",rType_Outer.getSignature());
		
		BcelWorld bWorld = new BcelWorld(getClass().getClassLoader(), IMessageHandler.THROW, null);
		bWorld.setBehaveInJava5Way(true);
		UnresolvedType javaUtilHashMap = UnresolvedType.forName("java.util.HashMap");
		ReferenceType rawType = (ReferenceType) bWorld.resolve(javaUtilHashMap);
		assertNotNull(rawType);
		
		// Now use bcel to resolve the same m2 method, and compare the signatures of the return types
		ResolvedType bResolved_TestClass2 = bWorld.resolve(UnresolvedType.forName(TestClass2.class.getName()));
		assertNotNull(bResolved_TestClass2);
		ResolvedMember bMethod_m2 = findMethod(bResolved_TestClass2,"m2");
		ResolvedType bType_Inner = (ResolvedType) bMethod_m2.getReturnType();
		assertEquals("Lorg/aspectj/weaver/reflect/ReflectionWorldTest$TestClass2$Inner;",bType_Inner.getSignature());
		assertEquals(UnresolvedType.TypeKind.SIMPLE,bType_Inner.getTypekind());
		ResolvedType bType_Outer = bType_Inner.getOuterClass();
		assertEquals("Lorg/aspectj/weaver/reflect/ReflectionWorldTest$TestClass2;",bType_Outer.getSignature());

		assertEquals(bType_Inner.getSignature(),rType_Inner.getSignature());
		assertEquals(bType_Outer.getSignature(),rType_Outer.getSignature());
	}
	

	public void testTypeConversions_509327_2() throws Exception {
		ReflectionWorld world = new ReflectionWorld(getClass().getClassLoader());
		JavaLangTypeToResolvedTypeConverter converter = new JavaLangTypeToResolvedTypeConverter(world);
		BcelWorld bWorld = new BcelWorld(getClass().getClassLoader(), IMessageHandler.THROW, null);
		bWorld.setBehaveInJava5Way(true);
		
		// Slightly more advanced, now the method is returning a parameterized form of the outer
		// generic class
		
		// public TestClass2<String>.Inner m3() { return new TestClass2<String>.Inner("Foo"); }
		Method method = TestClass2.class.getDeclaredMethod("m3");
		Type type_ParameterizedInner = method.getGenericReturnType();
		assertEquals("org.aspectj.weaver.reflect.ReflectionWorldTest.org.aspectj.weaver.reflect.ReflectionWorldTest$TestClass2<java.lang.String>.Inner",type_ParameterizedInner.getTypeName());
		ResolvedType rType_ParameterizedInner = converter.fromType(type_ParameterizedInner);
		// NOTE: DECLARED PARAMETERIZATION OF OUTER IS LOST
		assertEquals("Lorg/aspectj/weaver/reflect/ReflectionWorldTest$TestClass2$Inner;",rType_ParameterizedInner.getSignature());

		ResolvedType bResolved_TestClass2 = bWorld.resolve(UnresolvedType.forName(TestClass2.class.getName()));
		assertNotNull(bResolved_TestClass2);
		ResolvedMember bMethod_m3 = findMethod(bResolved_TestClass2,"m3");
		ResolvedType bType_Inner = (ResolvedType) bMethod_m3.getReturnType();
		// NOTE: DECLARED PARAMETERIZATION OF OUTER IS LOST
		assertEquals("Lorg/aspectj/weaver/reflect/ReflectionWorldTest$TestClass2$Inner;",bType_Inner.getSignature());

		assertEquals(UnresolvedType.TypeKind.SIMPLE,bType_Inner.getTypekind());
		ResolvedType bType_Outer = bType_Inner.getOuterClass();
			
		// Fields seem to lose it too, although the backinggenericmember has the info
//		ResolvedMember bField_f = findField(bResolved_TestClass2,"f");
//		ResolvedMember backingGenericMember = bField_f.getBackingGenericMember();
//		System.out.println(backingGenericMember);
//		System.out.println(backingGenericMember.getGenericReturnType());
//		System.out.println(bField_f);
//		System.out.println(bField_f.getSignature());
//		System.out.println(bField_f.getGenericReturnType());
	}
	
//	public void testbar() throws Exception {
//		ReflectionWorld world = new ReflectionWorld(getClass().getClassLoader());
//		JavaLangTypeToResolvedTypeConverter converter = new JavaLangTypeToResolvedTypeConverter(world);
//		
//		// public TestClass2<String>.Inner m3() { return new TestClass2<String>.Inner("Foo"); }
//		Method method = TestClass2.class.getDeclaredMethod("m3");
//		Type type_ParameterizedInner = method.getGenericReturnType();
//		assertEquals("org.aspectj.weaver.reflect.ReflectionWorldTest.org.aspectj.weaver.reflect.ReflectionWorldTest$TestClass2<java.lang.String>.Inner",type_ParameterizedInner.getTypeName());
//		ResolvedType rType_ParameterizedInner = converter.fromType(type_ParameterizedInner);
//		System.out.println(rType_ParameterizedInner);	
//		System.out.println(type_ParameterizedInner.getTypeName());
//	}
//	
//	public void testfoo() {
//		ReflectionWorld world = new ReflectionWorld(getClass().getClassLoader());
//		JavaLangTypeToResolvedTypeConverter converter = new JavaLangTypeToResolvedTypeConverter(world);
//		BcelWorld bWorld = new BcelWorld(getClass().getClassLoader(), IMessageHandler.THROW, null);
//		bWorld.setBehaveInJava5Way(true);
//		
//
//		ResolvedType bResolved_TestClass2 = bWorld.resolve(UnresolvedType.forName(TestClass2.class.getName()));
//		ResolvedMember bField_f = findField(bResolved_TestClass2,"f");		
//		System.out.println(bField_f);
//		System.out.println(bField_f.getGenericReturnType());
//		System.out.println(bField_f.getReturnType());
//		System.out.println(bField_f.getBackingGenericMember().getGenericReturnType());
//	}

	static class TestClass {
		public String m() { return ""; }
	}
	
	static class TestClass2<T> {
		class Inner {
			T t;
			Inner(T t) {
				this.t = t;
			}
		}
		public String m() { return ""; }
		public Inner m2() { return null; }
		public TestClass2<String> f;
		public TestClass2<String>.Inner m3() { return new TestClass2<String>.Inner("Foo"); }
	}

	private ResolvedMember findMethod(ResolvedType resolvedType, String methodName) {
		for (ResolvedMember method: resolvedType.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
	
	private ResolvedMember findField(ResolvedType resolvedType, String fieldName) {
		for (ResolvedMember field: resolvedType.getDeclaredFields()) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		return null;
	}


}
