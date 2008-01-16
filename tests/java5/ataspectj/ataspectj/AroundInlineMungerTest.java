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
