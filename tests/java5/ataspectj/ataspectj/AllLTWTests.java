/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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

        suite.addTestSuite(SingletonAspectBindingsTest.class);
        suite.addTestSuite(CflowTest.class);
        suite.addTestSuite(PointcutReferenceTest.class);
        suite.addTestSuite(AfterXTest.class);
        suite.addTestSuite(IfPointcutTest.class);
        suite.addTestSuite(XXJoinPointTest.class);
        suite.addTestSuite(PrecedenceTest.class);
        suite.addTestSuite(BindingTest.class);
        suite.addTestSuite(PerClauseTest.class);
        suite.addTestSuite(AroundInlineMungerTest.class);
        suite.addTestSuite(SingletonInheritanceTest.class);
        suite.addTestSuite(PerClauseInheritanceTest.class);
        suite.addTestSuite(IfPointcut2Test.class);

        return suite;
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

}
