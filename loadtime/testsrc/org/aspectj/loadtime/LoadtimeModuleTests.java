package org.aspectj.loadtime;
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.aspectj.weaver.loadtime.AjTest;
import org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest;
import org.aspectj.weaver.loadtime.JRockitAgentTest;
import org.aspectj.weaver.loadtime.WeavingContextTest;
import org.aspectj.weaver.loadtime.WeavingURLClassLoaderTest;
import org.aspectj.weaver.loadtime.test.DocumentParserTest;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class LoadtimeModuleTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite(LoadtimeModuleTests.class.getName());

        suite.addTestSuite(DocumentParserTest.class);
        suite.addTestSuite(AjTest.class);
        suite.addTestSuite(ClassLoaderWeavingAdaptorTest.class);
        suite.addTestSuite(JRockitAgentTest.class);
        suite.addTestSuite(WeavingContextTest.class);
        suite.addTestSuite(WeavingURLClassLoaderTest.class);
        return suite;
    }

    public static void main(String args[]) throws Throwable {
        TestRunner.run(suite());
    }

}
