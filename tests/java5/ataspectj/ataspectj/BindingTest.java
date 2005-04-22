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

import junit.framework.TestCase;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class BindingTest extends TestCase {

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(BindingTest.class);
    }

    public int substract(int i, int j) {
        int res = i - j;
        return res+1;//see +1 here
    }

    public static int dup(int i) {
        return i*3;//see x3 here
    }

    public void testAroundArgs() {
        int res = substract(3, 2);// should be 2 without around advice
        assertEquals(1, res);
    }

    public void testProceedArgs() {
        int res = dup((3+1));// advice will change arg with arg-1
        assertEquals(6, res);
        callWithinStatic();
    }

    private static void callWithinStatic() {
        int res = dup((3+1));
        assertEquals(6, res);
    }

    @Aspect
    public static class TestAspect_1 {

        @Pointcut("call(int substract(int, int)) && within(ataspectj.BindingTest) && args(arg1, arg2)")
        void pc(int arg2, int arg1) {}// see rather fancy ordering here..

        @Around("pc(argAdvice2, argAdvice1) && target(t)")//see here ordering remade consistent
        public int aaround(ProceedingJoinPoint jp, BindingTest t, int argAdvice1, int argAdvice2) throws Throwable {
            int res = ((Integer)jp.proceed()).intValue();
            return res-1;
        }

        @Pointcut("call(int dup(int)) && within(ataspectj.BindingTest) && args(arg1)")
        void pc2(int arg1) {}

        @Around("pc2(argAdvice1)")
        public Object aaround2(int argAdvice1, ProceedingJoinPoint jp) throws Throwable {
            int res = ((Integer)jp.proceed(new Object[]{new Integer(argAdvice1-1)})).intValue();
            return new Integer(res/3*2);
        }
    }
}
