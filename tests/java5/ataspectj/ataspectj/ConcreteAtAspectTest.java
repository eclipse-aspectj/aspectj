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
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.Aspects;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ConcreteAtAspectTest extends TestCase {

    static int I;

    void target() {
        I++;
    }

    // abstract aspect
    // pc() is undefined hence always false, and advice not applied
    // this aspect is illegal as is in aop.xml
    // ones must use a concrete-aspect
    @Aspect
    abstract static class ConcreteAtAspect {

        @Pointcut
        abstract void pc();
        // must be abstract
        // for concrete-aspect, must further be no-arg, void
        // !!! but can be more complex for non-xml inheritance !!! ie not error for AJDTcore

        @Before("pc()")
        public void before() {
            I++;
        }
    }

    // legal abstract aspect resolved with inheritance
    @Aspect
    static class ConcreteAtAspectSub extends ConcreteAtAspect {
        @Pointcut("execution(* ataspectj.ConcreteAtAspectTest.target())")
        void pc() {}
    }

    public void testConcrete() {
        I = 0;
        target();
        assertEquals(3, I);
    }

    public void tesCanLoad() {
        try {
            Class jitAspect = Class.forName("ataspectj.Foo");
            Object aspect = Aspects.aspectOf(jitAspect);
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static Test suite() {
        return new TestSuite(ConcreteAtAspectTest.class);
    }

}
