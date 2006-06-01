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

import junit.framework.TestCase;

import org.aspectj.testing.util.TestUtil;

/**
 * This is a test case for all the portions of Member that don't require a world.
 */
public class MemberTestCase extends TestCase {

    public MemberTestCase(String name) {
        super(name);
    }

    public void testMethodConstruction() {
        Member s = MemberImpl.methodFromString("void Foo.goo(int)");
        Member t = MemberImpl.method(UnresolvedType.forName("Foo"), 0, "goo", "(I)V");
        Member u = MemberImpl.methodFromString("void Foo1.goo(int)");
        Member v = MemberImpl.methodFromString("int Foo.goo(int)");

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

        s = MemberImpl.fieldFromString("int Foo.goo");
        t = MemberImpl.field("Foo", 0, "goo", "I");
        u = MemberImpl.fieldFromString("int Foo.goo1");
        v = MemberImpl.fieldFromString("long Foo.goo");

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
        Member m = MemberImpl.methodFromString("void Foo.goo(int)");
        kindTest(m, Member.METHOD);
        declaringTypeTest(m, "Foo");
        nameTest(m, "goo");
        parameterTypesTest(m, new UnresolvedType[] { ResolvedType.INT });
        returnTypeTest(m, ResolvedType.VOID);
        isInterfaceTest(m, false);
        isPrivateTest(m, false);
        isConstructorTest(m, false);
        isStaticTest(m, false);

        m = MemberImpl.methodFromString("interface java.lang.Object java.util.Iterator.next()");
        kindTest(m, Member.METHOD);
        declaringTypeTest(m, "java.util.Iterator");
        nameTest(m, "next");
        parameterTypesTest(m, UnresolvedType.NONE);
        returnTypeTest(m, UnresolvedType.OBJECT);
        isInterfaceTest(m, true);
        isPrivateTest(m, false);
        isConstructorTest(m, false);
        isStaticTest(m, false);

        m = MemberImpl.methodFromString("void Foo.<init>(int, java.lang.Object)");
        kindTest(m, Member.CONSTRUCTOR);
        declaringTypeTest(m, "Foo");
        nameTest(m, "<init>");
        parameterTypesTest(m, new UnresolvedType[] { ResolvedType.INT, UnresolvedType.OBJECT } );
        returnTypeTest(m, ResolvedType.VOID);
        isInterfaceTest(m, false);
        isPrivateTest(m, false);
        isConstructorTest(m, true);
        isStaticTest(m, false);

        m = MemberImpl.methodFromString("private double Foo.sqrt(double)");
        kindTest(m, Member.METHOD);
        declaringTypeTest(m, "Foo");
        nameTest(m, "sqrt");
        parameterTypesTest(m, new UnresolvedType[] { ResolvedType.DOUBLE } );
        returnTypeTest(m, ResolvedType.DOUBLE);
        isInterfaceTest(m, false);
        isPrivateTest(m, true);
        isConstructorTest(m, false);
        isStaticTest(m, false);

        m = MemberImpl.methodFromString("static int java.lang.Math.max(int, int)");
        kindTest(m, Member.METHOD);
        declaringTypeTest(m, "java.lang.Math");
        nameTest(m, "max");
        parameterTypesTest(m, new UnresolvedType[] { ResolvedType.INT, ResolvedType.INT } );
        returnTypeTest(m, ResolvedType.INT);
        isInterfaceTest(m, false);
        isPrivateTest(m, false);
        isConstructorTest(m, false);
        isStaticTest(m, true);
    }

    public void testFieldContents() {
        Member m = MemberImpl.fieldFromString("int Foo.goo");
        kindTest(m, Member.FIELD);
        declaringTypeTest(m, "Foo");
        nameTest(m, "goo");
        parameterTypesTest(m, UnresolvedType.NONE);
        returnTypeTest(m, ResolvedType.INT);
        isInterfaceTest(m, false);
        isPrivateTest(m, false);
        isConstructorTest(m, false);
        isStaticTest(m, false);

        m = MemberImpl.fieldFromString("static java.util.Iterator goo.Bar.i");
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
        assertEquals(m + " is static", b, m.isStatic());
    }
    private void isConstructorTest(Member m, boolean b) {
        assertEquals(m + " is constructor", b, m.getKind() == Member.CONSTRUCTOR);
    }
    private void isPrivateTest(Member m, boolean b) {
        assertEquals(m + " is private", b, m.isPrivate());
    }
    private void isInterfaceTest(Member m, boolean b) {
        assertEquals(m + " is interface", b, m.isInterface());
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
    private void kindTest(Member m, MemberImpl.Kind kind) {
        assertEquals(m + " kind", kind, m.getKind());
    }   
   
    
    
}
