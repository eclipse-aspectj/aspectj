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
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Aspects;
import junit.framework.Assert;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class PerClauseTestAspects {

    @Aspect()
    public static class TestAspectPerSingleton {
        static int s_count = 0;
        public TestAspectPerSingleton() {
            s_count++;
        }

        @SuppressAjWarnings
        @Before("execution(* ataspectj.PerClauseTest.perSingleton()) && target(t)")
        public void before(JoinPoint jp, Object t) {
            PerClauseTest.log("AOP."+jp.getSignature().getName());
            Assert.assertTrue("perX match", this.equals(Aspects.aspectOf(getClass())));
        }
    }

    @Aspect("pertarget(execution(* ataspectj.PerClauseTest.perTarget()))")
    public static class TestAspectPerTarget {
        static int s_count;

        public TestAspectPerTarget() {
            s_count++;
        }

        @SuppressAjWarnings
        @Before("execution(* ataspectj.PerClauseTest.perTarget()) && target(t)")
        public void before(JoinPoint jp, Object t) {
            PerClauseTest.log("AOP."+jp.getSignature().getName());
            Assert.assertTrue("perX match", this.equals(Aspects.aspectOf(getClass(), t)));
        }
    }

    @Aspect("percflow(execution(* ataspectj.PerClauseTest.perCflowEntry()))")
    public static class TestAspectPerCflow {
        static int s_count;

        public TestAspectPerCflow() {
            s_count++;
        }

        @SuppressAjWarnings
        @Before("execution(* ataspectj.PerClauseTest.perCflow())")
        public void before(JoinPoint jp) {
            PerClauseTest.log("AOP."+jp.getSignature().getName());
            Assert.assertTrue("perX match", this.equals(Aspects.aspectOf(getClass())));
        }
    }

    @Aspect("pertypewithin(ataspectj.PerClauseTest.PTW* && !ataspectj.PerClauseTest.PTWNoMatch)")
    public static class TestAspectPTW {
        static int s_count;

        public TestAspectPTW() {
            s_count++;
        }

        @SuppressAjWarnings
        @Before("execution(* ataspectj.PerClauseTest.PTW*.foo())")
        public void before(JoinPoint jp) {
            ;
        }
    }

    @Aspect("perthis(this(ataspectj.PerClauseTest.PerThis))")
    public static class TestAspectPerThis {
        static int s_count;
        static int a_count;

        public TestAspectPerThis() {
            s_count++;
        }

        @SuppressAjWarnings
        @Before("execution(* ataspectj.PerClauseTest.PerThis.foo())")
        public void before(JoinPoint jp) {
            a_count++;
        }
    }


}
