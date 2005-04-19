/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package ataspectj;

import junit.framework.TestCase;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.JoinPoint;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class XXJoinPointTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(XXJoinPointTest.class);
    }

    public void hello() {
        log("hello");
    }

    public void testJoinPointsInAdviceSignature() {
        s_log = new StringBuffer();
        XXJoinPointTest me = new XXJoinPointTest();
        me.hello();
        assertEquals("jp sjp esjp jp-sjp sjp-esjp sjp-jp-esjp esjp-jp-sjp hello ", s_log.toString());
    }

    @Aspect
    public static class TestAspect {

        @Pointcut("call(* ataspectj.XXJoinPointTest.hello()) && within(ataspectj.XXJoinPointTest)")
        void pc() {}

        @Before("pc()")
        void before(JoinPoint jp) {
            assertEquals("hello", jp.getSignature().getName());
            log("jp");
        }

        @Before("pc()")
        void before(JoinPoint.StaticPart sjp) {
            assertEquals("hello", sjp.getSignature().getName());
            log("sjp");
        }

        @Before("pc()")
        void beforeEnclosing(JoinPoint.EnclosingStaticPart esjp) {
            assertEquals("testJoinPointsInAdviceSignature", esjp.getSignature().getName());
            log("esjp");
        }

        //weird order
        @Before("pc()")
        void beforeWEIRD1(JoinPoint jp, JoinPoint.StaticPart sjp) {
            assertEquals("hello", jp.getSignature().getName());
            assertEquals("hello", sjp.getSignature().getName());
            log("jp-sjp");
        }

        @Before("pc()")
        void before(JoinPoint.StaticPart sjp, JoinPoint.EnclosingStaticPart esjp) {
            assertEquals("hello", sjp.getSignature().getName());
            assertEquals("testJoinPointsInAdviceSignature", esjp.getSignature().getName());
            log("sjp-esjp");
        }

        // conventional order
        @Before("pc()")
        void before(JoinPoint.StaticPart sjp, JoinPoint jp, JoinPoint.EnclosingStaticPart esjp) {
            assertEquals("hello", sjp.getSignature().getName());
            assertEquals("hello", jp.getSignature().getName());
            assertEquals("testJoinPointsInAdviceSignature", esjp.getSignature().getName());
            log("sjp-jp-esjp");
        }

        // weird order
        @Before("pc()")
        void beforeWEIRD2(JoinPoint.EnclosingStaticPart esjp, JoinPoint jp, JoinPoint.StaticPart sjp) {
            assertEquals("testJoinPointsInAdviceSignature", esjp.getSignature().getName());
            assertEquals("hello", jp.getSignature().getName());
            assertEquals("hello", sjp.getSignature().getName());
            log("esjp-jp-sjp");
        }
    }

}
