/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import junit.framework.TestCase;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

public class ReflectionWorldSpecificTest extends TestCase {

	public void testDelegateCreation() {
		World world = new ReflectionWorld(true, getClass().getClassLoader());
		ResolvedType rt = world.resolve("java.lang.Object");
		assertNotNull(rt);
		assertEquals("Ljava/lang/Object;", rt.getSignature());
	}

	public void testArrayTypes() {
		IReflectionWorld world = new ReflectionWorld(true, getClass().getClassLoader());
		String[] strArray = new String[1];
		ResolvedType rt = world.resolve(strArray.getClass());
		assertTrue(rt.isArray());
	}

	public void testPrimitiveTypes() {
		IReflectionWorld world = new ReflectionWorld(true, getClass().getClassLoader());
		assertEquals("int", UnresolvedType.INT, world.resolve(int.class));
		assertEquals("void", UnresolvedType.VOID, world.resolve(void.class));
	}

}
