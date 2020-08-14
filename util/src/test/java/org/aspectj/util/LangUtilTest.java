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

package org.aspectj.util;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 *
 */
public class LangUtilTest extends TestCase {

	public LangUtilTest(String name) {
		super(name);
	}

	// /** @see LangUtil.extractOptions(String[], String[], int[], List) */
	// public void testExtractOptions() {
	// ArrayList extracted = new ArrayList();
	// String[] args = new String[] { "-d", "classes", "-classpath", "foo.jar", "-verbose", "Bar.java" };
	// String[] validOptions = new String[] { "-classpath", "-d", "-verbose", "-help" };
	// int[] optionArgs = new int[] { 1, 1, 0, 0 };
	// String[] result = LangUtil.extractOptions(args, validOptions, optionArgs, extracted);
	// String resultString = "" + Arrays.asList(result);
	// String EXP = "[Bar.java]";
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	// EXP = "[-d, classes, -classpath, foo.jar, -verbose]";
	// resultString = "" + extracted;
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	//
	// // no input, no output
	// extracted.clear();
	// args = new String[] {};
	// result = LangUtil.extractOptions(args, validOptions, optionArgs, extracted);
	// resultString = "" + Arrays.asList(result);
	// EXP = "[]";
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	// resultString = "" + extracted;
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	//
	// // one input, nothing extracted
	// extracted.clear();
	// args = new String[] {"Bar.java"};
	// result = LangUtil.extractOptions(args, validOptions, optionArgs, extracted);
	// resultString = "" + Arrays.asList(result);
	// EXP = "[Bar.java]";
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	// EXP = "[]";
	// resultString = "" + extracted;
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	//
	// // one input, extracted
	// extracted.clear();
	// args = new String[] {"-verbose"};
	// result = LangUtil.extractOptions(args, validOptions, optionArgs, extracted);
	// resultString = "" + Arrays.asList(result);
	// EXP = "[]";
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	// EXP = "[-verbose]";
	// resultString = "" + extracted;
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	//
	// // ------- booleans
	// validOptions = new String[] { "-help", "-verbose" };
	// optionArgs = null;
	//
	// // one input, extracted
	// extracted.clear();
	// args = new String[] {"-verbose"};
	// result = LangUtil.extractOptions(args, validOptions, optionArgs, extracted);
	// resultString = "" + Arrays.asList(result);
	// EXP = "[]";
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	// EXP = "[-verbose]";
	// resultString = "" + extracted;
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	//
	// // one input, not extracted
	// extracted.clear();
	// args = new String[] {"Bar.java"};
	// result = LangUtil.extractOptions(args, validOptions, optionArgs, extracted);
	// resultString = "" + Arrays.asList(result);
	// EXP = "[Bar.java]";
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	// EXP = "[]";
	// resultString = "" + extracted;
	// assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
	// }

	public void testVersion() {
		assertTrue(LangUtil.is18VMOrGreater()); // min vm now - floor may change
		if (LangUtil.is11VMOrGreater()) {
			assertTrue(LangUtil.is19VMOrGreater());
			assertTrue(LangUtil.is10VMOrGreater());
		}
	}

	/** @see LangUtil.extractOptions(String[], String[][]) */
	public void testExtractOptionsArrayCollector() {
		String[] args = new String[] { "-d", "classes", "-classpath", "foo.jar", "-verbose", "Bar.java" };
		String[][] OPTIONS = new String[][] { new String[] { "-classpath", null }, new String[] { "-d", null },
			new String[] { "-verbose" }, new String[] { "-help" } };

			String[][] options = LangUtil.copyStrings(OPTIONS);

			String[] result = LangUtil.extractOptions(args, options);
			String resultString = "" + Arrays.asList(result);
			String EXP = "[Bar.java]";
			assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
			assertTrue("-verbose".equals(options[2][0]));
			assertTrue("foo.jar".equals(options[0][1]));
			assertTrue("classes".equals(options[1][1]));
			assertTrue("-classpath".equals(options[0][0]));
			assertTrue("-d".equals(options[1][0]));
			assertTrue(null == options[3][0]);

			// get args back, no options set
			args = new String[] { "Bar.java" };
			options = LangUtil.copyStrings(OPTIONS);

			result = LangUtil.extractOptions(args, options);
			resultString = "" + Arrays.asList(result);
			EXP = "[Bar.java]";
			assertTrue(resultString + " != " + EXP, resultString.equals(EXP));
			assertTrue(null == options[0][0]);
			assertTrue(null == options[1][0]);
			assertTrue(null == options[2][0]);
			assertTrue(null == options[3][0]);
	}

