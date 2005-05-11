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

import org.aspectj.weaver.loadtime.LoadtimeTests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class Loadtime515ModuleTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite(Loadtime515ModuleTests.class.getName());
        suite.addTestSuite(LoadtimeTests.class);
        return suite;
    }

}
