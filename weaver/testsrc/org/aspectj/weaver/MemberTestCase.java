/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
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
        Member s = Member.methodFromString("void Foo.goo(int)");
        Member t = Member.method(TypeX.forName("Foo"), 0, "goo", "(I)V");
        Member u = Member.methodFromString("void Foo1.goo(int)");
        Member v = Member.methodFromString("int Foo.goo(int)");

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

        s = Member.fieldFromString("int Foo.goo");
        t = Member.field("Foo", 0, "goo", "I");
        u = Member.fieldFromString("int Foo.goo1");
        v = Member.fieldFromString("long Foo.goo");

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
        Member m = Member.methodFromString("void Foo.goo(int)");
        kindTest(m, Member.METHOD);
        declaringTypeTest(m, "Foo");
        nameTest(m, "goo");
        parameterTypesTest(m, new TypeX[] { ResolvedTypeX.INT });
        returnTypeTest(m, ResolvedTypeX.VOID);
        isInterfaceTest(m, false);
        isPrivateTest(m, false);
        isConstructorTest(m, false);
        isStaticTest(m, false);

        m = Member.methodFromString("interface java.lang.Object java.util.Iterator.next()");
        kindTest(m, Member.METHOD);
        declaringTypeTest(m, "java.util.Iterator");
        nameTest(m, "next");
        parameterTypesTest(m, TypeX.NONE);
        returnTypeTest(m, TypeX.OBJECT);
        isInterfaceTest(m, true);
        isPrivateTest(m, false);
        isConstructorTest(m, false);
        isStaticTest(m, false);

        m = Member.methodFromString("void Foo.<init>(int, java.lang.Object)");
        kindTest(m, Member.CONSTRUCTOR);
        declaringTypeTest(m, "Foo");
        nameTest(m, "<init>");
        parameterTypesTest(m, new TypeX[] { ResolvedTypeX.INT, TypeX.OBJECT } );
        returnTypeTest(m, ResolvedTypeX.VOID);
        isInterfaceTest(m, false);
        isPrivateTest(m, false);
        isConstructorTest(m, true);
        isStaticTest(m, false);

        m = Member.methodFromString("private double Foo.sqrt(double)");
        kindTest(m, Member.METHOD);
        declaringTypeTest(m, "Foo");
        nameTest(m, "sqrt");
        parameterTypesTest(m, new TypeX[] { ResolvedTypeX.DOUBLE } );
        returnTypeTest(m, ResolvedTypeX.DOUBLE);
        isInterfaceTest(m, false);
        isPrivateTest(m, true);
        isConstructorTest(m, false);
        isStaticTest(m, false);

        m = Member.methodFromString("static int java.lang.Math.max(int, int)");
        kindTest(m, Member.METHOD);
        declaringTypeTest(m, "java.lang.Math");
        nameTest(m, "max");
        parameterTypesTest(m, new TypeX[] { ResolvedTypeX.INT, ResolvedTypeX.INT } );
        returnTypeTest(m, ResolvedTypeX.INT);
        isInterfaceTest(m, false);
        isPrivateTest(m, false);
        isConstructorTest(m, false);
        isStaticTest(m, true);
    }

    public void testFieldContents() {
        Member m = Member.fieldFromString("int Foo.goo");
        kindTest(m, Member.FIELD);
        declaringTypeTest(m, "Foo");
        nameTest(m, "goo");
        parameterTypesTest(m, TypeX.NONE);
        returnTypeTest(m, ResolvedTypeX.INT);
        isInterfaceTest(m, false);
        isPrivateTest(m, false);
        isConstructorTest(m, false);
        isStaticTest(m, false);

        m = Member.fieldFromString("static java.util.Iterator goo.Bar.i");
        kindTest(m, Member.FIELD);
        declaringTypeTest(m, "goo.Bar");
        nameTest(m, "i");
        parameterTypesTest(m, TypeX.NONE);
        returnTypeTest(m, TypeX.forName("java.util.Iterator"));
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
    private void returnTypeTest(Member m, TypeX returnType) {
        assertEquals(m + " return type", returnType, m.getReturnType());
    }
    private void parameterTypesTest(Member m, TypeX[] paramTypes) {
        TestUtil.assertArrayEquals(m + " parameters", paramTypes, m.getParameterTypes());
    }
    private void nameTest(Member m, String name) {
        assertEquals(m + " name", name, m.getName());
    }
    private void declaringTypeTest(Member m, String declaringName) {
        assertEquals(m + " declared in", TypeX.forName(declaringName), m.getDeclaringType());
    }
    private void kindTest(Member m, Member.Kind kind) {
        assertEquals(m + " kind", kind, m.getKind());
    }   
   
    
    
}
