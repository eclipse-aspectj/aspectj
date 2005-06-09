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
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import junit.framework.TestCase;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class IfPointcutTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public void hello(int i) {
        log("hello-" + i);
    }

    public void testIf() {
        s_log = new StringBuffer();
        IfPointcutTest me = new IfPointcutTest();
        me.hello(1);
        assertEquals("aop hello-1 ", s_log.toString());
        me.hello(-1);
        assertEquals("aop hello-1 hello--1 ", s_log.toString());//unchanged
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(IfPointcutTest.class);
    }

    @Aspect
    public static class TestAspect {

        @Pointcut("args(i) && if() && if(true)")
        public static boolean positive(int i) {
            return i>=0;
        }

        @Before("execution(* ataspectj.IfPointcutTest.hello(int)) && positive(i)")
        public void before(int i) {
            log("aop");
        }
    }
}
