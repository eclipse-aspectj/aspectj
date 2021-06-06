package org.aspectj.tools.ajdoc;
/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Mik Kersten     initial implementation
 * ******************************************************************/

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Mik Kersten
 */
public class AjdocModuleTests {
    public static Test suite() {
        TestSuite suite = new TestSuite(AjdocModuleTests.class.getName());
        suite.addTestSuite(DeclareFormsTest.class);
        suite.addTestSuite(SpacewarTestCase.class);
        suite.addTestSuite(PatternsTestCase.class);
        suite.addTestSuite(CoverageTestCase.class);
        suite.addTestSuite(ITDTest.class);
        suite.addTestSuite(FullyQualifiedArgumentTest.class);
        suite.addTestSuite(EnumTest.class);
        suite.addTestSuite(PointcutVisibilityTest.class);
        suite.addTestSuite(ExecutionTestCase.class);
        suite.addTestSuite(BugTest.class);
        return suite;
    }
}
