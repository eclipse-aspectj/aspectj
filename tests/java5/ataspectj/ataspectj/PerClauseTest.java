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
        return new junit.framework.TestSuite(PerClauseTest.class);
    }

    public void perSingleton() {
        log("perSingleton");
    }

    public void testPerSingleton() {
        s_log = new StringBuffer();

        // singleton is bound as soon as clinit
        try {
            assertTrue(Aspects.hasAspect(TestAspectPerSingleton.class));
            Aspects.aspectOf(TestAspectPerSingleton.class);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }

        perSingleton();
        assertEquals("AOP.perSingleton perSingleton ", s_log.toString());

        perSingleton();
        assertEquals(1, TestAspectPerSingleton.s_count);
    }

    @Aspect()
    public static class TestAspectPerSingleton {
        static int s_count = 0;
        public TestAspectPerSingleton() {
            s_count++;
        }

        @Before("execution(* ataspectj.PerClauseTest.perSingleton()) && target(t)")
        public void before(JoinPoint jp, Object t) {
            log("AOP."+jp.getSignature().getName());
            assertTrue("perX match", this.equals(Aspects.aspectOf(getClass())));
        }
    }


    public void perTarget() {
        log("perTarget");
    }

    public void testPerTarget() {
        s_log = new StringBuffer();
        perTarget();
        assertEquals("AOP.perTarget perTarget ", s_log.toString());

        // calling singleton API will fail
        try {
            assertFalse(Aspects.hasAspect(TestAspectPerTarget.class));
            Aspects.aspectOf(TestAspectPerTarget.class);
            fail("should fail with NOABE");
        } catch (NoAspectBoundException e) {
            ;//ok
        }

        // this per
        try {
            assertTrue(Aspects.hasAspect(TestAspectPerTarget.class, this));
            TestAspectPerTarget aspect = (TestAspectPerTarget) Aspects.aspectOf(TestAspectPerTarget.class, this);
            assertNotNull(aspect);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }

        // another per
        PerClauseTest me = new PerClauseTest();
        try {
            assertFalse(Aspects.hasAspect(TestAspectPerTarget.class, me));
            Aspects.aspectOf(TestAspectPerTarget.class, me);
            fail("should fail");
        } catch (NoAspectBoundException e) {
            ;//ok
        }
        me.perTarget();
        try {
            assertTrue(Aspects.hasAspect(TestAspectPerTarget.class, me));
            TestAspectPerTarget aspect = (TestAspectPerTarget) Aspects.aspectOf(TestAspectPerTarget.class, me);
            assertNotNull(aspect);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }

        assertEquals(2, TestAspectPerTarget.s_count);
    }

    @Aspect("pertarget(execution(* ataspectj.PerClauseTest.perTarget()))")
    public static class TestAspectPerTarget {
        static int s_count;

        public TestAspectPerTarget() {
            s_count++;
        }

        @Before("execution(* ataspectj.PerClauseTest.perTarget()) && target(t)")
        public void before(JoinPoint jp, Object t) {
            log("AOP."+jp.getSignature().getName());
            assertTrue("perX match", this.equals(Aspects.aspectOf(getClass(), t)));
        }
    }

    public void perCflowEntry() {
        // the aspect is bound to the executing thread
        try {
            assertTrue(Aspects.hasAspect(TestAspectPerCflow.class));
            Aspects.aspectOf(TestAspectPerCflow.class);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }
        perCflow();
    }

    public void perCflow() {
        log("perCflow");
    }

    public void testPerCflow() throws Throwable {
        s_log = new StringBuffer();

        // no aspect bound yet
        try {
            assertFalse(Aspects.hasAspect(TestAspectPerCflow.class));
            Aspects.aspectOf(TestAspectPerCflow.class);
            fail("No perCflow should be bound yet");
        } catch (NoAspectBoundException e) {
            ;//ok
        }

        perCflow();
        assertEquals("perCflow ", s_log.toString());
        // still no aspect bound yet
        try {
            assertFalse(Aspects.hasAspect(TestAspectPerCflow.class));
            Aspects.aspectOf(TestAspectPerCflow.class);
            fail("No perCflow should be bound yet");
        } catch (NoAspectBoundException e) {
            ;//ok
        }

        s_log = new StringBuffer();
        perCflowEntry();
        assertEquals("AOP.perCflow perCflow ", s_log.toString());
        // no aspect bound anymore since went OUT of the per clause
        try {
            assertFalse(Aspects.hasAspect(TestAspectPerCflow.class));
            Aspects.aspectOf(TestAspectPerCflow.class);
            fail("No perCflow should be bound anymore");
        } catch (NoAspectBoundException e) {
            ;//ok
        }

        Runnable rok = new Runnable() {
            public void run() {
                perCflowEntry();
            }
        };
        Thread trok = new Thread(rok);
        trok.start();
        trok.join();

        Runnable rko = new Runnable() {
            public void run() {
                perCflow();
            }
        };
        Thread trko = new Thread(rko);
        trko.start();
        trko.join();

        assertEquals(2, TestAspectPerCflow.s_count);
    }

    @Aspect("percflow(execution(* ataspectj.PerClauseTest.perCflowEntry()))")
    public static class TestAspectPerCflow {
        static int s_count;

        public TestAspectPerCflow() {
            s_count++;
        }

        @Before("execution(* ataspectj.PerClauseTest.perCflow())")
        public void before(JoinPoint jp) {
            log("AOP."+jp.getSignature().getName());
            assertTrue("perX match", this.equals(Aspects.aspectOf(getClass())));
        }
    }

    public void testPerTypeWithin() {
        assertTrue(Aspects.hasAspect(TestAspectPTW.class, PTW1.class));
        assertTrue(Aspects.hasAspect(TestAspectPTW.class, PTW2.class));
        assertFalse(Aspects.hasAspect(TestAspectPTW.class, PTWNoMatch.class));

        PTW1.foo();
        PTW2.foo();
        PTWNoMatch.foo();

        assertEquals(2, TestAspectPTW.s_count);
        try {
            assertTrue(Aspects.hasAspect(TestAspectPTW.class, PTW1.class));
            assertTrue(Aspects.hasAspect(TestAspectPTW.class, PTW2.class));
            Aspects.aspectOf(TestAspectPTW.class, PTW1.class);
            Aspects.aspectOf(TestAspectPTW.class, PTW2.class);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }
        try {
            assertFalse(Aspects.hasAspect(TestAspectPTW.class, PTWNoMatch.class));
            Aspects.aspectOf(TestAspectPTW.class, PTWNoMatch.class);
            fail("should not have PTW aspect");
        } catch (NoAspectBoundException e) {
            ;//ok
        }
    }
    
    static class PTW1 {
        static void foo() {};
    }
    static class PTW2 {
        static void foo() {};
    }
    static class PTWNoMatch {
        static void foo() {};
    }

    @Aspect("pertypewithin(ataspectj.PerClauseTest.PTW* && !ataspectj.PerClauseTest.PTWNoMatch)")
    public static class TestAspectPTW {
        static int s_count;

        public TestAspectPTW() {
            s_count++;
        }

        @Before("execution(* ataspectj.PerClauseTest.PTW*.foo())")
        public void before(JoinPoint jp) {
            ;
        }

    }
}
