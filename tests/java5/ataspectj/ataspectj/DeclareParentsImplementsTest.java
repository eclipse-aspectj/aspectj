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
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Before;

import java.util.Arrays;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DeclareParentsImplementsTest extends TestCase {

    static class Target {
        void target() {
            log("hello");
        }
    }

    static interface Introduced {
        final static int field1 = 1;
        @Some
        void intro();
    }

    static class Implementation implements Introduced {
        public Implementation(int i) {}
        public Implementation() {}

        public void intro() {
            log("intro-"+field1);
            // we cannot copy the raw bytecode as there might be super.* calls, and other OO stuff
        }
    }

    @Aspect
    static class TestAspect {

        @DeclareParents(value="ataspectj.DeclareParentsImplementsTest.Target",
                        defaultImpl=Implementation.class)
        public static Introduced i;
        // will lead to: class Target implements Introduced {
        //    void intro(args) { delegate to some hidden field, lazy initialized here for now }
        // }

        @Before("execution(* ataspectj.DeclareParentsImplementsTest.Introduced.intro())")
        public void before() {
            log("aop");
        }
    }

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public void testDecPInt() {
        Class[] intfs = Target.class.getInterfaces();
        assertTrue("Was not introduced", Arrays.asList(intfs).contains(Introduced.class));
    }

    public void testDecPIntAdvised() {
        s_log = new StringBuffer();
        ((Introduced)new Target()).intro();
        assertEquals("aop intro-1 ", s_log.toString());
    }

    public void testAddedMethodKeepAnnotation() throws Throwable {
        Method m = Target.class.getDeclaredMethod("intro");
        assertTrue("annotation not retained", m.isAnnotationPresent(Some.class));
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DeclareParentsImplementsTest.class);
    }

    @Retention(RetentionPolicy.RUNTIME)
    static @interface Some {
    }
}
