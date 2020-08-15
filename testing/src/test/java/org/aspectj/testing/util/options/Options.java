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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.util.LangUtil;

/**
 * A bunch of options that handle search boilerplate.
 * This enforces an initialization phase by permitting
 * options to be added only until frozen, and 
 * permitting matching only after frozen.
 */
public class Options {

    /** if true, then perform extra checks to debug problems */
//    private static final boolean verifying = false;
    private static final boolean FROZEN = true;

    /**
     * List input unmatched by options, if any.
     * @param input the String[] used to generate the values
     * @param values the Option.Value[] found from the input
     * @return null if no values are null, String list of missed otherwise
     */
    public static String missedMatchError(
        String[] input,
        Values values) {
        int[] missed = values.indexMissedMatches();
        LangUtil.throwIaxIfNull(input, "input");
        LangUtil.throwIaxIfNull(values, "values");
        LangUtil.throwIaxIfFalse(
            input.length == values.length(),
            "input is not matched by values");
        if (0 == missed.length) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("missed values: [");
        for (int i = 0; i < missed.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(missed[i] + ": " + input[missed[i]]);
        }
        sb.append("]");
        return sb.toString();
    }

    private final List options = new ArrayList();
    private final boolean stopAtFirstMatch;
    private boolean frozen = !FROZEN;

    public Options(boolean stopAtFirstMatch) {
        this.stopAtFirstMatch = stopAtFirstMatch;
    }
    
    public void freeze() {
        if (frozen != FROZEN) {
            frozen = FROZEN;
        } 
    }

    public boolean isFrozen() {
        return (frozen == FROZEN);
    }

    public void addOption(Option option) {
        checkFrozen("adding option", !FROZEN);
        LangUtil.throwIaxIfNull(option, "option");
        options.add(option);
    }

    /**
     * Associate options matched, if any, with input by index.
     * If an input element is not matched, the corresponding
     * result element will be null.
     * If there are multi-argument options matched, then
     * only the initial element will be non-null, but it
     * will contain the accumulated value of the arguments.
     * @param input the String[] of input
     * @return Option.Value[] corresponding to input
     * @throws Option.InvalidInputException when encountering
     *         invalid arguments to a matched multi-argument option.
     */
    public Values acceptInput(String[] input)
        throws Option.InvalidInputException {
        checkFrozen("matching options", FROZEN);
        if ((null == input) || (0 == input.length)) {
            return Values.EMPTY;
        }
        Option.Value[] results = new Option.Value[input.length];
        for (int i = 0; i < input.length; i++) {
            Option.Value result = firstMatch(input[i]);
            final int index = i;
            if (null != result) {
                for (int len = result.option.numArguments();
                    len > 0;
                    len--) {
                    i++;
                    if (i >= input.length) {
                        throw new Option.InvalidInputException(
                            "not enough arguments",
                            null,
                            result.option);
                    }
                    result = result.nextInput(input[i]);
                }
            }
            results[index] = result;
        }
        return Values.wrapValues(results);
    }

    private void checkFrozen(String actionLabel, boolean expectFrozen) {
        if (expectFrozen != isFrozen()) {
            if (null == actionLabel) {
                actionLabel = "use";
            }
            if (expectFrozen) {
                actionLabel = "must freeze before " + actionLabel;
            } else {
                actionLabel = "frozen before " + actionLabel;
            }
            throw new IllegalStateException(actionLabel);
        }
    }

    private Option.Value firstMatch(String value) {
        LangUtil.throwIaxIfNull(value, "value");
//        ArrayList list = new ArrayList();
		for (Object o : options) {
			Option option = (Option) o;
			Option.Value result = option.acceptValue(value);
			if (null != result) {
				return result;
			}
		}
        return null;
    }
}
