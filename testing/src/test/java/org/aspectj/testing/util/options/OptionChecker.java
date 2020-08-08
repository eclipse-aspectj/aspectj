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

import java.util.Arrays;

import org.aspectj.testing.util.LangUtil;

import junit.framework.Assert;

/**
 * Drivers to test a given set of Options.
 * They now throw AssertionFailedError on failure, 
 * but subclasses can reimplement 
 * <code>assertionFailed(String)</code>
 * to handle failures differently.
 */
public class OptionChecker {
    private final Options options;

    public OptionChecker(Options options) {
        this.options = options;
        LangUtil.throwIaxIfNull(options, "options");
    }

    /**
     * Subclasses override this to throw different exceptions
     * on assertion failures.
     * This implementation delegates to 
     * <code>Assert.assertTrue(label, false)</code>.
     * @param label the String message for the assertion
     * 
     */
    public void assertionFailed(String label) {
        Assert.assertTrue(label, false);
    }

    public void checkAssertion(String label, boolean test) {
        if (!test) {
            assertionFailed(label);
        }
    }

    public void checkOptions(String[] input, String[] expected) {
        checkOptions(input, expected, true);
    }

    public void checkOptions(
        String[] input,
        String[] expected,
        boolean resolve) {
        Values values = getValues(input);
        if (resolve) {
            String err = values.resolve();
            checkAssertion("error: \"" + err + "\"", null == err);
        }
        String[] actual = values.render(); // Value.render(values);
        checkEqual(expected, actual);
    }

    public void checkOptionsNegative(
        String[] input,
        String expectedMissedMatchErr,
        String expectedResolveErr) {
        checkOptionsNegative(
            input,
            null,
            expectedMissedMatchErr,
            expectedResolveErr);
    }

    public void checkOptionsNegative(
        String[] input,
        String expectedInValuesException,
        String expectedMissedMatchErr,
        String expectedResolveErr) {
        Values values =
            getValuesNegative(input, expectedInValuesException);
        if (null == expectedInValuesException) {
            String err = Options.missedMatchError(input, values);
            checkContains(expectedMissedMatchErr, err);
            err = values.resolve();
            checkContains(expectedResolveErr, err);
        }
    }

    private Values getValuesNegative(
        String[] input,
        String expectedInExceptionMessage,
        Options options) {
        try {
            return options.acceptInput(input);
        } catch (Option.InvalidInputException e) {
            String m = e.getFullMessage();
            boolean ok =
                (null != expectedInExceptionMessage)
                    && (m.contains(expectedInExceptionMessage));
            if (!ok) {
                e.printStackTrace(System.err);
                if (null != expectedInExceptionMessage) {
                    m =
                        "expected \""
                            + expectedInExceptionMessage
                            + "\" in "
                            + m;
                }
                assertionFailed(m);
            }
            return null; // actually never executed
        }
    }

    private Values getValuesNegative(
        String[] input,
        String expectedInExceptionMessage) {
        return getValuesNegative(
            input,
            expectedInExceptionMessage,
            options);
    }

//    private Values getValues(String[] input, Options options) {
//        return getValuesNegative(input, null, options);
//    }

    private Values getValues(String[] input) {
        return getValuesNegative(input, null);
    }

    private void checkContains(String expected, String expectedIn) {
        if (null == expected) {
            if (null != expectedIn) {
                assertionFailed("did not expect \"" + expectedIn + "\"");
            }
        } else {
            if ((null == expectedIn)
                || (!expectedIn.contains(expected))) {
                assertionFailed(
                    "expected \""
                        + expected
                        + "\" in \""
                        + expectedIn
                        + "\"");
            }
        }
    }

    private String safeString(String[] ra) {
        return (null == ra ? "null" : Arrays.asList(ra).toString());
    }

    private void checkEqual(String[] expected, String[] actual) {
        if (!isEqual(expected, actual)) {
            assertionFailed(
                "expected \""
                    + safeString(expected)
                    + "\" got \""
                    + safeString(actual)
                    + "\"");
        }
    }

    private boolean isEqual(String[] expected, String[] actual) {
        if (null == expected) {
            return (null == actual ? true : false);
        } else if (null == actual) {
            return false;
        } else if (expected.length != actual.length) {
            return false;
        }
        for (int i = 0; i < actual.length; i++) {
            String e = expected[i];
            String a = actual[i];
            if (null == e) {
                if (null != a) {
                    return false;
                }
            } else if (null == a) {
                return false;
            } else if (!(e.equals(a))) {
                return false;
            }
        }
        return true;
    }

}
