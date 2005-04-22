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
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.JoinPoint;
import junit.framework.TestCase;

/**
 * AfterXXX tests
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AfterXTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AfterXTest.class);
    }

    public int dupPos(int i) throws NoSuchMethodException {
        if (i > 0)
            return i*2;
        else
            throw new NoSuchMethodException("arg is negative");
    }

    public void testAfterReturningAndThrowing() {
        AfterXTest me = new AfterXTest();

        try {
            s_log = new StringBuffer();
            int dup2 = me.dupPos(2);
            // see after advice precedence here..
            //TODO really weird after finally comes first
            // see BcelWeaver fix on sorting of shadowMungerList
            //assertEquals("after after2- 2 4 afterRet- 2 ", s_log.toString());
            assertEquals("afterRet- 2 after2- 2 4 after ", s_log.toString());
        } catch (Exception e) {
            fail("Should not fail " + e.toString());
        }

        s_log = new StringBuffer();
        try {
            int dupm2 = me.dupPos(-2);
            fail("should not be reached");
        } catch (NoSuchMethodException e) {
            //TODO really weird after finally comes first
            // see BcelWeaver fix in sorting of shadowMungerList
            assertEquals("after afterThrowing afterThrowing3- [arg is negative] ", s_log.toString());
        }
    }



    @Aspect
    public static class TestAspect {

        @AfterReturning("execution(int ataspectj.AfterXTest.dupPos(..)) && args(arg)")
        public void afterRet(int arg) {
            log("afterRet-");
            log(""+arg);
        }

        @AfterReturning(returning="ret", pointcut="execution(int ataspectj.AfterXTest.dupPos(..)) && args(arg)")
        public void after2(int arg, int ret) {//CORRECT
        //public void after2(int ret, int arg) {//INCORRECT
            log("after2-");
            log(""+arg);
            log(""+ret);
        }

        @After("execution(int ataspectj.AfterXTest.dupPos(..))")
        public void after() {
            log("after");
        }

        @AfterThrowing("execution(int ataspectj.AfterXTest.dupPos(..))")
        public void afterThrowing(JoinPoint jp) {
            log("afterThrowing");
        }

        // formal binding is mandatory in AJ
        //@AfterThrowing(throwned="java.lang.RuntimeException", pointcut="execution(int alex.test.ReturnAndThrowHelloWorld.dupPos(..))")
        //public void afterThrowing2() {
        @AfterThrowing(throwing= "e", pointcut="execution(int ataspectj.AfterXTest.dupPos(..))")
        public void afterThrowing2(RuntimeException e) {
            fail("should not be bounded");
        }

        @AfterThrowing(throwing= "e", value="execution(int ataspectj.AfterXTest.dupPos(..))")
        public void afterThrowing3(NoSuchMethodException e) {
            log("afterThrowing3-");
            log("["+e.getMessage()+"]");
        }
    }

}
