/* *******************************************************************
 * Copyright (c) 2005-2019 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
import org.aspectj.ajde.AjdeModuleTests;
import org.aspectj.ajde.core.AjdeCoreModuleTests;
import org.aspectj.ajdt.AjdtCoreModuleTests;
import org.aspectj.asm.AsmModuleTests;
import org.aspectj.bridge.BridgeModuleTests;
import org.aspectj.build.BuildModuleTests;
import org.aspectj.loadtime.LoadtimeModuleTests;
import org.aspectj.matcher.MatcherModuleTests;
import org.aspectj.runtime.RuntimeModuleTests;
import org.aspectj.testing.TestingClientModuleTests;
import org.aspectj.testing.TestingModuleTests;
import org.aspectj.testing.drivers.TestingDriversModuleTests;
import org.aspectj.testingutil.TestingUtilModuleTests;
import org.aspectj.tests.TestsModuleTests;
import org.aspectj.tools.ajdoc.AjdocModuleTests;
import org.aspectj.tools.ant.taskdefs.TaskdefsModuleTests;
import org.aspectj.util.UtilModuleTests;
import org.aspectj.weaver.WeaverModuleTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andy Clement
 */
public class RunTheseBeforeYouCommitTests {

	public static Test suite() {
        String name = RunTheseBeforeYouCommitTests.class.getName();
		TestSuite suite = new TestSuite(name);

		// Main modules
		suite.addTest(UtilModuleTests.suite());
		suite.addTest(AjdeCoreModuleTests.suite());
		suite.addTest(AjdeModuleTests.suite());
		suite.addTest(AsmModuleTests.suite());
		suite.addTest(BridgeModuleTests.suite());
		suite.addTest(LoadtimeModuleTests.suite());
		suite.addTest(RuntimeModuleTests.suite());
		suite.addTest(AjdocModuleTests.suite());
		suite.addTest(WeaverModuleTests.suite());
		suite.addTest(TaskdefsModuleTests.suite());
		suite.addTest(MatcherModuleTests.suite());
//		suite.addTest(AjbrowserModuleTests.suite()); // There are none so far...
//		suite.addTest(LibModuleTests.suite()); // anyone using this?
		suite.addTest(AjdtCoreModuleTests.suite());
		
		// Support modules
		suite.addTest(TestingModuleTests.suite());
		suite.addTest(TestingClientModuleTests.suite());
		suite.addTest(TestingDriversModuleTests.suite());
		suite.addTest(TestingUtilModuleTests.suite());
		suite.addTest(BuildModuleTests.suite());
		
		// Compiler tests
		suite.addTest(TestsModuleTests.suite()); 

		return suite;
	}
}
