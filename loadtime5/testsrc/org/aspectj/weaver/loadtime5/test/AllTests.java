/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.loadtime5.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.loadtime5.AtAspectJTest;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AllTests extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");

        suite.addTestSuite(AtAspectJTest.class);

//        suite.addTestSuite(SingletonAspectBindingsTest.class);
//        suite.addTestSuite(CflowTest.class);
//        suite.addTestSuite(PointcutReferenceTest.class);
//        suite.addTestSuite(AfterXTest.class);
//
//        //FIXME AV - fix the pc grammar to support if for @AJ aspects
//        System.err.println("(AllTests: IfPointcutTest fails)");
//        //suite.addTestSuite(IfPointcutTest.class);
//
//        suite.addTestSuite(XXJoinPointTest.class);
//        suite.addTestSuite(PrecedenceTest.class);
//        suite.addTestSuite(BindingTest.class);
//
//        suite.addTestSuite(PerClauseTest.class);

        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
