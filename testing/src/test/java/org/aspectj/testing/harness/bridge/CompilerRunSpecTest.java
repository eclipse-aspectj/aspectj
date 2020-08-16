/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Wes Isberg     2003 modifications
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import java.util.Arrays;
import java.util.Set;

import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.harness.bridge.CompilerRun.Spec.CRSOptions;
import org.aspectj.testing.util.options.Option;
import org.aspectj.testing.util.options.OptionChecker;
import org.aspectj.testing.util.options.Options;
import org.aspectj.util.LangUtil;

import junit.framework.TestCase;

/**
 * 
 */
public class CompilerRunSpecTest extends TestCase {

    private static boolean PRINTING = true;
    private static final boolean SETUP_JAVA13 =
        haveProperty(Globals.J2SE13_RTJAR_NAME);
    private static final boolean SETUP_JAVA14 =
        haveProperty(Globals.J2SE14_RTJAR_NAME);
    static {
        if (!SETUP_JAVA14) {
            System.err.println(
                "warning: set -D"
                    + Globals.J2SE14_RTJAR_NAME
                    + "=... in order to run all tests");
        }
        if (!SETUP_JAVA13) {
            System.err.println(
                "warning: set -D"
                    + Globals.J2SE13_RTJAR_NAME
                    + "=... in order to run all tests");
        }
    }

    private static String[][] duplicate(String[][] input, String prefix) {
        String[][] output = new String[input.length][];
        final int prefixLength = (null == prefix ? 0 : prefix.length());
        for (int i = 0; i < output.length; i++) {
            int length = input[i].length;
            output[i] = new String[length];
            if ((length > 0) && (prefixLength > 0)) {
                System.arraycopy(input[i], 0, output[i], 0, length);
                output[i][0] =
                    prefix + output[i][0].substring(prefixLength);
            }
        }
        return output;
    }

    private static boolean haveProperty(String key) {
        try {
            return (null != System.getProperty(key));
        } catch (Throwable t) {
            // ignore
        }
        return false;
    }

    /**
     * Constructor for CompilerRunSpecTest.
     * @param name
     */
    public CompilerRunSpecTest(String name) {
        super(name);
    }

    public void testSetupArgs() {
        checkSetupArgs("eclipse", true);
        checkSetupArgs("verbose", false);
    }

    public void testCompliance() {
        // 1.3 should work
        String specOptions = "-1.3";
        String[] globalOptions = new String[0];
        boolean expectAdopted = true;
        String resultContains = "1.3";
        String messagesContain = null;
        MessageHandler handler =
            runTest(
                specOptions,
                globalOptions,
                expectAdopted,
                resultContains,
                messagesContain);
        checkMessages(handler, null);

        // 1.3 should work with collision?
        globalOptions = new String[] { "-1.3" };
        resultContains = "1.3";
        handler =
            runTest(
                specOptions,
                globalOptions,
                expectAdopted,
                resultContains,
                messagesContain);
        checkMessages(handler, null);

        // 1.4 should work
        globalOptions = new String[0];
        specOptions = "-1.4";
        resultContains = "1.4";
        handler =
            runTest(
                specOptions,
                globalOptions,
                expectAdopted,
                resultContains,
                messagesContain);
        checkMessages(handler, null);

        // compliance not checked for valid numbers, so -1.2 would pass
    }

    private void checkMessages(MessageHandler handler, String contains) {
        if (null == contains) {
            if (0 != handler.numMessages(null, true)) {
                assertTrue("" + handler, false);
            }
        } else {
            String messages = "" + handler;
            if (!messages.contains(contains)) {
                assertTrue(messages, false);
            }
        }
    }

