/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg       initial implementation 
 * ******************************************************************/

package org.aspectj.lib.pointcuts;

import junit.framework.TestCase;

/**
 * 
 */
public class PointcutsTest extends TestCase {

    public void test_anyPublicMethodExecution() {
        try {
            Test_anyPublicMethodExecution.error();
            assertTrue("no exception thrown", false);
        } catch (Error e) {
            // ok, advice worked
        }
    }
    
    private static aspect Test_anyPublicMethodExecution {
        public static void error() {
            throw new RuntimeException("wrong exception");
        }
        
        static void nonpublic() {}
        
        before() : 
            execution(static void Test_anyPublicMethodExecution.error()) 
            && Pointcuts.anyPublicMethodExecution() {
            throw new Error("");
        }
        
        declare error : 
            execution(static void Test_anyPublicMethodExecution.nonpublic()) 
            && Pointcuts.anyPublicMethodExecution() 
            : "anyPublicMethodExecution failed - not public";
        
    }
    private static aspect compileChecks {
        /** balk if Pointcuts has code - s.b. only pointcuts */
        declare error : within(Pointcuts) && 
            (set(* *) || Pointcuts.anyMethodExecution() || 
                    (Pointcuts.anyConstructorExecution()
                            && !execution(private Pointcuts.new()))) :
                "only pointcuts permitted in Pointcuts";

        
    }
}
