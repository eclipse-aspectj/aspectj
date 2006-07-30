/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors (See CVS checkin's): 
 * 
 * ******************************************************************/

import org.aspectj.tests.TestsModuleTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RunTheseBeforeYouCommitTests {

	public static Test suite() {
        String name = RunTheseBeforeYouCommitTests.class.getName();
		TestSuite suite = new TestSuite(name);
        // unit tests
        suite.addTest(AllTests.suite());
        // compiler tests
        suite.addTest(TestsModuleTests.suite());            
		return suite;
	}
}
