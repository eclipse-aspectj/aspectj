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
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AroundInlineMungerTest2 extends TestCase {

    public static void main(String[] args) {
    	new AroundInlineMungerTest2().target();
    	System.out.println("AroundInlineMungerTestAspects2.Open.aroundCount="+AroundInlineMungerTestAspects2.Open.aroundCount);
    	System.out.println("AroundInlineMungerTestAspects2.Open.beforeCount="+AroundInlineMungerTestAspects2.Open.beforeCount);
//        TestHelper.runAndThrowOnFailure(suite());
    }
//
    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AroundInlineMungerTest2.class);
    }

    public void testAccessNonPublicFromAroundAdvice() {
        target();
        assertEquals(3, AroundInlineMungerTestAspects2.Open.aroundCount);
        assertEquals(6, AroundInlineMungerTestAspects2.Open.beforeCount);
    }

    public void target() {}

}
