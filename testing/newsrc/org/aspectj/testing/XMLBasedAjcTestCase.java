/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer, 
 * ******************************************************************/
package org.aspectj.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.digester.Digester;
import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;
import org.aspectj.util.FileUtil;

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
    private Stack clearTestAfterRun = new Stack();
	
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
	protected Map getSuiteTests() {
		return testMap;
	}
	
	/**
	 * This helper method runs the test with the given title in the
	 * suite spec file. All tests steps in given ajc-test execute
	 * in the same sandbox.
	 */
	protected void runTest(String title) {
		currentTest = (AjcTest) testMap.get(title);
        final boolean clearTest = clearTestAfterRun();
		if (currentTest == null) {
            if (clearTest) {
                System.err.println("test already run: " + title);
                return;
            } else {
                fail("No test '" + title + "' in suite.");
            }
		} 
		boolean run = currentTest.runTest(this);
		assertTrue("Test not run",run);
        if (clearTest) {
            testMap.remove(title);
        }
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
    protected final void pushClearTestAfterRun(boolean val) {
        clearTestAfterRun.push(val ? Boolean.FALSE: Boolean.TRUE);
    }
    protected final boolean popClearTestAfterRun() {
        return clearTest(true);
    }
    protected final boolean clearTestAfterRun() {
        return clearTest(false);
    }
    private boolean clearTest(boolean pop) {
        if (clearTestAfterRun.isEmpty()) {
            return false;
        }
        boolean result = ((Boolean) clearTestAfterRun.peek()).booleanValue();
        if (pop) {
            clearTestAfterRun.pop();
        }
        return result;
    }

	/*
	 * The rules for parsing a suite spec file. The Digester using bean properties to match attributes
	 * in the XML document to properties in the associated classes, so this simple implementation should
	 * be very easy to maintain and extend should you ever need to.
	 */
	protected Digester getDigester() {
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
		digester.addSetProperties("suite/ajc-test/run","ltw","ltwFile");
		digester.addSetProperties("suite/ajc-test/run","xlintfile","xlintFile");
		digester.addSetProperties("suite/ajc-test/run/stderr","ordered","orderedStderr");
		digester.addSetNext("suite/ajc-test/run","addTestStep","org.aspectj.testing.ITestStep");
		digester.addObjectCreate("*/message",ExpectedMessageSpec.class);
		digester.addSetProperties("*/message");
		digester.addSetNext("*/message","addExpectedMessage","org.aspectj.testing.ExpectedMessageSpec");
		digester.addObjectCreate("suite/ajc-test/weave",WeaveSpec.class);
		digester.addSetProperties("suite/ajc-test/weave");
		digester.addSetNext("suite/ajc-test/weave","addTestStep","org.aspectj.testing.ITestStep");

        digester.addObjectCreate("suite/ajc-test/ant",AntSpec.class);
        digester.addSetProperties("suite/ajc-test/ant");
        digester.addSetNext("suite/ajc-test/ant","addTestStep","org.aspectj.testing.ITestStep");
        digester.addObjectCreate("suite/ajc-test/ant/stderr",OutputSpec.class);
        digester.addSetProperties("suite/ajc-test/ant/stderr");
        digester.addSetNext("suite/ajc-test/ant/stderr","addStdErrSpec","org.aspectj.testing.OutputSpec");
        digester.addObjectCreate("suite/ajc-test/ant/stdout",OutputSpec.class);
        digester.addSetProperties("suite/ajc-test/ant/stdout");
        digester.addSetNext("suite/ajc-test/ant/stdout","addStdOutSpec","org.aspectj.testing.OutputSpec");

		digester.addObjectCreate("suite/ajc-test/run/stderr",OutputSpec.class);
		digester.addSetProperties("suite/ajc-test/run/stderr");
		digester.addSetNext("suite/ajc-test/run/stderr","addStdErrSpec","org.aspectj.testing.OutputSpec");
		digester.addObjectCreate("suite/ajc-test/run/stdout",OutputSpec.class);
		digester.addSetProperties("suite/ajc-test/run/stdout");
		digester.addSetNext("suite/ajc-test/run/stdout","addStdOutSpec","org.aspectj.testing.OutputSpec");
		digester.addObjectCreate("*/line",OutputLine.class);
		digester.addSetProperties("*/line");
		digester.addSetNext("*/line","addLine","org.aspectj.testing.OutputLine");
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

	  protected long nextIncrement(boolean doWait) {
	  	long time = System.currentTimeMillis();
	  	if (doWait) {
	  		try {
	  	  		Thread.sleep(1000);
	  		} catch (InterruptedException intEx) {}
	  	}
	  	return time;
	  }

	  protected void copyFile(String from, String to) throws Exception {
	  	String dir = getCurrentTest().getDir();
	  	FileUtil.copyFile(new File(dir + File.separator + from),
	  			          new File(ajc.getSandboxDirectory(),to));
	  }
	  
	  protected void copyFileAndDoIncrementalBuild(String from, String to) throws Exception {
	  	copyFile(from,to);
	  	CompilationResult result = ajc.doIncrementalCompile();
	  	assertNoMessages(result,"Expected clean compile from test '" + getCurrentTest().getTitle() + "'");
	  }
	  
	  protected void copyFileAndDoIncrementalBuild(String from, String to, MessageSpec expectedResults) throws Exception {
	  	String dir = getCurrentTest().getDir();
	  	FileUtil.copyFile(new File(dir + File.separator + from),
	  			          new File(ajc.getSandboxDirectory(),to));
	  	CompilationResult result = ajc.doIncrementalCompile();
	  	assertMessages(result,"Test '" + getCurrentTest().getTitle() + "' did not produce expected messages",expectedResults);
	  }
	  
	  protected void deleteFile(String file) {
	  	new File(ajc.getSandboxDirectory(),file).delete();
	  }
	  
	  protected void deleteFileAndDoIncrementalBuild(String file, MessageSpec expectedResult) throws Exception {
	  	deleteFile(file);
	  	CompilationResult result = ajc.doIncrementalCompile();
	  	assertMessages(result,"Test '" + getCurrentTest().getTitle() + "' did not produce expected messages",expectedResult);
	  }
	  
	  protected void deleteFileAndDoIncrementalBuild(String file) throws Exception {
	  	deleteFileAndDoIncrementalBuild(file,MessageSpec.EMPTY_MESSAGE_SET);
	  }

	  protected void assertAdded(String file) {
	  	assertTrue("File " + file + " should have been added",
	  			new File(ajc.getSandboxDirectory(),file).exists());
	  }

	  protected void assertDeleted(String file) {
	  	assertFalse("File " + file + " should have been deleted",
	  			new File(ajc.getSandboxDirectory(),file).exists());
	  }

	  protected void assertUpdated(String file, long sinceTime) {
	  	File f = new File(ajc.getSandboxDirectory(),file);
	  	assertTrue("File " + file + " should have been updated",f.lastModified() > sinceTime);
	  }

	  public static class CountingFilenameFilter implements FilenameFilter {
	    	
		  private String suffix;
		  private int count;
		  
		  public CountingFilenameFilter (String s) {
			  this.suffix = s;
		  }
	    	
		  public boolean accept(File dir, String name) {
			  if (name.endsWith(suffix)) count++;
			  return false;
		  }
	
		  public int getCount() {
			  return count;
		  }
	  }
}