    /** @testcase check -target converts for 1.1 and 1.2, not others */
    public void testTarget() {
        final boolean PASS = true;
        final boolean FAIL = false;
        checkSourceTargetVersionConversion("target", 1, PASS, null);
        checkSourceTargetVersionConversion("target", 2, PASS, null);
        checkSourceTargetVersionConversion("target", 3, PASS, null);
        checkSourceTargetVersionConversion("target", 4, PASS, null);
        checkSourceTargetVersionConversion("target", 5, PASS, null);
        checkSourceTargetVersionConversion("target", 6, FAIL, "illegal input");
    }

    /** @testcase check -source converts for 1.3 and 1.4, not others */
    public void testSource() {
        final boolean PASS = true;
        final boolean FAIL = false;
        if (SETUP_JAVA13) {
            checkSourceTargetVersionConversion("source", 3, PASS, null);
        }
        if (SETUP_JAVA14) {
            checkSourceTargetVersionConversion("source", 4, PASS, null);
        }

        checkSourceTargetVersionConversion(
            "source",
            2,
            FAIL,
            "not permitted");
        checkSourceTargetVersionConversion(
            "source",
            6,
            FAIL,
            "not permitted");
    }

    public void testSourceOverride() {
        if (SETUP_JAVA13 && SETUP_JAVA14) {
            checkSourceTargetOverride("source", 3, 4);
            checkSourceTargetOverride("source", 4, 3);
            checkSourceTargetOverride("source", 3, 3);
            checkSourceTargetOverride("source", 4, 4);
        }
    }

    public void testTargetOverride() {
        checkSourceTargetOverride("target", 1, 2);
        checkSourceTargetOverride("target", 2, 1);
        checkSourceTargetOverride("target", 1, 1);
        checkSourceTargetOverride("target", 2, 2);
    }

    public void testCompilerOptions() {
        checkCompilerOption(null, CompilerRun.Spec.DEFAULT_COMPILER);
        CRSOptions crsOptions = CompilerRun.Spec.testAccessToCRSOptions();
        Set options = crsOptions.compilerOptions();
        assertTrue(null != options);
        StringBuffer notLoaded = new StringBuffer();
		for (Object option : options) {
			Option compilerOption = (Option) option;
			if (!(crsOptions.compilerIsLoadable(compilerOption))) {
				notLoaded.append(" " + compilerOption);
			} else {
				String className = crsOptions.compilerClassName(compilerOption);
				String argValue = compilerOption.toString(); // XXX snoop
				String arg = Option.ON.render(argValue);
				checkCompilerOption(arg, className);
			}
		}
        if (0 < notLoaded.length()) {
            System.err.println(
                getClass().getName()
                    + ".testCompilerOptions()"
                    + " warning: compiler options not tested because not loadable: "
                    + notLoaded);
        }
    }

