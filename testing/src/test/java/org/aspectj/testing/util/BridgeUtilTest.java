/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.util;

import org.aspectj.bridge.MessageHandler;
import org.aspectj.testing.run.IRun;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.RunStatus;
import org.aspectj.testing.run.Runner;

import junit.framework.TestCase;

/**
 * 
 */
public class BridgeUtilTest extends TestCase {

	public BridgeUtilTest(String name) {
		super(name);
	}

    public void testChildString() {
        String expect;
        String id;
        id = "run status identifier";
        expect  = "PASS " + id + " 0 tests";
        checkChildString(id, 0, 0, 0, 0, expect);
        expect  = "PASS " + id + " 2 tests (2 skipped)";
        checkChildString(id, 2, 0, 0, 0, expect);
        expect  = "PASS " + id + " 3 tests (1 skipped, 2 passed)";
        checkChildString(id, 1, 0, 0, 2, expect);
        expect  = "FAIL " + id + " 3 tests (1 skipped, 2 failed)";
        checkChildString(id, 1, 0, 2, 0, expect);
        expect  = "FAIL " + id + " 6 tests (1 skipped, 2 failed, 3 passed)";
        checkChildString(id, 1, 0, 2, 3, expect);
        expect  = "FAIL " + id + " 1 tests (1 failed)";
        checkChildString(id, 0, 0, 1, 0, expect);
        expect  = "FAIL " + id + " 4 tests (1 failed, 3 passed)";
        checkChildString(id, 0, 0, 1, 3, expect);
        expect  = "PASS " + id + " 1 tests (1 passed)";
        checkChildString(id, 0, 0, 0, 1, expect);
        
        // "incomplete" variants
        expect  = "PASS " + id + " 5 tests (5 incomplete)";
        checkChildString(id, 0, 5, 0, 0, expect);
        expect  = "PASS " + id + " 7 tests (2 skipped, 5 incomplete)";
        checkChildString(id, 2, 5, 0, 0, expect);
        expect  = "PASS " + id + " 8 tests (1 skipped, 5 incomplete, 2 passed)";
        checkChildString(id, 1, 5, 0, 2, expect);
        expect  = "FAIL " + id + " 8 tests (1 skipped, 5 incomplete, 2 failed)";
        checkChildString(id, 1, 5, 2, 0, expect);
        expect  = "FAIL " + id + " 11 tests (1 skipped, 5 incomplete, 2 failed, 3 passed)";
        checkChildString(id, 1, 5, 2, 3, expect);
        expect  = "FAIL " + id + " 6 tests (5 incomplete, 1 failed)";
        checkChildString(id, 0, 5, 1, 0, expect);
        expect  = "FAIL " + id + " 9 tests (5 incomplete, 1 failed, 3 passed)";
        checkChildString(id, 0, 5, 1, 3, expect);
        expect  = "PASS " + id + " 6 tests (5 incomplete, 1 passed)";
        checkChildString(id, 0, 5, 0, 1, expect);
    }
    
    void checkChildString(String id, int numSkips, int numIncomplete, int numFails, int numPasses, 
                        String expected) {
        Runner runner = new Runner();
        MessageHandler holder = new MessageHandler();
        RunStatus status = new RunStatus(holder, runner);
        status.setIdentifier(id);
        status.start();

        final IRun failer = new IRun() {
            public boolean run(IRunStatus status) { return false; }
        };
        final IRun passer = new IRun() {
            public boolean run(IRunStatus status) { return true; }
        };
        final Object result = (numFails > 0 ? IRunStatus.FAIL : IRunStatus.PASS);
        while (numFails-- > 0) {
            runner.runChild(failer,status, null, null);         
        }
        while (numPasses-- > 0) {
            runner.runChild(passer,status, null, null);         
        }
        status.finish(result);
        String actual = BridgeUtil.childString(status, numSkips, numIncomplete);
        String label = " expected \"" + expected + "\" got \"" + actual + "\"";
        assertTrue(label, expected.equals(actual));
    }
}
