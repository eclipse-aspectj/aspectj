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
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.runtime.internal.CFlowCounter;
import junit.framework.TestCase;

/**
 * Test cflow (LTW of the aspect to add cflow fields to it)
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CflowTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(CflowTest.class);
    }

    public void hello() {
        log("hello");
    }

    public void startCflow() {
        hello();
    }

    public void testCflow() {
        CflowTest me = new CflowTest();
        s_log = new StringBuffer();
        me.hello();
        assertEquals("hello ", s_log.toString());

        s_log = new StringBuffer();
        me.startCflow();
        assertEquals("before hello ", s_log.toString());
    }

    @Aspect
    public static class TestAspect {

        //LTW will add:
        //public static final CFlowCounter ajc$cflowCounter$0 = new CFlowCounter();
    	    @SuppressAjWarnings
        @Before("execution(* ataspectj.CflowTest.hello(..)) && this(t) && cflow(execution(* ataspectj.CflowTest.startCflow(..)))")
        public void before(Object t, JoinPoint jp) {
            assertEquals(CflowTest.class.getName(), t.getClass().getName());
            try {
                //jp.proceed();
            } catch (Throwable throwable) {
                fail("proceed called in before Advice must be a NO-OP:" + throwable.toString());
            }
            log("before");
        }
    }

}
