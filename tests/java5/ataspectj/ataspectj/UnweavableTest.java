/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package ataspectj;

import junit.framework.TestCase;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.ProtectionDomain;
import java.io.Serializable;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.AnnotationVisitor;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class UnweavableTest extends TestCase {

    public void testUnweavableProxy() {
        TestAspect.I = 0;
        ISome some = getProxy();
        some.giveOne();
        assertEquals(1, TestAspect.I);
    }

    static interface ISome {
        public int giveOne();
    }

    ISome getProxy() {
        return (ISome) Proxy.newProxyInstance(
                ISome.class.getClassLoader(),
                new Class[]{ISome.class},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return new Integer(1);
                    }
                }
        );
    }

    @Aspect
    public static class TestAspect {

        static int I = 0;

        @Before("execution(* ataspectj.UnweavableTest.ISome+.giveOne())")
        public void before() {
            I++;
        }
    }

    public void testJit() {
        TestAspect.I = 0;
        TestAspect2.I = 0;
        ISome some = getJit();
        assertNotNull(some.getClass().getAnnotation(ASome.class));
        assertEquals(2, some.giveOne());
        assertEquals(1, TestAspect.I);
        assertEquals(1, TestAspect2.I);
    }

    public void testJitNotMatched() {
        // just load a jit to make sure the weaver does not complains for classes coming from nowhere
        Serializable serial = getJitNoMatch();
        assertEquals(0, serial.getClass().getDeclaredMethods().length);
    }

    @Retention(RetentionPolicy.RUNTIME)
    static @interface ASome {}

    ISome getJit() {
        ClassWriter cw = new ClassWriter(true, true);
        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "ataspectj/ISomeGen", null, "java/lang/Object", new String[]{"ataspectj/UnweavableTest$ISome"});
        AnnotationVisitor av = cw.visitAnnotation("Lataspectj/UnweavableTest$ASome;", true);
        av.visitEnd();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, new String[0]);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "giveOne", "()I", null, new String[0]);
        mv.visitInsn(Opcodes.ICONST_2);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        cw.visitEnd();

        try {
            ClassLoader loader = this.getClass().getClassLoader();
            Method def = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
            def.setAccessible(true);
            Class gen = (Class) def.invoke(loader, "ataspectj.ISomeGen", cw.toByteArray(), 0, cw.toByteArray().length);
            return (ISome) gen.newInstance();
        } catch (Throwable t) {
            fail(t.toString());
            return null;
        }
    }

    Serializable getJitNoMatch() {
        ClassWriter cw = new ClassWriter(true, true);
        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "ataspectj/unmatched/Gen", null, "java/lang/Object", new String[]{"java/io/Serializable"});

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, new String[0]);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        cw.visitEnd();

        try {
            ClassLoader loader = this.getClass().getClassLoader();
            Method def = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
            def.setAccessible(true);
            Class gen = (Class) def.invoke(loader, "ataspectj.unmatched.Gen", cw.toByteArray(), 0, cw.toByteArray().length);
            return (Serializable) gen.newInstance();
        } catch (Throwable t) {
            fail(t.toString());
            return null;
        }
    }

    @Aspect
    public static class TestAspect2 {
        static int I = 0;
        @Before("execution(* @ataspectj.UnweavableTest$ASome ataspectj..*.giveOne())")
        public void before() {
            I++;
        }
    }

    public static void main(String args[]) throws Throwable {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(UnweavableTest.class);
    }

}
