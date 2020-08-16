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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.aspectj.testing.util.options.Option.Value;
import org.aspectj.util.LangUtil;

/**
 * Wrapper for Value[] that handles search boilerplate.
 */
public class Values {
    public static final Values EMPTY;
    /** used by methods taking Selector to halt processing early */
    private static final boolean VERIFYING = true;
    private static final boolean FIND_ALL = true;

    private static final String NO_ERROR = "no error";

    static {
        EMPTY = new Values(new Value[0]);
    }

    public static Values wrapValues(Value[] values) {
        if ((null == values) || (0 == values.length)) {
            return EMPTY;
        }
        return new Values(values);
    }

    public static Values wrapValues(Values[] values) {
        if ((null == values) || (0 == values.length)) {
            return EMPTY;
        }
        Value[] input = null;
        if (values.length == 1) {
            input = values[0].asArray();
            LangUtil.throwIaxIfNull(input, "values");
        } else {
            int length = 0;
            for (int i = 0; i < values.length; i++) {            
                if (values[i] == null) {
                    LangUtil.throwIaxIfNull(null, "null value[" + i + "]");
                }
                length += values[i].length();
            }
            input = new Value[length];
            length = 0;
            Value[] temp;
			for (Values value : values) {
				temp = value.asArray();
				System.arraycopy(temp, 0, input, length, temp.length);
				length += temp.length;
			}
        }
        return new Values(input);
    }
    
    static int[] invert(int[] missed, int length) {
        final int MAX = length;
        final int len = MAX - missed.length;
        final int[] result = new int[len];
        int missedIndex = 0;
        int resultIndex = 0;
        for (int counter = 0; counter < MAX; counter++) {
            // catch result up to missed
            while (((missedIndex >= missed.length)
                || (missed[missedIndex] > counter))
                && (counter < MAX)) {
                result[resultIndex++] = counter++;
            }
            // absorb missed up to counter
            while ((missedIndex < missed.length)
                && (missed[missedIndex] <= counter)
                && (counter < MAX)) {
                missedIndex++;
            }
        }
        return result;
    }

    private static Option.Value[] toArray(ArrayList list) {
        return (Option.Value[]) list.toArray(new Option.Value[0]);
    }

    /**
     * Resolve input to remove any values matching the same options,
     * where option conflicts are handled by option forcing.
     * First, for any force-off value, all matching set-on and
     * the force-off itself are removed.  At this time, if there
     * is a matching force-on, then this will return an error.
     * Next, for any force-on value, it is converted to set-on,
     * and any other matching set-on value is removed.
     * Finally, this signals a collision if two values share 
     * the same option family and the family reports that this is
     * a collision.
     * In all cases, only the first error detected is reported.
     * @param input the Option.Value[] matched from the input,
     *              (forced/duplicate options will be set to null)
     * @return String error during resolution, or null if no error
     */
    private static String resolve(Option.Value[] input) {
        String err = null;
        if (LangUtil.isEmpty(input)) {
            return null;
        }

        Map familyToMatches = new TreeMap();
        for (int i = 0;(null == err) && (i < input.length); i++) {
            if (null != input[i]) {
                Option.Family family = input[i].option.getFamily();
                int[] matches = (int[]) familyToMatches.get(family);
                if (null == matches) {
                    matches = match(input, i);
                    familyToMatches.put(family, matches);
                }
            }
        }

        familyToMatches = Collections.unmodifiableMap(familyToMatches);
        for (Iterator iter = familyToMatches.entrySet().iterator();
            (null == err) && iter.hasNext();
            ) {
            Map.Entry entry = (Map.Entry) iter.next();
            int[] matches = (int[]) entry.getValue();
            err = resolve(input, matches);
        }
        return err;
    }

