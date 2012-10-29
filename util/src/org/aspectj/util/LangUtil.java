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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 */
public class LangUtil {

	public static final String EOL;

	private static double vmVersion;

	static {
		StringWriter buf = new StringWriter();
		PrintWriter writer = new PrintWriter(buf);
		writer.println("");
		String eol = "\n";
		try {
			buf.close();
			StringBuffer sb = buf.getBuffer();
			if (sb != null) {
				eol = buf.toString();
			}
		} catch (Throwable t) {
		}
		EOL = eol;
	}

	static {
		try {
			String vm = System.getProperty("java.version"); // JLS 20.18.7
			if (vm == null) {
				vm = System.getProperty("java.runtime.version");
			}
			if (vm == null) {
				vm = System.getProperty("java.vm.version");
			}
			if (vm == null) {
				new RuntimeException(
						"System properties appear damaged, cannot find: java.version/java.runtime.version/java.vm.version")
						.printStackTrace(System.err);
				vmVersion = 1.5;
			} else {
				try {
					String versionString = vm.substring(0, 3);
					Double temp = new Double(Double.parseDouble(versionString));
					vmVersion = temp.floatValue();
				} catch (Exception e) {
					vmVersion = 1.4;
				}
			}
		} catch (Throwable t) {
			new RuntimeException(
					"System properties appear damaged, cannot find: java.version/java.runtime.version/java.vm.version", t)
					.printStackTrace(System.err);
			vmVersion = 1.5;
		}
	}

	public static boolean is13VMOrGreater() {
		return 1.3 <= vmVersion;
	}

	public static boolean is14VMOrGreater() {
		return 1.4 <= vmVersion;
	}

	public static boolean is15VMOrGreater() {
		return 1.5 <= vmVersion;
	}

	public static boolean is16VMOrGreater() {
		return 1.6 <= vmVersion;
	}

	public static boolean is17VMOrGreater() {
		return 1.7 <= vmVersion;
	}

