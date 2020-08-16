/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package org.aspectj.testing.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.aspectj.util.LangUtil;

/**
 * Calculated differences between two test runs
 * based on their output files
 * assuming that tests are logged with prefix [PASS|FAIL]
 * (as they are when using <tt>-traceTestsMin</tt> with the Harness).
 * @see org.aspectj.testing.drivers.Harness
 */
public class TestDiffs { // XXX pretty dumb implementation

    /** @param args expected, actual test result files */
    public static void main(String[] args) {
        if ((null == args) || (2 > args.length)) {
            System.err.println("java " + TestDiffs.class.getName() + " expectedFile actualFile {test}");
            return;
        }
        File expected  = new File(args[0]);
        File actual = new File(args[1]);

        TestDiffs result = compareResults(expected, actual);

        System.out.println("## Differences between test runs");
        print(System.out, result.added, "added");
        print(System.out, result.missing, "missing");
        print(System.out, result.fixed, "fixed");
        print(System.out, result.broken, "broken");

        System.out.println("## Summary");
        System.out.println(" # expected " + result.expected.size() + " tests: " + args[0] );
        System.out.println(" #   actual " +   result.actual.size() + " tests: " + args[1]);
        StringBuffer sb = new StringBuffer();
        append(sb, result.added, " added");
        append(sb, result.missing, " missing");
        append(sb, result.broken, " broken");
        append(sb, result.fixed, " fixed");
        append(sb, result.stillPassing, " still passing");
        append(sb, result.stillFailing, " still failing");
        System.out.println(" #    diffs: " + sb);
    }

    /**
     * @param expected the expected/old File results with Harness -traceTestsMin lines
     * @param actual the actual/new File results with Harness -traceTestsMin lines
     * @return TestDiffs null if error, valid otherwise
     */
    public static TestDiffs compareResults(File expected, File actual) {
		List<TestResult> exp = null;
		List<TestResult> act = null;
        File reading = expected;
        try {
            exp = TestDiffs.readTestResults(expected, expected.getPath());
            reading = actual;
            act = TestDiffs.readTestResults(actual, actual.getPath());

            Diffs tests = Diffs.makeDiffs("tests", exp, act, TestResult.BY_NAME);
            // remove missing/unexpected (removed, added) tests from results
            // otherwise, unexpected-[pass|fail] look like [fixes|broken]
            List<TestResult> expResults = trimByName(exp, tests.missing);
            List<TestResult> actResults = trimByName(act, tests.unexpected);

            Diffs results = Diffs.makeDiffs("results", expResults, actResults, TestResult.BY_PASSNAME);

            // broken tests show up in results as unexpected-fail or missing-pass
            //  fixed tests show up in results as unexpected-pass or missing-fail
            ArrayList<TestResult> broken = new ArrayList<TestResult>();
            ArrayList<TestResult> fixed = new ArrayList<TestResult>();
            split(results.unexpected, fixed, broken);

            return new TestDiffs(
                exp,
                act,
                tests.missing,
                tests.unexpected,
                broken,
                fixed);
        } catch (IOException e) {
            System.err.println("error reading " + reading);
            e.printStackTrace(System.err); // XXX
            return null;
        }
    }

    private static void append(StringBuffer sb, List<TestResult> list, String label) {
        if (!LangUtil.isEmpty(list)) {
            if (0 < sb.length()) {
                sb.append(" ");
            }
            sb.append(list.size() + label);
        }
    }

    private static void print(PrintStream out, List<TestResult> list, String label) {
        if ((null == out) || LangUtil.isEmpty(list)) {
            return;
        }
//        int i = 0;
        final String suffix = " " + label;
        final String LABEL = list.size() + suffix;
        out.println("## START " + LABEL);
		for (Object o : list) {
			TestResult result = (TestResult) o;
			out.println(" " + result.test + " ## " + suffix);
		}
        out.println("##   END " + LABEL);
    }

    /**
	 * Create ArrayList with input TestResult list
     * but without elements in trim list,
     * comparing based on test name only.
	 * @param input
	 * @param trim
	 * @return ArrayList with all input except those in trim (by name)
	 */
	private static List<TestResult> trimByName(List<TestResult> input, List<TestResult> trim) {
		List<TestResult> result = new ArrayList<TestResult>(input);
        if (!LangUtil.isEmpty(input) && !LangUtil.isEmpty(trim)) {
            for (ListIterator<TestResult> iter = result.listIterator(); iter.hasNext();) {
				TestResult inputItem = iter.next();
				for (Object o : trim) {
					TestResult trimItem = (TestResult) o;
					if (inputItem.test.equals(trimItem.test)) {
						iter.remove();
						break;
					}
				}
			}
        }
        return result;
	}


    /** split input List by whether the TestResult element passed or failed */
    private static void split(List<TestResult> input, List<TestResult> pass, List<TestResult> fail) {
		for (Object o : input) {
			TestResult result = (TestResult) o;
			if (result.pass) {
				pass.add(result);
			} else {
				fail.add(result);
			}
		}
    }

