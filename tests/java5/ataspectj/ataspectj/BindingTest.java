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
import org.aspectj.lang.Aspects;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import java.security.PrivilegedAction;

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

    public int echo(int i) {
        return i;
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

    public void testProceedInInner() {
        int res = echo(3);//advice will x2 using an inner
        assertEquals(6, res);
    }

    public void testNoProceed() {
        int res = echo(3);//advice will return 0 and not proceed
        assertEquals(0, res);
    }

    public void testDoubleProceed() {
        int res = echo(3);//advice will proceed twice and add the returned values
        assertEquals(6, res);
    }

    public void testDoubleProceedOneInner() {
        int res = echo(3);//advice will proceed twice with one in inner and add the returned values
        assertEquals(6, res);
    }

    public void testAccessAspectState() {
        TestAspect_1 aspect = (TestAspect_1) Aspects.aspectOf(TestAspect_1.class);
        aspect.m_count = 0;
        int res = echo(3);
        res += echo(3);
        assertEquals(6, res);
        assertEquals(2, aspect.m_count);
    }

    public void testTryCatch() {
        assertEquals(6, echo(3));
    }

    private static void callWithinStatic() {
        int res = dup((3+1));
        assertEquals(6, res);
    }

    @Aspect
    public static class TestAspect_1 {

        private int m_count = 0;

        @Pointcut("call(int substract(int, int)) && within(ataspectj.BindingTest) && args(arg1, arg2)")
        void pc(int arg2, int arg1) {}// see rather fancy ordering here..

        // see return int here.
        @SuppressAjWarnings
        @Around("pc(argAdvice2, argAdvice1) && target(t)")//see here ordering remade consistent
        public int aaround(ProceedingJoinPoint jp, BindingTest t, int argAdvice1, int argAdvice2) throws Throwable {
            int res = ((Integer)jp.proceed()).intValue();
            return res-1;
        }

        @Pointcut("call(int dup(int)) && within(ataspectj.BindingTest) && args(arg1)")
        void pc2(int arg1) {}

        @SuppressAjWarnings
        @Around("pc2(argAdvice1)")
        public Object aaround2(int argAdvice1, ProceedingJoinPoint jp) throws Throwable {
            int res = ((Integer)jp.proceed(new Object[]{new Integer(argAdvice1-1)})).intValue();
            return new Integer(res/3*2);
        }

        @SuppressAjWarnings
        @Around("call(int echo(int)) && withincode(void ataspectj.BindingTest.testProceedInInner()) && args(i)")
        public int aaround3(int i, final ProceedingJoinPoint jp) throws Throwable {
            final StringBuffer sb = new StringBuffer();
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        int res = ((Integer)jp.proceed()).intValue();
                        sb.append(res);
                    } catch (Throwable t) {
                        fail(t.toString());
                    }
                }
            };
            Thread t = new Thread(r);
            t.start();
            t.join();
            assertEquals(i, Integer.parseInt(sb.toString()));
            return Integer.parseInt(sb.toString())*2;
        }

       @SuppressAjWarnings
       @Around("call(int echo(int)) && withincode(void ataspectj.BindingTest.testNoProceed()) && args(i)")
        public int aaround4(int i, final ProceedingJoinPoint jp) throws Throwable {
            // since no proceed() is call, this advice won't be inlined
            return 0;
        }

        @SuppressAjWarnings
        @Around("call(int echo(int)) && withincode(void ataspectj.BindingTest.testDoubleProceed()) && args(i)")
        public int aaround5(int i, final ProceedingJoinPoint jp) throws Throwable {
            int i1 = ((Integer)jp.proceed()).intValue();
            int i2 = ((Integer)jp.proceed()).intValue();
            return i1 + i2;
        }

       @SuppressAjWarnings
       @Around("call(int echo(int)) && withincode(void ataspectj.BindingTest.testDoubleProceedOneInner()) && args(i)")
        public int aaround6(int i, final ProceedingJoinPoint jp) throws Throwable {
            int i1 = ((Integer)jp.proceed()).intValue();
            Object io2 = new PrivilegedAction() {
                public Object run() {
                    try {
                        return jp.proceed();
                    } catch (Throwable t) {
                        fail(t.toString());
                        return null;
                    }
                }
            }.run();
            if (io2 == null) {
                // since inlining occured, proceed was never called
                fail("should not happen - advice was probably inlined while it must not be");
                return i1 * 2;
            } else {
                int i2 = ((Integer)io2).intValue();
                return i1 + i2;
            }
        }

        @SuppressAjWarnings
        @Around("call(int echo(int)) && withincode(void ataspectj.BindingTest.testAccessAspectState()) && args(i)")
        public Object aaround7(int i, final ProceedingJoinPoint jp) throws Throwable {
            m_count++;// will be wrapped for inlining support
            return jp.proceed();
        }

       @SuppressAjWarnings
       @Around("call(int echo(int)) && withincode(void ataspectj.BindingTest.testTryCatch()) && args(i)")
        public Object aaround8(int i, final ProceedingJoinPoint jp) throws Throwable {
            try {
                return 2*((Integer)jp.proceed()).intValue();
            } catch (Throwable t) {
                throw t;
            }
        }
    }

}
