/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.drivers;

import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.harness.bridge.AbstractRunSpec;
import org.aspectj.testing.harness.bridge.AjcTest;
import org.aspectj.testing.harness.bridge.Sandbox;
import org.aspectj.testing.harness.bridge.Validator;
import org.aspectj.testing.run.IRun;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.RunStatus;
import org.aspectj.testing.run.Runner;
import org.aspectj.testing.util.RunUtils;
import org.aspectj.util.LangUtil;

import java.io.File;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * 
 */
public class SuiteTest extends TestCase {
    private static final String[] SUITES = new String[]
        { "../tests/ajcHarnessTests.xml",
         "testdata/incremental/harness/selectionTest.xml",
          "../tests/ajcTests.xml"
        };
        
    public static Test suite() {
        TestSuite suite = new TestSuite();
        for (int i = 0; i < SUITES.length; i++) {
            suite.addTest(JUnitSuite.suite(SUITES[i]));			
		}
        return suite;
    }
    
	public SuiteTest(String name) {
		super(name);
	}
}

/** adapt ajc test suite to JUnit TestSuite */
class JUnitSuite extends TestSuite {
    public static final String DEFAULT_SUITE = "../tests/ajcTests.xml";
    public static final String[] DEFAULT_OPTIONS = new String[] 
        { "-eclipse", "-hideStreams", "-logMinFail" };

    static TestSuite suite() {
        return suite(DEFAULT_SUITE);
    }

    static TestSuite suite(String path, String[] options, boolean verbose) {
        return new JUnitSuite(path, options, verbose);
    }

    static TestSuite suite(String path) {
        return new JUnitSuite(path, DEFAULT_OPTIONS, true);
    }

    private final Runner runner;

    private boolean loadedTestCases;
    
    private JUnitSuite(String suiteFile, String[] options, boolean verbose) {
        super(suiteFile);
        runner = new Runner();
        loadTestCases(suiteFile, options, verbose);
    }
    
    public void runTest(Test test, TestResult result) {
        if (!(test instanceof JUnitRunAdapter)) {
            test.run(result);
            return;
        }
        RunStatus status = new RunStatus(new MessageHandler(), runner);
        result.startTest(test);
        IRun testRun = ((JUnitRunAdapter) test).run;
        try {
            runner.run(testRun, status, null);
            if (!status.runResult()) { 
                RunUtils.VERBOSE_PRINTER.printRunStatus(System.out, status);
                AssertionFailedError failure = new AssertionFailedError(""+status);
                result.addFailure(test, failure);
            }
        } finally {
            result.endTest(test);
            if (testRun instanceof AjcTest) {
                AjcTest ajcTest = (AjcTest) testRun;
            }
        }
    }
    
    private void loadTestCases(String suiteFile, String[] options, boolean verbose) {
        if (loadedTestCases) { // guard that cleanup is last
            throw new IllegalStateException("already loaded test cases");
        }
        loadedTestCases = true;
        final SuiteReader reader = new SuiteReader();
        final Validator validator = new Validator(new MessageHandler());
        AjcTest.Suite.Spec spec = reader.readSuite(new File(suiteFile));
        if (null == spec) {
            return;
        }
        IMessageHolder holder = new MessageHandler();
        final AbstractRunSpec.RT parentValues = new AbstractRunSpec.RT();
        parentValues.setOptions(options);
        parentValues.setVerbose(verbose);
        spec.adoptParentValues(parentValues, holder);
        if (0 < holder.numMessages(null, true)) {
            System.err.println("init: messages adopting options ");
            MessageUtil.print(System.err, holder, "init: ");
            holder = new MessageHandler();
        }        
        final RunStatus status = new RunStatus(holder, runner);
        status.setIdentifier(spec);
        final Sandbox sandbox = new Sandbox(spec.getSuiteDirFile(), validator);
        int i = 0;
        System.out.println("-- loading tests");
        for (IRunIterator tests = spec.makeRunIterator(sandbox, validator);
            tests.hasNextRun();) {
            final IRun run = tests.nextRun(holder, runner);
            if (0 < holder.numMessages(null, true)) {
                System.err.println(i + ": messages loading " + run);
                MessageUtil.print(System.err, holder, i + ": ");
                holder = new MessageHandler();
            } else if (null == run) {
                System.err.println(i + ": null run ");
            } else {
                addTest(new JUnitRunAdapter(run));
            }
            System.out.print(".");
            i++;
            if ((i % 50) == 0) {
                System.out.println(" -- " + i);
            }
        }
        System.out.println("-- done loading tests for this suite");
        // add a cleanup test
        addTest(new Test() {
            public int countTestCases() { return 1;}
            public void run(TestResult result) {
                result.startTest(this);
                validator.deleteTempFiles(false);
                result.endTest(this);
            }
            public String toString() { return "validator cleanup"; }
        });
    }

    /** just opens up access, protected to public */
    static class SuiteReader extends Harness {
        public AjcTest.Suite.Spec readSuite(File suiteFile) {
            return super.readSuite(suiteFile);
        }

    }
    
    /** non-functional wrapper for the enclosed IRun */
    public static class JUnitRunAdapter implements Test {
        static final UnsupportedOperationException EX =
            new UnsupportedOperationException("");
        
        public final IRun run;
        public JUnitRunAdapter(IRun run) {
            LangUtil.throwIaxIfNull(run, "run");
            this.run = run;
        }
        public final void run(TestResult result) {
          throw EX;
        }
        public final int countTestCases() {
            return 1;
        }
        public String toString() {
            return run.toString();
        }

    }
}
