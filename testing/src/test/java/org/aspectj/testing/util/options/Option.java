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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.aspectj.util.LangUtil;

/**
 * Immutable schema for an input (command-line) option.
 * The schema contains the expected name/label,
 * the family (for comparison purposes), 
 * and permitted prefixes.
 * This has operations to accept input values and compare options.
 * Options cannot be created directly; for that, use an 
 * <code>Option.Factory</code>, since it enforces uniqueness
 * within the families and options created by the factory.
 * <p>
 * Option is used with related nested classes to implement relations:
 * <ul>
 * <li>Option.Factory produces Option</li>
 * <li>An Option has a set of Option.Prefixes, 
 *     which are variants of Option.Prefix 
 *     valid for the option (e.g., on/set, force-off, and force-on)</li>
 * <li>Option evaluates input, produces Option.Value</li>
 * <li>Related instances of Option share an Option.Family, 
 *     which enforce option exclusion, etc.</li>
 * </ul>
 * The classes are nested as "friends" in order to hide private
 * members (esp. constructors) that can be used within relations.
 * Most of these classes are immutable.
 */
public class Option implements Comparable {
    public static final Prefix ON = new Prefix("-", "on", true, false);
    public static final Prefix NONE = new Prefix("", "none", true, false);
    public static final Prefix FORCE_ON =
                                new Prefix("!", "force-on", true, true);

    public static final Prefix FORCE_OFF =
                                new Prefix("^", "force-off", false, true);
    public static final Prefixes LITERAL_PREFIXES =
        new Prefixes(new Prefix[] { NONE });
    public static final Prefixes STANDARD_PREFIXES =
        new Prefixes(new Prefix[] { ON });
    public static final Prefixes FORCE_PREFIXES =
        new Prefixes(new Prefix[] { ON, FORCE_ON, FORCE_OFF });

    /** family is the key for comparing two options */
    private final Family family;

    /** label expected for the option */
    private final String name;

    /** unique identifier for option */
    private final String optionIdentifier;

    /** prefixes permitted for the option in input */
    private final Prefixes permittedPrefixes;

    /** if true, then match on input that has extra suffix beyond prefix and name */
    private final boolean acceptSuffixedInput;

    /** 
     * If true, no collision if there are multiple values 
     * that share the same family but not the same literal value 
     */
    private final boolean permitMultipleValues;

    /** int number of arguments expected after the option itself */
    private final int numArguments;

    /**
     * If numArguments > 0, each element has a list of arguments
     * permitted at that index from the initial matching value.
     * Elements are not null.
     */
    private final String[][] permittedArguments;

    private final int nameLength;

    /*
     * Create a standard named boolean option,
     * permitting force-on and force-off.
     * @param name the String name of the option, e.g., "1.3" for "-1.3"
     * @param family
     * @param permittedPrefixes
     * @param acceptSuffixedInput
     * @param permittedArguments
     */
    public Option(
        String name,
        Family family,
        Prefixes permittedPrefixes,
        boolean acceptSuffixedInput,
        String[][] permittedArguments) {
        LangUtil.throwIaxIfNull(name, "name");
        LangUtil.throwIaxIfNull(family, "family");
        LangUtil.throwIaxIfNull(permittedPrefixes, "permittedPrefixes");
        this.name = name;
        this.nameLength = name.length();
        this.family = family;
        this.permittedPrefixes = permittedPrefixes;
        this.acceptSuffixedInput = acceptSuffixedInput;
        this.permitMultipleValues = false;
        if (LangUtil.isEmpty(permittedArguments)) {
            permittedArguments = new String[][] { };
            // nominal, unused
        } else {
            String[][] temp = new String[permittedArguments.length][];
            for (int i = 0; i < temp.length; i++) {
                String[] toCopy = permittedArguments[i];
                LangUtil.throwIaxIfNull(toCopy, "no permitted args");
                final int len = toCopy.length;
                String[] variants = new String[len];
                System.arraycopy(toCopy, 0, variants, 0, len);
                temp[i] = variants;
            }
            permittedArguments = temp;
        }
        this.permittedArguments = permittedArguments;
        numArguments = permittedArguments.length;
        optionIdentifier = family.familyName + "." + name;
    }

    public int compareTo(Object other) {
        Option option = (Option) other;
        int diff = family.compareTo(option.family);
        if (0 == diff) {
            diff = name.compareTo(option.name);
        }
        return diff;
    }

    public Family getFamily() {
        return family;
    }

    public boolean permitMultipleValues() {
        return permitMultipleValues;
    }

    /**
     * @return int number of elements in this option,
     *   e.g., 0 for -g or 1 for -source 1.4
     */
    public int numArguments() {
        return numArguments;
    }