    /**
     * Resolve all related options into one 
     * by nullifying or modifying the values.
     * 
     * First, for any force-off value, 
     * remove all identical set-on 
     * and the force-off itself, 
     * and alert on any identical force-on.
     *
     * Next, for any force-on value, 
     * convert to set-on,
     * throw Error on any same-family force-off value,
     * remove any identical force-on or set-on value,
     * alert on any other non-identical same-family force-on value,
     * remove any same-family set-on value,
     * and alert on any same-family set-off value.
     * 
     * Finally, alert if any two remaining values share 
     * the same option family, unless the option is marked
     * as permitting multiple values.
     * 
     * @param input the Option.Value[] matching the input
     * @param matches the int[] list of indexes into input for
     *      values for related by option
     *     (all such values must have option matched by family)
     * @return String error, if any, or null if no error
     * @see #match(Option.Value[], int)
     */
    private static String resolve(Option.Value[] input, int[] matches) {
        String err = null;
        // seek force-off
//        Option.Value forceOff = null;
        Option option = null;
        // find and remove any force-off
        for (int i = 0;(null == err) && (i < matches.length); i++) {
            Option.Value value = input[matches[i]];
            if (null != value) {
                // verify that matches are in the same family
                if (VERIFYING) {
                    if (null == option) {
                        option = value.option;
                    } else if (!(option.sameOptionFamily(value.option))) {
                        String s =
                            value.option
                                + " has different family from "
                                + option;
                        throw new IllegalArgumentException(s);
                    }
                }
                if (value.prefix.forceOff()) {
                    err = removeForceOff(input, value, matches);
                }
            }
        }
        // find and set any force-on, removing others
        for (int i = 0;(null == err) && (i < matches.length); i++) {
            Option.Value value = input[matches[i]];
            if (null != value) {
                if (value.prefix.forceOn()) {
                    err = convertForceOn(input, i, matches);
                }
            }
        }
        // remove any exact duplicates
        for (int i = 0;(null == err) && (i < matches.length); i++) {
            Option.Value value = input[matches[i]];
            if (null != value) {
                for (int j = i + 1; j < matches.length; j++) {
                    if (value.sameValueIdentifier(input[matches[j]])) {
                        input[matches[j]] = null;
                    }
                }
            }
        }
        // signal error if two left unless permitMultipleFamilyValues
        Option.Value first = null;
        for (int i = 0;(null == err) && (i < matches.length); i++) {
            Option.Value value = input[matches[i]];
            if (null != value) {
                if (null == first) {
                    first = value;
                    if (first
                        .option
                        .getFamily()
                        .permitMultipleFamilyValues()) {
                        break;
                    }
                } else {
                    err = "collision between " + first + " and " + value;
                }
            }
        }

        return err;
    }

    /**
     * For any force-off value, 
     * remove all  set-on or force-off with same value
     * (including the force-off itself), 
     * and alert on any identical force-on.
     * @param input the Option.Value[] matching the input
     * @param value the force-off Option.Value to remove
     * @param matches the int[] list of indexes into input for
     *      values for related by option
     *     (all such values must have matching option)
     * @return String error if any
     */
    private static String removeForceOff(
        Option.Value[] input,
        Option.Value value,
        int[] matches) {
        if (!value.prefix.forceOff()) {
            throw new IllegalArgumentException(
                "expecting force-off: " + value);
        }
		for (int j : matches) {
			Value match = input[j];
			if ((null != match) && value.sameValueIdentifier(match)) {
				if (match.prefix.forceOn()) {
					return "force conflict between "
							+ value
							+ " and "
							+ match;
				} else {
					input[j] = null; // unset matches[i]?
				}
			}
		}
        return null;
    }

    /**
     * For this force-on value, convert to set-on,
     * throw Error on any same-family force-off value,
     * remove any identical force-on or set-on value,
     * alert on any other non-identical same-family force-on value,
     * remove any same-family set-on value,
     * and alert on any same-family set-off value.
     * This must be called after <code>removeForceOff(..)</code>.
     * @param input the Option.Value[] to modify
     * @param valueIndex the int index in matches to find the force-on
     *                   and to start after
     * @param matches the int[] map into input entries with matching options
     * @return
     * @throw Error if any matching force-off found
     */
    private static String convertForceOn(
        Option.Value[] input,
        int valueIndex,
        int[] matches) {
        Option.Value value = input[matches[valueIndex]];
        if (!value.prefix.forceOn()) {
            throw new IllegalArgumentException(
                "expecting force-on: " + value);
        }
        input[matches[valueIndex]] = value.convert(Option.ON);
        for (int i = 0; i < matches.length; i++) {
            if (i == valueIndex) {
                continue;
            }
            Option.Value match = input[matches[i]];
            if (null != match) {
                // assert match.sameOptionFamily(value);
                if (match.prefix.forceOff()) {
                    throw new Error(
                        "unexpected force-off:"
                            + match
                            + " when processing "
                            + value);
                }
                if (value.option.sameOptionIdentifier(match.option)) {
                    input[matches[i]] = null;
                    // remove any identical force-on or set
                } else if (match.prefix.forceOn()) {
                    return "conflict between " + match + " and " + value;
                } else if (match.prefix.isSet()) {
                    input[matches[i]] = null;
                    // remove any same-value set-on value
                } else { // same family, force-off
                    return "collision between " + match + " and " + value;
                }
            }
        }
        return null;
    }

