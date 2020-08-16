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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.aspectj.util.PartialOrder;
import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.TestCase;

// test cases for Adrian's additions to ReferenceType
// XXX - couldn't find any unit test cases for the rest of the ReferenceType class
@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
public class ReferenceTypeTestCase extends TestCase {

	public void testIsRawTrue() {
		BcelWorld world = new BcelWorld();
		world.setBehaveInJava5Way(true);
		UnresolvedType javaLangClass = UnresolvedType.forName("java.lang.Class");
		ResolvedType rtx = world.resolve(javaLangClass);
		assertTrue("Resolves to reference type", (rtx instanceof ReferenceType));
		ReferenceType rt = (ReferenceType) rtx;
		assertTrue("java.lang.Class is raw", rt.isRawType());
	}

	public void testIsRawFalse() {
		BcelWorld world = new BcelWorld();
		world.setBehaveInJava5Way(true);
		UnresolvedType javaLangObject = UnresolvedType.forName("java.lang.Object");
		ResolvedType rtx = world.resolve(javaLangObject);
		assertTrue("Resolves to reference type", (rtx instanceof ReferenceType));
		ReferenceType rt = (ReferenceType) rtx;
		assertFalse("java.lang.Object is  not raw", rt.isRawType());
	}

	public void testIsGenericTrue() {
		BcelWorld world = new BcelWorld();
		world.setBehaveInJava5Way(true);
		UnresolvedType javaLangClass = UnresolvedType.forName("java.lang.Class");
		ResolvedType rtx = world.resolve(javaLangClass);
		assertTrue("java.lang.Class has underpinning generic type", rtx.getGenericType().isGenericType());
	}

	public void testIsGenericFalse() {
		BcelWorld world = new BcelWorld();
		world.setBehaveInJava5Way(true);
		UnresolvedType javaLangObject = UnresolvedType.forName("java.lang.Object");
		ResolvedType rtx = world.resolve(javaLangObject);
		assertFalse(rtx.isGenericType());
	}