    /**
     * Checck that setting arg as spec compiler and setting up args
     * results in this compiler classname.
     * @param arg
     * @param className
     */
    void checkCompilerOption(String arg, String className) {
        MessageHandler handler = new MessageHandler();
        try {
            CompilerRun.Spec spec = null;
            try {
                spec = new CompilerRun.Spec();
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
            assertTrue(spec != null);
            AbstractRunSpec.RT parentRuntime = new AbstractRunSpec.RT();
            String result;
//            String expResult;

            if (null != arg) {
                parentRuntime.setOptions(new String[] { arg });
            }
            if (!spec.adoptParentValues(parentRuntime, handler)) {
                if (0 != handler.numMessages(null, true)) {
                    assertTrue(handler.toString(), false);
                } else {
                    assertTrue("adopt failed, but no messages", false);
                }
            }
            result = "" + spec.testSetup.commandOptions;
            assertEquals("[]", result);
            assertEquals(className, spec.testSetup.compilerName);
        } finally {
        }
    }

    public void testSpecOptions() {
        Options options = CompilerRun.Spec.testAccessToOptions();
        OptionChecker optionChecker = new OptionChecker(options);
        // known failures: extdirs, aspectpath, Xlintfile <file>
        // progress, time, noExit, repeat <n>,
        // help, 
        String[][] input =
            new String[][] {
                new String[] { "-verbose" },
                new String[] { "-incremental" },
                new String[] { "-emacssym" },
                new String[] { "-Xlint" },
                new String[] { "-Xlint:error" },
                new String[] { "-1.3" },
                new String[] { "-1.4" },
                new String[] { "-source", "1.3" },
                new String[] { "-source", "1.4" },
                new String[] { "-target", "1.1" },
                new String[] { "-target", "1.2" },
                new String[] { "-preserveAllLocals" },
                new String[] { "-referenceInfo" },
                new String[] { "-deprecation" },
                new String[] { "-noImportError" },
                new String[] { "-proceedOnError" }
        };
        String[][] literalInput =
            new String[][] {
                new String[] { "-nowarn" },
                new String[] { "-warn:deprecated" },
                new String[] { "-warn:deprecated,unverified" },
                new String[] { "-warn:deprecated", "-warn:unusedLocals" },
                new String[] { "-g" },
                new String[] { "-g", "-g:none" },
                new String[] { "-g:vars,source" },
                new String[] { "-verbose", "-g:vars,source" },
                };
        // normal
		for (String[] value : input) {
			optionChecker.checkOptions(value, value);
		}
		for (String[] strings : literalInput) {
			optionChecker.checkOptions(strings, strings);
		}
        // force-on
        String[][] forceInput = duplicate(input, "!");
        for (int i = 0; i < input.length; i++) {
            optionChecker.checkOptions(forceInput[i], input[i]);
        }
        // force-off
        forceInput = duplicate(input, "^");
        String[] none = new String[0];
        for (int i = 0; i < input.length; i++) {
            optionChecker.checkOptions(forceInput[i], none);
        }
    }

    public void checkSourceTargetOverride(String name, int from, int to) {
        final String specOptions = "-" + name + ", 1." + from;
        String[] globalOptions = new String[] { "!" + name, "1." + to };
        boolean expectAdopted = true;
        String resultContains = "[-" + name + ", 1." + to;
        String messagesContain = null;
        MessageHandler handler =
            runTest(
                specOptions,
                globalOptions,
                expectAdopted,
                resultContains,
                messagesContain);
        checkMessages(handler, null);
    }

    void checkSourceTargetVersionConversion(
        String name,
        int i,
        boolean expectPass,
        String expectedErr) {
        final String specOptions = "-" + name + ", 1." + i;
        String[] globalOptions = new String[0];
        boolean expectAdopted = expectPass;
        String resultContains =
            !expectPass ? null : "[-" + name + ", 1." + i;
        String messagesContain = expectedErr;
        /*MessageHandler handler =*/
            runTest(
                specOptions,
                globalOptions,
                expectAdopted,
                resultContains,
                messagesContain);
    }
    
    /**
     * Drive option-setting for CompilerRun.Spec, including
     * expected errors.
     * @param specOptions
     * @param globalOptions
     * @param expectAdopted
     * @param resultContains
     * @param messagesContain
     * @return
     */

    MessageHandler runTest(
        String specOptions,
        String[] globalOptions,
        boolean expectAdopted,
        String resultContains,
        String messagesContain) {
        MessageHandler handler = new MessageHandler();
        try {
            CompilerRun.Spec spec = new CompilerRun.Spec();
            AbstractRunSpec.RT parentRuntime = new AbstractRunSpec.RT();

            if (!LangUtil.isEmpty(specOptions)) {
                spec.setOptions(specOptions);
            }
            if (!LangUtil.isEmpty(globalOptions)) {
                parentRuntime.setOptions(globalOptions);
            }
            boolean adopted =
                spec.adoptParentValues(parentRuntime, handler);
            if (adopted != expectAdopted) {
                String s =
                    (expectAdopted ? "not " : "")
                        + "adopted spec="
                        + specOptions
                        + " globals="
                        + (LangUtil.isEmpty(globalOptions)
                            ? "[]"
                            : Arrays.asList(globalOptions).toString())
                        + " -- "
                        + handler;
                assertTrue(s, false);
            }
            if (null != resultContains) {
                String result = "" + spec.testSetup.commandOptions;
                if (!result.contains(resultContains)) {
                    assertTrue(
                        "expected " + resultContains + " got " + result,
                        false);
                }
            }
            if (null != messagesContain) {
                boolean haveMessages =
                    (0 != handler.numMessages(null, true));
                if (!haveMessages) {
                    assertTrue("expected " + messagesContain, false);
                } else {
                    String messages = handler.toString();
                    if (!messages.contains(messagesContain)) {
                        assertTrue(
                            "expected "
                                + messagesContain
                                + " got "
                                + messages,
                            false);
                    }
                }
            }
            return handler;
        } finally {
        }
    }

    void checkSetupArgs(String arg, final boolean isTestArg) {
        MessageHandler handler = new MessageHandler();
        try {
            CompilerRun.Spec spec = null;
            try {
                spec = new CompilerRun.Spec();
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
            assertTrue(spec != null);
            AbstractRunSpec.RT parentRuntime = new AbstractRunSpec.RT();
            String result;
            String expResult;

            // -------- local set
            // global - (set) does not change local-set
            parentRuntime.setOptions(new String[] { "-" + arg });
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = "" + spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));

            // global ^ (force-off) to disable
            spec.setOptions("-" + arg);
            parentRuntime.setOptions(new String[] { "^" + arg });
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = "" + spec.testSetup.commandOptions;
            assertTrue(result, "[]".equals(result));

            // global ! (force-on) does not change local-set
            parentRuntime.setOptions(new String[] { "!" + arg });
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = "" + spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            if (!expResult.equals(result)) {
                assertTrue(
                    "expected " + expResult + " got " + result,
                    false);
            }

            // global (unset) does not change local-set
            parentRuntime.setOptions(new String[] { "" });
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = "" + spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));

