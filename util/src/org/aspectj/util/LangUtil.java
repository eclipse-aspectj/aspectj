/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.util;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 */
public class LangUtil {    
    /** map from String version to String class implemented in that version or later */
    private static final Map VM_CLASSES;

    public static final String EOL;
    static {
        StringWriter buf = new StringWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println("");
        String eol = "\n";
        try { 
            buf.close(); 
            StringBuffer sb = buf.getBuffer(); 
            if ((null != sb) || (0 < sb.length())) {
                eol = buf.toString();
            }
        } catch (Throwable t) { }
        EOL = eol;
        
        HashMap map = new HashMap();
        map.put("1.2", "java.lang.ref.Reference");
        map.put("1.3", "java.lang.reflect.Proxy");
        map.put("1.4", "java.nio.Buffer");
        
        VM_CLASSES = Collections.unmodifiableMap(map);
    }

    /**
     * Detect whether Java version is supported.
     * @param version String "1.2" or "1.3" or "1.4"
     * @return true if the currently-running VM supports the version 
     * @throws IllegalArgumentException if version is not known
     */
    public static final boolean supportsJava(String version) {
        LangUtil.throwIaxIfNull(version, "version");
        String className = (String) VM_CLASSES.get(version);
        if (null == className) {
            throw new IllegalArgumentException("unknown version: " + version);
        }
        try {
            Class.forName(className);
            return true;
        } catch (Throwable t) {
            return false;
        }        
    }
    
    /**
     * Shorthand for "if null, throw IllegalArgumentException"
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
     * @param c the Class to check - use null to ignore type check
     * @throws IllegalArgumentException "null {name}" if o is null 
     */
    public static final void throwIaxIfNotAssignable(final Object ra[], final Class c, final String name) {
        throwIaxIfNull(ra, name);
        String label = (null == name ? "input" : name);
        for (int i = 0; i < ra.length; i++) {
            if (null == ra[i]) {
                String m = " null " + label + "[" + i + "]";
                throw new IllegalArgumentException(m);
            } else if (null != c) {
                Class actualClass = ra[i].getClass();
                if (!c.isAssignableFrom(actualClass)) {
                    String message = label + " not assignable to " + c.getName();
                    throw new IllegalArgumentException(message);           
                }
            }
        }
    }
    /**
     * Shorthand for "if not null or not assignable, throw IllegalArgumentException"
     * @throws IllegalArgumentException "null {name}" if o is null 
     */
    public static final void throwIaxIfNotAssignable(final Object o, final Class c, final String name) {
        throwIaxIfNull(o, name);
        if (null != c) {
            Class actualClass = o.getClass();
            if (!c.isAssignableFrom(actualClass)) {
                String message = name + " not assignable to " + c.getName();
                throw new IllegalArgumentException(message);           
            }
        }
    }
    
    /**
     * Shorthand for "if any not null or not assignable, throw IllegalArgumentException"
     * @throws IllegalArgumentException "{name} is not assignable to {c}"  
     */
    public static final void throwIaxIfNotAllAssignable(final Collection collection, 
        final Class c, final String name) {
        throwIaxIfNull(collection, name);
        if (null != c) {
            for (Iterator iter = collection.iterator(); iter.hasNext();) {
				throwIaxIfNotAssignable(iter.next(), c, name);
				
			}
        }
    }
    /**
     * Shorthand for "if false, throw IllegalArgumentException"
     * @throws IllegalArgumentException "{message}" if test is false
     */
    public static final void throwIaxIfFalse(final boolean test, final String message) {
        if (!test) {
            throw new IllegalArgumentException(message);
        }
    }
    
    /** @return ((null == s) || (0 == s.trim().length())); */
    public static boolean isEmptyTrimmed(String s) {
        return ((null == s) || (0 == s.length())
            || (0 == s.trim().length()));
    }

    /** @return ((null == s) || (0 == s.length())); */
    public static boolean isEmpty(String s) {
        return ((null == s) || (0 == s.length()));
    }

