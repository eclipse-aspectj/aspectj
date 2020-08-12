/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.util.options;

import org.aspectj.testing.util.options.Option.InvalidInputException;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 */
public class OptionsTest extends TestCase {

    private static Options OPTIONS;

    private static Options getOptions() {
        if (null == OPTIONS) {
            OPTIONS = new Options(true);
            Option.Factory factory = new Option.Factory("OptionsTest");
            OPTIONS.addOption(factory.create("verbose"));
            OPTIONS.addOption(factory.create("quiet"));
            OPTIONS.addOption(factory.create("debug"));
            OPTIONS.addOption(
                factory.create("ajc", "compiler", Option.FORCE_PREFIXES, false));
            OPTIONS.addOption(
                factory.create(
                    "eclipse",
                    "compiler",
                    Option.FORCE_PREFIXES,
                    false));
            OPTIONS.addOption(
                factory.create(
                    "ajdeCompiler",
                    "compiler",
                    Option.FORCE_PREFIXES,
                    false));
            OPTIONS.addOption(
                factory.create("1.3", "compliance", Option.FORCE_PREFIXES, false));
            OPTIONS.addOption(
                factory.create("1.4", "compliance", Option.FORCE_PREFIXES, false));

            // treating multi-arg as single - extrinsic flatten/unflatten
            OPTIONS.addOption(
                factory.create("target11", "target", Option.FORCE_PREFIXES, false));
            OPTIONS.addOption(
                factory.create("target12", "target", Option.FORCE_PREFIXES, false));
            OPTIONS.addOption(
                factory.create("source13", "source", Option.FORCE_PREFIXES, false));
            OPTIONS.addOption(
                factory.create("source14", "source", Option.FORCE_PREFIXES, false));

            // suffix options (a) -warn:... (b) -g, -g:...
            Assert.assertTrue(factory.setupFamily("warning", true));
            OPTIONS.addOption(
                factory.create("warn", "warning", Option.STANDARD_PREFIXES, true));

            Assert.assertTrue(factory.setupFamily("debugSymbols", true));
            OPTIONS.addOption(
                factory.create(
                    "g",
                    "debugSymbols",
                    Option.STANDARD_PREFIXES,
                    false));
            OPTIONS.addOption(
                factory.create(
                    "g:",
                    "debugSymbols",
                    Option.STANDARD_PREFIXES,
                    true));

            // treating multi-arg as single - intrinsic flatten/unflatten
            OPTIONS
                .addOption(
                    factory
                    .create(
                        "target",
                        "target",
                        Option.FORCE_PREFIXES,
                        false,
                        new String[][] {
                            new String[] { "1.1", "1.2" }
            }));
            OPTIONS
                .addOption(
                    factory
                    .create(
                        "source",
                        "source",
                        Option.FORCE_PREFIXES,
                        false,
                        new String[][] {
                            new String[] { "1.3", "1.4" }
            }));
            OPTIONS.freeze();
        }
        return OPTIONS;
    }

//    private boolean verbose;
    private OptionChecker localOptionChecker;

    public void testDebugCase() {
        OptionChecker optionChecker = getOptionChecker();
        String[] input = new String[] {
        };
        String[] expected = new String[0];
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-target", "1.1", "^target", "1.2" };
        expected = new String[] { "-target", "1.1" };

        optionChecker.checkOptions(input, expected);
    }
    public void testNegDebugCase() {
        OptionChecker optionChecker = getOptionChecker();
        String[] input = new String[] { "-target" };
        String expectedInValuesException = "not enough arguments";
        String expectedMissedMatchErr = null;
        String expectedResolveErr = null;

        optionChecker.checkOptionsNegative(
            input,
            expectedInValuesException,
            expectedMissedMatchErr,
            expectedResolveErr);
    }

