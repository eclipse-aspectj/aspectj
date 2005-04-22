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
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Aspects;
import org.aspectj.lang.NoAspectBoundException;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class PerClauseTest extends TestCase {

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

    public void perTarget() {
        log("perTarget");
    }

    public void testPerTarget() {
        s_log = new StringBuffer();
        perTarget();
        assertEquals("AOP.perTarget perTarget ", s_log.toString());

        // singleton
        try {
            Aspects.aspectOf(TestAspectPerTarget.class);
            fail("should fail with NOABE");
        } catch (NoAspectBoundException e) {
            ;//ok
        }

        // this per
        try {
            TestAspectPerTarget aspect = (TestAspectPerTarget) Aspects.aspectOf(TestAspectPerTarget.class, this);
            assertNotNull(aspect);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }

        // another per
        PerClauseTest me = new PerClauseTest();
        try {
            Aspects.aspectOf(TestAspectPerTarget.class, me);
            fail("should fail");
        } catch (NoAspectBoundException e) {
            ;//ok
        }
        me.perTarget();
        try {
            TestAspectPerTarget aspect = (TestAspectPerTarget) Aspects.aspectOf(TestAspectPerTarget.class, me);
            assertNotNull(aspect);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }
    }

    @Aspect("pertarget(execution(* ataspectj.PerClauseTest.perTarget()))")
    public static class TestAspectPerTarget {

        public TestAspectPerTarget() {
            ;
        }

        @Before("execution(* ataspectj.PerClauseTest.perTarget()) && target(t)")
        public void before(JoinPoint jp, Object t) {
            log("AOP."+jp.getSignature().getName());
            assertTrue("perX match", this.equals(Aspects.aspectOf(getClass(), t)));
        }
    }

    public void perCflowEntry() {
        perCflow();
    }

    public void perCflow() {
        log("perCflow");
    }

    public void testPerCflow() {
        s_log = new StringBuffer();
        perCflow();
        assertEquals("perCflow ", s_log.toString());

        s_log = new StringBuffer();
        perCflowEntry();
        assertEquals("AOP.perCflow perCflow ", s_log.toString());
    }

    @Aspect("percflow(execution(* ataspectj.PerClauseTest.perCflowEntry()))")
    public static class TestAspectPerCflow {

        public TestAspectPerCflow() {
            ;
        }

        @Before("execution(* ataspectj.PerClauseTest.perCflow())")
        public void before(JoinPoint jp) {
            log("AOP."+jp.getSignature().getName());
            assertTrue("perX match", this.equals(Aspects.aspectOf(getClass(), Thread.currentThread())));
        }
    }
}
