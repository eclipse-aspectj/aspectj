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
import org.aspectj.bridge.MessageHandler;
import org.aspectj.testing.harness.bridge.*;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.util.RunUtils;
import org.aspectj.testing.util.RunUtils.IRunStatusPrinter;
import org.aspectj.testing.xml.AjcSpecXmlReader;

/**
 * Utilities for adapting AjcTest.{Suite.}Spec to JUnit.
 */
public class HarnessJUnitUtil {
    /** bug?: eclipse RemoteTestRunner hangs if n>1 */
    public static final boolean ONE_ERROR_PER_TEST = true;
    public static final boolean FLATTEN_RESULTS = true;
    public static final boolean PRINT_OTHER_MESSAGES = false;

    /**
     * Create TestSuite with all suites running all options.
     * @param suites the String[] of paths to harness test suite files
     * @param options the String[][] of option sets to run (may be null)
     * @return Test with all TestSuites and TestCases
     *         specified in suites and options.
     */
    public static TestSuite suite(String name, String[] suites, String[][] options) {
        if (null == name) {
            name = AjcHarnessTestsUsingJUnit.class.getName();
        }
        TestSuite suite = new TestSuite(name);
        if (!HarnessJUnitUtil.isEmpty(suites)) {
            if (HarnessJUnitUtil.isEmpty(options)) {
                options = new String[][] {new String[0]};
            }
            for (int i = 0; i < suites.length; i++) {
                for (int j = 0; j < options.length; j++) {
                    Test t = AjctestsAdapter.make(suites[i], options[j]);
                    suite.addTest(t);
                }
            }
        }
        return suite;
    }

    public static boolean isEmpty(Object[] ra) {
        return ((null == ra) || (0 == ra.length));
    }
    /**
     * Render status using a given printer.
     * @param status the IRunStatus to render to String
     * @param printer the IRunStatusPrinter to use
     *        (defaults to AJC_PRINTER if null)
     * @return the String rendering of the status,
     *         or "((IRunStatus) null)" if null
     */
    public static String render(IRunStatus status, IRunStatusPrinter printer) {
        if (null == status) {
            return "((IRunStatus) null)";
        }
        if (null == printer) {
            printer = RunUtils.AJC_PRINTER;
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream);
        printer.printRunStatus(out, status);
        out.flush();
          
        return outStream.toString();
    }
    
    /**
     * Dump results for Test from status into TestResult.
     * This defers to the status to determine if it failed,
     * and treats ABORT as errors 
     * and everything else as failures.
     * @param result the TestResult sink
     * @param status the IRunStatus source
     * @param test the Test to associate with the results
     * @param numIncomplete ignored
     * @return 0 (ignored)
     */
    public static int reportResult(
            TestResult result, 
            IRunStatus status,
            Test test,
            int numIncomplete) {
      if (!status.runResult()) {
          String m = render(status, null);
          AssertionFailedError failure = new AssertionFailedError(m);
          if (status.hasAnyMessage(IMessage.ABORT, true)) {
              result.addError(test, failure);
          } else {
              result.addFailure(test, failure);
          }
      }
      return 0; // XXX not doing incomplete
    }
    
//    public static int reportResultComplex(
//        TestResult result, 
//        IRunStatus status,
//        Test test,
//        int numIncomplete) {
//        int errs = 0;
//        if (FLATTEN_RESULTS) {
//            IRunStatus[] kids = status.getChildren();
//            for (int i = 0; i < kids.length; i++) {
//                errs += reportResult(result, kids[i], test, 0);
//                if ((errs > 0) && ONE_ERROR_PER_TEST) {
//                    return errs;
//                }
//            }
//        }
//            
//        Throwable thrown = status.getThrown();
//        if (null != thrown) { // always report this? XXX what if expected?
//            result.addError(test, thrown);
//            errs++;
//        }
//        boolean previewPass = status.runResult();
//        IMessage[] errors = status.getMessages(null, true);
//        for (int i = 0; ((errs == 0) || !ONE_ERROR_PER_TEST)
//                        && i < errors.length; i++) {
//            IMessage message = errors[i];
//            if (message.isAbort()) {
//                result.addError(test, new ErrorMessage(message));
//                errs++;
//            } else if (message.isFailed()) {
//                result.addFailure(test, new ErrorMessage(message));
//                errs++;
//            } else if (PRINT_OTHER_MESSAGES || !previewPass) {
//                System.out.println("#### message for " + test + ": ");
//                System.out.println(message);
//            }
//        }
//        if (((errs == 0) || !ONE_ERROR_PER_TEST)
//            && ((errs == 0) != status.runResult())) {
//            String s = "expected pass=" + (errs == 0);
//            result.addFailure(test, new ErrorMessage(s));
//            errs++;
//        }
//        if (((errs == 0) || !ONE_ERROR_PER_TEST)
//            && !status.isCompleted()) {
//            result.addFailure(test, new ErrorMessage("test incomplete? "));
//            errs++;
//        }
//        if (((errs == 0) || !ONE_ERROR_PER_TEST)
//            && (0 < numIncomplete)) {
//            result.addFailure(test, new ErrorMessage("incomplete steps: " + numIncomplete));
//            errs++;
//        }
//        return errs;
//    }