    public void testOptionsPositive() {
        OptionChecker optionChecker = getOptionChecker();
        String[] input = new String[] {
        };
        String[] expected = new String[0];
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-verbose" };
        expected = new String[] { "-verbose" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!verbose" };
        expected = new String[] { "-verbose" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!verbose", "-verbose" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-verbose", "!verbose" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "^verbose" };
        expected = new String[] {
        };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "^verbose", "-verbose" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-verbose", "^verbose" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-verbose", "-debug" };
        expected = new String[] { "-verbose", "-debug" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!verbose", "!debug" };
        expected = new String[] { "-verbose", "-debug" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-verbose", "^verbose", "!debug" };
        expected = new String[] { "-debug" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "^verbose", "-verbose", "!debug" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!debug", "^verbose", "-verbose" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-ajc" };
        expected = new String[] { "-ajc" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-eclipse" };
        expected = new String[] { "-eclipse" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-eclipse", "-ajc", "!ajc" };
        expected = new String[] { "-ajc" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-eclipse", "!ajc" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-ajdeCompiler", "^ajc" };
        expected = new String[] { "-ajdeCompiler" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!ajdeCompiler", "^ajc" };
        optionChecker.checkOptions(input, expected);

        input =
            new String[] {
                "-verbose",
                "^verbose",
                "!quiet",
                "-quiet",
                "-debug",
                "^debug" };
        expected = new String[] { "-quiet" };
        optionChecker.checkOptions(input, expected);

        input =
            new String[] {
                "-verbose",
                "^debug",
                "!quiet",
                "!quiet",
                "-debug",
                "^verbose" };
        expected = new String[] { "-quiet" };
        optionChecker.checkOptions(input, expected);
    }

    public void testOptionsNegative() {
        OptionChecker optionChecker = getOptionChecker();
        String[] input = new String[] { "-unknown" };
        String expectedMissedMatchErr = "-unknown";
        String expectedResolveErr = null;
        optionChecker.checkOptionsNegative(
            input,
            expectedMissedMatchErr,
            expectedResolveErr);

        input = new String[] { "!verbose", "^verbose" };
        expectedMissedMatchErr = null;
        expectedResolveErr = "conflict";
        optionChecker.checkOptionsNegative(
            input,
            expectedMissedMatchErr,
            expectedResolveErr);

        input = new String[] { "!ajc", "!eclipse" };
        optionChecker.checkOptionsNegative(
            input,
            expectedMissedMatchErr,
            expectedResolveErr);

        input = new String[] { "-ajc", "-eclipse" };
        expectedResolveErr = "collision";
        optionChecker.checkOptionsNegative(
            input,
            expectedMissedMatchErr,
            expectedResolveErr);

        input = new String[] { "-verbose", "-verbose" };
        expectedResolveErr = null; // duplicates redundant, not colliding
        optionChecker.checkOptionsNegative(
            input,
            expectedMissedMatchErr,
            expectedResolveErr);
    }

    public void testMissedMatches() throws InvalidInputException {
        checkMissedMatches(new int[0], Values.EMPTY);
        checkMissedMatches(new int[] { 0 }, 
            Values.wrapValues(new Option.Value[1])); // null in [0]
        checkMissedMatches(
            new int[] { 0, 1, 2 },
            Values.wrapValues(new Option.Value[] { null, null, null }));

        Option.Factory factory = new Option.Factory("testMissedMatches");
        Option single = factory.create("verbose");
        Option multiple =
            factory
                .create(
                    "source",
                    "source",
                    Option.STANDARD_PREFIXES,
                    false,
                    new String[][] { new String[] { "1.3", "1.4" }
        });

        Options options = new Options(false);
        options.addOption(single);
        options.addOption(multiple);
        options.freeze();
        int[] expectNone = new int[0];
        String[] input = new String[] { "-verbose" };
        Values result = options.acceptInput(input);
        checkMissedMatches(expectNone, result);

        input = new String[] { "-verbose", "-verbose" };
        result = options.acceptInput(input);
        checkMissedMatches(expectNone, result);

        input = new String[] { "-source", "1.3" };
        result = options.acceptInput(input);
        checkMissedMatches(expectNone, result);

        input = new String[] { "-source", "1.4" };
        result = options.acceptInput(input);
        checkMissedMatches(expectNone, result);

        input = new String[] { "-verbose", "-missed" };
        result = options.acceptInput(input);
        checkMissedMatches(new int[] { 1 }, result);

        input = new String[] { "-source", "1.4", "-missed" };
        result = options.acceptInput(input);
        checkMissedMatches(new int[] { 2 }, result);

        input = new String[] { "-source", "1.4", "-missed", "-verbose" };
        result = options.acceptInput(input);
        checkMissedMatches(new int[] { 2 }, result);

    }
    
    void checkMissedMatches(int[] expected, Values actual) {
        int[] result = actual.indexMissedMatches();
        boolean failed = (result.length != expected.length);
        
        for (int i = 0; !failed && (i < result.length); i++) {
            failed = (result[i] != expected[i]);
        }
        if (failed) {
            assertTrue(
                "expected "
                    + Values.IntList.render(expected)
                    + " got "
                    + Values.IntList.render(result)
                    + " for "
                    + actual,
                false);
        }
    }

    public void testComplexOptionsPositive() {
        OptionChecker optionChecker = getOptionChecker();
        String[] input = new String[] {
        };
        String[] expected = new String[0];
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-target11" };
        expected = new String[] { "-target11" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!target12" };
        expected = new String[] { "-target12" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!target12", "-target11" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!target12", "^target11" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "^target12", "^target11" };
        expected = new String[] {
        };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!1.3" };
        expected = new String[] { "-1.3" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!1.3", "-1.4" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!1.3", "^1.4" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "^1.3", "^1.4" };
        expected = new String[] {
        };
        optionChecker.checkOptions(input, expected);

    }

    public void testMultiArgOptionsPositive() {
        OptionChecker optionChecker = getOptionChecker();
        String[] input = new String[] {
        };
        String[] expected = new String[0];
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-target", "1.1" };
        expected = new String[] { "-target", "1.1" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!target", "1.1" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!target", "1.1", "-target", "1.2" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-target", "1.1", "^target", "1.2" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-source", "1.3" };
        expected = new String[] { "-source", "1.3" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "!source", "1.3", "-source", "1.4" };
        optionChecker.checkOptions(input, expected);

        input = new String[] { "-source", "1.3", "^source", "1.4" };
        input =
            new String[] {
                "-source",
                "1.3",
                "^source",
                "1.4",
                "!source",
                "1.3",
                "-source",
                "1.4" };
        optionChecker.checkOptions(input, expected);
    }

    public void testMultiArgOptionsNegative() {
        OptionChecker optionChecker = getOptionChecker();
        String[] input = new String[] { "-target" };
        String expectedException = "not enough arguments";

        optionChecker.checkOptionsNegative(
            input,
            expectedException,
            null,
            null);

        input = new String[] { "-source" };
        optionChecker.checkOptionsNegative(
            input,
            expectedException,
            null,
            null);

        input = new String[] { "-source", "1.1" };
        expectedException = "not permitted";
        optionChecker.checkOptionsNegative(
            input,
            expectedException,
            null,
            null);

        input = new String[] { "-target", "1.3" };
        optionChecker.checkOptionsNegative(
            input,
            expectedException,
            null,
            null);
    }

    public void testMultipleInput() {
        OptionChecker optionChecker = getOptionChecker();
        String[][] input =
            new String[][] {
                new String[] { "-warn:deprecated" },
                new String[] { "-warn:deprecated,unverified" },
                new String[] { "-warn:deprecated", "-warn:unusedLocals" },
                new String[] { "-g" },
                new String[] { "-g", "-g:none" },
                new String[] { "-g:vars,source" },
                new String[] { "-verbose", "-g:vars,source" },
                };
		for (String[] strings : input) {
			optionChecker.checkOptions(strings, strings);
		}
    }

    private OptionChecker getOptionChecker() {
        if (null == localOptionChecker) {
            localOptionChecker = new OptionChecker(getOptions());
        }
        return localOptionChecker;
    }
}
