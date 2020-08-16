/*******************************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  initial implementation      Alexandre Vasseur
 *******************************************************************************/
package org.aspectj.testing;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.aspectj.tools.ajc.Ajc;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Autowiring of XML test spec file as JUnit tests.
 * <p/>
 * Extend this class and implement the getSpecFile and the static suite() method.
 * All tests described in the XML spec file will be auto-registered as JUnit tests.
 * Any regular test() method will be registered as well.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public abstract class AutowiredXMLBasedAjcTestCase extends XMLBasedAjcTestCase {

    private Map<String,AjcTest> testMap = new HashMap<>();

    public void addTest(AjcTest test) {
        testMap.put(test.getTitle(), test);
    }

    /*
	 * Return a map from (String) test title -> AjcTest
	 */
    protected Map<String,AjcTest> getSuiteTests() {
        return testMap;
    }

    public static Test loadSuite(Class<? extends TestCase> testCaseClass) {
        TestSuite suite = new TestSuite(testCaseClass.getName());
        //suite.addTestSuite(testCaseClass);

        // wire the spec file
        try {
            final AutowiredXMLBasedAjcTestCase wired = (AutowiredXMLBasedAjcTestCase) testCaseClass.newInstance();
            System.out.println("LOADING SUITE: " + wired.getSpecFile().getPath());
            Digester d = wired.getDigester();
            try {
            	InputStreamReader isr = new InputStreamReader(wired.getSpecFile().openStream());
//                InputStreamReader isr = new InputStreamReader(new FileInputStream(wired.getSpecFile()));
                d.parse(isr);
            } catch (Exception ex) {
                fail("Unable to load suite " + wired.getSpecFile().getPath() + " : " + ex);
            }
            wired.ajc = new Ajc();

            Map<String,AjcTest> ajTests = wired.getSuiteTests();

			for (final Map.Entry<String, AjcTest> entry : ajTests.entrySet()) {
				suite.addTest(
						new TestCase(entry.getKey().toString()) {

							protected void runTest() {
								entry.getValue().runTest(wired);
							}

							public String getName() {
								return entry.getKey();
							}
						}
				);
			}
        } catch (Throwable t) {
            final String message = t.toString();
            suite.addTest(
                    new TestCase("error") {
                        protected void runTest() {
                            fail(message);
                        }
                    }
            );
        }

        // wire the test methods as well if any
        // this simple check avoids failure when no test.. method is found.
        // it could be refined to lookup in the hierarchy as well, and excluding private method as JUnit does.
        Method[] testMethods = testCaseClass.getDeclaredMethods();
		for (Method testMethod : testMethods) {
			if (testMethod.getName().startsWith("test")) {
				suite.addTestSuite(testCaseClass);
				break;
			}
		}

        TestSetup wrapper = new TestSetup(suite) {
            /* (non-Javadoc)
             * @see junit.extensions.TestSetup#setUp()
             */
            protected void setUp() throws Exception {
                super.setUp();
                //suiteLoaded = false;
            }
            /* (non-Javadoc)
             * @see junit.extensions.TestSetup#tearDown()
             */
            protected void tearDown() throws Exception {
                super.tearDown();
                //suiteLoaded = false;
            }
        };
        return wrapper;

        //return suite;
    }

    /* (non-Javadoc)
     * @see org.aspectj.tools.ajc.AjcTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * This helper method runs the test with the given title in the
     * suite spec file. All tests steps in given ajc-test execute
     * in the same sandbox.
     */
    protected void runTest(String title) {
        AjcTest currentTest = (AjcTest) testMap.get(title);
        if (currentTest == null) {
            fail("No test '" + title + "' in suite.");
        }
        ajc.setShouldEmptySandbox(true);
        currentTest.runTest(this);
    }



}