    /**
     * Get a list of input matching the option in the initial value,
     * rendered as indexes into the input array.
     * @param input the Option.Value[] to seek in
     * @param start the int index of the starting position
     * @return int[] of indexes into input with the same option
     *         as index[start] - never null, but can be empty
     */
    private static int[] match(Option.Value[] input, int start) {
        IntList result = new IntList();
        Option.Family key = null;
        Option.Family nextKey = null;
        for (int i = start; i < input.length; i++) {
            if (null != input[i]) {
                nextKey = input[i].option.getFamily();
                if (null == key) {
                    key = nextKey;
                    result.add(i);
                } else if (key.equals(nextKey)) {
                    result.add(i);
                }
            }
        }
        return result.getList();
    }
       
    static int nullify(Option.Value[] values, Selector selector) {
        LangUtil.throwIaxIfNull(selector, "selector");
        int changed = 0;
        for (int i = 0; i < values.length; i++) {
            final boolean accepted;
            try {
                accepted = selector.accept(values[i]);
            } catch (Error e) {
                if (e != Selector.STOP) {
                    throw e;
                }
                break;
            }
            if (accepted) {
                if (null != values[i]) {
                    values[i] = null;
                    changed++;
                }
            }
        }
        return changed;
    }

    /**
     * Render set values as String using associated prefix.
     * @param values the Value[] to render
     * @return String[] of values rendered for output
     *   (never null or longer than values, but might be shorter)
     */
    private static String[] render(Value[] values) {
        ArrayList list = new ArrayList();
		for (Value value : values) {
			if (null != value) {
				String[] output = value.unflatten();
				if (LangUtil.isEmpty(output)) {
					throw new Error("no output for " + value);
				}

				String s = value.prefix.render(output[0]);
				if (null != s) { // this means the prefix is set
					list.add(s);
					list.addAll(Arrays.asList(output).subList(1, output.length));
				}
			}
		}
        return (String[]) list.toArray(new String[0]);
    }

    private final Option.Value[] values;
    private Option.Value[] valuesNotNull;
    private String resolveError;

