/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.runtime.internal.AroundClosure;
import junit.framework.TestCase;

/**
 * Test various advice and JoinPoint + binding, without pc ref
 *
 * test commit..
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class SingletonAspectBindingsTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(SingletonAspectBindingsTest.class);
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
        System.err.println("First test running");
        SingletonAspectBindingsTest me = new SingletonAspectBindingsTest();
        System.err.println("Calling hello()");
        me.hello();
        System.err.println("Returned from hello()");
        // see here advice precedence as in source code order
        //TODO check around relative order
        // see fix in BcelWeaver sorting shadowMungerList
        System.err.println(">GOT:"+s_log.toString());
        System.err.println("Expected:"+"around2_ around_  before hello after _around _around2 ");
        //assertEquals("around2_ around_ before hello after _around _around2 ", s_log.toString());
        assertEquals("around_ around2_ before hello _around2 _around after ", s_log.toString());
    }

    public void testExecutionWithArgBinding() {
        s_log = new StringBuffer();
        SingletonAspectBindingsTest me = new SingletonAspectBindingsTest();
        System.err.println("Calling hello(x)");
        me.hello("x");
        System.err.println("Returned from hello(x)");
        assertEquals("before- x hello- x ", s_log.toString());
    }


    @Aspect("issingleton")
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

        @Around("execution(* SingletonAspectBindingsTest.hello())")
        public void aaround(JoinPoint jp) {
            log("around_");
            System.err.println("In first around advice");
            try {
                jp.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            log("_around");
        }

        @Around("execution(* SingletonAspectBindingsTest.hello()) && this(t)")
        public void around2(Object t, JoinPoint jp) {
            log("around2_");
            System.err.println("In second around advice");
            System.err.println("Joinpoint is "+jp);
            assertEquals(SingletonAspectBindingsTest.class.getName(), t.getClass().getName());
            try {
                jp.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            log("_around2");
        }

        @Before("execution(* SingletonAspectBindingsTest.hello())")
        public void before(JoinPoint.StaticPart sjp) {
            log("before");
            System.err.println("In before advice");
            assertEquals("hello", sjp.getSignature().getName());
        }

        @After("execution(* SingletonAspectBindingsTest.hello())")
        public void after(JoinPoint.StaticPart sjp) {
            log("after");
            System.err.println("In after advice");
            assertEquals("execution(public void SingletonAspectBindingsTest.hello())", sjp.toLongString());
        }

        //TODO see String, see before advice name clash - all that works
        // 1/ String is in java.lang.* - see SimpleScope.javalangPrefix array
        // 2/ the advice is register thru its Bcel Method mirror
        @Before("execution(* SingletonAspectBindingsTest.hello(String)) && args(s)")
        public void before(String s, JoinPoint.StaticPart sjp) {
            log("before-");
            log(s);
            assertEquals("hello", sjp.getSignature().getName());
        }

    }
}
