/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package ataspectj;

import junit.textui.TestRunner;
import junit.framework.TestResult;

import java.util.Enumeration;

/**
 * Helper to run a test as a main class, but still throw exception and not just print on stderr
 * upon test failure.
 * <p/>
 * This is required for Ajc test case that are also designed to work with LTW.
 * 
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class TestHelper {

    public static void runAndThrowOnFailure(junit.framework.Test test) {
        TestRunner r = new TestRunner();
        TestResult rr = r.doRun(test);
        if (!rr.wasSuccessful()) {
            StringBuffer sb = new StringBuffer("\n");
            Enumeration e = rr.failures();
            while (e.hasMoreElements()) {
                sb.append("Failure: ");
                sb.append(e.nextElement());
                sb.append("\n");
            }
            e = rr.errors();
            while (e.hasMoreElements()) {
                sb.append("Error: ");
                sb.append(e.nextElement());
                sb.append("\n");
            }
            throw new RuntimeException(sb.toString());
        }
    }

}