    private Values(Value[] values) {
        this.values = new Value[values.length];
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    public int length() {
        return values.length;
    }

    public Option.Value[] asArray() {
        Option.Value[] result = new Option.Value[values.length];
        System.arraycopy(values, 0, result, 0, result.length);
        return result;
    }

    /**
     * Emit as String[] the non-null values. 
     * @return String[] of matched entries (never null, elements not null)
     */
    public String[] render() {
        return Values.render(valuesNotNull());
    }

    public String toString() {
        return Arrays.asList(values).toString();
    }

    /**
     * Create index into values of those that were matched,
     * including the options and their arguments.
     * @return int[] of elements in values that are not null (options)
     *         or that represent option arguments
     */
    public int[] indexMatches() {
        // must be in order, low to high
        final int[] missed = indexMissedMatches();
        return invert(missed, length());
    }

    /**
     * Create index into values of missed input,
     * taking into account that matched arguments are 
     * represented as null.
     * @return int[] of elements in values that are null
     *         or optionally represent option arguments
     */
    public int[] indexMissedMatches() {
        MissedSelector selector = new MissedSelector();
        find(selector, FIND_ALL);
        String errors = selector.getErrors();
        if (null != errors) {
            throw new Error(errors);
        }
        return selector.getResult();
    }

    public Value firstInFamily(Option.Family family) {
        return findFirst(new ValueSelector(family));
    }

    public Value[] allInFamily(Option.Family family) {
        return find(new ValueSelector(family), FIND_ALL);
    }

    public Value firstOption(Option option) {
        return findFirst(new ValueSelector(option));
    }

    public Value[] allOption(Option option) {
        return find(new ValueSelector(option), FIND_ALL);
    }

    public Value firstValue(Option option, String value) {
        LangUtil.throwIaxIfNull(value, "value");
        return findFirst(new ValueSelector(option, value));
    }

    public Value[] allValues(Option option, String value) {
        LangUtil.throwIaxIfNull(value, "value");
        return find(new ValueSelector(option, value), FIND_ALL);
    }

    public boolean isResolved() {
        return ((this != EMPTY) && (null != resolveError));
    }

    /**
     * 
     * @param selector the Selector to pick out entries to nullify
     *    (should throw STOP to halt processing)
     * @return Values resulting from nullifying entries,
     * or this if none were changed
     */
    public Values nullify(Selector selector) {
        if (null == selector) {
            return this;
        }
        Value[] temp = asArray();
        int changed = nullify(temp, selector);
        if (0 == changed) {
            return this;    
        }
        return new Values(temp);        
    }
    
    /**
     * Resolve options, removing duplicates by force if necessary.
     * If any error is returned, then the values are left unchanged.
     * @return String error, if any
     * @throws IllegalStateException if <code>isResolved()</code>
     */
    public String resolve() {
        if (isResolved()) {
            throw new IllegalStateException("already resolved");
        }
        Option.Value[] temp = asArray();
        resolveError = resolve(temp);
        if (null == resolveError) {
            System.arraycopy(temp, 0, values, 0, temp.length);
            valuesNotNull = null;
            resolveError = NO_ERROR;
            return null;
        }
        return resolveError;
    }

    protected Option.Value findFirst(Selector filter) {
        Option.Value[] result = find(filter, !FIND_ALL);
        return (0 == result.length ? null : result[0]);
    }

    protected Option.Value[] find(Selector filter, boolean findAll) {
        LangUtil.throwIaxIfNull(filter, "filter");
        ArrayList result = new ArrayList();
		for (Value value : values) {
			final boolean accepted;
			try {
				accepted = filter.accept(value);
			} catch (Error e) {
				if (Selector.STOP != e) {
					throw e;
				}
				break;
			}
			if (accepted) {
				result.add(value);
				if (findAll != FIND_ALL) {
					break;
				}
			}
		}
        return toArray(result);
    }

    private Option.Value[] valuesNotNull() {
        if (null == valuesNotNull) {
            ArrayList list = new ArrayList();
			for (Value value : this.values) {
				if (null != value) {
					list.add(value);
				}
			}
            valuesNotNull = toArray(list);
        }
        return valuesNotNull;
    }

    public static class Selector {
        public static final Error STOP = new Error("stop invoking Selector");
        protected Selector() {
        }
        protected boolean accept(Value value) {
            return false;
        }
    }
    protected static class ValueSelector extends Selector {

        private final Option option;
        private final Option.Family family;
        private final String value;
        ValueSelector(Option.Family family) {
            LangUtil.throwIaxIfNull(family, "family");
            this.family = family;
            option = null;
            value = null;
        }
        ValueSelector(Option option) {
            this(option, (String) null);
        }
        ValueSelector(Option option, String value) {
            LangUtil.throwIaxIfNull(option, "option");
            this.option = option;
            family = null;
            this.value = value;
        }
        protected boolean accept(Value value) {
            if (null == value) {
                return false;
            }
            if (null != family) {
                return family.sameFamily(value.option.getFamily());
            } else if (!option.sameOptionIdentifier(value.option)) {
                return false;
            } else {
                return ((null == this.value)
                    || (this.value.equals(value.value)));
            }
        }
    }

    /** pick all null entries (except for args), return as int[] */
    protected static class MissedSelector extends Selector {
        public static final String DELIM = "; ";
        final IntList result = new IntList();
        int index;
        final StringBuffer errors = new StringBuffer();
        int argsExpected;
        Option argsExpectedFor;
        MissedSelector() {
        }

        int[] getResult() {
            return result.getList();
        }

        /**
         * add index if value is null
         * unless skipArguments 
         */
        protected boolean accept(Value value) {
            index++;
            if (null != value) {
                if (0 < argsExpected) { // expected more (null) args
                    missedArgsFor(argsExpectedFor, argsExpected);
                }
                argsExpected = value.option.numArguments();
                argsExpectedFor = value.option;
            } else if (0 < argsExpected) { // ignore null in arg position
                argsExpected--;
                if (0 == argsExpected) {
                    argsExpectedFor = null;
                }
            } else { // null, not expecting arg, so missing
                result.add(index - 1);
                return true;
            }
            return false;
        }

        private void missedArgsFor(Option option, int numArgs) {
            errors.append("missed ");
            errors.append(numArgs + " args for ");
            errors.append(option + DELIM);
        }

        String getErrors() {
            if (0 < argsExpected) {
            }
            if (0 == errors.length()) {
                return null;
            }
            return errors.toString();
        }
    }

    static class IntList {
        // not synchronized - used only in one thread
        static String render(int[] input) {
            if (null == input) {
                return "null";
            }
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            for (int i = 0; i < input.length; i++) {
                if (i > 0) {
                    sb.append(", " + input[i]);
                } else {
                    sb.append("" + input[i]);
                }
            }
            sb.append("]");
            return sb.toString();
        }

        private int[] input = new int[256];
        private int insert;
        private void add(int i) {
            if (insert >= input.length) {
                int[] temp = new int[insert + 256];
				System.arraycopy(input, 0, temp, 0, input.length);
                input = temp;
            }
            input[insert++] = i;
        }

        private int[] getList() {
            int[] result = new int[insert];
			if (result.length >= 0) System.arraycopy(input, 0, result, 0, result.length);
            return result;
        }
    }
}