	/**
	 * Shorthand for "if null, throw IllegalArgumentException"
	 * 
	 * @throws IllegalArgumentException "null {name}" if o is null
	 */
	public static final void throwIaxIfNull(final Object o, final String name) {
		if (null == o) {
			String message = "null " + (null == name ? "input" : name);
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Shorthand for "if not null or not assignable, throw IllegalArgumentException"
	 * 
	 * @param c the Class to check - use null to ignore type check
	 * @throws IllegalArgumentException "null {name}" if o is null
	 */
	public static final void throwIaxIfNotAssignable(final Object ra[], final Class<?> c, final String name) {
		throwIaxIfNull(ra, name);
		String label = (null == name ? "input" : name);
		for (int i = 0; i < ra.length; i++) {
			if (null == ra[i]) {
				String m = " null " + label + "[" + i + "]";
				throw new IllegalArgumentException(m);
			} else if (null != c) {
				Class<?> actualClass = ra[i].getClass();
				if (!c.isAssignableFrom(actualClass)) {
					String message = label + " not assignable to " + c.getName();
					throw new IllegalArgumentException(message);
				}
			}
		}
	}

	/**
	 * Shorthand for "if not null or not assignable, throw IllegalArgumentException"
	 * 
	 * @throws IllegalArgumentException "null {name}" if o is null
	 */
	public static final void throwIaxIfNotAssignable(final Object o, final Class<?> c, final String name) {
		throwIaxIfNull(o, name);
		if (null != c) {
			Class<?> actualClass = o.getClass();
			if (!c.isAssignableFrom(actualClass)) {
				String message = name + " not assignable to " + c.getName();
				throw new IllegalArgumentException(message);
			}
		}
	}

	// /**
	// * Shorthand for
	// "if any not null or not assignable, throw IllegalArgumentException"
	// * @throws IllegalArgumentException "{name} is not assignable to {c}"
	// */
	// public static final void throwIaxIfNotAllAssignable(final Collection
	// collection,
	// final Class c, final String name) {
	// throwIaxIfNull(collection, name);
	// if (null != c) {
	// for (Iterator iter = collection.iterator(); iter.hasNext();) {
	// throwIaxIfNotAssignable(iter.next(), c, name);
	//				
	// }
	// }
	// }
	/**
	 * Shorthand for "if false, throw IllegalArgumentException"
	 * 
	 * @throws IllegalArgumentException "{message}" if test is false
	 */
	public static final void throwIaxIfFalse(final boolean test, final String message) {
		if (!test) {
			throw new IllegalArgumentException(message);
		}
	}

	// /** @return ((null == s) || (0 == s.trim().length())); */
	// public static boolean isEmptyTrimmed(String s) {
	// return ((null == s) || (0 == s.length())
	// || (0 == s.trim().length()));
	// }

	/** @return ((null == s) || (0 == s.length())); */
	public static boolean isEmpty(String s) {
		return ((null == s) || (0 == s.length()));
	}

	/** @return ((null == ra) || (0 == ra.length)) */
	public static boolean isEmpty(Object[] ra) {
		return ((null == ra) || (0 == ra.length));
	}

	/** @return ((null == ra) || (0 == ra.length)) */
	public static boolean isEmpty(byte[] ra) {
		return ((null == ra) || (0 == ra.length));
	}

	/** @return ((null == collection) || (0 == collection.size())) */
	public static boolean isEmpty(Collection<?> collection) {
		return ((null == collection) || (0 == collection.size()));
	}

	/** @return ((null == map) || (0 == map.size())) */
	public static boolean isEmpty(Map<?,?> map) {
		return ((null == map) || (0 == map.size()));
	}

	/**
	 * Splits <code>text</code> at whitespace.
	 * 
	 * @param text <code>String</code> to split.
	 */
	public static String[] split(String text) {
		return (String[]) strings(text).toArray(new String[0]);
	}

	/**
	 * Splits <code>input</code> at commas, trimming any white space.
	 * 
	 * @param input <code>String</code> to split.
	 * @return List of String of elements.
	 */
	public static List<String> commaSplit(String input) {
		return anySplit(input, ",");
	}

	/**
	 * Split string as classpath, delimited at File.pathSeparator. Entries are not trimmed, but empty entries are ignored.
	 * 
	 * @param classpath the String to split - may be null or empty
	 * @return String[] of classpath entries
	 */
	public static String[] splitClasspath(String classpath) {
		if (LangUtil.isEmpty(classpath)) {
			return new String[0];
		}
		StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
		ArrayList<String> result = new ArrayList<String>(st.countTokens());
		while (st.hasMoreTokens()) {
			String entry = st.nextToken();
			if (!LangUtil.isEmpty(entry)) {
				result.add(entry);
			}
		}
		return (String[]) result.toArray(new String[0]);
	}

	/**
	 * Get System property as boolean, but use default value where the system property is not set.
	 * 
	 * @return true if value is set to true, false otherwise
	 */
	public static boolean getBoolean(String propertyName, boolean defaultValue) {
		if (null != propertyName) {
			try {
				String value = System.getProperty(propertyName);
				if (null != value) {
					return Boolean.valueOf(value).booleanValue();
				}
			} catch (Throwable t) {
				// default below
			}
		}
		return defaultValue;
	}

	/**
	 * Splits <code>input</code>, removing delimiter and trimming any white space. Returns an empty collection if the input is null.
	 * If delimiter is null or empty or if the input contains no delimiters, the input itself is returned after trimming white
	 * space.
	 * 
	 * @param input <code>String</code> to split.
	 * @param delim <code>String</code> separators for input.
	 * @return List of String of elements.
	 */
	public static List<String> anySplit(String input, String delim) {
		if (null == input) {
			return Collections.emptyList();
		}
		ArrayList<String> result = new ArrayList<String>();

		if (LangUtil.isEmpty(delim) || (-1 == input.indexOf(delim))) {
			result.add(input.trim());
		} else {
			StringTokenizer st = new StringTokenizer(input, delim);
			while (st.hasMoreTokens()) {
				result.add(st.nextToken().trim());
			}
		}
		return result;
	}

	/**
	 * Splits strings into a <code>List</code> using a <code>StringTokenizer</code>.
	 * 
	 * @param text <code>String</code> to split.
	 */
	public static List<String> strings(String text) {
		if (LangUtil.isEmpty(text)) {
			return Collections.emptyList();
		}
		List<String> strings = new ArrayList<String>();
		StringTokenizer tok = new StringTokenizer(text);
		while (tok.hasMoreTokens()) {
			strings.add(tok.nextToken());
		}
		return strings;
	}

	/** @return a non-null unmodifiable List */
	public static <T> List<T> safeList(List<T> list) {
		return (null == list ? Collections.<T>emptyList() : Collections.unmodifiableList(list));
	}

	// /**
	// * Select from input String[] based on suffix-matching
	// * @param inputs String[] of input - null ignored
	// * @param suffixes String[] of suffix selectors - null ignored
	// * @param ignoreCase if true, ignore case
	// * @return String[] of input that end with any input
	// */
	// public static String[] endsWith(String[] inputs, String[] suffixes,
	// boolean ignoreCase) {
	// if (LangUtil.isEmpty(inputs) || LangUtil.isEmpty(suffixes)) {
	// return new String[0];
	// }
	// if (ignoreCase) {
	// String[] temp = new String[suffixes.length];
	// for (int i = 0; i < temp.length; i++) {
	// String suff = suffixes[i];
	// temp[i] = (null == suff ? null : suff.toLowerCase());
	// }
	// suffixes = temp;
	// }
	// ArrayList result = new ArrayList();
	// for (int i = 0; i < inputs.length; i++) {
	// String input = inputs[i];
	// if (null == input) {
	// continue;
	// }
	// if (!ignoreCase) {
	// input = input.toLowerCase();
	// }
	// for (int j = 0; j < suffixes.length; j++) {
	// String suffix = suffixes[j];
	// if (null == suffix) {
	// continue;
	// }
	// if (input.endsWith(suffix)) {
	// result.add(input);
	// break;
	// }
	// }
	// }
	// return (String[]) result.toArray(new String[0]);
	// }
	//    
	// /**
	// * Select from input String[] if readable directories
	// * @param inputs String[] of input - null ignored
	// * @param baseDir the base directory of the input
	// * @return String[] of input that end with any input
	// */
	// public static String[] selectDirectories(String[] inputs, File baseDir) {
	// if (LangUtil.isEmpty(inputs)) {
	// return new String[0];
	// }
	// ArrayList result = new ArrayList();
	// for (int i = 0; i < inputs.length; i++) {
	// String input = inputs[i];
	// if (null == input) {
	// continue;
	// }
	// File inputFile = new File(baseDir, input);
	// if (inputFile.canRead() && inputFile.isDirectory()) {
	// result.add(input);
	// }
	// }
	// return (String[]) result.toArray(new String[0]);
	// }

	/**
	 * copy non-null two-dimensional String[][]
	 * 
	 * @see extractOptions(String[], String[][])
	 */
	public static String[][] copyStrings(String[][] in) {
		String[][] out = new String[in.length][];
		for (int i = 0; i < out.length; i++) {
			out[i] = new String[in[i].length];
			System.arraycopy(in[i], 0, out[i], 0, out[i].length);
		}
		return out;
	}

	/**
	 * Extract options and arguments to input option list, returning remainder. The input options will be nullified if not found.
	 * e.g.,
	 * 
	 * <pre>
	 * String[] options = new String[][] { new String[] { &quot;-verbose&quot; }, new String[] { &quot;-classpath&quot;, null } };
	 * String[] args = extractOptions(args, options);
	 * boolean verbose = null != options[0][0];
	 * boolean classpath = options[1][1];
	 * </pre>
	 * 
	 * @param args the String[] input options
	 * @param options the String[][]options to find in the input args - not null for each String[] component the first subcomponent
	 *        is the option itself, and there is one String subcomponent for each additional argument.
	 * @return String[] of args remaining after extracting options to extracted
	 */
	public static String[] extractOptions(String[] args, String[][] options) {
		if (LangUtil.isEmpty(args) || LangUtil.isEmpty(options)) {
			return args;
		}
		BitSet foundSet = new BitSet();
		String[] result = new String[args.length];
		int resultIndex = 0;
		for (int j = 0; j < args.length; j++) {
			boolean found = false;
			for (int i = 0; !found && (i < options.length); i++) {
				String[] option = options[i];
				LangUtil.throwIaxIfFalse(!LangUtil.isEmpty(option), "options");
				String sought = option[0];
				found = sought.equals(args[j]);
				if (found) {
					foundSet.set(i);
					int doMore = option.length - 1;
					if (0 < doMore) {
						final int MAX = j + doMore;
						if (MAX >= args.length) {
							String s = "expecting " + doMore + " args after ";
							throw new IllegalArgumentException(s + args[j]);
						}
						for (int k = 1; k < option.length; k++) {
							option[k] = args[++j];
						}
					}
				}
			}
			if (!found) {
				result[resultIndex++] = args[j];
			}
		}

		// unset any not found
		for (int i = 0; i < options.length; i++) {
			if (!foundSet.get(i)) {
				options[i][0] = null;
			}
		}
		// fixup remainder
		if (resultIndex < args.length) {
			String[] temp = new String[resultIndex];
			System.arraycopy(result, 0, temp, 0, resultIndex);
			args = temp;
		}

		return args;
	}

	//    
	// /**
	// * Extract options and arguments to input parameter list, returning
	// remainder.
	// * @param args the String[] input options
	// * @param validOptions the String[] options to find in the input args -
	// not null
	// * @param optionArgs the int[] number of arguments for each option in
	// validOptions
	// * (if null, then no arguments for any option)
	// * @param extracted the List for the matched options
	// * @return String[] of args remaining after extracting options to
	// extracted
	// */
	// public static String[] extractOptions(String[] args, String[]
	// validOptions,
	// int[] optionArgs, List extracted) {
	// if (LangUtil.isEmpty(args)
	// || LangUtil.isEmpty(validOptions) ) {
	// return args;
	// }
	// if (null != optionArgs) {
	// if (optionArgs.length != validOptions.length) {
	// throw new IllegalArgumentException("args must match options");
	// }
	// }
	// String[] result = new String[args.length];
	// int resultIndex = 0;
	// for (int j = 0; j < args.length; j++) {
	// boolean found = false;
	// for (int i = 0; !found && (i < validOptions.length); i++) {
	// String sought = validOptions[i];
	// int doMore = (null == optionArgs ? 0 : optionArgs[i]);
	// if (LangUtil.isEmpty(sought)) {
	// continue;
	// }
	// found = sought.equals(args[j]);
	// if (found) {
	// if (null != extracted) {
	// extracted.add(sought);
	// }
	// if (0 < doMore) {
	// final int MAX = j + doMore;
	// if (MAX >= args.length) {
	// String s = "expecting " + doMore + " args after ";
	// throw new IllegalArgumentException(s + args[j]);
	// }
	// if (null != extracted) {
	// while (j < MAX) {
	// extracted.add(args[++j]);
	// }
	// } else {
	// j = MAX;
	// }
	// }
	// break;
	// }
	// }
	// if (!found) {
	// result[resultIndex++] = args[j];
	// }
	// }
	// if (resultIndex < args.length) {
	// String[] temp = new String[resultIndex];
	// System.arraycopy(result, 0, temp, 0, resultIndex);
	// args = temp;
	// }
	// return args;
	// }

	// /** @return String[] of entries in validOptions found in args */
	// public static String[] selectOptions(String[] args, String[]
	// validOptions) {
	// if (LangUtil.isEmpty(args) || LangUtil.isEmpty(validOptions)) {
	// return new String[0];
	// }
	// ArrayList result = new ArrayList();
	// for (int i = 0; i < validOptions.length; i++) {
	// String sought = validOptions[i];
	// if (LangUtil.isEmpty(sought)) {
	// continue;
	// }
	// for (int j = 0; j < args.length; j++) {
	// if (sought.equals(args[j])) {
	// result.add(sought);
	// break;
	// }
	// }
	// }
	// return (String[]) result.toArray(new String[0]);
	// }

	// /** @return String[] of entries in validOptions found in args */
	// public static String[] selectOptions(List args, String[] validOptions) {
	// if (LangUtil.isEmpty(args) || LangUtil.isEmpty(validOptions)) {
	// return new String[0];
	// }
	// ArrayList result = new ArrayList();
	// for (int i = 0; i < validOptions.length; i++) {
	// String sought = validOptions[i];
	// if (LangUtil.isEmpty(sought)) {
	// continue;
	// }
	// for (Iterator iter = args.iterator(); iter.hasNext();) {
	// String arg = (String) iter.next();
	// if (sought.equals(arg)) {
	// result.add(sought);
	// break;
	// }
	// }
	// }
	// return (String[]) result.toArray(new String[0]);
	// }

	// /**
	// * Generate variants of String[] options by creating an extra set for
	// * each option that ends with "-". If none end with "-", then an
	// * array equal to <code>new String[][] { options }</code> is returned;
	// * if one ends with "-", then two sets are returned,
	// * three causes eight sets, etc.
	// * @return String[][] with each option set.
	// * @throws IllegalArgumentException if any option is null or empty.
	// */
	// public static String[][] optionVariants(String[] options) {
	// if ((null == options) || (0 == options.length)) {
	// return new String[][] { new String[0]};
	// }
	// // be nice, don't stomp input
	// String[] temp = new String[options.length];
	// System.arraycopy(options, 0, temp, 0, temp.length);
	// options = temp;
	// boolean[] dup = new boolean[options.length];
	// int numDups = 0;
	//        
	// for (int i = 0; i < options.length; i++) {
	// String option = options[i];
	// if (LangUtil.isEmpty(option)) {
	// throw new IllegalArgumentException("empty option at " + i);
	// }
	// if (option.endsWith("-")) {
	// options[i] = option.substring(0, option.length()-1);
	// dup[i] = true;
	// numDups++;
	// }
	// }
	// final String[] NONE = new String[0];
	// final int variants = exp(2, numDups);
	// final String[][] result = new String[variants][];
	// // variant is a bitmap wrt doing extra value when dup[k]=true
	// for (int variant = 0; variant < variants; variant++) {
	// ArrayList next = new ArrayList();
	// int nextOption = 0;
	// for (int k = 0; k < options.length; k++) {
	// if (!dup[k] || (0 != (variant & (1 << (nextOption++))))) {
	// next.add(options[k]);
	// }
	// }
	// result[variant] = (String[]) next.toArray(NONE);
	// }
	// return result;
	// }
	//    
	// private static int exp(int base, int power) { // not in Math?
	// if (0 > power) {
	// throw new IllegalArgumentException("negative power: " + power);
	// }
	// int result = 1;
	// while (0 < power--) {
	// result *= base;
	// }
	// return result;
	// }

	// /**
	// * Make a copy of the array.
	// * @return an array with the same component type as source
	// * containing same elements, even if null.
	// * @throws IllegalArgumentException if source is null
	// */
	// public static final Object[] copy(Object[] source) {
	// LangUtil.throwIaxIfNull(source, "source");
	// final Class c = source.getClass().getComponentType();
	// Object[] result = (Object[]) Array.newInstance(c, source.length);
	// System.arraycopy(source, 0, result, 0, result.length);
	// return result;
	// }

	/**
	 * Convert arrays safely. The number of elements in the result will be 1 smaller for each element that is null or not
	 * assignable. This will use sink if it has exactly the right size. The result will always have the same component type as sink.
	 * 
	 * @return an array with the same component type as sink containing any assignable elements in source (in the same order).
	 * @throws IllegalArgumentException if either is null
	 */
	public static Object[] safeCopy(Object[] source, Object[] sink) {
		final Class<?> sinkType = (null == sink ? Object.class : sink.getClass().getComponentType());
		final int sourceLength = (null == source ? 0 : source.length);
		final int sinkLength = (null == sink ? 0 : sink.length);

		final int resultSize;
		ArrayList<Object> result = null;
		if (0 == sourceLength) {
			resultSize = 0;
		} else {
			result = new ArrayList<Object>(sourceLength);
			for (int i = 0; i < sourceLength; i++) {
				if ((null != source[i]) && (sinkType.isAssignableFrom(source[i].getClass()))) {
					result.add(source[i]);
				}
			}
			resultSize = result.size();
		}
		if (resultSize != sinkLength) {
			sink = (Object[]) Array.newInstance(sinkType, result.size());
		}
		if (0 < resultSize) {
			sink = result.toArray(sink);
		}
		return sink;
	}

	/**
	 * @return a String with the unqualified class name of the class (or "null")
	 */
	public static String unqualifiedClassName(Class<?> c) {
		if (null == c) {
			return "null";
		}
		String name = c.getName();
		int loc = name.lastIndexOf(".");
		if (-1 != loc) {
			name = name.substring(1 + loc);
		}
		return name;
	}

	/**
	 * @return a String with the unqualified class name of the object (or "null")
	 */
	public static String unqualifiedClassName(Object o) {
		return LangUtil.unqualifiedClassName(null == o ? null : o.getClass());
	}

	/** inefficient way to replace all instances of sought with replace */
	public static String replace(String in, String sought, String replace) {
		if (LangUtil.isEmpty(in) || LangUtil.isEmpty(sought)) {
			return in;
		}
		StringBuffer result = new StringBuffer();
		final int len = sought.length();
		int start = 0;
		int loc;
		while (-1 != (loc = in.indexOf(sought, start))) {
			result.append(in.substring(start, loc));
			if (!LangUtil.isEmpty(replace)) {
				result.append(replace);
			}
			start = loc + len;
		}
		result.append(in.substring(start));
		return result.toString();
	}

	/** render i right-justified with a given width less than about 40 */
	public static String toSizedString(long i, int width) {
		String result = "" + i;
		int size = result.length();
		if (width > size) {
			final String pad = "                                              ";
			final int padLength = pad.length();
			if (width > padLength) {
				width = padLength;
			}
			int topad = width - size;
			result = pad.substring(0, topad) + result;
		}
		return result;
	}

	// /** clip StringBuffer to maximum number of lines */
	// static String clipBuffer(StringBuffer buffer, int maxLines) {
	// if ((null == buffer) || (1 > buffer.length())) return "";
	// StringBuffer result = new StringBuffer();
	// int j = 0;
	// final int MAX = maxLines;
	// final int N = buffer.length();
	// for (int i = 0, srcBegin = 0; i < MAX; srcBegin += j) {
	// // todo: replace with String variant if/since getting char?
	// char[] chars = new char[128];
	// int srcEnd = srcBegin+chars.length;
	// if (srcEnd >= N) {
	// srcEnd = N-1;
	// }
	// if (srcBegin == srcEnd) break;
	// //log("srcBegin:" + srcBegin + ":srcEnd:" + srcEnd);
	// buffer.getChars(srcBegin, srcEnd, chars, 0);
	// for (j = 0; j < srcEnd-srcBegin/*chars.length*/; j++) {
	// char c = chars[j];
	// if (c == '\n') {
	// i++;
	// j++;
	// break;
	// }
	// }
	// try { result.append(chars, 0, j); }
	// catch (Throwable t) { }
	// }
	// return result.toString();
	// }

	/**
	 * @return "({UnqualifiedExceptionClass}) {message}"
	 */
	public static String renderExceptionShort(Throwable e) {
		if (null == e) {
			return "(Throwable) null";
		}
		return "(" + LangUtil.unqualifiedClassName(e) + ") " + e.getMessage();
	}

	/**
	 * Renders exception <code>t</code> after unwrapping and eliding any test packages.
	 * 
	 * @param t <code>Throwable</code> to print.
	 * @see #maxStackTrace
	 */
	public static String renderException(Throwable t) {
		return renderException(t, true);
	}

	/**
	 * Renders exception <code>t</code>, unwrapping, optionally eliding and limiting total number of lines.
	 * 
	 * @param t <code>Throwable</code> to print.
	 * @param elide true to limit to 100 lines and elide test packages
	 * @see StringChecker#TEST_PACKAGES
	 */
	public static String renderException(Throwable t, boolean elide) {
		if (null == t) {
			return "null throwable";
		}
		t = unwrapException(t);
		StringBuffer stack = stackToString(t, false);
		if (elide) {
			elideEndingLines(StringChecker.TEST_PACKAGES, stack, 100);
		}
		return stack.toString();
	}

	/**
	 * Trim ending lines from a StringBuffer, clipping to maxLines and further removing any number of trailing lines accepted by
	 * checker.
	 * 
	 * @param checker returns true if trailing line should be elided.
	 * @param stack StringBuffer with lines to elide
	 * @param maxLines int for maximum number of resulting lines
	 */
	static void elideEndingLines(StringChecker checker, StringBuffer stack, int maxLines) {
		if (null == checker || (null == stack) || (0 == stack.length())) {
			return;
		}
		final LinkedList<String> lines = new LinkedList<String>();
		StringTokenizer st = new StringTokenizer(stack.toString(), "\n\r");
		while (st.hasMoreTokens() && (0 < --maxLines)) {
			lines.add(st.nextToken());
		}
		st = null;

		String line;
		int elided = 0;
		while (!lines.isEmpty()) {
			line = (String) lines.getLast();
			if (!checker.acceptString(line)) {
				break;
			} else {
				elided++;
				lines.removeLast();
			}
		}
		if ((elided > 0) || (maxLines < 1)) {
			final int EOL_LEN = EOL.length();
			int totalLength = 0;
			while (!lines.isEmpty()) {
				totalLength += EOL_LEN + ((String) lines.getFirst()).length();
				lines.removeFirst();
			}
			if (stack.length() > totalLength) {
				stack.setLength(totalLength);
				if (elided > 0) {
					stack.append("    (... " + elided + " lines...)");
				}
			}
		}
	}

	/** Dump message and stack to StringBuffer. */
	public static StringBuffer stackToString(Throwable throwable, boolean skipMessage) {
		if (null == throwable) {
			return new StringBuffer();
		}
		StringWriter buf = new StringWriter();
		PrintWriter writer = new PrintWriter(buf);
		if (!skipMessage) {
			writer.println(throwable.getMessage());
		}
		throwable.printStackTrace(writer);
		try {
			buf.close();
		} catch (IOException ioe) {
		} // ignored
		return buf.getBuffer();
	}

	/** @return Throwable input or tail of any wrapped exception chain */
	public static Throwable unwrapException(Throwable t) {
		Throwable current = t;
		Throwable next = null;
		while (current != null) {
			// Java 1.2 exceptions that carry exceptions
			if (current instanceof InvocationTargetException) {
				next = ((InvocationTargetException) current).getTargetException();
			} else if (current instanceof ClassNotFoundException) {
				next = ((ClassNotFoundException) current).getException();
			} else if (current instanceof ExceptionInInitializerError) {
				next = ((ExceptionInInitializerError) current).getException();
			} else if (current instanceof PrivilegedActionException) {
				next = ((PrivilegedActionException) current).getException();
			} else if (current instanceof SQLException) {
				next = ((SQLException) current).getNextException();
			}
			// ...getException():
			// javax.naming.event.NamingExceptionEvent
			// javax.naming.ldap.UnsolicitedNotification
			// javax.xml.parsers.FactoryConfigurationError
			// javax.xml.transform.TransformerFactoryConfigurationError
			// javax.xml.transform.TransformerException
			// org.xml.sax.SAXException
			// 1.4: Throwable.getCause
			// java.util.logging.LogRecord.getThrown()
			if (null == next) {
				break;
			} else {
				current = next;
				next = null;
			}
		}
		return current;
	}

	/**
	 * Replacement for Arrays.asList(..) which gacks on null and returns a List in which remove is an unsupported operation.
	 * 
	 * @param array the Object[] to convert (may be null)
	 * @return the List corresponding to array (never null)
	 */
	public static List<Object> arrayAsList(Object[] array) {
		if ((null == array) || (1 > array.length)) {
			return Collections.emptyList();
		}
		ArrayList<Object> list = new ArrayList<Object>();
		list.addAll(Arrays.asList(array));
		return list;
	}

	/** check if input contains any packages to elide. */
	public static class StringChecker {
		static StringChecker TEST_PACKAGES = new StringChecker(new String[] { "org.aspectj.testing",
				"org.eclipse.jdt.internal.junit", "junit.framework.",
				"org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner" });

		String[] infixes;

		/** @param infixes adopted */
		StringChecker(String[] infixes) {
			this.infixes = infixes;
		}

		/** @return true if input contains infixes */
		public boolean acceptString(String input) {
			boolean result = false;
			if (!LangUtil.isEmpty(input)) {
				for (int i = 0; !result && (i < infixes.length); i++) {
					result = (-1 != input.indexOf(infixes[i]));
				}
			}
			return result;
		}
	}

	/**
	 * Gen classpath.
	 * 
	 * @param bootclasspath
	 * @param classpath
	 * @param classesDir
	 * @param outputJar
	 * @return String combining classpath elements
	 */
	public static String makeClasspath( // XXX dumb implementation
			String bootclasspath, String classpath, String classesDir, String outputJar) {
		StringBuffer sb = new StringBuffer();
		addIfNotEmpty(bootclasspath, sb, File.pathSeparator);
		addIfNotEmpty(classpath, sb, File.pathSeparator);
		if (!addIfNotEmpty(classesDir, sb, File.pathSeparator)) {
			addIfNotEmpty(outputJar, sb, File.pathSeparator);
		}
		return sb.toString();
	}

	/**
	 * @param input ignored if null
	 * @param sink the StringBuffer to add input to - return false if null
	 * @param delimiter the String to append to input when added - ignored if empty
	 * @return true if input + delimiter added to sink
	 */
	private static boolean addIfNotEmpty(String input, StringBuffer sink, String delimiter) {
		if (LangUtil.isEmpty(input) || (null == sink)) {
			return false;
		}
		sink.append(input);
		if (!LangUtil.isEmpty(delimiter)) {
			sink.append(delimiter);
		}
		return true;
	}

	/**
	 * Create or initialize a process controller to run a process in another VM asynchronously.
	 * 
	 * @param controller the ProcessController to initialize, if not null
	 * @param classpath
	 * @param mainClass
	 * @param args
	 * @return initialized ProcessController
	 */
	public static ProcessController makeProcess(ProcessController controller, String classpath, String mainClass, String[] args) {
		File java = LangUtil.getJavaExecutable();
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add(java.getAbsolutePath());
		cmd.add("-classpath");
		cmd.add(classpath);
		cmd.add(mainClass);
		if (!LangUtil.isEmpty(args)) {
			cmd.addAll(Arrays.asList(args));
		}
		String[] command = (String[]) cmd.toArray(new String[0]);
		if (null == controller) {
			controller = new ProcessController();
		}
		controller.init(command, mainClass);
		return controller;
	}

	// /**
	// * Create a process to run asynchronously.
	// * @param controller if not null, initialize this one
	// * @param command the String[] command to run
	// * @param controller the ProcessControl for streams and results
	// */
	// public static ProcessController makeProcess( // not needed?
	// ProcessController controller,
	// String[] command,
	// String label) {
	// if (null == controller) {
	// controller = new ProcessController();
	// }
	// controller.init(command, label);
	// return controller;
	// }

	/**
	 * Find java executable File path from java.home system property.
	 * 
	 * @return File associated with the java command, or null if not found.
	 */
	public static File getJavaExecutable() {
		String javaHome = null;
		File result = null;
		// java.home
		// java.class.path
		// java.ext.dirs
		try {
			javaHome = System.getProperty("java.home");
		} catch (Throwable t) {
			// ignore
		}
		if (null != javaHome) {
			File binDir = new File(javaHome, "bin");
			if (binDir.isDirectory() && binDir.canRead()) {
				String[] execs = new String[] { "java", "java.exe" };
				for (int i = 0; i < execs.length; i++) {
					result = new File(binDir, execs[i]);
					if (result.canRead()) {
						break;
					}
				}
			}
		}
		return result;
	}

	// /**
	// * Sleep for a particular period (in milliseconds).
	// *
	// * @param time the long time in milliseconds to sleep
	// * @return true if delay succeeded, false if interrupted 100 times
	// */
	// public static boolean sleep(long milliseconds) {
	// if (milliseconds == 0) {
	// return true;
	// } else if (milliseconds < 0) {
	// throw new IllegalArgumentException("negative: " + milliseconds);
	// }
	// return sleepUntil(milliseconds + System.currentTimeMillis());
	// }

	/**
	 * Sleep until a particular time.
	 * 
	 * @param time the long time in milliseconds to sleep until
	 * @return true if delay succeeded, false if interrupted 100 times
	 */
	public static boolean sleepUntil(long time) {
		if (time == 0) {
			return true;
		} else if (time < 0) {
			throw new IllegalArgumentException("negative: " + time);
		}
		// final Thread thread = Thread.currentThread();
		long curTime = System.currentTimeMillis();
		for (int i = 0; (i < 100) && (curTime < time); i++) {
			try {
				Thread.sleep(time - curTime);
			} catch (InterruptedException e) {
				// ignore
			}
			curTime = System.currentTimeMillis();
		}
		return (curTime >= time);
	}

	/**
	 * Handle an external process asynchrously. <code>start()</code> launches a main thread to wait for the process and pipes
	 * streams (in child threads) through to the corresponding streams (e.g., the process System.err to this System.err). This can
	 * complete normally, by exception, or on demand by a client. Clients can implement <code>doCompleting(..)</code> to get notice
	 * when the process completes.
	 * <p>
	 * The following sample code creates a process with a completion callback starts it, and some time later retries the process.
	 * 
	 * <pre>
	 * LangUtil.ProcessController controller = new LangUtil.ProcessController() {
	 * 	protected void doCompleting(LangUtil.ProcessController.Thrown thrown, int result) {
	 * 		// signal result 
	 * 	}
	 * };
	 * controller.init(new String[] { &quot;java&quot;, &quot;-version&quot; }, &quot;java version&quot;);
	 * controller.start();
	 * // some time later...
	 * // retry...
	 * if (!controller.completed()) {
	 * 	controller.stop();
	 * 	controller.reinit();
	 * 	controller.start();
	 * }
	 * </pre>
	 * 
	 * <u>warning</u>: Currently this does not close the input or output streams, since doing so prevents their use later.
	 */
	public static class ProcessController {
		/*
		 * XXX not verified thread-safe, but should be. Known problems: - user stops (completed = true) then exception thrown from
		 * destroying process (stop() expects !completed) ...
		 */
		private String[] command;
		private String[] envp;
		private String label;

		private boolean init;
		private boolean started;
		private boolean completed;
		/** if true, stopped by user when not completed */
		private boolean userStopped;

		private Process process;
		private FileUtil.Pipe errStream;
		private FileUtil.Pipe outStream;
		private FileUtil.Pipe inStream;
		private ByteArrayOutputStream errSnoop;
		private ByteArrayOutputStream outSnoop;

		private int result;
		private Thrown thrown;

		public ProcessController() {
		}

		/**
		 * Permit re-running using the same command if this is not started or if completed. Can also call this when done with
		 * results to release references associated with results (e.g., stack traces).
		 */
		public final void reinit() {
			if (!init) {
				throw new IllegalStateException("must init(..) before reinit()");
			}
			if (started && !completed) {
				throw new IllegalStateException("not completed - do stop()");
			}
			// init everything but command and label
			started = false;
			completed = false;
			result = Integer.MIN_VALUE;
			thrown = null;
			process = null;
			errStream = null;
			outStream = null;
			inStream = null;
		}

		public final void init(String classpath, String mainClass, String[] args) {
			init(LangUtil.getJavaExecutable(), classpath, mainClass, args);
		}

		public final void init(File java, String classpath, String mainClass, String[] args) {
			LangUtil.throwIaxIfNull(java, "java");
			LangUtil.throwIaxIfNull(mainClass, "mainClass");
			LangUtil.throwIaxIfNull(args, "args");
			ArrayList<String> cmd = new ArrayList<String>();
			cmd.add(java.getAbsolutePath());
			cmd.add("-classpath");
			cmd.add(classpath);
			cmd.add(mainClass);
			if (!LangUtil.isEmpty(args)) {
				cmd.addAll(Arrays.asList(args));
			}
			init((String[]) cmd.toArray(new String[0]), mainClass);
		}

		public final void init(String[] command, String label) {
			this.command = (String[]) LangUtil.safeCopy(command, new String[0]);
			if (1 > this.command.length) {
				throw new IllegalArgumentException("empty command");
			}
			this.label = LangUtil.isEmpty(label) ? command[0] : label;
			init = true;
			reinit();
		}

		public final void setEnvp(String[] envp) {
			this.envp = (String[]) LangUtil.safeCopy(envp, new String[0]);
			if (1 > this.envp.length) {
				throw new IllegalArgumentException("empty envp");
			}
		}

		public final void setErrSnoop(ByteArrayOutputStream snoop) {
			errSnoop = snoop;
			if (null != errStream) {
				errStream.setSnoop(errSnoop);
			}
		}

		public final void setOutSnoop(ByteArrayOutputStream snoop) {
			outSnoop = snoop;
			if (null != outStream) {
				outStream.setSnoop(outSnoop);
			}
		}

		/**
		 * Start running the process and pipes asynchronously.
		 * 
		 * @return Thread started or null if unable to start thread (results available via <code>getThrown()</code>, etc.)
		 */
		public final Thread start() {
			if (!init) {
				throw new IllegalStateException("not initialized");
			}
			synchronized (this) {
				if (started) {
					throw new IllegalStateException("already started");
				}
				started = true;
			}
			try {
				process = Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				stop(e, Integer.MIN_VALUE);
				return null;
			}
			errStream = new FileUtil.Pipe(process.getErrorStream(), System.err);
			if (null != errSnoop) {
				errStream.setSnoop(errSnoop);
			}
			outStream = new FileUtil.Pipe(process.getInputStream(), System.out);
			if (null != outSnoop) {
				outStream.setSnoop(outSnoop);
			}
			inStream = new FileUtil.Pipe(System.in, process.getOutputStream());
			// start 4 threads, process & pipes for in, err, out
			Runnable processRunner = new Runnable() {
				public void run() {
					Throwable thrown = null;
					int result = Integer.MIN_VALUE;
					try {
						// pipe threads are children
						new Thread(errStream).start();
						new Thread(outStream).start();
						new Thread(inStream).start();
						process.waitFor();
						result = process.exitValue();
					} catch (Throwable e) {
						thrown = e;
					} finally {
						stop(thrown, result);
					}
				}
			};
			Thread result = new Thread(processRunner, label);
			result.start();
			return result;
		}

		/**
		 * Destroy any process, stop any pipes. This waits for the pipes to clear (reading until no more input is available), but
		 * does not wait for the input stream for the pipe to close (i.e., not waiting for end-of-file on input stream).
		 */
		public final synchronized void stop() {
			if (completed) {
				return;
			}
			userStopped = true;
			stop(null, Integer.MIN_VALUE);
		}

		public final String[] getCommand() {
			String[] toCopy = command;
			if (LangUtil.isEmpty(toCopy)) {
				return new String[0];
			}
			String[] result = new String[toCopy.length];
			System.arraycopy(toCopy, 0, result, 0, result.length);
			return result;
		}

		public final boolean completed() {
			return completed;
		}

		public final boolean started() {
			return started;
		}

		public final boolean userStopped() {
			return userStopped;
		}

		/**
		 * Get any Throwable thrown. Note that the process can complete normally (with a valid return value), at the same time the
		 * pipes throw exceptions, and that this may return some exceptions even if the process is not complete.
		 * 
		 * @return null if not complete or Thrown containing exceptions thrown by the process and streams.
		 */
		public final Thrown getThrown() { // cache this
			return makeThrown(null);
		}

		public final int getResult() {
			return result;
		}

		/**
		 * Subclasses implement this to get synchronous notice of completion. All pipes and processes should be complete at this
		 * time. To get the exceptions thrown for the pipes, use <code>getThrown()</code>. If there is an exception, the process
		 * completed abruptly (including side-effects of the user halting the process). If <code>userStopped()</code> is true, then
		 * some client asked that the process be destroyed using <code>stop()</code>. Otherwise, the result code should be the
		 * result value returned by the process.
		 * 
		 * @param thrown same as <code>getThrown().fromProcess</code>.
		 * @param result same as <code>getResult()</code>
		 * @see getThrown()
		 * @see getResult()
		 * @see stop()
		 */
		protected void doCompleting(Thrown thrown, int result) {
		}

		/**
		 * Handle termination (on-demand, abrupt, or normal) by destroying and/or halting process and pipes.
		 * 
		 * @param thrown ignored if null
		 * @param result ignored if Integer.MIN_VALUE
		 */
		private final synchronized void stop(Throwable thrown, int result) {
			if (completed) {
				throw new IllegalStateException("already completed");
			} else if (null != this.thrown) {
				throw new IllegalStateException("already set thrown: " + thrown);
			}
			// assert null == this.thrown
			this.thrown = makeThrown(thrown);
			if (null != process) {
				process.destroy();
			}
			if (null != inStream) {
				inStream.halt(false, true); // this will block if waiting
				inStream = null;
			}
			if (null != outStream) {
				outStream.halt(true, true);
				outStream = null;
			}
			if (null != errStream) {
				errStream.halt(true, true);
				errStream = null;
			}
			if (Integer.MIN_VALUE != result) {
				this.result = result;
			}
			completed = true;
			doCompleting(this.thrown, result);
		}

		/**
		 * Create snapshot of Throwable's thrown.
		 * 
		 * @param thrown ignored if null or if this.thrown is not null
		 */
		private final synchronized Thrown makeThrown(Throwable processThrown) {
			if (null != thrown) {
				return thrown;
			}
			return new Thrown(processThrown, (null == outStream ? null : outStream.getThrown()), (null == errStream ? null
					: errStream.getThrown()), (null == inStream ? null : inStream.getThrown()));
		}

		public static class Thrown {
			public final Throwable fromProcess;
			public final Throwable fromErrPipe;
			public final Throwable fromOutPipe;
			public final Throwable fromInPipe;
			/** true only if some Throwable is not null */
			public final boolean thrown;

			private Thrown(Throwable fromProcess, Throwable fromOutPipe, Throwable fromErrPipe, Throwable fromInPipe) {
				this.fromProcess = fromProcess;
				this.fromErrPipe = fromErrPipe;
				this.fromOutPipe = fromOutPipe;
				this.fromInPipe = fromInPipe;
				thrown = ((null != fromProcess) || (null != fromInPipe) || (null != fromOutPipe) || (null != fromErrPipe));
			}

			public String toString() {
				StringBuffer sb = new StringBuffer();
				append(sb, fromProcess, "process");
				append(sb, fromOutPipe, " stdout");
				append(sb, fromErrPipe, " stderr");
				append(sb, fromInPipe, "  stdin");
				if (0 == sb.length()) {
					return "Thrown (none)";
				} else {
					return sb.toString();
				}
			}

			private void append(StringBuffer sb, Throwable thrown, String label) {
				if (null != thrown) {
					sb.append("from " + label + ": ");
					sb.append(LangUtil.renderExceptionShort(thrown));
					sb.append(LangUtil.EOL);
				}
			}
		} // class Thrown
	}

}