	// public void testOptionVariants() {
	// String[] NONE = new String[0];
	// String[] one = new String[] {"-1"};
	// String[] two = new String[] {"-2"};
	// String[] three= new String[] {"-3"};
	// String[] both = new String[] {"-1", "-2" };
	// String[] oneB = new String[] {"-1-"};
	// String[] bothB = new String[] {"-1-", "-2-" };
	// String[] onetwoB = new String[] {"-1", "-2-" };
	// String[] oneBtwo = new String[] {"-1-", "-2" };
	// String[] threeB = new String[] {"-1-", "-2-", "-3-"};
	// String[] athreeB = new String[] {"a", "-1-", "-2-", "-3-"};
	// String[] threeaB = new String[] {"-1-", "a", "-2-", "-3-"};
	//
	// checkOptionVariants(NONE, new String[][] { NONE });
	// checkOptionVariants(one, new String[][] { one });
	// checkOptionVariants(both, new String[][] { both });
	// checkOptionVariants(oneB, new String[][] { NONE, one });
	// checkOptionVariants(bothB, new String[][] { NONE, one, new String[] {"-2"}, both });
	// checkOptionVariants(onetwoB, new String[][] { one, new String[] {"-1", "-2"}});
	// checkOptionVariants(oneBtwo, new String[][] { two, new String[] {"-1", "-2"}});
	// checkOptionVariants(threeB, new String[][]
	// {
	// NONE,
	// one,
	// two,
	// new String[] {"-1", "-2"},
	// three,
	// new String[] {"-1", "-3"},
	// new String[] {"-2", "-3"},
	// new String[] {"-1", "-2", "-3"}
	// });
	// checkOptionVariants(athreeB, new String[][]
	// {
	// new String[] {"a"},
	// new String[] {"a", "-1"},
	// new String[] {"a", "-2"},
	// new String[] {"a", "-1", "-2"},
	// new String[] {"a", "-3"},
	// new String[] {"a", "-1", "-3"},
	// new String[] {"a", "-2", "-3"},
	// new String[] {"a", "-1", "-2", "-3"}
	// });
	// checkOptionVariants(threeaB, new String[][]
	// {
	// new String[] {"a"},
	// new String[] {"-1", "a"},
	// new String[] {"a", "-2"},
	// new String[] {"-1", "a", "-2"},
	// new String[] {"a", "-3"},
	// new String[] {"-1", "a", "-3"},
	// new String[] {"a", "-2", "-3"},
	// new String[] {"-1", "a", "-2", "-3"}
	// });
	// }

	// void checkOptionVariants(String[] options, String[][] expected) {
	// String[][] result = LangUtil.optionVariants(options);
	// if (expected.length != result.length) {
	// assertTrue("exp=" + expected.length + " actual=" + result.length, false);
	// }
	// for (int i = 0; i < expected.length; i++) {
	// assertEquals(""+i,
	// "" + Arrays.asList(expected[i]),
	// "" + Arrays.asList(result[i]));
	// }
	// }

	/** @see XMLWriterTest#testUnflattenList() */
	public void testCommaSplit() {
		checkCommaSplit("", new String[] { "" });
		checkCommaSplit("1", new String[] { "1" });
		checkCommaSplit(" 1 2 ", new String[] { "1 2" });
		checkCommaSplit(" 1 , 2 ", new String[] { "1", "2" });
		checkCommaSplit("1,2,3,4", new String[] { "1", "2", "3", "4" });
	}