    /**
     * If this value String represents a valid input for this option,
     * then create and return the associated Value.
     * 
     * @param value the Value created, or null if invalid
     * @return Value if this value is permitted by this option
     */
    public Value acceptValue(String value) {
        Prefix prefix = hasPrefix(value);
        if (null != prefix) {
            if (value.startsWith(name, prefix.length())) {
                value = value.substring(prefix.length());
                if (value.length() == nameLength) {
                    return new Value(value, prefix, this);
                } else if (acceptSuffixedInput) {
                    return new Value(value, prefix, this);
                } else {
                    return rejectingSuffixedInput(value);
                }
            }
        }
        return null;
    }

    /** @return true if we have same option family */
    public boolean sameOptionFamily(Option other) {
        return ((null != other) && other.family.equals(family));
    }

    /** @return true if we have same option family and name */
    public boolean sameOptionIdentifier(Option other) {
        return (sameOptionFamily(other) && name.equals(other.name));
    }

    public String toString() {
        return name;
    }

    /** 
     * Called when ignoreSuffix is off but we got value with suffix.
     */
    protected Value rejectingSuffixedInput(String value) {
        return null;
    }

    /** 
     * Verify that the input is permitted at this position.
     * @param input the String input to check for validity
     * @param position the int proposed position (0-based) 
     *   for the input (position 0 is for first argument)
     * @return null if this input is valid at this position,
     *         or a String error message otherwise.
     */
    String validArgument(String input, int position) {
        if (null == input) {
            return "null input";
        }
        // assert numArguments == permittedInput.length
        if ((position < 0) || (position >= numArguments)) {
            return "no input permitted at " + position;
        }
        String[] permitted = permittedArguments[position];
		for (String s : permitted) {
			if (input.equals(s)) {
				return null;
			}
		}
        return input + " not permitted, expecting one of "
            + Arrays.asList(permitted);
    }

    String getName() {
        return name;
    }
    Object getKey() {
        return family;
    }

    private String optionIdentifier() {
        return optionIdentifier;
    }

    private Prefix hasPrefix(String value) {
        for (Iterator iter = permittedPrefixes.iterator();
            iter.hasNext();
            ) {
            Prefix prefix = (Prefix) iter.next();
            if (-1 != prefix.prefixLength(value)) {
                return prefix;
            }
        }
        return null;
    }

    /**
     * An option family identifies a set of related options that
     * might share no literal specification.
     * E.g., the compiler family of options might include
     * -ajc and -eclipse, and the debugInfo family of options
     * might include -g and -g:vars.
     * Option families may permit or avoid option collisions.
     * <p>
     * For subclasses to permit some collisions and not others,
     * they should set permitMultipleFamilyValues to false 
     * and implement <code>doCollision(Option, Option)</code>.
     * <p>
     * This relies on Factory to ensure that familyName is
     * a unique identifier for the factory.
     */
    public static class Family implements Comparable {

        /** unique String identifier for this family */
        private final String familyName;

        /** if true, then report no collisions */
        private final boolean permitMultipleFamilyValues;

        protected Family(
            String familyName,
            boolean permitMultipleFamilyValues) {
            this.familyName = familyName;
            this.permitMultipleFamilyValues = permitMultipleFamilyValues;
        }

        public int compareTo(Object arg0) {
            Family family = (Family) arg0;
            return familyName.compareTo(family.familyName);
        }

        public boolean sameFamily(Family family) {
            return (
                (null != family) && familyName.equals(family.familyName));
        }

        boolean permitMultipleFamilyValues() {
            return permitMultipleFamilyValues;
        }

        /**
         * Options collide if they share the same family
         * but are not the same,
         * and multiple values are not permitted by the family.
         * @param lhs the Option to compare with rhs
         * @param rhs the Option to compare with lhs
         * @return true if the two options collide, false otherwise
         * @throws IllegalArgumentException if the input differ
         *         and share the same family, but this isn't it.
         */
        public final boolean collision(Option lhs, Option rhs) {
            if ((lhs == rhs) || (null == lhs) || (null == rhs)) {
                return false;
            }
            Family lhsFamily = lhs.getFamily();
            Family rhsFamily = rhs.getFamily();
            if (!(lhsFamily.sameFamily(rhsFamily))) {
                return false;
            }
            if (lhs.sameOptionIdentifier(rhs)) {
                return false;
            }
            if (this != lhsFamily) {
                String s =
                    "expected family " + this +" got family " + lhsFamily;
                throw new IllegalArgumentException(s);
            }
            return doCollision(lhs, rhs);
        }