    /**
     * Fix up test names for JUnit.
     * (i.e., workaround eclipse JUnit bugs)
     * @param name the String identifier for the test
     * @return the String permitted by (Eclipse) JUnit support
     */
    public static String cleanTestName(String name) { 
        name = name.replace(',', ' ');
        name = name.replace('[', ' ');
        name = name.replace(']', ' ');
        name = name.replace('-', ' ');
        return name;
    }

    public static boolean readBooleanSystemProperty(String name) {
        boolean result = false;
        try {
            String value = System.getProperty(name);
            if (null != value) {
                // XXX convert using Boolean?
                value = value.toLowerCase();
                result = ("true".equals(value) || "on".equals(value));
            }
        } catch (Throwable t) {
            // ignore
        }
        return result;
    }
    
    /**
     * Get the test suite specifications from the suite file,
     * apply the options to all,
     * and report any messages to the holder.
     * @param suitePath the String path to the harness suite file
     * @param options the String[] options for the tests - may be null
     * @param holder the IMessageHolder for any messages - may be null
     * @return AjcTest.Suite.Spec test descriptions
     *   (non-null but empty if some error)
     */
    public static AjcTest.Suite.Spec getSuiteSpec(
            String suitePath, 
            String[] options,
            IMessageHolder holder) {
        if (null == suitePath) {
            MessageUtil.fail(holder, "null suitePath");
            return EmptySuite.ME;
        }
        File suiteFile = new File(suitePath);
        if (!suiteFile.canRead() || !suiteFile.isFile()) {
            MessageUtil.fail(holder, "unable to read file " + suitePath);
            return EmptySuite.ME;
        }
        try {
            AjcTest.Suite.Spec tempSpec;
            AbstractRunSpec.RT runtime = new AbstractRunSpec.RT();
            tempSpec = AjcSpecXmlReader.getReader().
                        readAjcSuite(suiteFile);
            tempSpec.setSuiteDirFile(suiteFile.getParentFile());
            if (null == options) {
                options = new String[0];
            }
            runtime.setOptions(options);
            boolean skip = !tempSpec.adoptParentValues(runtime, holder);                
            if (skip) {
                tempSpec = EmptySuite.ME;
            }
            return tempSpec;
        } catch (IOException e) {
            MessageUtil.abort(holder, "IOException", e);
            return EmptySuite.ME;
        }
    }
    
    private static class EmptySuite extends AjcTest.Suite.Spec {
        static final EmptySuite ME = new EmptySuite();
        final ArrayList children;
        private EmptySuite(){
            children = new ArrayList() {
                // XXX incomplete...
                public void add(int arg0, Object arg1) { fail();}
                public boolean addAll(int arg0, Collection arg1) { return fail();}
                public boolean addAll(Collection o) { return fail(); }
                public boolean add(Object o) { return fail(); }
                public boolean remove(Object o) { return fail(); }
                private boolean fail() {
                    throw new Error("unmodifiable");
                }
            };
        }
        public ArrayList getChildren() {
            return children;
        }
    }
}

