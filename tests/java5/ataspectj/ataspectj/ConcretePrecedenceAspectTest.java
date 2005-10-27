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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ConcretePrecedenceAspectTest extends TestCase {

    static String LOG = "";

    void target() {
        LOG = LOG + "target ";
    }

    @Aspect
    static class TestAspect_1 {
        @Before("execution(* ataspectj.ConcretePrecedenceAspectTest.target())")
        public void before() {
            LOG = LOG + "1 ";
        }
    }

    @Aspect
    static class TestAspect_2 {
        @Before("execution(* ataspectj.ConcretePrecedenceAspectTest.target())")
        public void before() {
            LOG = LOG + "2 ";
        }
    }

    @Aspect
    static class TestAspect_3 {
        @Before("execution(* ataspectj.ConcretePrecedenceAspectTest.target())")
        public void before() {
            LOG = LOG + "3 ";
        }
    }

    public void testPrecedenceFromXML() {
        LOG = "";
        target();
        assertEquals("2 3 1 target ", LOG);
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static Test suite() {
        return new TestSuite(ConcretePrecedenceAspectTest.class);
    }

}
