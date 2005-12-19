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

import junit.framework.TestCase;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;

import java.util.Arrays;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DeclareParentsInterfaceTest extends TestCase {

    static class Target {
        void target() {
            log("hello");
        }
    }

    static interface Marker {}

    @Aspect
    static class TestAspect {

        @DeclareParents("ataspectj.DeclareParentsInterfaceTest.Target")
        Marker introduce;

        @Before("execution(* ataspectj.DeclareParentsInterfaceTest.Marker+.target())")
        public void before() {
            log("aop");
        }
    }

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public void testDecPInt() {
        Class[] intfs = Target.class.getInterfaces();
        assertTrue("Was not introduced", Arrays.asList(intfs).contains(Marker.class));
    }

    public void testDecPIntAdvised() {
        s_log = new StringBuffer();
        new Target().target();
        assertEquals("aop hello ", s_log.toString());
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DeclareParentsInterfaceTest.class);
    }
}
