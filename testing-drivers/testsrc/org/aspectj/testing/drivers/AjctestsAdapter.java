/* *******************************************************************
 * Copyright (c) 2003 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation
 * ******************************************************************/

package org.aspectj.testing.drivers;

import java.io.*;
import java.util.*;

import junit.framework.*;

import org.aspectj.bridge.*;
import org.aspectj.testing.harness.bridge.*;
import org.aspectj.testing.run.*;
//import org.aspectj.testing.util.RunUtils;
//import org.aspectj.testing.xml.AjcSpecXmlReader;

/*
 * Adapt Harness tests to JUnit driver.
 * This renders suite files as TestSuite
 * and AjcTest as TestCase.
 * When run, aborts are reported as error
 * and fails as failures, with all messages stuffed
 * into the one JUnit exception message.
 * Test options are supported, but no harness options.
 * The TestSuite implementation prevents us
 * from re-running tests.
 * In the Eclipse JUnit test runner,
 * the tests display hierarchically and with annotations 
 * and with messages.  You can stop the tests, but not
 * traverse to the source or re-run the test.
 */
public class AjctestsAdapter extends TestSuite {
    public static final String VERBOSE_NAME =
        AjctestsAdapter.class.getName() + ".VERBOSE";
    
    private static final boolean VERBOSE
        = HarnessJUnitUtil.readBooleanSystemProperty(VERBOSE_NAME);
        
    /**
     * Factory to make and populate suite without options.
     * @param suitePath the String path to a harness suite file
     * @return AjctestJUnitSuite populated with tests
     */
    public static AjctestsAdapter make(String suitePath) {
        return make(suitePath, null);
    }

    /**
     * Factory to make and populate suite
     * @param suitePath the String path to a harness suite file
     * @param options the String[] options to use when creating tests
     * @return AjctestJUnitSuite populated with tests
     */
    public static AjctestsAdapter make(String suitePath, String[] options) {
        AjctestsAdapter result = new AjctestsAdapter(suitePath, options);
        AjcTest.Spec[] tests = AjcTest.Suite.getTests(result.getSpec());
        if (VERBOSE) {
            log("loading " + tests.length + " tests in " + suitePath);
        }
        for (int i = 0; i < tests.length; i++) {
            AjcTest.Spec ajcTest = (AjcTest.Spec) tests[i];
            result.addTest(new AjcTestSpecAsTest(ajcTest, result));
        }
        return result;
    }
    
    private static void log(String message) {
        System.err.println(message);
        System.err.flush();
    }

    private final String suitePath;
    private final String[] options;
    private AjcTest.Suite.Spec spec;
    private Runner runner;
    private Validator validator;
    private IMessageHolder holder;
    private Sandbox sandbox;
    private File suiteDir;
    private String name;

    private AjctestsAdapter(String suitePath, String[] options) { 
        this.suitePath = suitePath;
        String[] opts = new String[0];
        if (!HarnessJUnitUtil.isEmpty(options)) {
            opts = new String[options.length];
            System.arraycopy(options, 0, opts, 0, opts.length);
        }
        this.options = opts;
    }

    public void addTest(Test test) {
        if (! (test instanceof AjcTestSpecAsTest)) {
            String m = "expecting AjcTestSpecAsTest, got "
                + (null == test 
                    ? "null test" 
                    : test.getClass().getName() + ": " + test);
            throw new IllegalArgumentException(m);
        }
        super.addTest(test);
    }

    public void addTestSuite(Class testClass) {
        throw new Error("unimplemented");
    }
    
    public String getName() {
        if (null == name) {
            name = HarnessJUnitUtil.cleanTestName(suitePath + Arrays.asList(options));
        }
        return name;
    }
                
    /**
     * Callback from test to run it using suite-wide holder, etc.
     * The caller is responsible for calling 
     * result.startTest(test) and result.endTest(test);
     * @param test the AjcTestSpecAsTest to run
     * @param result the TestResult for any result messages
     */
    protected void runTest(AjcTestSpecAsTest test, TestResult result) {
        final Runner runner = getRunner();
        final Sandbox sandbox = getSandbox();
        final Validator validator = getValidator();
        int numIncomplete = 0;
        final RunStatus status = new RunStatus(new MessageHandler(), runner);
        status.setIdentifier(test.toString());
        try {
            IMessageHolder holder = getHolder();
            holder.clearMessages();
            IRunIterator steps = test.spec.makeRunIterator(sandbox, validator);
            if (0 < holder.numMessages(IMessage.ERROR, true)) {
                MessageUtil.handleAll(status, holder, IMessage.INFO, true, false);
            } else {
                runner.runIterator(steps, status, null);
            }
            if (steps instanceof RunSpecIterator) {
                numIncomplete = ((RunSpecIterator) steps).getNumIncomplete();
            }
        } finally {
            HarnessJUnitUtil.reportResult(result, status, test, numIncomplete);
            validator.deleteTempFiles(true);
        }
    }

    private File getSuiteDir() {
        if (null == suiteDir) {
            File file = new File(suitePath);
            file = file.getParentFile();
            if (null == file) {
                file = new File(".");
            }
            suiteDir = file;
        }
        return suiteDir;
    }

    private Validator getValidator() {
        if (null == validator) {
            validator = new Validator(getHolder());
            // XXX lock if keepTemp?
        }
        return validator;
    }

    private Runner getRunner() {
        if (null == runner) {
            runner = new Runner();
        }
        return runner;
    }

    private IMessageHolder getHolder() {
        if (null == holder) {
            holder = new MessageHandler();
        }
        return holder;
    }

    private AjcTest.Suite.Spec getSpec() {
        if (null == spec) {
            IMessageHolder holder = getHolder();
            spec = HarnessJUnitUtil.getSuiteSpec(suitePath, options, getHolder());
            if (VERBOSE && holder.hasAnyMessage(null, true)) {
                MessageUtil.print(System.err, holder, "skip ", 
                    MessageUtil.MESSAGE_MOST);
            }
            holder.clearMessages();
        }
        return spec; 
    }
    
    private Sandbox getSandbox() {
        if (null == sandbox) {
            sandbox = new Sandbox(spec.getSuiteDirFile(), getValidator());
        }
        return sandbox;
    }

    /** Wrap AjcTest.Spec as a TestCase. Run by delegation to suite */
    private static class AjcTestSpecAsTest extends TestCase 
        implements HarnessJUnitUtil.IHasAjcSpec {
        // this could implement Test, but Ant batchtest fails to pull name 
        final String name;
        final AjcTest.Spec spec;
        AjctestsAdapter suite;
        AjcTestSpecAsTest(AjcTest.Spec spec, AjctestsAdapter suite) {
            super(HarnessJUnitUtil.cleanTestName(spec.getDescription()));
            this.name = HarnessJUnitUtil.cleanTestName(spec.getDescription());
            this.suite = suite;
            this.spec = spec;
            spec.setSuiteDir(suite.getSuiteDir());
        }
        public int countTestCases() {
            return 1;
        }
        public AjcTest.Spec getAjcTestSpec() {
            return spec;
        }
        public void run(TestResult result) {
            if (null == suite) {
                throw new Error("need to re-init");
            }
            try {
                result.startTest(this);
                suite.runTest(this, result); 
            } finally {
                result.endTest(this);
                suite = null;
            }
        }

        public String getName() {
            return name;
        }
        public String toString() {
            return name;
        }
    }
}
