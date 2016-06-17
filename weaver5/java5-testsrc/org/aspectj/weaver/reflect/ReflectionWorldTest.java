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

import java.util.List;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ConcreteCflowPointcut;

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
	
	static class AbstractSuperClass<A,B> {}
	static interface InterfaceOne {}
	static interface InterfaceTwo<A> {}
	static class ID {}
	static abstract class AbstractTestClass<T> extends AbstractSuperClass<T,ID> implements InterfaceOne, InterfaceTwo<T> {

	}
	static class TestType {}
//	static class ConcreteClass extends AbstractTestClass<TestType> {
	static class ConcreteClass extends AbstractTestClass<List<TestType>> {
	}
	
	static class Bar extends ConcreteClass {}
	
	public void testGenerics() {
		ReflectionWorld world = new ReflectionWorld(getClass().getClassLoader());
//		world.lookupOrCreateName(UnresolvedType.forName(AbstractTestClass.class.getName()));
//		ResolvedType resolvedType = world.resolve(AbstractTestClass.class);
		JavaLangTypeToResolvedTypeConverter converter = new JavaLangTypeToResolvedTypeConverter(world);
		ResolvedType resolvedType2 = converter.fromType(ConcreteClass.class);
	}

}
