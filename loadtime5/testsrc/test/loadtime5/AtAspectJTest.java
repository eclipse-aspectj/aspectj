/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package test.loadtime5;

import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Test various advice and JoinPoint + binding, without pc ref
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAspectJTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        TestRunner.run(AtAspectJTest.class);
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AtAspectJTest.class);
    }

    public void hello() {
        log("hello");
    }

    public void hello(String s) {
        log("hello-");
        log(s);
    }

    public void testExecutionWithThisBinding() {
        s_log = new StringBuffer();
        AtAspectJTest me = new AtAspectJTest();
        me.hello();
        // see here advice precedence as in source code order
        //TODO check around relative order
        // see fix in BcelWeaver sorting shadowMungerList
        //assertEquals("around2_ around_  before hello after _around _around2 ", s_log.toString());
        assertEquals("around_ around2_ before hello _around2 _around after ", s_log.toString());
    }

    public void testExecutionWithArgBinding() {
        s_log = new StringBuffer();
        AtAspectJTest me = new AtAspectJTest();
        me.hello("x");
        assertEquals("before- x hello- x ", s_log.toString());
    }


    @Aspect
    public static class TestAspect {

        static int s = 0;

        static {
            s++;
        }

        public TestAspect() {
            // assert clinit has run when singleton aspectOf reaches that
            assertTrue(s>0);
        }

        //public static TestAspect aspectOf() {return null;}

        @Around("execution(* test.loadtime5.AtAspectJTest.hello())")
        public void aaround(ProceedingJoinPoint jp) {
            log("around_");
            try {
                jp.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            log("_around");
        }

        @Around("execution(* test.loadtime5.AtAspectJTest.hello()) && this(t)")
        public void around2(ProceedingJoinPoint jp, Object t) {
            log("around2_");
            assertEquals(AtAspectJTest.class.getName(), t.getClass().getName());
            try {
                jp.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            log("_around2");
        }

        @Before("execution(* test.loadtime5.AtAspectJTest.hello())")
        public void before(JoinPoint.StaticPart sjp) {
            log("before");
            assertEquals("hello", sjp.getSignature().getName());
        }

        @After("execution(* test.loadtime5.AtAspectJTest.hello())")
        public void after(JoinPoint.StaticPart sjp) {
            log("after");
            assertEquals("execution(public void test.loadtime5.AtAspectJTest.hello())", sjp.toLongString());
        }

        //TODO see String alias, see before advice name clash - all that works
        // 1/ String is in java.lang.* - see SimpleScope.javalangPrefix array
        // 2/ the advice is register thru its Bcel Method mirror
        @Before("execution(* test.loadtime5.AtAspectJTest.hello(String)) && args(s)")
        public void before(String s, JoinPoint.StaticPart sjp) {
            log("before-");
            log(s);
            assertEquals("hello", sjp.getSignature().getName());
        }

    }
}
