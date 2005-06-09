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
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class IfPointcut2Test extends TestCase {

    static StringBuffer s_log = new StringBuffer();

    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public void testSome() {
        Foo f = new Foo();
        f.doo();
        f.doo(1);
        f.dooMulti();
        assertEquals(
                "test aop test2-doo-doo aop2 doo test3-1-doo-doo-doo aop3 doo-1 testTWO-dooMulti testONE-dooMulti aop doMulti ",
                s_log.toString()
        );

        s_log = new StringBuffer();
        IfAspect.ISON = false;
        f.doo();
        f.doo(1);
        f.dooMulti();
        assertEquals(
                "test test2-doo-doo doo test3-1-doo-doo-doo doo-1 testTWO-dooMulti doMulti ",                
                s_log.toString()
        );
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(IfPointcut2Test.class);
    }


    @Aspect
    public static class IfAspect {

        static boolean ISON = true;

        @Pointcut("execution(* ataspectj.IfPointcut2Test.Foo.doo()) && if()")
        public static boolean test() {
            log("test");
            return ISON;
        }

        @Pointcut("execution(* ataspectj.IfPointcut2Test.Foo.doo()) && if()")
        public static boolean test2(JoinPoint.StaticPart sjp, JoinPoint.StaticPart sjp2) {// will require JP flags
            log("test2-" + sjp.getSignature().getName() + "-" + sjp2.getSignature().getName());
            return ISON;
        }

        @Pointcut("execution(* ataspectj.IfPointcut2Test.Foo.doo(int)) && args(val) && if()")
        public static boolean test3(JoinPoint jp, int val, JoinPoint jpbis, JoinPoint.StaticPart jps) {
            log(
                    "test3-" + val + "-" + jp.getSignature().getName() + "-" + jpbis.getSignature().getName() + "-" + jps.getSignature().getName()
            );
            return ISON;
        }

        //-- if && if thru reference
        @Pointcut("if()")
        public static boolean testONE(JoinPoint jp) {
            log("testONE-" + jp.getSignature().getName());
            return ISON;
        }

        @Pointcut("if()")
        public static boolean testTWO(JoinPoint.EnclosingStaticPart ejp) {
            log("testTWO-" + ejp.getSignature().getName());
            return ISON;
        }

        @Before("execution(* ataspectj.IfPointcut2Test.Foo.dooMulti()) && testONE(jp) && testTWO(ejp)")
        public void beforeMULTI(JoinPoint jp, JoinPoint.EnclosingStaticPart ejp) {
            log("aop");
        }

        //-- basic
        @Before("test()")
        public void doBefore() {
            log("aop");
        }

        @Before("test2(jp, jp2)")
        public void doBefore2(JoinPoint.StaticPart jp, JoinPoint.StaticPart jp2) {
            log("aop2");
        }

        @Before("test3(jp, i, jpbis, jps)")
        public void doBefore3(int i, JoinPoint jp, JoinPoint.StaticPart jps, JoinPoint jpbis) {
            log("aop3");
        }
    }

    static class Foo {
        public void doo() {
            log("doo");
        }

        public void doo(int i) {
            log("doo-" + i);
        }

        public void dooMulti() {
            log("doMulti");
        }
    }
}
