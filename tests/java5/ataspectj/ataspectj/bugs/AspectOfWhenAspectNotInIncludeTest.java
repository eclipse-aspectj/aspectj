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

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import ataspectj.TestHelper;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.Aspects;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AspectOfWhenAspectNotInIncludeTest extends TestCase {

    static int I;

    void target() {
        I++;
    }

    @Aspect
    static class TestAspectForAspect {
        @DeclareParents("ataspectj.bugs.AspectOfWhenAspectNotInIncludeTest.TestAspect")
        Serializable shouldNotHappenDueToInclude;
    }

    @Aspect("perthis(execution(* ataspectj.bugs.AspectOfWhenAspectNotInIncludeTest.target()))")
    static class TestAspect {

        public TestAspect() {
            int i = 0;
            i++;
        }

        @Before("execution(* ataspectj.bugs.AspectOfWhenAspectNotInIncludeTest.target())")
        public void before() {
            I++;
        }
    }

    public void testInclude() {
        I = 0;
        target();
        assertEquals(1, I);//aspect not applied as per aop.xml include
    }

    public void testAspectOf() {
        // aspectOf method has been added to the aspect, no matter the aop.xml include/exclude
        try {
            Method m = TestAspect.class.getDeclaredMethod("aspectOf", new Class[]{Object.class});
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

    public void testAspectUntouched() {
        if (TestAspect.class.getInterfaces().length > 0) {
            fail("Aspect was touched by some aspect while NOT in aop.xml include");
        }
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static Test suite() {
        return new TestSuite(AspectOfWhenAspectNotInIncludeTest.class);
    }

}
