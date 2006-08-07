package org.aspectj.weaver;
/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: (See CVS logs)
 * 
 *******************************************************************************/

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aspectj.testing.util.TestUtil;

/**
 */
public class Weaver5ModuleTests extends TestCase {

    public static Test suite() {
    	TestSuite suite = new TestSuite(Weaver5ModuleTests.class.getName());
        if (TestUtil.is15VMOrGreater()) {
        	TestUtil.loadTestsReflectively(suite, "org.aspectj.weaver.AllWeaver5Tests", false);
        } else {
            suite.addTest(TestUtil.testNamed("all tests require 1.5"));
        }
        return suite;
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.main(new String[] {Weaver5ModuleTests.class.getName()});
    }

}
