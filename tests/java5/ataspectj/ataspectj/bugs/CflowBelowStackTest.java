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
package ataspectj.bugs;

import ataspectj.TestHelper;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CflowBelowStackTest extends TestCase {

    public void testMe() {
        assertTrue(true);
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static Test suite() {
        return new TestSuite(CflowBelowStackTest.class);
    }


    @Aspect
    public static class TestAspect {

        @Pointcut("this(testCase) && execution(void test*())")
        public void inTestClass(TestCase testCase) {
        }

        private Map<String, Map<String, Integer>> coverage;

        @Before("cflowbelow(inTestClass(testCase)) && execution(* *(..))")
        public void beforeMethodExecution(JoinPoint thisJoinPoint, TestCase testCase) {
            String testname = testCase.getClass().getName();
            String methodSignature = thisJoinPoint.getStaticPart().getSignature().toString();
            Map<String, Integer> tests = coverage.get(methodSignature);
            if (tests == null) {
                tests = new HashMap<String, Integer>();
                coverage.put(methodSignature, tests);
            }
            Integer count = tests.get(testname);
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            tests.put(testname, count);
        }

        @Before("inTestClass(testCase)")
        public void beforeExecutingTestMethod(TestCase testCase) {
        }

        @After("inTestClass(testCase)")
        public void afterExecutingTestMethod(TestCase testCase) {
        }
    }
}