    /**
     * Read a file of test results,
     * defined as lines starting with [PASS|FAIL]
     * (produced by Harness option <tt>-traceTestsmin</tt>).
     * @return ArrayList of TestResult, one for every -traceTestsMin line in File
     */
    private static ArrayList<TestResult> readTestResults(File file, String config) throws IOException {
        LangUtil.throwIaxIfNull(file, "file");
        if (null == config) {
            config = file.getPath();
        }
        ArrayList<TestResult> result = new ArrayList<TestResult>();
        FileReader in = null;
        try {
            in = new FileReader(file);
            BufferedReader input = new BufferedReader(in);
            String line;
            // XXX handle stream interleaving more carefully
            // XXX clip trailing ()
            // XXX fix elision in test name rendering by -traceTestsMin?
            while (null != (line = input.readLine())) {
                boolean pass = line.startsWith("PASS");
                boolean fail = false;
                if (!pass) {
                    fail = line.startsWith("FAIL");
                }
                if (pass || fail) {
                    String test = line.substring(4).trim();
                    result.add(new TestResult(test, config, pass));
                }
            }
        } finally {
            if (null != in) {
                try { in.close(); }
                catch (IOException e) {} // ignore
            }
        }
        return result;
    }

    private static List<TestResult> safeList(List<TestResult> list) {
        return (null == list
            ? Collections.EMPTY_LIST
            : Collections.unmodifiableList(list));
    }

    /** List of TestResult results from expected run. */
    public final List<TestResult> expected;

    /** List of TestResult results from actual run. */
    public final List<TestResult> actual;

    /** List of TestResult tests disappeared from test suite between expected and actual runs. */
    public final List<TestResult> missing;

    /** List of TestResult tests added to test suite between expected and actual runs. */
    public final List<TestResult> added;

    /** List of TestResult tests in both runs, expected to pass but actually failed */
    public final List<TestResult> broken;

    /** List of TestResult tests in both runs, expected to fail but actually passed */
    public final List<TestResult> fixed;

    /** List of TestResult passed tests in expected run */
    public final List<TestResult> expectedPassed;

    /** List of TestResult failed tests in expected run */
    public final List<TestResult> expectedFailed;

    /** List of TestResult passed tests in actual run */
    public final List<TestResult> actualPassed;

    /** List of TestResult tests failed in actual run */
    public final List<TestResult> actualFailed;

    /** List of TestResult tests passed in both expected and actual run */
    public final List<TestResult> stillPassing;

    /** List of TestResult tests failed in both expected and actual run */
    public final List<TestResult> stillFailing;

    private TestDiffs(
        List<TestResult> expected,
        List<TestResult> actual,
        List<TestResult> missing,
        List<TestResult> added,
        List<TestResult> broken,
        List<TestResult> fixed) {
        this.expected = safeList(expected);
        this.actual = safeList(actual);
        this.missing = safeList(missing);
        this.added = safeList(added);
        this.broken = safeList(broken);
        this.fixed = safeList(fixed);
        // expected[Passed|Failed]
		List<TestResult> passed = new ArrayList<TestResult>();
		List<TestResult> failed = new ArrayList<TestResult>();
        split(this.expected, passed, failed);
        expectedPassed = safeList(passed);
        expectedFailed = safeList(failed);

        // actual[Passed|Failed]
        passed = new ArrayList<TestResult>();
        failed = new ArrayList<TestResult>();
        split(this.actual, passed, failed);
        actualPassed = safeList(passed);
        actualFailed = safeList(failed);

        // stillPassing: expected.passed w/o broken, missingPasses
		passed = new ArrayList<TestResult>(expectedPassed);
        passed = trimByName(passed, this.broken);
        ArrayList<TestResult> missingPasses = new ArrayList<TestResult>();
        ArrayList<TestResult> missingFails = new ArrayList<TestResult>();
        split(this.missing, missingPasses, missingFails);
        passed = trimByName(passed, missingPasses);
        stillPassing = safeList(passed);

        // stillFailing: expected.failed w/o fixed, missingFails
		failed = new ArrayList<TestResult>(expectedFailed);
        failed = trimByName(failed, this.fixed);
        failed = trimByName(failed, missingFails);
        stillFailing = safeList(failed);
    }

    /** results of a test */
    public static class TestResult {
        public static final Comparator BY_PASSNAME = new Comparator() {
            public int compare(Object o1, Object o2) {
                if (o1 == o2) {
                    return 0;
                }
                TestResult lhs = (TestResult) o1;
                TestResult rhs = (TestResult) o2;
                return (lhs.pass == rhs.pass
                    ? lhs.test.compareTo(rhs.test)
                    : (lhs.pass ? 1 : -1 ));
            }

            public boolean equals(Object lhs, Object rhs) {
                return (0 == compare(lhs, rhs));
            }
        };

        public static final Comparator BY_NAME = new Comparator() {
            public int compare(Object o1, Object o2) {
                if (o1 == o2) {
                    return 0;
                }
                TestResult lhs = (TestResult) o1;
                TestResult rhs = (TestResult) o2;
                return lhs.test.compareTo(rhs.test);
            }

            public boolean equals(Object lhs, Object rhs) {
                return (0 == compare(lhs, rhs));
            }
        };

        //private static final ArrayList TESTS = new ArrayList();
        public static final String FIELDSEP = "\t";

        public final String test;
        public final String config;
        public final boolean pass;
        private final String toString;

        public TestResult(String test, String config, boolean pass) {
            LangUtil.throwIaxIfNull(test, "test");
            LangUtil.throwIaxIfNull(test, "config");
            this.test = test;
            this.config = config;
            this.pass = pass;
            toString = (pass ? "PASS" : "FAIL") + FIELDSEP + test + FIELDSEP + config;

        }

        /** @return [PASS|FAIL]{FIELDSEP}test{FIELDSEP}config */
        public String toString() {
            return toString;
        }
    }
}
