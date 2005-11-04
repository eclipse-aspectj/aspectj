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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.JoinPoint;
import org.aspectj.runtime.internal.CFlowCounter;
import junit.framework.TestCase;

/**
 * Test pointcut reference without binding
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class PointcutReferenceTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(PointcutReferenceTest.class);
    }

    public void hello() {
        log("hello");
    }

    public void helloWithRef() {
        log("helloWithRef");
    }

    public void testPointcutReferenceNoBinding() {
        PointcutReferenceTest me = new PointcutReferenceTest();
        s_log = new StringBuffer();
        me.hello();
        assertEquals("before hello ", s_log.toString());
    }

    public void testPointcutReferenceBinding() {
        PointcutReferenceTest me = new PointcutReferenceTest();
        s_log = new StringBuffer();
        me.helloWithRef();
        assertEquals("beforeWithRef helloWithRef ", s_log.toString());
    }


    @Aspect
    public static class TestAspect {

        // order of pointcut in source code does not matter for pc refs
        @Pointcut("execution(* ataspectj.PointcutReferenceTest.hello(..))")
        void pcRef() {}

        @Pointcut("pcRef()")
        void pcRef2() {}

        @SuppressAjWarnings
        @Before("pcRef2()")
        public void before(JoinPoint jp) {
            log("before");
        }


        // see here outer aspect reference
        @Pointcut("execution(* ataspectj.PointcutReferenceTest.helloWithRef(..))" +
                " && ataspectj.PointcutReferenceTest.RefAspect.pcRefObjectBinding(t)")
        void pcRefBinding(Object t) {}

        @SuppressAjWarnings
        @Before("pcRefBinding(ttt)")
        public void before(Object ttt, JoinPoint jp) {
            log("beforeWithRef");
        }
    }

    @Aspect
    public static class RefAspect {
        @Pointcut("this(obj)")
        public void pcRefObjectBinding(PointcutReferenceTest obj) {}

    }

}
