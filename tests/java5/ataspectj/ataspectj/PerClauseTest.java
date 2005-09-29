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
            assertTrue(Aspects.hasAspect(PerClauseTestAspects.TestAspectPerSingleton.class));
            Aspects.aspectOf(PerClauseTestAspects.TestAspectPerSingleton.class);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }

        perSingleton();
        assertEquals("AOP.perSingleton perSingleton ", s_log.toString());

        perSingleton();
        assertEquals(1, PerClauseTestAspects.TestAspectPerSingleton.s_count);
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
            assertFalse(Aspects.hasAspect(PerClauseTestAspects.TestAspectPerTarget.class));
            Aspects.aspectOf(PerClauseTestAspects.TestAspectPerTarget.class);
            fail("should fail with NOABE");
        } catch (NoAspectBoundException e) {
            ;//ok
        }

        // this per
        try {
            assertTrue(Aspects.hasAspect(PerClauseTestAspects.TestAspectPerTarget.class, this));
            PerClauseTestAspects.TestAspectPerTarget aspect = (PerClauseTestAspects.TestAspectPerTarget) Aspects.aspectOf(PerClauseTestAspects.TestAspectPerTarget.class, this);
            assertNotNull(aspect);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }

        // another per
        PerClauseTest me = new PerClauseTest();
        try {
            assertFalse(Aspects.hasAspect(PerClauseTestAspects.TestAspectPerTarget.class, me));
            Aspects.aspectOf(PerClauseTestAspects.TestAspectPerTarget.class, me);
            fail("should fail");
        } catch (NoAspectBoundException e) {
            ;//ok
        }
        me.perTarget();
        try {
            assertTrue(Aspects.hasAspect(PerClauseTestAspects.TestAspectPerTarget.class, me));
            PerClauseTestAspects.TestAspectPerTarget aspect = (PerClauseTestAspects.TestAspectPerTarget) Aspects.aspectOf(PerClauseTestAspects.TestAspectPerTarget.class, me);
            assertNotNull(aspect);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }

        assertEquals(2, PerClauseTestAspects.TestAspectPerTarget.s_count);
    }

    public void perCflowEntry() {
        // the aspect is bound to the executing thread
        try {
            assertTrue(Aspects.hasAspect(PerClauseTestAspects.TestAspectPerCflow.class));
            Aspects.aspectOf(PerClauseTestAspects.TestAspectPerCflow.class);
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
            assertFalse(Aspects.hasAspect(PerClauseTestAspects.TestAspectPerCflow.class));
            Aspects.aspectOf(PerClauseTestAspects.TestAspectPerCflow.class);
            fail("No perCflow should be bound yet");
        } catch (NoAspectBoundException e) {
            ;//ok
        }

        perCflow();
        assertEquals("perCflow ", s_log.toString());
        // still no aspect bound yet
        try {
            assertFalse(Aspects.hasAspect(PerClauseTestAspects.TestAspectPerCflow.class));
            Aspects.aspectOf(PerClauseTestAspects.TestAspectPerCflow.class);
            fail("No perCflow should be bound yet");
        } catch (NoAspectBoundException e) {
            ;//ok
        }

        s_log = new StringBuffer();
        perCflowEntry();
        assertEquals("AOP.perCflow perCflow ", s_log.toString());
        // no aspect bound anymore since went OUT of the per clause
        try {
            assertFalse(Aspects.hasAspect(PerClauseTestAspects.TestAspectPerCflow.class));
            Aspects.aspectOf(PerClauseTestAspects.TestAspectPerCflow.class);
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

        assertEquals(2, PerClauseTestAspects.TestAspectPerCflow.s_count);
    }

    public void testPerTypeWithin() {
        assertTrue(Aspects.hasAspect(PerClauseTestAspects.TestAspectPTW.class, PTW1.class));
        assertTrue(Aspects.hasAspect(PerClauseTestAspects.TestAspectPTW.class, PTW2.class));
        assertFalse(Aspects.hasAspect(PerClauseTestAspects.TestAspectPTW.class, PTWNoMatch.class));

        PTW1.foo();
        PTW2.foo();
        PTWNoMatch.foo();

        assertEquals(2, PerClauseTestAspects.TestAspectPTW.s_count);
        try {
            assertTrue(Aspects.hasAspect(PerClauseTestAspects.TestAspectPTW.class, PTW1.class));
            assertTrue(Aspects.hasAspect(PerClauseTestAspects.TestAspectPTW.class, PTW2.class));
            Aspects.aspectOf(PerClauseTestAspects.TestAspectPTW.class, PTW1.class);
            Aspects.aspectOf(PerClauseTestAspects.TestAspectPTW.class, PTW2.class);
        } catch (NoAspectBoundException e) {
            fail(e.toString());
        }
        try {
            assertFalse(Aspects.hasAspect(PerClauseTestAspects.TestAspectPTW.class, PTWNoMatch.class));
            Aspects.aspectOf(PerClauseTestAspects.TestAspectPTW.class, PTWNoMatch.class);
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

    public void testPerThis() {
        assertEquals(0, PerClauseTestAspects.TestAspectPerThis.s_count);

        PerThis i1 = new PerThis();
        i1.foo();
        assertEquals(1, PerClauseTestAspects.TestAspectPerThis.s_count);
        i1.foo();
        assertEquals(1, PerClauseTestAspects.TestAspectPerThis.s_count);

        new PerThis().foo();
        assertEquals(2, PerClauseTestAspects.TestAspectPerThis.s_count);

        assertEquals(3, PerClauseTestAspects.TestAspectPerThis.a_count);
    }

    static class PerThis {
        void foo() {};
    }
}