	BcelWorld world;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		world = new BcelWorld();
		world.setBehaveInJava5Way(true);
	}

	public void testCoercion01() {
		ReferenceType listOfString = (ReferenceType) world.resolve(UnresolvedType
				.forSignature("Pjava/util/List<Ljava/lang/String;>;"));
		ReferenceType listOfInteger = (ReferenceType) world.resolve(UnresolvedType
				.forSignature("Pjava/util/List<Ljava/lang/Integer;>;"));
		assertFalse(listOfInteger.isAssignableFrom(listOfString));
		assertFalse(listOfString.isAssignableFrom(listOfInteger));
		assertFalse(listOfInteger.isCoerceableFrom(listOfString));
		assertFalse(listOfString.isCoerceableFrom(listOfInteger));
	}

	public void testAssignable01() {
		List list = new ArrayList();
		List<String> listOfString = new ArrayList<>();
		List<?> listOfSomething = new ArrayList<Integer>();
		List<? extends Number> listOfSomethingNumberish = new ArrayList<Integer>();
		List<? super Double> listOfSomethingSuperDouble = new ArrayList<Number>();
		// interfaces too List<? extends A,B>

		ReferenceType ajList = resolve("Ljava/util/List;");
		ReferenceType ajListOfString = resolve("Pjava/util/List<Ljava/lang/String;>;");
		ReferenceType ajListOfSomething = resolve("Pjava/util/List<*>;");
		ReferenceType ajListOfSomethingNumberish = resolve("Pjava/util/List<+Ljava/lang/Number;>;");
		ReferenceType ajListOfSomethingSuperDouble = resolve("Pjava/util/List<-Ljava/lang/Double;>;");

		// try and write the java equivalent, if it succeeds then check isAssignableFrom() is true
		// if the java is only correct with a cast, check isCoerceableFrom()
		list = listOfString;
		assertTrue(ajList.isAssignableFrom(ajListOfString));
		list = listOfSomething;
		assertTrue(ajList.isAssignableFrom(ajListOfSomething));
		list = listOfSomethingNumberish;
		assertTrue(ajList.isAssignableFrom(ajListOfSomething));
		list = listOfSomethingSuperDouble;
		assertTrue(ajList.isAssignableFrom(ajListOfSomethingSuperDouble));

		listOfString = list; // unchecked conversion to List<String>
		assertFalse(ajListOfString.isAssignableFrom(ajList));
		assertTrue(ajListOfString.isCoerceableFrom(ajListOfSomething));
		// error: listOfString = listOfSomething;
		assertFalse(ajListOfString.isAssignableFrom(ajListOfSomething));
		// error: listOfString = listOfSomethingNumberish;
		assertFalse(ajListOfString.isAssignableFrom(ajListOfSomethingNumberish));
		// error: listOfString = listOfSomethingSuperDouble;
		assertFalse(ajListOfString.isAssignableFrom(ajListOfSomethingSuperDouble));
		// error: listOfString = (List<String>) listOfSomethingSuperDouble;
		assertFalse(ajListOfString.isCoerceableFrom(ajListOfSomethingSuperDouble));

		listOfSomething = list;
		assertTrue(ajListOfSomething.isAssignableFrom(ajList));
		listOfSomething = listOfString;
		assertTrue(ajListOfSomething.isAssignableFrom(ajListOfString));
		listOfSomething = listOfSomethingNumberish;
		assertTrue(ajListOfSomething.isAssignableFrom(ajListOfSomething));
		listOfSomething = listOfSomethingSuperDouble;
		assertTrue(ajListOfSomething.isAssignableFrom(ajListOfSomethingSuperDouble));

		listOfSomethingNumberish = list; // unchecked conversion
		assertFalse(ajListOfSomethingNumberish.isAssignableFrom(ajList));
		assertTrue(ajListOfSomethingNumberish.isCoerceableFrom(ajList));
		// error: listOfSomethingNumberish = listOfString;
		assertFalse(ajListOfSomethingNumberish.isAssignableFrom(ajListOfString));
		assertFalse(ajListOfSomethingNumberish.isCoerceableFrom(ajListOfString));
		// error: listOfSomethingNumberish = listOfSomething;
		assertFalse(ajListOfSomethingNumberish.isAssignableFrom(ajListOfSomething));
		listOfSomethingNumberish = (List<? extends Number>) listOfSomething;
		assertTrue(ajListOfSomethingNumberish.isCoerceableFrom(ajListOfSomething));
		// error: listOfSomethingNumberish = listOfSomethingSuperDouble;
		assertFalse(ajListOfSomethingNumberish.isAssignableFrom(ajListOfSomethingSuperDouble));
		// listOfSomethingNumberish = (List<? extends Number>) listOfSomethingSuperDouble;
		// assertTrue(ajListOfSomethingNumberish.isCoerceableFrom(ajListOfSomethingSuperDouble));
	}

	class C<E extends Number> {
		void m1(List<Integer> e) {
		}

		void m2(List<? extends Number> e) {
		}

		void m3(List<Number> e) {
		}

		void m4(List<?> e) {
		}

		void m5(List<E> e) {
		}

		void m6(List<? extends E> e) {
		}

		void m7(List<? extends List<? extends E>> e) {
		}

		void m8(List e) {
		}

		void m9(E e) {
		}
	}

	class A1 {
	}

	class B1 extends A1 {
	}

	class C1 extends B1 {
	}

	class D1 extends C1 {
	}

	class D2<E2 extends C1> {
		void m5(List<E2> e) {
		}
	}

	public void testAssignable02() {
		List list = new ArrayList();
		List arraylist = null;
		List<String> listOfString = new ArrayList<>();
		List<?> listOfSomething = new ArrayList<Integer>();
		List<?> arrayListOfSomething = null;
		List<Number> listOfNumber = null;
		ArrayList<Number> arrayListOfNumber = null;
		List<? extends Number> arrayListOfSomethingNumberish = null;
		List<? extends Number> listOfSomethingNumberish = new ArrayList<Integer>();
		List<? super Double> listOfSomethingSuperDouble = new ArrayList<Number>();
		List<Integer> listOfInteger = new ArrayList<>();
		ArrayList<String> arrayListOfString;
		ArrayList<Integer> arraylistOfInteger;
		// interfaces too List<? extends A,B>

		ReferenceType ajArrayListOfString = resolve("Pjava/util/ArrayList<Ljava/lang/String;>;");
		ReferenceType ajArrayListOfInteger = resolve("Pjava/util/ArrayList<Ljava/lang/Integer;>;");
		ReferenceType ajArrayListOfNumber = resolve("Pjava/util/ArrayList<Ljava/lang/Number;>;");
		ReferenceType ajArrayListOfSomethingNumberish = resolve("Pjava/util/ArrayList<+Ljava/lang/Number;>;");
		ReferenceType ajList = resolve("Ljava/util/List;");
		ReferenceType ajArrayList = resolve("Ljava/util/ArrayList;");
		ReferenceType ajListOfString = resolve("Pjava/util/List<Ljava/lang/String;>;");
		ReferenceType ajListOfSomething = resolve("Pjava/util/List<*>;");
		ReferenceType ajArrayListOfSomething = resolve("Pjava/util/ArrayList<*>;");
		ReferenceType ajListOfSomethingNumberish = resolve("Pjava/util/List<+Ljava/lang/Number;>;");
		ReferenceType ajListOfSomethingSuperDouble = resolve("Pjava/util/List<-Ljava/lang/Double;>;");
		ReferenceType ajListOfInteger = resolve("Pjava/util/List<Ljava/lang/Integer;>;");
		ReferenceType ajListOfNumber = resolve("Pjava/util/List<Ljava/lang/Number;>;");
		// Effectively, whether the advice matches is based on whether what we pass at the joinpoint could
		// be bound to the specification in the args() pointcut

		// void around(): execution(* C.m1(..)) && args(List<Integer>){} //: Should match (it does)
		assertTrue(ajListOfInteger.isAssignableFrom(ajListOfInteger));

		// void around(): execution(* C.m1(..)) && args(ArrayList<Integer>){}//: Should runtime check (it does!)
		ArrayList<Integer> x = (ArrayList<Integer>) listOfInteger;
		assertFalse(ajArrayListOfInteger.isAssignableFrom(ajListOfInteger));
		assertTrue(ajArrayListOfInteger.isCoerceableFrom(ajListOfInteger));

		// void around(): execution(* C.m1(..)) && args(List<Number>){} // Should not match (it does not!)
		// error: listOfNumber = listOfInteger;
		assertFalse(ajListOfNumber.isAssignableFrom(ajListOfInteger));
		assertFalse(ajListOfNumber.isCoerceableFrom(ajListOfInteger));

		// void around(): execution(* C.m1(..)) && args(ArrayList<Number>){} // Should not match (it does not)
		// error: arrayListOfNumber = listOfInteger;
		assertFalse(ajArrayListOfNumber.isAssignableFrom(ajListOfInteger));
		assertFalse(ajArrayListOfNumber.isCoerceableFrom(ajListOfInteger));

		// void around(): execution(* C.m1(..)) && args(List<? extends Number>){} // Should match (it does)
		listOfSomethingNumberish = listOfInteger;
		assertTrue(ajListOfSomethingNumberish.isAssignableFrom(ajListOfInteger));

		// void around(): execution(* C.m1(..)) && args(ArrayList<? extends Number>){}// Should runtime check (it does!)
		arrayListOfSomethingNumberish = listOfInteger;
		assertFalse(ajArrayListOfSomethingNumberish.isAssignableFrom(ajListOfInteger));
		assertTrue(ajArrayListOfSomethingNumberish.isCoerceableFrom(ajListOfInteger));

		// void around(): execution(* C.m1(..)) && args(List){}// Should match (it does)
		list = listOfInteger;
		assertTrue(ajList.isAssignableFrom(ajListOfInteger));

		// void around(): execution(* C.m1(..)) && args(ArrayList){}//: Should runtime check (it does not match!)
		arraylist = listOfInteger;
		assertFalse(ajArrayList.isAssignableFrom(ajListOfInteger));
		assertTrue(ajArrayList.isCoerceableFrom(ajListOfInteger));

		// void around(): execution(* C.m1(..)) && args(List<?>){}// Should match (it does)
		listOfSomething = listOfInteger;
		assertTrue(ajListOfSomething.isAssignableFrom(ajListOfInteger));

		// void around(): execution(* C.m1(..)) && args(ArrayList<?>){}// Should runtime check (it does not match!)
		arrayListOfSomething = listOfInteger;
		assertFalse(ajArrayListOfSomething.isAssignableFrom(ajListOfInteger));
		assertTrue(ajArrayListOfSomething.isCoerceableFrom(ajListOfInteger));

		// void around(): execution(* C.m1(..)) && args(ArrayList<String>){}// Should not match (it does not match!)
		// error: arrayListOfString = listOfInteger;
		assertFalse(ajArrayListOfString.isAssignableFrom(ajListOfInteger));
		assertFalse(ajArrayListOfString.isCoerceableFrom(ajListOfInteger));
	}

	public void testAssignable03_method_m2() {
		List list = new ArrayList();
		List arraylist = null;
		List<String> listOfString = new ArrayList<>();
		List<?> listOfSomething = new ArrayList<Integer>();
		List<?> arrayListOfSomething = null;
		List<Number> listOfNumber = null;
		List<Number> arrayListOfNumber = null;
		List<Integer> arrayListOfInteger = null;
		List<? extends Number> arrayListOfSomethingNumberish = null;
		List<? extends Number> listOfSomethingNumberish = new ArrayList<Integer>();
		List<? super Double> listOfSomethingSuperDouble = new ArrayList<Number>();
		List<Integer> listOfInteger = new ArrayList<>();
		ArrayList<String> arrayListOfString;
		ArrayList<Integer> arraylistOfInteger;
		// interfaces too List<? extends A,B>

		ReferenceType ajArrayListOfString = resolve("Pjava/util/ArrayList<Ljava/lang/String;>;");
		ReferenceType ajArrayListOfInteger = resolve("Pjava/util/ArrayList<Ljava/lang/Integer;>;");
		ReferenceType ajArrayListOfNumber = resolve("Pjava/util/ArrayList<Ljava/lang/Number;>;");
		ReferenceType ajArrayListOfSomethingNumberish = resolve("Pjava/util/ArrayList<+Ljava/lang/Number;>;");
		ReferenceType ajList = resolve("Ljava/util/List;");
		ReferenceType ajArrayList = resolve("Ljava/util/ArrayList;");
		ReferenceType ajListOfString = resolve("Pjava/util/List<Ljava/lang/String;>;");
		ReferenceType ajListOfSomething = resolve("Pjava/util/List<*>;");
		ReferenceType ajArrayListOfSomething = resolve("Pjava/util/ArrayList<*>;");
		ReferenceType ajListOfSomethingNumberish = resolve("Pjava/util/List<+Ljava/lang/Number;>;");
		ReferenceType ajListOfSomethingSuperDouble = resolve("Pjava/util/List<-Ljava/lang/Double;>;");
		ReferenceType ajListOfInteger = resolve("Pjava/util/List<Ljava/lang/Integer;>;");
		ReferenceType ajListOfNumber = resolve("Pjava/util/List<Ljava/lang/Number;>;");

		// void m2(List<? extends Number> e) {}

		// comment 11
		// void around(): execution(* C.m2(..)) && args(List<Integer>){} //: Should not match (but it does) ERROR
		listOfInteger = (List<Integer>) listOfSomethingNumberish;
		assertFalse(ajListOfInteger.isAssignableFrom(ajListOfSomethingNumberish));
		assertTrue(ajListOfInteger.isCoerceableFrom(ajListOfSomethingNumberish));

		// void around(): execution(* C.m2(..)) && args(ArrayList<Integer>){}//: Should not match (but it does!) ERROR
		arrayListOfInteger = (ArrayList<Integer>) listOfSomethingNumberish;
		assertFalse(ajArrayListOfInteger.isAssignableFrom(ajListOfSomethingNumberish));
		assertTrue(ajArrayListOfInteger.isCoerceableFrom(ajListOfSomethingNumberish));

		// void around(): execution(* C.m2(..)) && args(List<Number>){} //: Should not match (but it does) ERROR
		listOfNumber = (List<Number>) listOfSomethingNumberish;
		assertFalse(ajListOfNumber.isAssignableFrom(ajListOfSomethingNumberish));
		assertTrue(ajListOfNumber.isCoerceableFrom(ajListOfSomethingNumberish));

		// void around(): execution(* C.m2(..)) && args(ArrayList<Number>){}//: Should not runtime check (but it does!) ERROR
		arrayListOfNumber = (ArrayList<Number>) listOfSomethingNumberish;
		assertFalse(ajArrayListOfNumber.isAssignableFrom(ajListOfSomethingNumberish));
		assertTrue(ajArrayListOfNumber.isCoerceableFrom(ajListOfSomethingNumberish));

		// void around(): execution(* C.m2(..)) && args(List<? extends Number>){}//: Should match (it does)
		listOfSomethingNumberish = listOfSomethingNumberish;
		assertTrue(ajListOfSomethingNumberish.isAssignableFrom(ajListOfSomethingNumberish));

		// void around(): execution(* C.m2(..)) && args(ArrayList<? extends Number>){}//: Should runtime check (it does!)
		arrayListOfSomethingNumberish = listOfSomethingNumberish;
		assertFalse(ajArrayListOfSomethingNumberish.isAssignableFrom(ajListOfSomethingNumberish));
		assertTrue(ajArrayListOfSomethingNumberish.isCoerceableFrom(ajListOfSomethingNumberish));

		// void around(): execution(* C.m2(..)) && args(List){}//: Should match (it does)
		list = listOfSomethingNumberish;
		assertTrue(ajList.isAssignableFrom(ajListOfSomethingNumberish));

		// void around(): execution(* C.m2(..)) && args(ArrayList){}//: Should runtime check (it does not match!) ERROR
		arraylist = listOfSomethingNumberish;
		assertFalse(ajArrayList.isAssignableFrom(ajListOfSomethingNumberish));
		assertTrue(ajArrayList.isCoerceableFrom(ajListOfSomethingNumberish));

		// void around(): execution(* C.m2(..)) && args(List<?>){}//: Should match (it does)
		listOfSomething = listOfSomethingNumberish;
		assertTrue(ajListOfSomething.isAssignableFrom(ajListOfSomethingNumberish));

		// void around(): execution(* C.m2(..)) && args(ArrayList<?>){}//: Should runtime check (it does!)
		arrayListOfSomething = listOfSomethingNumberish;
		assertFalse(ajArrayListOfSomething.isAssignableFrom(ajListOfSomethingNumberish));
		assertTrue(ajArrayListOfSomething.isCoerceableFrom(ajListOfSomethingNumberish));

		// void around(): execution(* C.m2(..)) && args(ArrayList<String>){}//: Should not match (it does not match!)
		// error: arrayListOfString = listOfSomethingNumberish;
		assertFalse(ajArrayListOfString.isAssignableFrom(ajListOfSomethingNumberish));
		assertFalse(ajArrayListOfString.isCoerceableFrom(ajListOfSomethingNumberish));
	}

	public void testAssignable04_method_m3() {
		List list = new ArrayList();
		ArrayList arraylist = null;
		List<String> listOfString = new ArrayList<>();
		List<?> listOfSomething = new ArrayList<Integer>();
		List<?> arrayListOfSomething = null;
		List<Number> listOfNumber = null;
		List<Number> arrayListOfNumber = null;
		ArrayList<Integer> arrayListOfInteger = null;
		List<? extends Number> arrayListOfSomethingNumberish = null;
		List<? extends Number> listOfSomethingNumberish = new ArrayList<Integer>();
		List<? super Double> listOfSomethingSuperDouble = new ArrayList<Number>();
		List<Integer> listOfInteger = new ArrayList<>();
		List arrayList = null;
		ArrayList<String> arrayListOfString;
		ArrayList<Integer> arraylistOfInteger;
		// interfaces too List<? extends A,B>

		ReferenceType ajArrayListOfString = resolve("Pjava/util/ArrayList<Ljava/lang/String;>;");
		ReferenceType ajArrayListOfInteger = resolve("Pjava/util/ArrayList<Ljava/lang/Integer;>;");
		ReferenceType ajArrayListOfNumber = resolve("Pjava/util/ArrayList<Ljava/lang/Number;>;");
		ReferenceType ajArrayListOfSomethingNumberish = resolve("Pjava/util/ArrayList<+Ljava/lang/Number;>;");
		ReferenceType ajList = resolve("Ljava/util/List;");
		ReferenceType ajArrayList = resolve("Ljava/util/ArrayList;");
		ReferenceType ajListOfString = resolve("Pjava/util/List<Ljava/lang/String;>;");
		ReferenceType ajListOfSomething = resolve("Pjava/util/List<*>;");
		ReferenceType ajArrayListOfSomething = resolve("Pjava/util/ArrayList<*>;");
		ReferenceType ajListOfSomethingNumberish = resolve("Pjava/util/List<+Ljava/lang/Number;>;");
		ReferenceType ajListOfSomethingSuperDouble = resolve("Pjava/util/List<-Ljava/lang/Double;>;");
		ReferenceType ajListOfInteger = resolve("Pjava/util/List<Ljava/lang/Integer;>;");
		ReferenceType ajListOfNumber = resolve("Pjava/util/List<Ljava/lang/Number;>;");

		// void m3(List<Number> e) { }

		// void around(): execution(* C.m3(..)) && args(List<Integer>){} //: Should not match (it does not)
		// error: listOfInteger = listOfNumber;
		assertFalse(ajListOfInteger.isAssignableFrom(ajListOfNumber));
		assertFalse(ajListOfInteger.isCoerceableFrom(ajListOfNumber));

		// void around(): execution(* C.m3(..)) && args(ArrayList<Integer>){}//: Should not match (it does not)
		// error: arrayListOfInteger = listOfNumber;
		assertFalse(ajArrayListOfInteger.isAssignableFrom(ajListOfNumber));
		assertFalse(ajArrayListOfInteger.isCoerceableFrom(ajListOfNumber));

		// void around(): execution(* C.m3(..)) && args(List<Number>){}//: Should match (it does)
		listOfNumber = listOfNumber;
		assertTrue(ajListOfNumber.isAssignableFrom(ajListOfNumber));

		// void around(): execution(* C.m3(..)) && args(ArrayList<Number>){}//: Should runtime match (it does)
		arrayListOfNumber = listOfNumber;
		assertFalse(ajArrayListOfNumber.isAssignableFrom(ajListOfNumber));
		assertTrue(ajArrayListOfNumber.isCoerceableFrom(ajListOfNumber));

		// void around(): execution(* C.m3(..)) && args(List<? extends Number>){}//: Should match (it does)
		listOfSomethingNumberish = listOfNumber;
		assertTrue(ajListOfSomethingNumberish.isAssignableFrom(ajListOfNumber));

		// void around(): execution(* C.m3(..)) && args(ArrayList<? extends Number>){}//: Should runtime check (it does!)
		arrayListOfSomethingNumberish = listOfNumber;
		assertFalse(ajArrayListOfSomethingNumberish.isAssignableFrom(ajListOfNumber));
		assertTrue(ajArrayListOfSomethingNumberish.isCoerceableFrom(ajListOfNumber));

		// void around(): execution(* C.m3(..)) && args(List){}//: Should match (it does)
		list = listOfNumber;
		assertTrue(ajList.isAssignableFrom(ajListOfNumber));

		// void around(): execution(* C.m3(..)) && args(ArrayList){}//: Should runtime check (it does not match!) ERROR
		arrayList = listOfNumber;
		assertFalse(ajArrayList.isAssignableFrom(ajListOfNumber));
		assertTrue(ajArrayList.isCoerceableFrom(ajListOfNumber));

		// void around(): execution(* C.m3(..)) && args(List<?>){}//: Should match (it does)
		listOfSomething = listOfNumber;
		assertTrue(ajListOfSomething.isAssignableFrom(ajListOfNumber));

		// void around(): execution(* C.m3(..)) && args(ArrayList<?>){}//: Should runtime check (it does!)
		arrayListOfSomething = listOfNumber;
		assertFalse(ajArrayListOfSomething.isAssignableFrom(ajListOfNumber));
		assertTrue(ajArrayListOfSomething.isCoerceableFrom(ajListOfNumber));

		// void around(): execution(* C.m3(..)) && args(ArrayList<String>){}//: Should not match (it does not match!)
		// error: arrayListOfString = listOfNumber;
		assertFalse(ajArrayListOfString.isAssignableFrom(ajListOfNumber));
		assertFalse(ajArrayListOfString.isCoerceableFrom(ajListOfNumber));

	}

	static class ClassA<T> {
	}

	interface IMarker<H> {
	}

	static class ClassB<T> implements IMarker<ClassA<T>> {
	}

	static class ClassC<T> implements IMarker<T> {
	}

	public void testAssignability_pr267559() {
		ClassB cb = new ClassB();
		ClassB cb2 = new ClassB();

		ReferenceType rcb = resolve("Lorg/aspectj/weaver/ReferenceTypeTestCase$ClassB;");
		ReferenceType rcb2 = resolve("Lorg/aspectj/weaver/ReferenceTypeTestCase$ClassB;");
		boolean b = rcb.isAssignableFrom(rcb2);
		assertTrue(b);
		b = rcb2.isAssignableFrom(rcb);
		assertTrue(b);

		rcb = resolve("Porg/aspectj/weaver/ReferenceTypeTestCase$IMarker<Porg/aspectj/weaver/ReferenceTypeTestCase$ClassA<TT;>;>;");
		rcb2 = resolve("Lorg/aspectj/weaver/ReferenceTypeTestCase$ClassB;");
		b = rcb.isAssignableFrom(rcb2);
		assertTrue(b);
	}

	public void testAssignable03_method_m4() {
		List list = new ArrayList();
		List arraylist = null;
		List<String> listOfString = new ArrayList<>();
		List<?> listOfSomething = new ArrayList<Integer>();
		List<?> arrayListOfSomething = null;
		List<Number> listOfNumber = null;
		List<Number> arrayListOfNumber = null;
		List<? extends Number> arrayListOfSomethingNumberish = null;
		List<? extends Number> listOfSomethingNumberish = new ArrayList<Integer>();
		List<? super Double> listOfSomethingSuperDouble = new ArrayList<Number>();
		List<Integer> listOfInteger = new ArrayList<>();
		List<String> arrayListOfString;
		List<Integer> arraylistOfInteger;
		// interfaces too List<? extends A,B>

		ReferenceType ajArrayListOfString = resolve("Pjava/util/ArrayList<Ljava/lang/String;>;");
		ReferenceType ajArrayListOfInteger = resolve("Pjava/util/ArrayList<Ljava/lang/Integer;>;");
		ReferenceType ajArrayListOfNumber = resolve("Pjava/util/ArrayList<Ljava/lang/Number;>;");
		ReferenceType ajArrayListOfSomethingNumberish = resolve("Pjava/util/ArrayList<+Ljava/lang/Number;>;");
		ReferenceType ajList = resolve("Ljava/util/List;");
		ReferenceType ajArrayList = resolve("Ljava/util/ArrayList;");
		ReferenceType ajListOfString = resolve("Pjava/util/List<Ljava/lang/String;>;");
		ReferenceType ajListOfSomething = resolve("Pjava/util/List<*>;");
		ReferenceType ajArrayListOfSomething = resolve("Pjava/util/ArrayList<*>;");
		ReferenceType ajListOfSomethingNumberish = resolve("Pjava/util/List<+Ljava/lang/Number;>;");
		ReferenceType ajListOfSomethingSuperDouble = resolve("Pjava/util/List<-Ljava/lang/Double;>;");
		ReferenceType ajListOfInteger = resolve("Pjava/util/List<Ljava/lang/Integer;>;");
		ReferenceType ajListOfNumber = resolve("Pjava/util/List<Ljava/lang/Number;>;");

		// void m4(List<?> e) {}

		// void around(): execution(* C.m4(..)) && args(List<Integer>){} //: Should match with unchecked warning
		listOfInteger = (List<Integer>) listOfSomething;
		assertFalse(ajListOfInteger.isAssignableFrom(ajListOfSomething));
		assertTrue(ajListOfInteger.isCoerceableFrom(ajListOfSomething));

		// void around(): execution(* C.m4(..)) && args(ArrayList<Integer>){} // Should match with unchecked warning
		arraylistOfInteger = (ArrayList<Integer>) listOfSomething;
		assertFalse(ajArrayListOfInteger.isAssignableFrom(ajListOfSomething));
		assertTrue(ajArrayListOfInteger.isCoerceableFrom(ajListOfSomething));

		// void around(): execution(* C.m4(..)) && args(List<Number>){} // Should match with unchecked warning
		listOfNumber = (List<Number>) listOfSomething;
		assertFalse(ajListOfNumber.isAssignableFrom(ajListOfSomething));
		assertTrue(ajListOfNumber.isCoerceableFrom(ajListOfSomething));

		// void around(): execution(* C.m4(..)) && args(ArrayList<Number>){} // Should match with unchecked warning
		arrayListOfNumber = (ArrayList<Number>) listOfSomething;
		assertFalse(ajArrayListOfNumber.isAssignableFrom(ajListOfSomething));
		assertTrue(ajArrayListOfNumber.isCoerceableFrom(ajListOfSomething));

		// void around(): execution(* C.m4(..)) && args(List<? extends Number>){} // Should match with unchecked warning
		listOfSomethingNumberish = (List<? extends Number>) listOfSomething;
		assertFalse(ajListOfSomethingNumberish.isAssignableFrom(ajListOfSomething));
		assertTrue(ajListOfSomethingNumberish.isCoerceableFrom(ajListOfSomething));

		// void around(): execution(* C.m4(..)) && args(ArrayList<? extends Number>){} // Should match with unchecked warning
		arrayListOfSomethingNumberish = (ArrayList<? extends Number>) listOfSomething;
		assertFalse(ajArrayListOfSomethingNumberish.isAssignableFrom(ajListOfSomething));
		assertTrue(ajArrayListOfSomethingNumberish.isCoerceableFrom(ajListOfSomething));

		// void around(): execution(* C.m4(..)) && args(List){} // Should match
		list = listOfSomething;
		assertTrue(ajList.isAssignableFrom(ajListOfSomething));

		// void around(): execution(* C.m4(..)) && args(ArrayList){} // Should runtime check
		arraylist = listOfSomething;
		assertFalse(ajArrayList.isAssignableFrom(ajListOfSomething));
		assertTrue(ajArrayList.isCoerceableFrom(ajListOfSomething));

		// void around(): execution(* C.m4(..)) && args(List<?>){}//: Should match
		list = listOfSomething;
		assertTrue(ajList.isAssignableFrom(ajListOfSomething));

		// void around(): execution(* C.m4(..)) && args(ArrayList<?>){} // Should runtime check
		arrayListOfSomething = listOfSomething;
		assertFalse(ajArrayListOfSomething.isAssignableFrom(ajListOfSomething));
		assertTrue(ajArrayListOfSomething.isCoerceableFrom(ajListOfSomething));

		// void around(): execution(* C.m4(..)) && args(ArrayList<String>){} // Should match with unchecked warning
		arrayListOfString = (ArrayList<String>) listOfSomething;
		assertFalse(ajArrayListOfString.isAssignableFrom(ajListOfSomething));
		assertTrue(ajArrayListOfString.isCoerceableFrom(ajListOfSomething));
	}

	// copy of the real one in BcelClassWeaver
	public static class IfaceInitList implements PartialOrder.PartialComparable {
		final ResolvedType onType;
		List<ConcreteTypeMunger> list = new ArrayList<>();

		IfaceInitList(ResolvedType onType) {
			this.onType = onType;
		}

		public int compareTo(Object other) {
			IfaceInitList o = (IfaceInitList) other;
			if (onType.isAssignableFrom(o.onType)) {
				return +1;
			} else if (o.onType.isAssignableFrom(onType)) {
				return -1;
			} else {
				return 0;
			}
		}

		public int fallbackCompareTo(Object other) {
			return 0;
		}
	}

	public void testExpensiveAssignableChecks_309336() {
		List objects = new ArrayList();
		ReferenceType rcb = resolve("Lorg/aspectj/weaver/ReferenceTypeTestCase$Foo;");
		ReferenceType i = (ReferenceType) rcb.getDeclaredInterfaces()[0];
		while (i != null && i.isInterface()) {
			objects.add(Math.abs(new Random(12).nextInt(objects.size() + 1)), new IfaceInitList(i));
			ResolvedType[] rt = i.getDeclaredInterfaces();
			i = rt == null || rt.length == 0 ? null : (ReferenceType) rt[0];
		}
		for (int loop = 0; loop < 10; loop++) {
			// ReferenceType.r = 0;
			long stime = System.nanoTime();
			for (int j = 0; j < 10; j++) {
				List objects2 = new ArrayList(objects);
				PartialOrder.sort(objects2);
			}
			long etime = System.nanoTime();
			System.err.println("Took " + ((etime - stime) / 1000000) + "ms: calls ");// + ReferenceType.r);
		}
		// could do with asserting something... basically we are just checking we didn't run out of memory doing the sorts above!
	}

	public interface Operator14<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable, E10 extends Throwable, E11 extends Throwable, E12 extends Throwable, E13 extends Throwable, E14 extends Throwable> {
		T execute(String aArg) throws E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, RemoteException;
	}

	public interface Operator13<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable, E10 extends Throwable, E11 extends Throwable, E12 extends Throwable, E13 extends Throwable>
			extends Operator14<T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E13> {
	}

	public interface Operator12<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable, E10 extends Throwable, E11 extends Throwable, E12 extends Throwable>
			extends Operator13<T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E12> {
	}

	public interface Operator11<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable, E10 extends Throwable, E11 extends Throwable>
			extends Operator12<T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E11> {
	}

	public interface Operator10<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable, E10 extends Throwable>
			extends Operator11<T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E10> {

	}

	public interface Operator9<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable>
			extends Operator10<T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E9> {
	}

	public interface Operator8<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable>
			extends Operator9<T, E1, E2, E3, E4, E5, E6, E7, E8, E8> {
	}

	public interface Operator7<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable>
			extends Operator8<T, E1, E2, E3, E4, E5, E6, E7, E7> {
	}

	public interface Operator6<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable>
			extends Operator7<T, E1, E2, E3, E4, E5, E6, E6> {

	}

	public interface Operator5<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable>
			extends Operator6<T, E1, E2, E3, E4, E5, E5> {
	}

	public interface Operator4<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable> extends
			Operator5<T, E1, E2, E3, E4, E4> {
	}

	public interface Operator3<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable> extends
			Operator4<T, E1, E2, E3, E3> {
	}

	public interface Operator2<T, E1 extends Throwable, E2 extends Throwable> extends Operator3<T, E1, E2, E2> {

	}

	public interface Operator1<T, E1 extends Throwable> extends Operator2<T, E1, E1> {
	}

	public interface Operator<T> extends Operator1<T, RuntimeException> {
	}

	class Foo implements Operator<String> {
		public String execute(String aArg) throws NullPointerException, RemoteException {
			System.out.println("Doh!");
			return aArg;
		}
	}

	// public void testAssignable_method_m5() {
	// List list = new ArrayList();
	// ArrayList arraylist = null;
	// List<String> listOfString = new ArrayList<String>();
	// List<?> listOfSomething = new ArrayList<Integer>();
	// ArrayList<?> arrayListOfSomething = null;
	// List<Number> listOfNumber = null;
	// ArrayList<Number> arrayListOfNumber = null;
	// ArrayList<? extends Number> arrayListOfSomethingNumberish = null;
	// List<? extends Number> listOfSomethingNumberish = new ArrayList<Integer>();
	// List<? super Double> listOfSomethingSuperDouble = new ArrayList<Number>();
	// List<Integer> listOfInteger = new ArrayList<Integer>();
	// ArrayList<String> arrayListOfString;
	// ArrayList<Integer> arraylistOfInteger;
	// // interfaces too List<? extends A,B>
	//
	// ReferenceType ajArrayListOfString = resolve("Pjava/util/ArrayList<Ljava/lang/String;>;");
	// ReferenceType ajArrayListOfInteger = resolve("Pjava/util/ArrayList<Ljava/lang/Integer;>;");
	// ReferenceType ajArrayListOfNumber = resolve("Pjava/util/ArrayList<Ljava/lang/Number;>;");
	// ReferenceType ajArrayListOfSomethingNumberish = resolve("Pjava/util/ArrayList<+Ljava/lang/Number;>;");
	// ReferenceType ajList = resolve("Ljava/util/List;");
	// ReferenceType ajArrayList = resolve("Ljava/util/ArrayList;");
	// ReferenceType ajListOfString = resolve("Pjava/util/List<Ljava/lang/String;>;");
	// ReferenceType ajListOfSomething = resolve("Pjava/util/List<*>;");
	// ReferenceType ajArrayListOfSomething = resolve("Pjava/util/ArrayList<*>;");
	// ReferenceType ajListOfSomethingNumberish = resolve("Pjava/util/List<+Ljava/lang/Number;>;");
	// ReferenceType ajListOfSomethingSuperDouble = resolve("Pjava/util/List<-Ljava/lang/Double;>;");
	// ReferenceType ajListOfInteger = resolve("Pjava/util/List<Ljava/lang/Integer;>;");
	// ReferenceType ajListOfNumber = resolve("Pjava/util/List<Ljava/lang/Number;>;");
	// ReferenceType ajListOfEextendsNumber = resolve("Pjava/util/List<+TE")
	//
	// // class C<E extends Number> {
	// // void m5(List<E> e) { }
	// //
	// // void around(): execution(* C.m5(..)) && args(List<Integer>){} Should not match (but it does) ERROR
	//
	// // void around(): execution(* C.m5(..)) && args(ArrayList<Integer>){}//: Should not match (but it does!) ERROR
	// // void around(): execution(* C.m5(..)) && args(List<Number>){}//: Should not match (but it does!) ERROR
	// // void around(): execution(* C.m5(..)) && args(ArrayList<Number>){}//: Should not match (it does) ERROR
	// // void around(): execution(* C.m5(..)) && args(List<? extends Number>){}//: Should match (it does)
	// // void around(): execution(* C.m5(..)) && args(ArrayList<? extends Number>){}//: Should runtime check (it does!)
	// // void around(): execution(* C.m5(..)) && args(List){}//: Should match (it does)
	// // void around(): execution(* C.m5(..)) && args(ArrayList){}//: Should runtime check (it does not match!) ERROR
	// // void around(): execution(* C.m5(..)) && args(List<?>){}//: Should match (it does)
	// // void around(): execution(* C.m5(..)) && args(ArrayList<?>){}//: Should runtime check (it does not match!)
	// // void around(): execution(* C.m5(..)) && args(ArrayList<String>){}//: Should not match (it does not match!)
	// //
	// // // void around(): execution(* D2.m5(..)) && args(List<D1>){} //: Should
	// // not match (but it does) ERROR
	// // // void around(): execution(* D2.m5(..)) && args(ArrayList<D1>){}//:
	// // Should not match (but it does!) ERROR
	// // // void around(): execution(* D2.m5(..)) && args(List<C1>){}//: Should
	// // not match (but it does!) ERROR
	// // // void around(): execution(* D2.m5(..)) && args(ArrayList<C1>){}//:
	// // Should not match (it does) ERROR
	// // // void around(): execution(* D2.m5(..)) && args(List<? extends B1>){}//:
	// // Should match (it does)
	// // // void around(): execution(* D2.m5(..)) && args(ArrayList<? extends
	// // B1>){}//: Should runtime check (it does!)
	// // // void around(): execution(* D2.m5(..)) && args(List<? extends C1>){}//:
	// // Should match (it does)
	// // // void around(): execution(* D2.m5(..)) && args(ArrayList<? extends
	// // C1>){}//: Should runtime check (it does!)
	// // // void around(): execution(* D2.m5(..)) && args(List){}//: Should match
	// // (it does)
	// // // void around(): execution(* D2.m5(..)) && args(ArrayList){}//: Should
	// // runtime check (it does not match!) ERROR
	// // // void around(): execution(* D2.m5(..)) && args(List<?>){}//: Should
	// // match (it does)
	// // // void around(): execution(* D2.m5(..)) && args(ArrayList<?>){}//:
	// // Should runtime check (it does not match!)
	// // // void around(): execution(* D2.m5(..)) && args(ArrayList<String>){}//:
	// // Should not match (it does not match!)
	// //
	// // // void around(): execution(* C.m6(..)) && args(List<Integer>){} //:
	// // Should not match (but it does) ERROR
	// // // void around(): execution(* C.m6(..)) && args(ArrayList<Integer>){}//:
	// // Should not match (but it does!) ERROR
	// // // void around(): execution(* C.m6(..)) && args(List<Number>){}//: Should
	// // not match (but it does!) ERROR
	// // // void around(): execution(* C.m6(..)) && args(ArrayList<Number>){}//:
	// // Should not match (it does) ERROR
	// // // void around(): execution(* C.m6(..)) && args(List<? extends
	// // Number>){}//: Should match (it does)
	// // // void around(): execution(* C.m6(..)) && args(ArrayList<? extends
	// // Number>){}//: Should runtime check (it does!)
	// // // void around(): execution(* C.m6(..)) && args(List){}//: Should match
	// // (it does)
	// // // void around(): execution(* C.m6(..)) && args(ArrayList){}//: Should
	// // runtime check (it does not match!)
	// // // void around(): execution(* C.m6(..)) && args(List<?>){}//: Should
	// // match (it does)
	// // // void around(): execution(* C.m6(..)) && args(ArrayList<?>){}//: Should
	// // runtime check (it does not match!)
	// // // void around(): execution(* C.m6(..)) && args(ArrayList<String>){}//:
	// // Should not match (it does not match!)
	// //
	// // // void around(): execution(* C.m7(..)) && args(List<List<Integer>>){}
	// // //: Should not match (but it does) ERROR
	// // // void around(): execution(* C.m7(..)) &&
	// // args(ArrayList<List<Integer>>){}//: Should not match (but it does!) ERROR
	// // // void around(): execution(* C.m7(..)) && args(List<List<Number>>){}//:
	// // Should not match (but it does!) ERROR
	// // // void around(): execution(* C.m7(..)) &&
	// // args(ArrayList<List<Number>>){}//: Should not match (but it does) ERROR
	// // // void around(): execution(* C.m7(..)) && args(List<? extends
	// // List<Number>>){}//: Should not match (but it does) ERROR
	// // // void around(): execution(* C.m7(..)) && args(ArrayList< ? extends
	// // List<Number>>){}//: Should not match (but it does!) ERROR
	// // // void around(): execution(* C.m7(..)) && args(List< ? extends List<?
	// // extends Number>>){}//: Should match (it does!)
	// // // void around(): execution(* C.m7(..)) && args(ArrayList< ? extends
	// // List<? extends Number>>){}//: Should match (it does!)
	// // // void around(): execution(* C.m7(..)) && args(List){}//: Should match
	// // (it does)
	// // // void around(): execution(* C.m7(..)) && args(ArrayList){}//: Should
	// // runtime check (it does not match!)
	// // // void around(): execution(* C.m7(..)) && args(List<?>){}//: Should
	// // match (it does)
	// // // void around(): execution(* C.m7(..)) && args(ArrayList<?>){}//: Should
	// // runtime check (it does!)
	// // // void around(): execution(* C.m7(..)) &&
	// // args(ArrayList<List<String>>){}//: Should not match (it does not match!)
	// //
	// // // void around(): execution(* C.m8(..)) && args(List<Integer>){} //:
	// // Should match with unchecked conversion (it does)
	// // // void around(): execution(* C.m8(..)) && args(ArrayList<Integer>){}//:
	// // Should runtime check with unchecked conversion (it does!)
	// // // void around(): execution(* C.m8(..)) && args(List<Number>){}//: Should
	// // match with unchecked conversion (it does!)
	// // // void around(): execution(* C.m8(..)) && args(ArrayList<Number>){}//:
	// // Should runtime check with unchecked conversion (it does)
	// // // void around(): execution(* C.m8(..)) && args(List<? extends
	// // Number>){}//: Should match with unchecked conversion (it does!)
	// // // void around(): execution(* C.m8(..)) && args(ArrayList<? extends
	// // Number>){}//: Should runtime check with unchecked conversion (it does)
	// // // void around(): execution(* C.m8(..)) && args(List){}//: Should match
	// // (it does)
	// // // void around(): execution(* C.m8(..)) && args(ArrayList){}//: Should
	// // runtime check (it does!)
	// // // void around(): execution(* C.m8(..)) && args(List<?>){}//: Should
	// // match (it does)
	// // // void around(): execution(* C.m8(..)) && args(ArrayList<?>){}//: Should
	// // runtime check (it does!)
	// // // void around(): execution(* C.m8(..)) && args(ArrayList<String>){}//:
	// // Should not match (it does not match!)
	// //
	// // // void around(): execution(* C.m9(..)) && args(List<Integer>){} //:
	// // Should not match (but it does) ERROR
	// // // void around(): execution(* C.m9(..)) && args(ArrayList<Integer>){}//:
	// // Should not match (it does not match!)
	// // // void around(): execution(* C.m9(..)) && args(Number){}//: Should match
	// // (it does!)
	// // // void around(): execution(* C.m9(..)) && args(Integer){}//: Should
	// // runtime check (it does)
	// // // void around(): execution(* C.m9(..)) && args(List<? extends
	// // Number>){}//: Should not match (but it does) ERROR
	// // // void around(): execution(* C.m9(..)) && args(ArrayList<? extends
	// // Number>){}//: Should not match (it does not match!)
	// // // void around(): execution(* C.m9(..)) && args(List){}//: Should not
	// // match (but it does) ERROR
	// // // void around(): execution(* C.m9(..)) && args(ArrayList){}//: Should
	// // not match (it does not match!)
	// // // void around(): execution(* C.m9(..)) && args(List<?>){}//: Should not
	// // match (but it does) ERROR
	// // // void around(): execution(* C.m9(..)) && args(ArrayList<?>){}//: Should
	// // not match (it does not match!)
	// // // void around(): execution(* C.m9(..)) && args(String){}//: Should not
	// // match (it does not match!)
	//
	// }

	private ReferenceType resolve(String sig) {
		return (ReferenceType) world.resolve(UnresolvedType.forSignature(sig));
	}
}
