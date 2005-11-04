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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AroundInlineMungerTestAspects {

    public static class OpenBase {
        protected void superMethod() {}
    }

    public static class OpenSubBase extends OpenBase {}

    // aspect will be prepared for inlining
    @Aspect
    public static class Open extends OpenSubBase {

        public static int aroundCount = 0;
        public static int beforeCount = 0;

        private int i;
        private static int I;

        @Around("execution(* ataspectj.AroundInlineMungerTest.target())")
        @SuppressAjWarnings
        public Object around1(ProceedingJoinPoint jp) throws Throwable {
            aroundCount++;
            priv(1, 2L, 3);
            super.superMethod();
            new Open.Inner().priv();//fails to be wrapped so this advice will not be inlined but previous call were still prepared
            return jp.proceed();
        }

        // this advice to test around advice body call/get/set advising
        @Before("(call(* ataspectj.AroundInlineMungerTestAspects.Open.priv(..))" +
                "  || get(int ataspectj.AroundInlineMungerTestAspects.Open.i)" +
                "  || set(int ataspectj.AroundInlineMungerTestAspects.Open.i)" +
                "  || get(int ataspectj.AroundInlineMungerTestAspects.Open.I)" +
                "  || set(int ataspectj.AroundInlineMungerTestAspects.Open.I)" +
                " )&& this(ataspectj.AroundInlineMungerTestAspects.Open)")
        @SuppressAjWarnings
        public void before1() {
            beforeCount++;
        }

        @Around("execution(* ataspectj.AroundInlineMungerTest.target())")
        @SuppressAjWarnings
        public Object around2(ProceedingJoinPoint jp) throws Throwable {
            aroundCount++;
            super.superMethod();
            new Open.Inner().priv();//fails to be wrapped so next calls won't be prepared but previous was
            priv(1, 2L, 3);
            return jp.proceed();
        }

        @Around("execution(* ataspectj.AroundInlineMungerTest.target())")
        @SuppressAjWarnings
        public Object around3(ProceedingJoinPoint jp) throws Throwable {
            aroundCount++;
            // all those field access will be wrapped
            int li = i;
            i = li;
            int lI = I;
            I = lI;
            return jp.proceed();
        }

        // -- some private member for which access will be wrapped so that around advice can be inlined

        private void priv(int i, long j, int k) {
            long l = i + j + k;
        }

        private static class Inner {
            private Inner() {}
            private void priv() {};
        }
    }

}
