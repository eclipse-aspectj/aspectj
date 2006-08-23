/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Alexandre Vasseur
 *******************************************************************************/
package ataspectj;

import junit.textui.TestRunner;
import junit.framework.TestResult;
import junit.framework.Assert;
import junit.framework.TestFailure;

import java.util.Enumeration;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.AbortException;
import org.aspectj.weaver.loadtime.DefaultMessageHandler;

/**
 * Helper to run a test as a main class, but still throw exception and not just print on stderr
 * upon test failure.
 * <p/>
 * This is required for Ajc test case that are also designed to work with LTW.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class TestHelper extends DefaultMessageHandler {

    public static void runAndThrowOnFailure(junit.framework.Test test) {
        TestRunner r = new TestRunner();
        TestResult rr = r.doRun(test);
        if (!rr.wasSuccessful()) {
            StringBuffer sb = new StringBuffer("\n");
            Enumeration e = rr.failures();
            while (e.hasMoreElements()) {
                sb.append("JUnit Failure: ");
                TestFailure failure = (TestFailure)e.nextElement();
                sb.append(failure.thrownException().toString());
                sb.append("\n");
            }
            e = rr.errors();
            while (e.hasMoreElements()) {
                sb.append("JUnit Error: ");
                TestFailure failure = (TestFailure)e.nextElement();
                sb.append(failure.thrownException().toString());
                sb.append("\n");
            }
            throw new RuntimeException(sb.toString());
        }
    }

    public boolean handleMessage(IMessage message) throws AbortException {
        boolean ret = super.handleMessage(message);
        if (message.getKind().isSameOrLessThan(IMessage.INFO));
        if (message.getKind().isSameOrLessThan(IMessage.DEBUG));
        else {
            // we do exit here since Assert.fail will only trigger a runtime exception that might
            // be catched by the weaver anyway
            System.err.println("*** Exiting - got a warning/fail/error/abort IMessage");
            System.exit(-1);
        }
        return ret;
    }

}