            // -------- local force-on
            // global ^ (force-off) conflicts with local force-on
            spec.setOptions("!" + arg);
            parentRuntime.setOptions(new String[] { "^" + arg });
            assertTrue(!spec.adoptParentValues(parentRuntime, handler));
            assertTrue(0 != handler.numMessages(null, true));
            handler.init();

            // global ! (force-on) does not change local force-on
            parentRuntime.setOptions(new String[] { "!" + arg });
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = "" + spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));

            // global - (set) does not change local force-on
            parentRuntime.setOptions(new String[] { "-" + arg });
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = "" + spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));

            // global (unset) does not change local force-on
            parentRuntime.setOptions(new String[] { "" });
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = "" + spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));

            // -------- local force-off
            // global ^ (force-off) does not change local force-off
            spec.setOptions("^" + arg);
            parentRuntime.setOptions(new String[] { "^" + arg });
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = "" + spec.testSetup.commandOptions;
            assertTrue(result, ("[]").equals(result));

            // global ! (force-on) conflicts with local force-off
            parentRuntime.setOptions(new String[] { "!" + arg });
            assertTrue(!spec.adoptParentValues(parentRuntime, handler));
            assertTrue(0 != handler.numMessages(null, true));
            handler.init();

            // global - (set) overridden by local force-off
            parentRuntime.setOptions(new String[] { "-" + arg });
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = "" + spec.testSetup.commandOptions;
            assertTrue(result, ("[]").equals(result));

            // global (unset) does not change local force-off
            parentRuntime.setOptions(new String[] { "" });
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = "" + spec.testSetup.commandOptions;
            assertTrue(result, ("[]").equals(result));

            // undefined whether global set overrides local set
            // for different sibling options 
        } finally {
            if (PRINTING && (0 < handler.numMessages(null, true))) {
                MessageUtil.print(System.err, handler, "checkSetupArgs: ");
            }
        }
    }
}
