/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
        System.out.println("IfPointcutTest.hello " + i);
    }

    public void testIf() {
        IfPointcutTest me = new IfPointcutTest();
        me.hello(1);
        me.hello(-1);
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(IfPointcutTest.class);
    }



    @Aspect
    public static class TestAspect {

        public boolean positive(int i) {
            return (i>=0);
        }

        @Pointcut("args(i) && if(i>0)")
        void ifPc(int i) {}

        @Before("execution(* ataspectj.IfPointcutTest.hello(int)) && ifPc(i)")
        void before(int i) {
            System.out.println("IfPointcutTest$TestAspect.before");
        }
    }
}
