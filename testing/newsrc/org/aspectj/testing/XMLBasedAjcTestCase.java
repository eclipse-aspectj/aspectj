/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer, 
 * ******************************************************************/
package org.aspectj.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.digester.Digester;
import org.aspectj.tools.ajc.AjcTestCase;

/**
 * Root class for all Test suites that are based on an AspectJ XML test suite
 * file. Extends AjcTestCase allowing a mix of programmatic and spec-file
 * driven testing. See org.aspectj.systemtest.incremental.IncrementalTests for
 * an example of this mixed style.
 * <p>The class org.aspectj.testing.MakeTestClass will generate a subclass of
 * this class for you, given a suite spec. file as input...</p>
 */
public abstract class XMLBasedAjcTestCase extends AjcTestCase {
	
	private static Map testMap = new HashMap();
	private static boolean suiteLoaded = false;
	private AjcTest currentTest = null;
	
	public XMLBasedAjcTestCase() {
	}
	
	/**
	 * You must define a suite() method in subclasses, and return
	 * the result of calling this method. (Don't you hate static
	 * methods in programming models). For example:
	 * <pre>
	 *   public static Test suite() {
	 *     return XMLBasedAjcTestCase.loadSuite(MyTestCaseClass.class);
	 *   }
	 * </pre>
	 * @param testCaseClass
	 * @return
	 */
	public static Test loadSuite(Class testCaseClass) {
		TestSuite suite = new TestSuite(testCaseClass.getName());
		suite.addTestSuite(testCaseClass);
		TestSetup wrapper = new TestSetup(suite) {
			/* (non-Javadoc)
			 * @see junit.extensions.TestSetup#setUp()
			 */
			protected void setUp() throws Exception {
				super.setUp();
				suiteLoaded = false;
			}
			/* (non-Javadoc)
			 * @see junit.extensions.TestSetup#tearDown()
			 */
			protected void tearDown() throws Exception {
				super.tearDown();
				suiteLoaded = false;
			}
		};
		return wrapper;
	}
	
	/**
	 * The file containing the XML specification for the tests.
	 */
	protected abstract File getSpecFile();
	
	/*
	 * Return a map from (String) test title -> AjcTest
	 */
	private Map getSuiteTests() {
		return testMap;
	}
	
	/**
	 * This helper method runs the test with the given title in the
	 * suite spec file. All tests steps in given ajc-test execute
	 * in the same sandbox.
	 */
	protected void runTest(String title) {
		currentTest = (AjcTest) testMap.get(title);
		if (currentTest == null) {
			fail("No test '" + title + "' in suite.");
		}
		ajc.setShouldEmptySandbox(true);
		currentTest.runTest(this);
	}

	/**
	 * Get the currently executing test. Useful for access to e.g.
	 * AjcTest.getTitle() etc..
	 */
	protected AjcTest getCurrentTest() {
		return currentTest;
	}
	
	/**
	 * For use by the Digester. As the XML document is parsed, it creates instances
	 * of AjcTest objects, which are added to this TestCase by the Digester by 
	 * calling this method.
	 */ 
	public void addTest(AjcTest test) {
		testMap.put(test.getTitle(),test);
	}

	/*
	 * The rules for parsing a suite spec file. The Digester using bean properties to match attributes
	 * in the XML document to properties in the associated classes, so this simple implementation should
	 * be very easy to maintain and extend should you ever need to.
	 */
	private Digester getDigester() {
		Digester digester = new Digester();
		digester.push(this);
		digester.addObjectCreate("suite/ajc-test",AjcTest.class);
		digester.addSetProperties("suite/ajc-test");
		digester.addSetNext("suite/ajc-test","addTest","org.aspectj.testing.AjcTest");
		digester.addObjectCreate("suite/ajc-test/compile",CompileSpec.class);
		digester.addSetProperties("suite/ajc-test/compile");
		digester.addSetNext("suite/ajc-test/compile","addTestStep","org.aspectj.testing.ITestStep");
		digester.addObjectCreate("suite/ajc-test/run",RunSpec.class);
		digester.addSetProperties("suite/ajc-test/run","class","classToRun");
		digester.addSetNext("suite/ajc-test/run","addTestStep","org.aspectj.testing.ITestStep");
		digester.addObjectCreate("*/message",ExpectedMessageSpec.class);
		digester.addSetProperties("*/message");
		digester.addSetNext("*/message","addExpectedMessage","org.aspectj.testing.ExpectedMessageSpec");
		return digester;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.tools.ajc.AjcTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		if (!suiteLoaded) {
			testMap = new HashMap();
			System.out.println("LOADING SUITE: " + getSpecFile().getPath());
			Digester d = getDigester();
			try {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(getSpecFile()));
				d.parse(isr);
			} catch (Exception ex) {
				fail("Unable to load suite " + getSpecFile().getPath() + " : " + ex);
			}
			suiteLoaded = true;
		}
	}
}