    /** @return ((null == ra) || (0 == ra.length)) */
    public static boolean isEmpty(Object[] ra) {
        return ((null == ra) || (0 == ra.length));
    }

    /** @return ((null == collection) || (0 == collection.size())) */
    public static boolean isEmpty(Collection collection) {
        return ((null == collection) || (0 == collection.size()));
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
     * Splits <code>input</code> at commas, 
     * trimming any white space.
     *
     * @param text <code>String</code> to split.
     * @return List of String of elements.
     */
    public static List commaSplit(String input) {
        return anySplit(input, ",");
    }
    
    /**
     * Split string as classpath, delimited at File.pathSeparator.
     * Entries are not trimmed, but empty entries are ignored.
     * @param classpath the String to split - may be null or empty
     * @return String[] of classpath entries
     */
    public static String[] splitClasspath(String classpath) {
        if (LangUtil.isEmpty(classpath)) {
            return new String[0];
        }
        StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
        ArrayList result = new ArrayList(st.countTokens());
        while (st.hasMoreTokens()) {
            String entry = st.nextToken();
            if (!LangUtil.isEmpty(entry)) {
                result.add(entry);
            }
        }
        return (String[]) result.toArray(new String[0]);
    }
    
    /**
     * Splits <code>input</code>, removing delimiter and 
     * trimming any white space.
     * Returns an empty collection if the input is null.
     * If delimiter is null or empty or if the input contains
     * no delimiters, the input itself is returned
     * after trimming white space.
     *
     * @param text <code>String</code> to split.
     * @param delimiter <code>String</code> separators for input.
     * @return List of String of elements.
     */
    public static List anySplit(String input, String delim) {
        if (null == input) {
            return Collections.EMPTY_LIST;
        } 
        ArrayList result = new ArrayList();
        
        if (LangUtil.isEmpty(delim)
            || (-1 == input.indexOf(delim))) {
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
	 * Splits strings into a <code>List</code> using a
	 * <code>StringTokenizer</code>.
	 *
	 * @param text <code>String</code> to split.
	 */
	public static List strings(String text) {
        if (LangUtil.isEmpty(text)) {
            return Collections.EMPTY_LIST;
        }
		List strings = new ArrayList();
		StringTokenizer tok = new StringTokenizer(text);
		while (tok.hasMoreTokens()) {
			strings.add(tok.nextToken());
		}
		return strings;
	}

	/** @return a non-null unmodifiable List */
	public static List safeList(List list) {
		return (
			null == list
				? Collections.EMPTY_LIST
				: Collections.unmodifiableList(list));
	}
    
    /**
     * Select from input String[] based on suffix-matching
     * @param inputs String[] of input - null ignored
     * @param suffixes String[] of suffix selectors - null ignored
     * @param ignoreCase if true, ignore case
     * @return String[] of input that end with any input
     */
    public static String[] endsWith(String[] inputs, String[] suffixes, boolean ignoreCase) {
        if (LangUtil.isEmpty(inputs) || LangUtil.isEmpty(suffixes)) {
            return new String[0];
        }
        if (ignoreCase) {
            String[] temp = new String[suffixes.length];
            for (int i = 0; i < temp.length; i++) {                
				String suff = suffixes[i];
                temp[i] = (null ==  suff ? null : suff.toLowerCase());
			}
            suffixes = temp;
        }
        ArrayList result = new ArrayList();
        for (int i = 0; i < inputs.length; i++) {
            String input = inputs[i];
            if (null == input) {
                continue;
            }
            if (!ignoreCase) {
                input = input.toLowerCase();
            }
            for (int j = 0; j < suffixes.length; j++) {
                String suffix = suffixes[j];
                if (null == suffix) {
                    continue;
                }
                if (input.endsWith(suffix)) {
                    result.add(input);
                    break;
                }
            }
        }
        return (String[]) result.toArray(new String[0]);
    }
    
    /** 
     * copy non-null two-dimensional String[][] 
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
     * Extract options and arguments to input option list, returning remainder.
     * The input options will be nullified if not found.  e.g., 
     * <pre>String[] options = new String[][] { new String[] { "-verbose" },
     *     new String[] { "-classpath", null } };
     * String[] args = extractOptions(args, options);
     * boolean verbose = null != options[0][0];
     * boolean classpath = options[1][1];</pre>
     * @param args the String[] input options
     * @param options the String[][]options to find in the input args - not null
     *         for each String[] component the first subcomponent is the option itself,
     *         and there is one String subcomponent for each additional argument.
     * @return String[] of args remaining after extracting options to extracted 
     */
    public static String[] extractOptions(String[] args, String[][] options) {
        if (LangUtil.isEmpty(args) || LangUtil.isEmpty(options) ) {
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
                if (found = sought.equals(args[j])) {                    
                    foundSet.set(i);
                    int doMore = option.length-1;
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
    
    /** 
     * Extract options and arguments to input parameter list, returning remainder.
     * @param args the String[] input options
     * @param validOptions the String[] options to find in the input args - not null
     * @param optionArgs the int[] number of arguments for each option in validOptions
     *         (if null, then no arguments for any option)
     * @param extracted the List for the matched options
     * @return String[] of args remaining after extracting options to extracted 
     */
    public static String[] extractOptions(String[] args, String[] validOptions,
        int[] optionArgs, List extracted) {
        if (LangUtil.isEmpty(args) 
            || LangUtil.isEmpty(validOptions) ) {
            return args;
        }
        if (null != optionArgs) {
            if (optionArgs.length != validOptions.length) {
                throw new IllegalArgumentException("args must match options");
            }
        }
        String[] result = new String[args.length];
        int resultIndex = 0;
        for (int j = 0; j < args.length; j++) {
            boolean found = false;
            for (int i = 0; !found && (i < validOptions.length); i++) {
                String sought = validOptions[i];
                int doMore = (null == optionArgs ? 0 : optionArgs[i]);
                if (LangUtil.isEmpty(sought)) {
                    continue;
                }
                if (found = sought.equals(args[j])) {                    
                    if (null != extracted) {
                        extracted.add(sought);
                    }
                    if (0 < doMore) {
                        final int MAX = j + doMore;
                        if (MAX >= args.length) {
                            String s = "expecting " + doMore + " args after ";
                            throw new IllegalArgumentException(s + args[j]);
                        }
                        if (null != extracted) {                            
                            while (j < MAX) {
                                extracted.add(args[++j]);
                            }
                        } else {
                            j = MAX;
                        }                        
                    }
                    break;
                }
            }
            if (!found) {
                result[resultIndex++] = args[j];
            }
        }
        if (resultIndex < args.length) {
            String[] temp = new String[resultIndex];
            System.arraycopy(result, 0, temp, 0, resultIndex);
            args = temp;
        }
        return args;        
    }

    /** @return String[] of entries in validOptions found in args */
    public static String[] selectOptions(String[] args, String[] validOptions) {
        if (LangUtil.isEmpty(args) || LangUtil.isEmpty(validOptions)) {
            return new String[0];
        }
        ArrayList result = new ArrayList();
        for (int i = 0; i < validOptions.length; i++) {
            String sought = validOptions[i];
            if (LangUtil.isEmpty(sought)) {
                continue;
            }
			for (int j = 0; j < args.length; j++) {
				if (sought.equals(args[j])) {
                    result.add(sought);
                    break;
                }
			}
		}
        return (String[]) result.toArray(new String[0]);
    }
    
    /** @return String[] of entries in validOptions found in args */
    public static String[] selectOptions(List args, String[] validOptions) {
        if (LangUtil.isEmpty(args) || LangUtil.isEmpty(validOptions)) {
            return new String[0];
        }
        ArrayList result = new ArrayList();
        for (int i = 0; i < validOptions.length; i++) {
            String sought = validOptions[i];
            if (LangUtil.isEmpty(sought)) {
                continue;
            }
            for (Iterator iter = args.iterator(); iter.hasNext();) {
				String arg = (String) iter.next();
                if (sought.equals(arg)) {
                    result.add(sought);
                    break;
                }
            }
        }
        return (String[]) result.toArray(new String[0]);
    }
    
    /**
     * Generate variants of String[] options by creating an extra set for
     * each option that ends with "-".  If none end with "-", then an
     * array equal to <code>new String[][] { options }</code> is returned;
     * if one ends with "-", then two sets are returned,
     * three causes eight sets, etc.
     * @return String[][] with each option set.
     * @throws IllegalArgumentException if any option is null or empty.
     */
    public static String[][] optionVariants(String[] options) {
        if ((null == options) || (0 == options.length)) {
            return new String[][] { new String[0]};            
        }
        // be nice, don't stomp input
        String[] temp = new String[options.length];
        System.arraycopy(options, 0, temp, 0, temp.length);
        options = temp;
        boolean[] dup = new boolean[options.length];
        int numDups = 0;
        
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            if (LangUtil.isEmpty(option)) {
                throw new IllegalArgumentException("empty option at " + i);
            }
            if (option.endsWith("-")) {
                options[i] = option.substring(0, option.length()-1);
                dup[i] = true;
                numDups++;
            }
        }
        final String[] NONE = new String[0];
        final int variants = exp(2, numDups);
        final String[][] result = new String[variants][];
        // variant is a bitmap wrt doing extra value when dup[k]=true
        for (int variant = 0; variant < variants; variant++) { 
            ArrayList next = new ArrayList();
            int nextOption = 0;
            for (int k = 0; k < options.length; k++) {
                if (!dup[k] || (0 != (variant & (1 << (nextOption++))))) {
                    next.add(options[k]);
                }                   
            }
            result[variant] = (String[]) next.toArray(NONE);
        }
        return result;
    }
    
    private static int exp(int base, int power) { // not in Math?
        if (0 > power) {
            throw new IllegalArgumentException("negative power: " + power);
        } 
        int result = 1;
        while (0 < power--) {
            result *= base;
        }
        return result;
    }

    /**
     * Make a copy of the array.
     * @return an array with the same component type as source
     * containing same elements, even if null.
     * @throws IllegalArgumentException if source is null
     */
    public static final Object[] copy(Object[] source) {
        LangUtil.throwIaxIfNull(source, "source");        
        final Class c = source.getClass().getComponentType();
        Object[] result = (Object[]) Array.newInstance(c, source.length);
        System.arraycopy(source, 0, result, 0, result.length);
        return result;
    }
    
    
    /**
     * Convert arrays safely.  The number of elements in the result
     * will be 1 smaller for each element that is null or not assignable.
     * This will use sink if it has exactly the right size.
     * The result will always have the same component type as sink.
     * @return an array with the same component type as sink
     * containing any assignable elements in source (in the same order).
     * @throws IllegalArgumentException if either is null
     */
    public static Object[] safeCopy(Object[] source, Object[] sink) {
        final Class sinkType = (null == sink 
                                ? Object.class 
                                : sink.getClass().getComponentType());
        final int sourceLength = (null == source ? 0 : source.length);
        final int sinkLength = (null == sink ? 0 : sink.length);
        
        final int resultSize;
        ArrayList result = null;
        if (0 == sourceLength) {
            resultSize = 0;
        } else {
            result = new ArrayList(sourceLength);
            for (int i = 0; i < sourceLength; i++) {
                if ((null != source[i])
                    && (sinkType.isAssignableFrom(source[i].getClass()))) {
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
    public static String unqualifiedClassName(Class c) {
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
                result.append(in.substring(start, loc));
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
            int topad = width-size;
            result = pad.substring(0, topad) + result;
        }
        return result;
    }

    /** clip StringBuffer to maximum number of lines */
    static String clipBuffer(StringBuffer buffer, int maxLines) {
        if ((null == buffer) || (1 > buffer.length())) return "";
        StringBuffer result = new StringBuffer();
        int j = 0;
        final int MAX = maxLines;
        final int N = buffer.length();
        for (int i = 0, srcBegin = 0; i < MAX; srcBegin += j) {
            // todo: replace with String variant if/since getting char?
            char[] chars = new char[128];
            int srcEnd = srcBegin+chars.length;
            if (srcEnd >= N) {
                srcEnd = N-1;
            }
            if (srcBegin == srcEnd) break;
            //log("srcBegin:" + srcBegin + ":srcEnd:" + srcEnd);
            buffer.getChars(srcBegin, srcEnd, chars, 0);            
            for (j = 0; j < srcEnd-srcBegin/*chars.length*/; j++) {
                char c = chars[j];
                if (c == '\n') {
                    i++;
                    j++;
                    break;
                }
            }
            try { result.append(chars, 0, j); } 
            catch (Throwable t) { }
        }
        return result.toString();
    }

    /**
     * @return "({UnqualifiedExceptionClass}) {message}"
     */
    public static String renderExceptionShort(Throwable e) {
        if (null == e)
            return "(Throwable) null";
        return "(" + LangUtil.unqualifiedClassName(e) + ") " + e.getMessage();
    }

   /**
     * Renders exception <code>t</code> after unwrapping and 
     * eliding any test packages.
     * @param t <code>Throwable</code> to print.
     * @see   #maxStackTrace
     */
    public static String renderException(Throwable t) { 
        return renderException(t, true);
    }
    
   /**
     * Renders exception <code>t</code>, unwrapping,
     * optionally eliding and limiting total number of lines.
     * @param t <code>Throwable</code> to print.
     * @param elide true to limit to 100 lines and elide test packages
     * @see StringChecker#TEST_PACKAGES
     */
    public static String renderException(Throwable t, boolean elide) {
        if (null == t) return "null throwable";
        t = unwrapException(t);
        StringBuffer stack = stackToString(t, false);
        if (elide) {
            elideEndingLines(StringChecker.TEST_PACKAGES, stack, 100);
        }
        return stack.toString();
    }

    /**
     * Trim ending lines from a StringBuffer,
     * clipping to maxLines and further removing any number of
     * trailing lines accepted by checker.
     * @param checker returns true if trailing line should be elided.
     * @param stack StringBuffer with lines to elide
     * @param maxLines int for maximum number of resulting lines
     */
    static void elideEndingLines(StringChecker checker, StringBuffer stack, int maxLines) {
        if (null == checker || (null == stack) || (0 == stack.length())) {
            return;
        }
        final LinkedList lines = new LinkedList();
        StringTokenizer st = new StringTokenizer(stack.toString(),"\n\r");
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
        try { buf.close(); } 
        catch (IOException ioe) {} //  ignored
        return buf.getBuffer(); 
    }

    
    /** @return Throwable input or tail of any wrapped exception chain */
    public static Throwable unwrapException(Throwable t) {
        if (t instanceof InvocationTargetException) {
            Throwable thrown = ((InvocationTargetException) t).getTargetException();
            if (null != thrown) {
                return unwrapException(thrown);
            }
        } else if (t instanceof ClassNotFoundException) {
            Throwable thrown = ((ClassNotFoundException) t).getException();
            if (null != thrown) {
                return unwrapException(thrown);
            }
        }
        // ChainedException
        // ExceptionInInitializerError
        return t;
    }

	/**
     * Replacement for Arrays.asList(..) which gacks on null
     * and returns a List in which remove is an unsupported operation.
     * @param array the Object[] to convert (may be null)
     * @return the List corresponding to array (never null)
     */
    public static List arrayAsList(Object[] array) {
        if ((null == array) || (1 > array.length)) {
            return Collections.EMPTY_LIST;
        }
        ArrayList list = new ArrayList();
        list.addAll(Arrays.asList(array));
        return list;
    }

       

    
    /** check if input contains any packages to elide. */
    public static class StringChecker {
        static StringChecker TEST_PACKAGES = new StringChecker( new String[] 
            { "org.aspectj.testing.",
              "org.eclipse.jdt.internal.junit",
              "junit.framework.",
              "org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner"
            }); 
            
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
}
