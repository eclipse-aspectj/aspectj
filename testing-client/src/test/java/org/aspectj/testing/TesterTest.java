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


package org.aspectj.testing;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Test the Tester client API's.
 * See also tests/harness/*.java and tests/ajcHarnessTests.xml for harness-
 * driven Tester tests.
 * @author isberg
 */
public class TesterTest extends TestCase {


   private static final String ME
        = "org.aspectj.testing.TesterTest";

    /** @param args ignored */
    public static void main(String[] args) {
        TestRunner.main(new String[] {ME});
    }

    /**
     * Constructor for TesterTest.
     * @param arg0
     */
    public TesterTest(String arg0) {
        super(arg0);
    }

    /**
     * Test the usage pattern
     * <pre>Tester.event("foo");
     * Tester.checkEvents(new String[] { "foo" }); </pre>
     */
    public void testEventArrayPattern() {
        MyTestReporter reporter = new MyTestReporter();
        Tester.setMessageHandler(reporter);

        //--------- positive test - got expected events
        reporter.clear();
        Tester.clear();
        Tester.event("one");
        Tester.event("two");
        Tester.checkEvents(new String[] { "one", "two"});
        reporter.assertSize(0);

        //--------- failed to get expected events
        reporter.clear();
        Tester.clear();
        Tester.checkEvents(new String[] { "one"});
        assertTrue(reporter.gotFail("one"));
        reporter.assertSize(1);

        //--------- got and didn't get expected events
        reporter.clear();
        Tester.clear();
        Tester.event("one");
        Tester.event("two");
        Tester.checkEvents(new String[] { "one", "two", "three"});
        reporter.assertSize(1);
        assertTrue(reporter.gotFail("three"));
    }

    /**
     * Test the usage pattern
     * <pre>Tester.event("foo");
     * Tester.expectEvent("foo");
     * Tester.checkAllEvents();</pre>
     */
    public void testEventStringPattern() {
        MyTestReporter reporter = new MyTestReporter();
        Tester.setMessageHandler(reporter);

        //--------- positive test - got expected events
        reporter.clear();
        Tester.clear();
        Tester.event("one");
        Tester.event("two");
        Tester.expectEvent("one");
        Tester.expectEvent("two");
        Tester.checkAllEvents();
        reporter.assertSize(0);

        //--------- failed to get expected events
        reporter.clear();
        Tester.clear();
        Tester.expectEvent("one");
        Tester.checkAllEvents();
        assertTrue(reporter.gotFail("one"));
        reporter.assertSize(1);

        //--------- got and didn't get expected events
        reporter.clear();
        Tester.clear();
        Tester.expectEvent("one");
        Tester.expectEvent("two");
        Tester.expectEvent("three");
        Tester.event("one");
        Tester.event("two");
        Tester.checkAllEvents();
        assertTrue(reporter.gotFail("three"));
        reporter.assertSize(1);
    }

    /**
     * Test the usage pattern
     * <pre>Tester.note("foo");
     * Tester.check("foo");</pre>
     */
    public void testNotePattern() {
        MyTestReporter reporter = new MyTestReporter();
        Tester.setMessageHandler(reporter);

        //--------- positive test - got expected events
        reporter.clear();
        Tester.clear();
        Tester.note("one");
        Tester.note("two");
        Tester.check("one");
        Tester.check("two");
        reporter.assertSize(0);

        //--------- failed to get expected events
        reporter.clear();
        Tester.clear();
        Tester.check("one");
        Tester.checkAllEvents();
        assertTrue(reporter.gotFail("one"));
        reporter.assertSize(1);

        //--------- got and didn't get expected events
        reporter.clear();
        Tester.clear();
        Tester.note("one");
        Tester.check("one");
        Tester.note("two");
        Tester.check("two");
        Tester.check("three");
        assertTrue(reporter.gotFail("three"));
        reporter.assertSize(1);
    }

    /**
     * Stub to record failures emitted by Tester.
     * @author isberg
     */
    public static class MyTestReporter implements IMessageHandler {
        public List<IMessage> failures = new ArrayList<>();
        public List<IMessage> passes = new ArrayList<>();

        public void clear() {
            failures.clear();
            passes.clear();
        }

        void assertSize(int size) {
            assertTrue(-1 < size);
            assertTrue("failures: " + failures, size == failures.size());
        }

        boolean gotPass(String substring) {
            return gotItem(passes, substring);
        }

        boolean gotFail(String substring) {
            return gotItem(failures, substring);
        }

        boolean gotItem(List<IMessage> list, String substring) {
        	for (IMessage element: list) {
                String s = element.getMessage();
				if ((null != s) && (s.contains(substring))) {
                    return true;
                }
			}
            return false;
        }

        public boolean isIgnoring(IMessage.Kind kind) {
            return false;
        }

        public void dontIgnore(IMessage.Kind kind) {

        }

        public void ignore(IMessage.Kind kind) {
		}

        public boolean handleMessage(IMessage message) {
            (message.isFailed() ? failures : passes).add(message);
            return true;
        }
    }
}
//        /**
//         * @see ReporterI#abortWithFailure(String, Throwable)
//         */
//        public void abortWithFailure(String message, Throwable exception) {
//            if (null == exception) {
//                check(message, true);
//            } else {
//                String s = message + Util.unqualifiedClassName(exception)
//                    + ": " + exception.getMessage();
//                check(s, false);
//            }
//        }
//
//        /**
//         * @see ReporterI#check(String, boolean)
//         */
//        public boolean check(String message, boolean passed) {
//            (!passed ? failures : passes).add(message);
//            return passed;
//        }
//

