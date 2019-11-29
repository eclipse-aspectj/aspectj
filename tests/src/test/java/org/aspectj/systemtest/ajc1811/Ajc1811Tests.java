/*******************************************************************************
 * Copyright (c) 2016 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc1811;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc1811Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testParameterizedWithInner() throws Exception {
		runTest("parameterized with inner");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "Outer");
		assertNotNull(jc);
		BcelWorld world = new BcelWorld(ajc.getSandboxDirectory().toString());

		ResolvedType outerType = world.resolve(UnresolvedType.forName("Outer"));
		ResolvedMember m = findMethod(outerType,"m");
		
		UnresolvedType type = m.getReturnType();
		assertEquals("LOuter$Inner;",type.getSignature());
		
		type = m.getGenericReturnType();
		assertEquals("LOuter$Inner;",type.getSignature());
		
		ResolvedType resolvedType = world.resolve(type);
		ResolvedType outerResolvedType = resolvedType.getOuterClass();
		assertEquals("LOuter;",outerResolvedType.getSignature());
		
		ResolvedMember m2 = findMethod(outerType,"m2");
		type = m2.getReturnType();
		assertEquals("LOuter$Inner;",type.getSignature());
		
		type = m2.getGenericReturnType();
		assertEquals("LOuter$Inner;",type.getSignature());
		
		// public Inner m() { ... }
//		Method m = findMethod(jc,"m");
//		System.out.println(m);
//		System.out.println(">"+m.getReturnType());
//		assertNotNull(returnType);
		
		// public Outer<String>.Inner m2() { ... }
	}
	
//	
//	public void testMultiArgs_509235() {
//		runTest("multiargs");
//	}
//
//	public void testMultiArgs_509235_2() {
//		runTest("multiargs - no ellipsis");
//	}

	// 1.8.13:

	public void testAjcGenerics() {
		runTest("generics");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1811Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc1811.xml");
	}

}
