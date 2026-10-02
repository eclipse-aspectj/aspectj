/* *******************************************************************
 * Copyright (c) 1999-2026 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 *     Andy Clement   weave message tests added
 * ******************************************************************/
package org.aspectj.bridge;

import org.aspectj.bridge.context.CompilationAndWeavingContextTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BridgeModuleTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite(BridgeModuleTests.class.getName());
        suite.addTestSuite(CountingMessageHandlerTest.class);
        suite.addTestSuite(MessageTest.class);
        suite.addTestSuite(WeaveMessageTest.class);
        suite.addTestSuite(VersionTest.class);
        suite.addTestSuite(CompilationAndWeavingContextTest.class);
        return suite;
    }

    public BridgeModuleTests(String name) { super(name); }

}
