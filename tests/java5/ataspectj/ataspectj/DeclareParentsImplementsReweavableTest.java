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
import org.aspectj.lang.annotation.DeclareParents;

import java.util.Arrays;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DeclareParentsImplementsReweavableTest extends TestCase {

    static class Target {}

    static interface I1 { int do1(); }

    public static class Imp1 implements I1 {
        public int do1() {return 1;}
    }

    public static interface I2 { int do2(); }

    public static class Imp2 implements I2 {
        public int do2() {return 2;}
    }

    @Aspect
    static class TestAspect {

        @DeclareParents(value="ataspectj.DeclareParentsImplementsReweavableTest.Target",
                        defaultImpl = Imp1.class)
        public static I1 i1;
    }

    public void testDecPInt() {
        Class[] intfs = Target.class.getInterfaces();
        assertTrue("I1 was not introduced", Arrays.asList(intfs).contains(I1.class));
        assertEquals(1, ((I1)new Target()).do1());

        // test stuff weaved in by DeclareParentsImplementsReweavableAspect
        // thru reweable mode
        assertTrue("I2 was not introduced", Arrays.asList(intfs).contains(I2.class));
        assertEquals(2, ((I2)new Target()).do2());
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DeclareParentsImplementsReweavableTest.class);
    }

}
