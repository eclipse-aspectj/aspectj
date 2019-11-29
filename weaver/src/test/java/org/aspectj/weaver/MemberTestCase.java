/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 *                      2005 contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation
 *     Adrian Colyer, canBeParameterized tests 
 * ******************************************************************/

package org.aspectj.weaver;

import java.lang.reflect.Modifier;

import org.aspectj.testing.util.TestUtil;

import junit.framework.TestCase;

/**
 * This is a test case for all the portions of Member that don't require a world.
 */
public class MemberTestCase extends TestCase {

	public MemberTestCase(String name) {
		super(name);
	}

	public void testMethodConstruction() {
		Member s = TestUtils.methodFromString("void Foo.goo(int)");
		Member t = MemberImpl.method(UnresolvedType.forName("Foo"), 0, "goo", "(I)V");
		Member u = TestUtils.methodFromString("void Foo1.goo(int)");
		Member v = TestUtils.methodFromString("int Foo.goo(int)");

		TestUtil.assertCommutativeEquals(s, s, true);
		TestUtil.assertCommutativeEquals(t, t, true);
		TestUtil.assertCommutativeEquals(u, u, true);
		TestUtil.assertCommutativeEquals(v, v, true);
		TestUtil.assertCommutativeEquals(s, t, true);
		TestUtil.assertCommutativeEquals(s, u, false);
		TestUtil.assertCommutativeEquals(s, v, false);
		TestUtil.assertCommutativeEquals(t, u, false);
		TestUtil.assertCommutativeEquals(t, v, false);
		TestUtil.assertCommutativeEquals(u, v, false);

		s = TestUtils.fieldFromString("int Foo.goo");
		t = MemberImpl.field("Foo", 0, "goo", "I");
		u = TestUtils.fieldFromString("int Foo.goo1");
		v = TestUtils.fieldFromString("long Foo.goo");

		TestUtil.assertCommutativeEquals(s, s, true);
		TestUtil.assertCommutativeEquals(t, t, true);
		TestUtil.assertCommutativeEquals(u, u, true);
		TestUtil.assertCommutativeEquals(v, v, true);
		TestUtil.assertCommutativeEquals(s, t, true);
		TestUtil.assertCommutativeEquals(s, u, false);
		TestUtil.assertCommutativeEquals(s, v, false);
		TestUtil.assertCommutativeEquals(t, u, false);
		TestUtil.assertCommutativeEquals(t, v, false);
		TestUtil.assertCommutativeEquals(u, v, false);
	}

	public void testMethodContents() {
		Member m = TestUtils.methodFromString("void Foo.goo(int)");
		kindTest(m, Member.METHOD);
		declaringTypeTest(m, "Foo");
		nameTest(m, "goo");
		parameterTypesTest(m, new UnresolvedType[] { UnresolvedType.INT });
		returnTypeTest(m, UnresolvedType.VOID);
		isInterfaceTest(m, false);
		isPrivateTest(m, false);
		isConstructorTest(m, false);
		isStaticTest(m, false);

		m = TestUtils.methodFromString("interface java.lang.Object java.util.Iterator.next()");
		kindTest(m, Member.METHOD);
		declaringTypeTest(m, "java.util.Iterator");
		nameTest(m, "next");
		parameterTypesTest(m, UnresolvedType.NONE);
		returnTypeTest(m, UnresolvedType.OBJECT);
		isInterfaceTest(m, true);
		isPrivateTest(m, false);
		isConstructorTest(m, false);
		isStaticTest(m, false);

		m = TestUtils.methodFromString("void Foo.<init>(int, java.lang.Object)");
		kindTest(m, Member.CONSTRUCTOR);
		declaringTypeTest(m, "Foo");
		nameTest(m, "<init>");
		parameterTypesTest(m, new UnresolvedType[] { UnresolvedType.INT, UnresolvedType.OBJECT });
		returnTypeTest(m, UnresolvedType.VOID);
		isInterfaceTest(m, false);
		isPrivateTest(m, false);
		isConstructorTest(m, true);
		isStaticTest(m, false);

		m = TestUtils.methodFromString("private double Foo.sqrt(double)");
		kindTest(m, Member.METHOD);
		declaringTypeTest(m, "Foo");
		nameTest(m, "sqrt");
		parameterTypesTest(m, new UnresolvedType[] { UnresolvedType.DOUBLE });
		returnTypeTest(m, UnresolvedType.DOUBLE);
		isInterfaceTest(m, false);
		isPrivateTest(m, true);
		isConstructorTest(m, false);
		isStaticTest(m, false);

		m = TestUtils.methodFromString("static int java.lang.Math.max(int, int)");
		kindTest(m, Member.METHOD);
		declaringTypeTest(m, "java.lang.Math");
		nameTest(m, "max");
		parameterTypesTest(m, new UnresolvedType[] { UnresolvedType.INT, UnresolvedType.INT });
		returnTypeTest(m, UnresolvedType.INT);
		isInterfaceTest(m, false);
		isPrivateTest(m, false);
		isConstructorTest(m, false);
		isStaticTest(m, true);
	}

	public void testFieldContents() {
		Member m = TestUtils.fieldFromString("int Foo.goo");
		kindTest(m, Member.FIELD);
		declaringTypeTest(m, "Foo");
		nameTest(m, "goo");
		parameterTypesTest(m, UnresolvedType.NONE);
		returnTypeTest(m, UnresolvedType.INT);
		isInterfaceTest(m, false);
		isPrivateTest(m, false);
		isConstructorTest(m, false);
		isStaticTest(m, false);

		m = TestUtils.fieldFromString("static java.util.Iterator goo.Bar.i");
		kindTest(m, Member.FIELD);
		declaringTypeTest(m, "goo.Bar");
		nameTest(m, "i");
		parameterTypesTest(m, UnresolvedType.NONE);
		returnTypeTest(m, UnresolvedType.forName("java.util.Iterator"));
		isInterfaceTest(m, false);
		isPrivateTest(m, false);
		isConstructorTest(m, false);
		isStaticTest(m, true);
	}

	private void isStaticTest(Member m, boolean b) {
		assertEquals(m + " is static", b, Modifier.isStatic(m.getModifiers()));
	}

	private void isConstructorTest(Member m, boolean b) {
		assertEquals(m + " is constructor", b, m.getKind() == Member.CONSTRUCTOR);
	}

	private void isPrivateTest(Member m, boolean b) {
		assertEquals(m + " is private", b, Modifier.isPrivate(m.getModifiers()));
	}

	private void isInterfaceTest(Member m, boolean b) {
		assertEquals(m + " is interface", b, Modifier.isInterface(m.getModifiers()));
	}

	private void returnTypeTest(Member m, UnresolvedType returnType) {
		assertEquals(m + " return type", returnType, m.getReturnType());
	}

	private void parameterTypesTest(Member m, UnresolvedType[] paramTypes) {
		TestUtil.assertArrayEquals(m + " parameters", paramTypes, m.getParameterTypes());
	}

	private void nameTest(Member m, String name) {
		assertEquals(m + " name", name, m.getName());
	}

	private void declaringTypeTest(Member m, String declaringName) {
		assertEquals(m + " declared in", UnresolvedType.forName(declaringName), m.getDeclaringType());
	}

	private void kindTest(Member m, MemberKind kind) {
		assertEquals(m + " kind", kind, m.getKind());
	}

}
