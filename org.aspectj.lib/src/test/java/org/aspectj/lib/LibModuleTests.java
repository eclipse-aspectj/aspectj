package org.aspectj.lib;
/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Wes Isberg       initial implementation
 * ******************************************************************/

// default package

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LibModuleTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite(LibModuleTests.class.getName());
        suite.addTest(org.aspectj.lib.pointcuts.PointcutsTests.suite());
        suite.addTest(org.aspectj.lib.tracing.TracingTests.suite());
        return suite;
    }

}