        /**
         * Subclasses implement this to resolve collisions on
         * a case-by-case basis.  Input are guaranteed to be 
         * non-null, different, and to share this family.
         * This implementation returns
         * <code>!permitMultipleFamilyValues</code>.
         * 
         * @param lhs the Option to compare
         * @param rhs the other Option to compare
         * @return true if there is a collision.
         */
        protected boolean doCollision(Option lhs, Option rhs) {
            return !permitMultipleFamilyValues;
        }
    }

    /** 
     * A factory enforces a namespace on options.
     * All options produced from a given factory are unique,
     * as are all families.
     * Once an option or family is created, it cannot be changed.
     * To have a family permit multiple values
     * (i.e., ignore collisions), set up the family before any
     * associated options are created using
     * <code>setupFamily(String, boolean)</code>.
     */
    public static class Factory {
        private final String factoryName;

        /** enforce uniqueness of family */
        private final Map familyNameToFamily = new TreeMap();

        /** enforce uniqueness of options */
        private final List names = new ArrayList();

        public Factory(String factoryName) {
            this.factoryName = factoryName;
        }

        /**
         * Ensure that the family with this name has the 
         * specified permission.  If the family does not exist,
         * it is created.  If it does, the permission is checked.
         * If this returns false, there is no way to change the
         * family permission.
         * @param name the String identifier for the family
         * @param permitMultipleValues the boolean permission whether to
         *  allow multiple values in this family
         * @return true if family exists with this name and permission
         */
        public boolean setupFamily(
            String name,
            boolean permitMultipleValues) {
            LangUtil.throwIaxIfNull(name, "name");
            Family family;
            synchronized (familyNameToFamily) {
                family = (Family) familyNameToFamily.get(name);
                if (null == family) {
                    family = new Family(name, permitMultipleValues);
                    familyNameToFamily.put(name, family);
                } else if (
                    permitMultipleValues
                        != family.permitMultipleFamilyValues) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Register a family with this factory.
         * @return null if the family was successfully registered,
         *        or a String error otherwise
         */
        public String registerFamily(Family family) {
            if (null == family) {
                return "null family";
            }
            synchronized (familyNameToFamily) {
                Family knownFamily =
                    (Family) familyNameToFamily.get(family.familyName);
                if (null == knownFamily) {
                    familyNameToFamily.put(family.familyName, family);
                } else if (!knownFamily.equals(family)) {
                    return "different family registered, have "
                        + knownFamily
                        + " registering "
                        + family;
                }
            }

            return null;
        }

        public Option create(String name) {
            return create(name, name, FORCE_PREFIXES, false);
        }

        public Option create(
            String name,
            String family,
            Prefixes permittedPrefixes,
            boolean acceptSuffixedInput) {
            return create(
                name,
                family,
                permittedPrefixes,
                acceptSuffixedInput,
                (String[][]) null);
        }

        public Option create(
            String name,
            String family,
            Prefixes permittedPrefixes,
            boolean acceptSuffixedInput,
            String[][] permittedArguments) {
            LangUtil.throwIaxIfNull(name, "name");
            LangUtil.throwIaxIfNull(family, "family");
            LangUtil.throwIaxIfNull(
                permittedPrefixes,
                "permittedPrefixes");
            Family resolvedFamily;
            synchronized (familyNameToFamily) {
                resolvedFamily = (Family) familyNameToFamily.get(family);
                if (null == resolvedFamily) {
                    resolvedFamily = new Family(family, false);
                    familyNameToFamily.put(family, resolvedFamily);
                }
            }
            Option result =
                new Option(
                    name,
                    resolvedFamily,
                    permittedPrefixes,
                    acceptSuffixedInput,
                    permittedArguments);
            synchronized (names) {
                String optionIdentifier = result.optionIdentifier();
                if (names.contains(optionIdentifier)) {
                    String s = "not unique: " + result;
                    throw new IllegalArgumentException(s);
                } else {
                    names.add(optionIdentifier);
                }
            }
            return result;
        }

        public String toString() {
            return Factory.class.getName() + ": " + factoryName;
        }

//        private void checkUnique(Option result) {
//            String name = result.family + "." + result.name;
//        }
    }

    /**
     * The actual input value for an option.
     * When an option takes arguments, all the arguments 
     * are absorbed/flattened into its value.
     */
    public static class Value {
        private static final String FLATTEN_DELIM = "_";

        private static final int NOTARGUMENT = -1;    

        private static String flatten(String prefix, String suffix) {
            return prefix + FLATTEN_DELIM + suffix;
        }

        private static String[] unflatten(Value value) {
            if (value.argIndex == Value.NOTARGUMENT) {
                return new String[] { value.value };
            }
            StringTokenizer st =
                new StringTokenizer(value.value, FLATTEN_DELIM);
            String[] result = new String[st.countTokens()];
            // assert result.length == 1+inputIndex
            for (int i = 0; i < result.length; i++) {
                result[i] = st.nextToken();
            }
            return result;

        }

        public final String value;
        public final Prefix prefix;
        public final Option option;
        private final int argIndex;

        private Value(
            String value,
            Prefix prefix,
            Option option) {
            this(value, prefix, option, NOTARGUMENT);
        }
        private Value(
            String value,
            Prefix prefix,
            Option option,
            int argIndex) {
            this.value = value;
            this.prefix = prefix;
            this.option = option;
            this.argIndex = argIndex;
            // asserts deferred - local clients only
            // assert null != value
            // assert null != prefix
            // assert null != option
            // assert 0 <= inputIndex
            // assert inputIndex <= option.numArguments()
            // assert {number of DELIM} == argIndex
        }

        public String[] unflatten() {
            return unflatten(this);
        }

        /**
         * Create new value same as this, but with new prefix.
         * If the prefix is the same, return this.
         * @param prefix the Prefix to convert to
         * @return Value with new prefix - never null
         */
        public Value convert(Prefix prefix) {
            LangUtil.throwIaxIfNull(prefix, "prefix");
            if (this.prefix.equals(prefix)) {
                return this;
            }
            return new Value(
                this.value,
                prefix,
                this.option,
                this.argIndex);
        }

        /** 
         * 
         * @param other
         * @return true if other == this for purposes of collisions
         */
        public boolean sameValueIdentifier(Value other) {
            return (
                (null != other)
                    && sameValueIdentifier(option, other.value));
        }

        public boolean sameValueIdentifier(Option option, String value) {
            return (
                (null != option)
                    && this.option.sameOptionIdentifier(option)
                    && this.value.equals(value));
        }

        public boolean conflictsWith(Value other) {
            return (
                (null != other)
                    && option.equals(other.option)
                    && (prefix.force == other.prefix.force)
                    && ((prefix.set != other.prefix.set)
                        || !value.equals(other.value)));
        }

        public String toString() {
            return option + "=" + prefix + value;
        }

        final Value nextInput(String input) throws InvalidInputException {
            final int index = argIndex + 1;
            String err = option.validArgument(input, index);
            if (null != err) {
                throw new InvalidInputException(err, input, option);
            }
            return new Value(flatten(value, input), prefix, option, index);
        }
    }

    /**
     * A bunch of prefixes.
     */
    public static class Prefixes {
        final List list;
        private Prefixes(Prefix[] prefixes) {
            if (LangUtil.isEmpty(prefixes)) {
                list = Collections.EMPTY_LIST;
            } else {
                list =
                    Collections.unmodifiableList(
                        Arrays.asList(
                            LangUtil.safeCopy(prefixes, new Prefix[0])));
            }
        }
        public Iterator iterator() {
            return list.iterator();
        }
    }

    /**
     * A permitted prefix for an option, mainly so that options
     * "-verbose", "^verbose", and "!verbose" can be treated
     * as variants (set, force-off, and force-on) of the
     * same "verbose" option.
     */
    public static class Prefix {
        private final String prefix;
        private final int prefixLength;
        private final String name;
        private final boolean set;
        private final boolean force;
        private Prefix(
            String prefix,
            String name,
            boolean set,
            boolean force) {
            this.prefix = prefix;
            this.name = name;
            this.set = set;
            this.force = force;
            this.prefixLength = prefix.length();
        }

        /** 
         * Render a value for input if this is set.
         * @param value the String to render as an input value
         * @return null if value is null or option is not set,
         *    "-" + value otherwise
         */
        public String render(String value) {
            return ((!set || (null == value)) ? null : "-" + value);
        }

        boolean forceOff() {
            return force && !set;
        }
        boolean forceOn() {
            return force && set;
        }
        public boolean isSet() {
            return set;
        }
        private int length() {
            return prefixLength;
        }
        private int prefixLength(String input) {
            if ((null != input) && input.startsWith(prefix)) {
                return length();
            }
            return -1;
        }
        public String toString() {
            return prefix;
        }
    }

    /**
     * Thrown when an Option specifies required arguments,
     * but the arguments are not available.
     */
    public static class InvalidInputException extends Exception {
        public final String err;
        public final String input;
        public final Option option;
        InvalidInputException(String err, String input, Option option) {
            super(err);
            this.err = err;
            this.input = input;
            this.option = option;
        }
        public String getFullMessage() {
            return "illegal input \""
                + input
                + "\" for option "
                + option
                + ": "
                + err;
        }
    }
}
