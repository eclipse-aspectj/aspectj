/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Alexandre Vasseur
 *******************************************************************************/
package ataspectj;

import junit.framework.TestCase;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import java.lang.annotation.Annotation;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class PrecedenceTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(PrecedenceTest.class);
    }

    public void hello() {
        log("hello");
    }

    public void testRuntimeVisible() {
        Annotation annotation = TestAspect_Order.class.getAnnotation(DeclarePrecedence.class);
        assertNotNull(annotation);
    }

    public void testPrecedence() {
        s_log = new StringBuffer();
        hello();
        assertEquals("TestAspect_3 TestAspect_2 TestAspect_1 hello ", s_log.toString());
    }


    @Aspect()
    public static class TestAspect_1 {
    		@SuppressAjWarnings
        @Before("execution(* ataspectj.PrecedenceTest.hello())")
        public void before() {
            log("TestAspect_1");
        }
    }

    @Aspect()
    @DeclarePrecedence("ataspectj.PrecedenceTest.TestAspect_3, ataspectj.PrecedenceTest.TestAspect_1")
    public static class TestAspect_2 {
		@SuppressAjWarnings
        @Before("execution(* ataspectj.PrecedenceTest.hello())")
        public void before() {
            log("TestAspect_2");
        }
    }

    @Aspect()
    public static class TestAspect_3 {
		@SuppressAjWarnings
        @Before("execution(* ataspectj.PrecedenceTest.hello())")
        public void before() {
            log("TestAspect_3");
        }
    }

    @Aspect()
    @DeclarePrecedence("ataspectj.PrecedenceTest.TestAspect_3, ataspectj.PrecedenceTest.TestAspect_2, ataspectj.PrecedenceTest.TestAspect_1, *")
    public static class TestAspect_Order {
    }
}
