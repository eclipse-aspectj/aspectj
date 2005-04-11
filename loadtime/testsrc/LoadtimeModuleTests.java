/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.aspectj.weaver.loadtime.test.DocumentParserTest;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class LoadtimeModuleTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite(LoadtimeModuleTests.class.getName());

        suite.addTestSuite(DocumentParserTest.class);

        return suite;
    }

    public static void main(String args[]) throws Throwable {
        TestRunner.run(suite());
    }

}