	void checkCommaSplit(String input, String[] expected) {
		List actual = LangUtil.commaSplit(input);
		String a = "" + actual;
		String e = "" + Arrays.asList(expected);
		assertTrue(e + "==" + a, e.equals(a));
	}

	public void testElideEndingLines() {
		StringBuffer stackBuffer = LangUtil.stackToString(new RuntimeException(""), true);
		LangUtil.elideEndingLines(LangUtil.StringChecker.TEST_PACKAGES, stackBuffer, 10);
		String result = stackBuffer.toString();

		if (!result.contains("(... ")) {
			// brittle - will fail under different top-level drivers
			String m = "when running under eclipse or Ant, expecting (... in trace: ";
			assertTrue(m + result, false);
		}

		stackBuffer = new StringBuffer(
				"java.lang.RuntimeException: unimplemented"
						+ "\n at org.aspectj.ajdt.internal.core.builder.EclipseUnwovenClassFile.writeWovenBytes(EclipseUnwovenClassFile.java:59)"
						+ "\n at org.aspectj.weaver.bcel.BcelWeaver.dump(BcelWeaver.java:271)"
						+ "\n at org.aspectj.weaver.bcel.BcelWeaver.weave(BcelWeaver.java:233)"
						+ "\n at org.aspectj.weaver.bcel.BcelWeaver.weave(BcelWeaver.java:198)"
						+ "\n at org.aspectj.ajdt.internal.core.builder.AjBuildManager.weaveAndGenerateClassFiles(AjBuildanager.java:230)"
						+ "\n at org.aspectj.ajdt.internal.core.builder.AjBuildManager.batchBuild(AjBuildManager.java:50)"
						+ "\n at org.aspectj.ajdt.ajc.AjdtCommand.runCommand(AjdtCommand.java:42)"
						+ "\n at org.aspectj.testing.harness.bridge.CompilerRun.run(CompilerRun.java:222)"
						+ "\n at org.aspectj.testing.run.Runner.runPrivate(Runner.java:363)"
						+ "\n at org.aspectj.testing.run.Runner.runChild(Runner.java:167)"
						+ "\n at org.aspectj.testing.run.Runner.runChild(Runner.java:126)"
						+ "\n at org.aspectj.testing.run.Runner$IteratorWrapper.run(Runner.java:441)"
						+ "\n at org.aspectj.testing.run.Runner.runPrivate(Runner.java:363)"
						+ "\n at org.aspectj.testing.run.Runner.runChild(Runner.java:167)"
						+ "\n at org.aspectj.testing.run.Runner.runChild(Runner.java:126)"
						+ "\n at org.aspectj.testing.run.Runner$IteratorWrapper.run(Runner.java:441)"
						+ "\n at org.aspectj.testing.run.Runner.runPrivate(Runner.java:363)"
						+ "\n at org.aspectj.testing.run.Runner.run(Runner.java:114)"
						+ "\n at org.aspectj.testing.run.Runner.run(Runner.java:105)"
						+ "\n at org.aspectj.testing.run.Runner.runIterator(Runner.java:228)"
						+ "\n at org.aspectj.testing.drivers.Harness.run(Harness.java:254)"
						+ "\n at org.aspectj.testing.drivers.Harness.runMain(Harness.java:217)"
						+ "\n at org.aspectj.testing.drivers.Harness.main(Harness.java:99)"
						+ "\n at org.aspectj.testing.Harness.main(Harness.java:37)" + "\n clip me");

		LangUtil.elideEndingLines(LangUtil.StringChecker.TEST_PACKAGES, stackBuffer, 25);
		result = stackBuffer.toString();
		assertTrue(result, result.contains("(... "));
		assertTrue(result, !result.contains("org.aspectj.testing"));
	}
}
