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

import org.aspectj.lang.annotation.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import junit.framework.TestCase;

/**
 * Test various advice and JoinPoint + binding, without pc ref
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class SingletonAspectBindingsTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
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
        SingletonAspectBindingsTest me = new SingletonAspectBindingsTest();
        me.hello();
        // see here advice precedence as in source code order
        //TODO check around relative order
        // see fix in BcelWeaver sorting shadowMungerList
        //assertEquals("around2_ around_  before hello after _around _around2 ", s_log.toString());
        assertEquals("around_ around2_ before hello _around2 _around after ", s_log.toString());
    }

    public void testExecutionWithArgBinding() {
        s_log = new StringBuffer();
        SingletonAspectBindingsTest me = new SingletonAspectBindingsTest();
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

        @Around("execution(* ataspectj.SingletonAspectBindingsTest.hello())")
        public void aaround(ProceedingJoinPoint jp) {
            log("around_");
            try {
                jp.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            log("_around");
        }

        @Around("execution(* ataspectj.SingletonAspectBindingsTest.hello()) && this(t)")
        public void around2(ProceedingJoinPoint jp, Object t) {
            log("around2_");
            assertEquals(SingletonAspectBindingsTest.class.getName(), t.getClass().getName());
            try {
                jp.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            log("_around2");
        }

        @Before("execution(* ataspectj.SingletonAspectBindingsTest.hello())")
        public void before(JoinPoint.StaticPart sjp) {
            log("before");
            assertEquals("hello", sjp.getSignature().getName());
        }

        @After("execution(* ataspectj.SingletonAspectBindingsTest.hello())")
        public void after(JoinPoint.StaticPart sjp) {
            log("after");
            assertEquals("execution(public void ataspectj.SingletonAspectBindingsTest.hello())", sjp.toLongString());
        }

        //TODO see String alias, see before advice name clash - all that works
        // 1/ String is in java.lang.* - see SimpleScope.javalangPrefix array
        // 2/ the advice is register thru its Bcel Method mirror
        @Before("execution(* ataspectj.SingletonAspectBindingsTest.hello(String)) && args(s)")
        public void before(String s, JoinPoint.StaticPart sjp) {
            log("before-");
            log(s);
            assertEquals("hello", sjp.getSignature().getName());
        }

    }

//    public void testHe() throws Throwable {
//        //Allow to look inn file based on advises/advised-by offset numbers
//        File f = new File("../tests/java5/ataspectj/ataspectj/SingletonAspectBindingsTest.java");
//        FileReader r = new FileReader(f);
//        int i = 0;
//        for (i = 0; i < 3700; i++) {
//            r.read();
//        }
//        for (;i < 3800; i++) {
//            if (i==3721 || i == 3742 || i == 3777) System.out.print("X");
//            System.out.print((char)r.read());
//        }
//        System.out.print("|DONE");
//        r.close();
//    }
}
