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

import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileReader;

/**
 * Test various advice and JoinPoint + binding, without pc ref
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class SingletonAspectBindingsTest2 extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(SingletonAspectBindingsTest2.class);
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
        SingletonAspectBindingsTest2 me = new SingletonAspectBindingsTest2();
        me.hello();
        // see here advice precedence as in source code order
        //TODO check around relative order
        // see fix in BcelWeaver sorting shadowMungerList
        //assertEquals("around2_ around_  before hello after _around _around2 ", s_log.toString());
        assertEquals("around_ around2_ before hello _around2 _around after ", s_log.toString());
    }

    public void testExecutionWithArgBinding() {
        s_log = new StringBuffer();
        SingletonAspectBindingsTest2 me = new SingletonAspectBindingsTest2();
        me.hello("x");
        assertEquals("before- x hello- x ", s_log.toString());
    }


    //@Aspect
    static aspect TestAspect {

        static int s = 0;

        static {
            s++;
        }

        public TestAspect() {
            // assert clinit has run when singleton aspectOf reaches that
            assertTrue(s>0);
        }

        //public static TestAspect aspectOf() {return null;}

        void around() : execution(* ataspectj.SingletonAspectBindingsTest2.hello()) {
        //public void aaround(ProceedingJoinPoint jp) {
            log("around_");
            try {
                proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            log("_around");
        }

        void around(Object t) : execution(* ataspectj.SingletonAspectBindingsTest2.hello()) && this(t) {
        //public void around2(ProceedingJoinPoint jp, Object t) {
            log("around2_");
            assertEquals(SingletonAspectBindingsTest2.class.getName(), t.getClass().getName());
            try {
                proceed(t);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            log("_around2");
        }

        before() : execution(* ataspectj.SingletonAspectBindingsTest2.hello()) {
        //public void before(JoinPoint.StaticPart sjp) {
            log("before");
            assertEquals("hello", thisJoinPointStaticPart.getSignature().getName());
        }

        after() : execution(* ataspectj.SingletonAspectBindingsTest2.hello()) {
        //public void after(JoinPoint.StaticPart sjp) {
            log("after");
            assertEquals("execution(public void ataspectj.SingletonAspectBindingsTest2.hello())", thisJoinPointStaticPart.toLongString());
        }

        //TODO see String alias, see before advice name clash - all that works
        // 1/ String is in java.lang.* - see SimpleScope.javalangPrefix array
        // 2/ the advice is register thru its Bcel Method mirror
        before(String s) : execution(* ataspectj.SingletonAspectBindingsTest2.hello(String)) && args(s) {
        //public void before(String s, JoinPoint.StaticPart sjp) {
            log("before-");
            log(s);
            assertEquals("hello", thisJoinPointStaticPart.getSignature().getName());
        }

    }

//    public void testHe() throws Throwable {
//        File f = new File("../tests/java5/ataspectj/ataspectj/SingletonAspectBindingsTest2.aj");
//        FileReader r = new FileReader(f);
//        int i = 0;
//        for (i = 0; i < 3950; i++) {
//            r.read();
//        }
//        for (;i < 4000; i++) {
//            if (i == 3983) System.out.print("X");
//            System.out.print((char)r.read());
//        }
//        System.out.print("|DONE");
//        r.close();
//    }
}
