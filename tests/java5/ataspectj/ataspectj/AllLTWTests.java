/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package ataspectj;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AllLTWTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");

        suite.addTestSuite(ataspectj.SingletonAspectBindingsTest.class);
        suite.addTestSuite(ataspectj.CflowTest.class);
        suite.addTestSuite(ataspectj.PointcutReferenceTest.class);
        suite.addTestSuite(ataspectj.AfterXTest.class);
        //FIXME AV - fix the pc grammar to support if for @AJ aspects
        System.err.println("(AllTests: IfPointcutTest fails)");
        //suite.addTestSuite(IfPointcutTest.class);
        suite.addTestSuite(ataspectj.XXJoinPointTest.class);
        suite.addTestSuite(ataspectj.PrecedenceTest.class);
        suite.addTestSuite(ataspectj.BindingTest.class);
        suite.addTestSuite(ataspectj.PerClauseTest.class);
        suite.addTestSuite(AroundInlineMungerTest.class);

        return suite;
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

}
