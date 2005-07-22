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
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Bug104212 extends TestCase {

    static int s_i = 0;

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(Bug104212.class);
    }

    public void testStaticMethodFromSuperClass() {
        Child.doSome();
        assertEquals(1, s_i);
    }

    static class Parent {
        static void foo() {}
    }

    static class Child extends Parent {
        static void doSome() {
            foo();// this is the bug
        }
    }

    @Aspect
    public static class TestAspect {

        @Before("call(* ataspectj.Bug104212.Parent.foo()) && within(ataspectj.Bug104212.Child)")
        public void before(JoinPoint jp) {
            // AJ bug was here since Java 1.4...
            // was: call(Bug104212.Child.foo())
            assertEquals("call(Bug104212.Parent.foo())", jp.toShortString());
            assertEquals(Parent.class, jp.getSignature().getDeclaringType());
            assertNotNull(((MethodSignature)jp.getSignature()).getMethod());
            s_i++;
        }
    }

}
