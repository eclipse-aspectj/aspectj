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


package org.aspectj.lib.tracing;

import junit.framework.TestCase;

import org.aspectj.lang.JoinPoint.StaticPart;

/**
 * 
 */
public class TraceJoinPointsTest extends TestCase {

    public void testTraceJoinPoints() {
        checkTjp();
        TestTJP aspect = TestTJP.aspectOf();
        assertNotNull("aspect", aspect);
        assertTrue("checked", aspect.checked);
    }

    static final int NUMJP = 1;
    
    static void checkTjp() {
        // NUMJP: only 1 join point
        long l = System.currentTimeMillis();
    }

    /** poor design/test */
    static aspect TestTJP extends TraceJoinPoints  {
       
        protected pointcut withinScope() : within(TraceJoinPointsTest)
            && !within(TestTJP);
        pointcut traceJoinPoints() :
            execution(static void TraceJoinPointsTest.testTraceJoinPoints());

        protected pointcut entry() : 
            execution(static void TraceJoinPointsTest.checkTjp());

        boolean checked;
        int logEnter = 10;
        int logExit = 10;
        int startLog = 10;
        int completeLog = 10;
        protected void logEnter(StaticPart jp) {
            logEnter++;
        }

        protected void logExit(StaticPart jp) {
            logExit++;
        }
        
        protected void startLog() {
            startLog = 0;
            completeLog = 0;
            logEnter = 0;
            logExit = 0;
            startLog++;
        }
        
        protected void completeLog() {
            completeLog++;
        }
        after() returning : entry() {
            assertEquals("startLog", 1, startLog);
            assertEquals("completeLog", 1, startLog);
            assertEquals("logExit", NUMJP, startLog);
            assertEquals("logEntry", NUMJP, startLog);
            assertTrue(!checked);
            checked = true;
        }
    }
}
