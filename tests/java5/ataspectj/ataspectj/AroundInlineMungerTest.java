/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package ataspectj;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AroundInlineMungerTest extends TestCase {

    public static void main(String[] args) {
    	new AroundInlineMungerTest().target();
    	System.out.println("AroundInlineMungerTestAspects.Open.aroundCount="+AroundInlineMungerTestAspects.Open.aroundCount);
    	System.out.println("AroundInlineMungerTestAspects.Open.beforeCount="+AroundInlineMungerTestAspects.Open.beforeCount);
//        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AroundInlineMungerTest.class);
    }

    public void testAccessNonPublicFromAroundAdvice() {
        target();
        assertEquals(3, AroundInlineMungerTestAspects.Open.aroundCount);
        assertEquals(6, AroundInlineMungerTestAspects.Open.beforeCount);
    }

    public void target() {}

}
