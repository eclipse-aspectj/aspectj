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
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class SingletonInheritanceTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();

    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ataspectj.SingletonInheritanceTest.class);
    }

    public void hello() {
        log("hello");
    }

    public void hello2() {
        log("hello2");
    }

    public void testInheritance() {
        s_log = new StringBuffer();
        hello();
        assertEquals("aop hello ", s_log.toString());
    }

    public void testStaticRef() {
        s_log = new StringBuffer();
        hello2();
        assertEquals("aop2 hello2 ", s_log.toString());
    }

    @Aspect
    static abstract class AbstractAspect {
        @Pointcut("execution(* ataspectj.SingletonInheritanceTest.hello2())")
        void pc2() {}
    }

    @Aspect
    static abstract class ParentAspect {
        @Pointcut("execution(* ataspectj.SingletonInheritanceTest.hello())")
        void pc() {}
    }

    @Aspect
    static class ChildAspect extends ParentAspect {
    	    @SuppressAjWarnings
        @Before("pc()")
        public void abefore() {
            log("aop");
        }

    	    @SuppressAjWarnings
        @Before("ataspectj.SingletonInheritanceTest.AbstractAspect.pc2()")
        public void abefore2() {
            log("aop2");
        }
    }


}
