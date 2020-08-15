/* *******************************************************************
 * Copyright (c) 1999-2000 Xerox Corporation. 
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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;


/** 
 * misc lang utilities
 */
public class LangUtil {

    /** Delimiter used by split(String) (and ArrayList.toString()?) */
    public static final String SPLIT_DELIM = ", ";

    /** prefix used by split(String) (and ArrayList.toString()?) */
    public static final String SPLIT_START = "[";

    /** suffix used by split(String) (and ArrayList.toString()?) */
    public static final String SPLIT_END = "]";

    /** system-dependent classpath separator */
    public static final String CLASSPATH_SEP;

    private static final String[] NONE = new String[0];
    
    /** bad: hard-wired unix, windows, mac path separators */
    private static final char[] SEPS = new char[] { '/', '\\', ':' };
    
    static {
        // XXX this has to be the wrong way to get system-dependent classpath separator
        String ps = ";";
        try {
            ps = System.getProperty("path.separator");
            if (null == ps) {
                ps = ";";
                String cp = System.getProperty("java.class.path");
                if (null != cp) {
                    if (cp.contains(";")) {
                        ps = ";";
                    } else if (cp.contains(":")) {
                        ps = ":";
                    }
                    // else warn?
                }
            }
        } catch (Throwable t) { // ignore
        } finally {
            CLASSPATH_SEP = ps;
        }
    }  
    
	/**
     * @return input if any are empty or no target in input,
     *           or input with escape prefixing all original target
     */
    public static String escape(String input, String target, String escape) {
        if (isEmpty(input) || isEmpty(target) || isEmpty(escape)) {
            return input;
        }
        StringBuffer sink = new StringBuffer();
        escape(input, target, escape, sink);
        return sink.toString();
    }
    
 
	/** 
     * Append escaped input to sink.
     * Cheap form of arbitrary escaping does not escape the escape String
     * itself, but unflatten treats it as significant only before the target.
     * (so this fails with input that ends with target).
     */
    public static void escape(String input, String target, String escape, StringBuffer sink) {
        if ((null == sink) || isEmpty(input) || isEmpty(target) || isEmpty(escape)) {
            return;
        } else if (!input.contains(target)) { // avoid StringTokenizer construction
            sink.append(input);
            return;
        }
        throw new Error("unimplemented");
    }

    /** flatten list per spec to sink */
    public static void flatten(List list, FlattenSpec spec, StringBuffer sink) {
        throwIaxIfNull(spec, "spec");
        final FlattenSpec s = spec;
        flatten(list, s.prefix, s.nullFlattened, s.escape, s.delim, s.suffix, sink);
    }

	/**
     * Flatten a List to String by first converting to String[] 
     * (using toString() if the elements are not already String)
     * and calling flatten(String[]...).
     */
    public static void flatten(
        List list, 
        String prefix, 
        String nullFlattened, 
        String escape,
        String delim, 
        String suffix,
        StringBuffer sink) {
        throwIaxIfNull(list, "list");
        Object[] ra = list.toArray();
        String[] result;
        if (String.class == ra.getClass().getComponentType()) {
            result = (String[]) ra;
        } else {
            result = new String[ra.length];
            for (int i = 0; i < result.length; i++) {
                if (null != ra[i]) {
                    result[i] = ra[i].toString();
                }
            }
        }
        flatten(result, prefix, nullFlattened, escape, delim, suffix, sink);
    }




	/** flatten String[] per spec to sink */
    public static void flatten(String[] input, FlattenSpec spec, StringBuffer sink) {
        throwIaxIfNull(spec, "spec");
        final FlattenSpec s = spec;
        flatten(input, s.prefix, s.nullFlattened, s.escape, s.delim,s.suffix, sink);
    }
    



	/**
     * Flatten a String[] to String by writing strings to sink,
     * prefixing with leader (if not null),
     * using nullRendering for null entries (skipped if null),
     * escaping any delim in entry by prefixing with escape (if not null),
     * separating entries with delim (if not null),
     * and suffixing with trailer (if not null).
     * Note that nullFlattened is not processed for internal delim,
     * and strings is not copied before processing.
     * @param strings the String[] input - not null
     * @param prefix the output starts with this if not null
     * @param nullFlattened the output of a null entry - entry is skipped (no delim) if null
     * @param escape any delim in an item will be prefixed by escape if not null
     * @param delim two items in the output will be separated by delim if not null
     * @param suffix the output ends with this if not null
     * @param sink the StringBuffer to use for output
     * @return null if sink is not null (results added to sink) or rendering otherwise
     */
    public static void flatten(
        String[] strings, 
        String prefix, 
        String nullFlattened, 
        String escape,
        String delim, 
        String suffix,
        StringBuffer sink) {
        throwIaxIfNull(strings, "strings");
        if (null == sink) {
            return;
        }
        final boolean haveDelim = (!isEmpty(delim));
        final boolean haveNullFlattened = (null != nullFlattened);
        final boolean escaping = (haveDelim && (null != escape));
        final int numStrings = (null == strings ? 0 : strings.length);
        if (null != prefix) {
            sink.append(prefix);
        }
        for (int i = 0; i < numStrings; i++) {
			String s = strings[i];
            if (null == s) {
                if (!haveNullFlattened) {
                    continue;
                }
                if (haveDelim && (i > 0)) {
                    sink.append(delim);
                }
                sink.append(nullFlattened);
            } else {
                if (haveDelim && (i > 0)) {
                    sink.append(delim);
                }
                
                if (escaping) {
                    escape(s, delim, escape, sink);
                } else {
                    sink.append(s);
                }
            }
		}
        if (null != suffix) {
            sink.append(suffix);
        }
    }
	
    /**
     * Get indexes of any invalid entries in array.
     * @param ra the Object[] entries to check
     *  (if null, this returns new int[] { -1 })
     * @param superType the Class, if any, to verify that
     *         any entries are assignable.
     * @return null if all entries are non-null, assignable to superType
     * or comma-delimited error String, with components 
     *     <code>"[#] {null || not {superType}"</code>,
     * e.g., "[3] null, [5] not String"
     */
    public static String invalidComponents(Object[] ra, Class superType) {
        if (null == ra) {
            return "null input array";
        } else if (0 == ra.length) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        final String cname = LangUtil.unqualifiedClassName(superType);
//        int index = 0;
        for (int i = 0; i < ra.length; i++) {
			if (null == ra[i]) {
                result.append(", [" + i + "] null");
            } else if ((null != superType) 
                && !superType.isAssignableFrom(ra[i].getClass())) {
                result.append(", [" + i + "] not " + cname);
            }
		}
        if (0 == result.length()) {
            return null;
        } else {
            return result.toString().substring(2);
        }
    }
	
    /** @return ((null == ra) || (0 == ra.length)) */
    public static boolean isEmpty(Object[] ra) {
        return ((null == ra) || (0 == ra.length));
    }

	/** @return ((null == s) || (0 == s.length())); */
    public static boolean isEmpty(String s) {
        return ((null == s) || (0 == s.length()));
    }

     
    /**
     * Throw IllegalArgumentException if any component in input array
     * is null or (if superType is not null) not assignable to superType.
     * The exception message takes the form 
     * <code>{name} invalid entries: {invalidEntriesResult}</code>
     * @throws IllegalArgumentException if any components bad
     * @see #invalidComponents(Object[], Class)
     */
    public static final void throwIaxIfComponentsBad(
        final Object[] input, 
        final String name,
        final Class superType) {
        String errs = invalidComponents(input, superType);
        if (null != errs) {
            String err = name + " invalid entries: " + errs;
            throw new IllegalArgumentException(err);
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

	public static ArrayList unflatten(String input, FlattenSpec spec) {
        throwIaxIfNull(spec, "spec");
        final FlattenSpec s = spec;
        return unflatten(input,s.prefix, s.nullFlattened, s.escape, s.delim, s.suffix, s.emptyUnflattened);
    }

	/**
     * Unflatten a String to String[] by separating elements at delim,
     * handling prefixes, suffixes, escapes, etc.
     * Any prefix or suffix is stripped from the input
     * (or, if not found, an IllegalArgumentException is thrown).
     * If delim is null or empty or input contains no delim, 
     * then return new String[] {stripped input}.
     * 
     * XXX fix comments
     * prefixing with leader (if not null),
     * using nullRendering for null entries (skipped if null),
     * escaping any delim in entry by prefixing with escape (if not null),
     * separating entries with delim (if not null),
     * and suffixing with trailer (if not null).
     * Note that nullRendering is not processed for internal delim,
     * and strings is not copied before processing.
     * @param strings the String[] input - not null
     * @param prefix the output starts with this if not null
     * @param nullRendering the output of a null entry - entry is skipped (no delim) if null
     * @param escape any delim in an item will be prefixed by escape if not null
     * @param delim two items in the output will be separated by delim if not null
     * @param suffix the output ends with this if not null
     * @param sink the StringBuffer to use for output
     * @return null if sink is not null (results added to sink) or rendering otherwise
     * @throws IllegalArgumentException if input is null
     *          or if any prefix does not start the input
     *          or if any suffix does not end the input
     */
    public static ArrayList unflatten(
        String input, 
        String prefix, 
        String nullFlattened, 
        String escape,
        String delim, 
        String suffix,
        String emptyUnflattened) {
        throwIaxIfNull(input, "input");
        final boolean haveDelim = (!isEmpty(delim));
//        final boolean haveNullFlattened = (null != nullFlattened);
//        final boolean escaping = (haveDelim && (null != escape));
        if (!isEmpty(prefix)) {
            if (input.startsWith(prefix)) {
                input = input.substring(prefix.length());
            } else {
                String s = "expecting \"" + prefix + "\" at start of " + input + "\"";
                throw new IllegalArgumentException(s);
            }
        }
        if (!isEmpty(suffix)) {
            if (input.endsWith(suffix)) {
                input = input.substring(0, input.length() - suffix.length());
            } else {
                String s = "expecting \"" + suffix + "\" at end of " + input + "\"";
                throw new IllegalArgumentException(s);
            }
        }

        final ArrayList result = new ArrayList();
        if (isEmpty(input)) {
            return result;
        }
        if ((!haveDelim) || (!input.contains(delim))) {
            result.add(input);
            return result;
        }
        
        StringTokenizer st = new StringTokenizer(input, delim, true);
//        StringBuffer cur = new StringBuffer();
//        boolean lastEndedWithEscape = false;
//        boolean lastWasDelim = false;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            System.out.println("reading " + token);
            if (delim.equals(token)) {
            } else {
                result.add(token);
            }
        }
        return result;
    }
      
    /** combine two string arrays, removing null and duplicates 
     * @return concatenation of both arrays, less null in either or dups in two
     * @see Util#combine(Object[], Object[])
     */
    public static String[] combine(String[] one, String[] two) {
		List twoList = new ArrayList(org.aspectj.util.LangUtil.arrayAsList(two));
        ArrayList result = new ArrayList();
        if (null != one) {
			for (String s : one) {
				if (null != s) {
					twoList.remove(s);
					result.add(s);
				}
			}
        }
		for (Object o : twoList) {
			String element = (String) o;
			if (null != element) {
				result.add(element);
			}
		}
        return (String[]) result.toArray(NONE);
    }
    
    public static Properties combine(Properties dest, Properties add, boolean respectExisting)  { // XXX
        if (null == add) return dest;
        if (null == dest) return add;
		for (Object o : add.keySet()) {
			String key = (String) o;
			if (null == key) {
				continue;
			}
			String value = add.getProperty(key);
			if (null == value) {
				continue;
			}
			if (!respectExisting || (null == dest.getProperty(key))) {
				dest.setProperty(key, value);
			}
		}
        return dest;
    }

    public static List arrayAsList(Object[] ra) {
        return org.aspectj.util.LangUtil.arrayAsList(ra);
    }
    
    /**
     * return the fully-qualified class names
     * inferred from the file names in dir
     * assuming dir is the root of the source tree
     * and class files end with ".class".
     * @throws Error if dir is not properly named as prefix
     *               of class files found in dir.
     */
    public static String[] classesIn(File dir) {
        boolean alwaysTrue = true;
        FileFilter filter = ValidFileFilter.CLASS_FILE;
        CollectorFileFilter collector = new CollectorFileFilter(filter, alwaysTrue);
        FileUtil.descendFileTree(dir, collector);
        List list = collector.getFiles();
        String[] result = new String[list.size()];
        Iterator it = list.iterator();
        String dirPrefix = dir.getPath();
        for (int i = 0; i < result.length; i++) {
            if (!it.hasNext()) {
                throw new Error("unexpected end of list at " + i);
            } 
            result[i] = fileToClassname((File) it.next(), dirPrefix);
        } 
        return result;
    }
    
    /**
     * Convert String[] to String by using conventions for
     * split.  Will ignore any entries containing SPLIT_DELIM
     * (and write as such to errs if not null).
     * @param input the String[] to convert
     * @param errs the StringBuffer for error messages (if any)
     */
    public static String unsplit(String[] input, StringBuffer errs) {     
        StringBuffer sb = new StringBuffer();
        sb.append(SPLIT_START);
        for (int i = 0; i < input.length; i++) {
            if (input[i].contains(SPLIT_DELIM)) {
                if (null != errs) {
                    errs.append("\nLangUtil.unsplit(..) - item " + i + ": \"" + input[i]
                        + " contains \"" + SPLIT_DELIM + "\"");
                }
            } else {
                sb.append(input[i]);
                if (1+i < input.length) {
                    sb.append(SPLIT_DELIM);
                }
            }
        }
        sb.append(SPLIT_END);
        return sb.toString();
        
    }
    
    /**
     * Split input into substrings on the assumption that it is
     * either only one string or it was generated using List.toString(),
     * with tokens
     * <pre>SPLIT_START {string} { SPLIT_DELIM {string}} SPLIT_END<pre>
     * (e.g., <code>"[one, two, three]"</code>).
     */
    public static String[] split(String s) {
        if (null == s) {
            return null;
        }
        if ((!s.startsWith(SPLIT_START)) || (!s.endsWith(SPLIT_END))) {
           return new String[] { s };
        }
        s = s.substring(SPLIT_START.length(),s.length()-SPLIT_END.length());
        final int LEN = s.length();
        int start = 0;
        final ArrayList result = new ArrayList();
        final String DELIM = ", ";
        int loc = s.indexOf(SPLIT_DELIM, start);
        while ((start < LEN) && (-1 != loc)) {
            result.add(s.substring(start, loc));
            start = DELIM.length() + loc;
            loc = s.indexOf(SPLIT_DELIM, start);
        }
        result.add(s.substring(start));
        return (String[]) result.toArray(new String[0]);
    }
 
    public static String[] strip(String[] src, String[] toStrip) {
        if (null == toStrip) {
            return strip(src, NONE);
        } else if (null == src) {
            return strip(NONE, toStrip);
        }
        List slist = org.aspectj.util.LangUtil.arrayAsList(src);
        List tlist = org.aspectj.util.LangUtil.arrayAsList(toStrip);
        slist.removeAll(tlist);
        return (String[]) slist.toArray(NONE);        
    }
    
    /**
     * Load all classes specified by args, logging success to out
     * and fail to err.
     */    
    public static void loadClasses(String[] args, StringBuffer out,
                                   StringBuffer err) {
        if (null != args) {
			for (String arg : args) {
				try {
					Class c = Class.forName(arg);
					if (null != out) {
						out.append("\n");
						out.append(arg);
						out.append(": ");
						out.append(c.getName());
					}
				} catch (Throwable t) {
					if (null != err) {
						err.append("\n");
						FileUtil.render(t, err);
					}
				}
			}
            
        }
    } 
    
    private static String fileToClassname(File f, String prefix) {
        // this can safely assume file exists, starts at base, ends with .class
        // this WILL FAIL if full path with drive letter on windows 
        String path = f.getPath();
        if (!path.startsWith(prefix)) {
            String err = "!\"" + path + "\".startsWith(\"" + prefix + "\")";
            throw new IllegalArgumentException(err);
        }
        int length = path.length() - ".class".length();
        path = path.substring(prefix.length()+1, length);
		for (char sep : SEPS) {
			path = path.replace(sep, '.');
		}
        return path;
    }
    
    public static void main (String[] args) { // todo remove as testing
        StringBuffer err = new StringBuffer();
        StringBuffer out = new StringBuffer();
		for (String arg : args) {
			String[] names = classesIn(new File(arg));
			System.err.println(arg + " -> " + render(names));
			loadClasses(names, out, err);
		}
        if (0 < err.length()) {
            System.err.println(err.toString());
        }
        if (0 < out.length()) {
            System.out.println(out.toString());
        }
    } 

    public static String render (String[] args) { // todo move as testing
        if ((null == args) || (1 > args.length)) {
            return "[]";
        }
        boolean longFormat = (args.length < 10);
        String sep = (longFormat ? ", " : "\n\t");
        StringBuffer sb = new StringBuffer();
        if (!longFormat) sb.append("[");
        for (int i = 0; i < args.length; i++) {
            if (0 < i) sb.append(sep);
            sb.append(args[i]);
        } 
        sb.append(longFormat ? "\n" : "]");
        return sb.toString();
    } 
    
   
    /**
     * @param thrown the Throwable to render
     */
    public static String debugStr(Throwable thrown) {
        if (null == thrown) {
            return "((Throwable) null)";
        } else if (thrown instanceof InvocationTargetException) {
            return debugStr(((InvocationTargetException)thrown).getTargetException());
        } else if (thrown instanceof AbortException) {
            IMessage m = ((AbortException) thrown).getIMessage();
            if (null != m) {
                return "" + m;
            }
        }
        StringWriter buf = new StringWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println(thrown.getMessage());
        thrown.printStackTrace(writer);
        try { buf.close(); } 
        catch (IOException ioe) {} 
        return buf.toString();
    }

    /**
     * <code>debugStr(o, false);</code>
     * @param source the Object to render
     */
    public static String debugStr(Object o) {
        return debugStr(o, false);
    }
    
    /**
     * Render standard debug string for an object in normal, default form.
     * @param source the Object to render
     * @param recurse if true, then recurse on all non-primitives unless rendered
     */
    public static String debugStr(Object o, boolean recurse) {
        if (null == o) {
            return "null";
        } else if (recurse) {
            ArrayList rendering = new ArrayList();
            rendering.add(o);
            return debugStr(o, rendering);
        } else {
            Class c = o.getClass();
            Field[] fields = c.getDeclaredFields();
            Object[] values = new Object[fields.length];
            String[] names = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				names[i] = field.getName();
                try {
                    values[i] = field.get(o);
                    if (field.getType().isArray()) {
                        List list = org.aspectj.util.LangUtil.arrayAsList((Object[]) values[i]);
                        values[i] = list.toString();
                    }
                } catch (IllegalAccessException e) {
                    values[i] = "<IllegalAccessException>";
                }
			}
            return debugStr(c, names, values);
        }
    }

    /**
     * recursive variant avoids cycles.
     * o added to rendering before call.
     */
    private static String debugStr(Object o, ArrayList rendering) {
        if (null == o) {
            return "null";
        } else if (!rendering.contains(o)) {
            throw new Error("o not in rendering");
        }
        Class c = o.getClass();
        if (c.isArray()) {
            Object[] ra = (Object[]) o;
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            for (int i = 0; i < ra.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                rendering.add(ra[i]);
				sb.append(debugStr(ra[i], rendering));
			}
            sb.append("]");
            return sb.toString();
        }
        Field[] fields = nonStaticFields(c.getFields());
        Object[] values = new Object[fields.length];
        String[] names = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            names[i] = field.getName();
            // collapse to String
            Object value = privilegedGetField(field,o);
            if (null == value) { 
                values[i] = "null";
            } else if (rendering.contains(value)) {
                values[i] = "<recursion>";
            } else {
                rendering.add(value);
                values[i] = debugStr(value, rendering);
            }
        }
        return debugStr(c, names, values);
    }

    /** incomplete - need protection domain */
    private static Object privilegedGetField(final Field field, final Object o) {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() {
                    try {
                        return field.get(o);
                    } catch(IllegalAccessException e) {
                        return "<IllegalAccessException>";
                    }
                }
            });
        } catch (PrivilegedActionException e) {
            return "<IllegalAccessException>";
        }               
    }
    
    private static Field[] nonStaticFields(Field[] fields) {
        if (null == fields) {
            return new Field[0];
        }
        int to = 0;
        int from = 0;
        while (from < fields.length) {
            if (!Modifier.isStatic(fields[from].getModifiers())) {
                if (to != from) {
                    fields[to] = fields[from];
                }
                to++;
            }
            from++;
        }
        if (to < from) {
            Field[] result = new Field[to];
            if (to > 0) {
                System.arraycopy(fields, 0, result, 0, to);
            }
            fields = result;
        }
        return fields;
    }

    /** <code> debugStr(source, names, items, null, null, null, null)<code> */ 
    public static String debugStr(Class source, String[] names, Object[] items) {
        return debugStr(source, null, names, null, items, null, null);
    }

    /**
     * Render standard debug string for an object.
     * This is the normal form and an example with the default values:<pre>
     * {className}{prefix}{{name}{infix}{value}{delimiter}}..{suffix}
     * Structure[head=root, tail=leaf]</pre>
     * Passing null for the formatting entries provokes the default values,
     * so to print nothing, you should pass "".  Default values:<pre>
     *    prefix: "["   SPLIT_START
     *     infix: "="
     * delimiter: ", "  SPLIT_DELIM
     *    suffix: "]"   SPLIT_END
     * @param source the Class prefix to render unqualified - omitted if null
     * @param names the String[] (field) names of the items - omitted if null
     * @param items the Object[] (field) values
     * @param prefix the String to separate classname and start of name/values
     * @param delimiter the String to separate name/value instances
     * @param infix the String to separate name and value
     *         used only if both name and value exist
     * @param suffix the String to delimit the end of the name/value instances
     *         used only if classname exists
     */
    public static String debugStr(Class source, String prefix, String[] names, 
        String infix, Object[] items, String delimiter,  String suffix) {

        if (null == delimiter) {
            delimiter = SPLIT_DELIM;
        }
        if (null == prefix) {
            prefix = SPLIT_START;
        }
        if (null == infix) {
            infix = "=";
        }
        if (null == suffix) {
            suffix = SPLIT_END;
        }
        StringBuffer sb = new StringBuffer();
        if (null != source) {
            sb.append(org.aspectj.util.LangUtil.unqualifiedClassName(source));
        }
        sb.append(prefix);
        if (null == names) {
            names = NONE;
        }
        if (null == items) {
            items = NONE;
        }
        final int MAX 
            = (names.length > items.length ? names.length : items.length);
        for (int i = 0; i < MAX; i++) {
			if (i > 0) {
                sb.append(delimiter);
            }
            if (i < names.length) {
                sb.append(names[i]);
            }
            if (i < items.length) {
                if (i < names.length) {
                    sb.append(infix);
                }
                sb.append(items[i] + "");
            }            
		}
        sb.append(suffix);
        return sb.toString();
    }

    /** 
     * @return a String with the unqualified class name of the object (or "null")
     */
    public static String unqualifiedClassName(Object o) {
        return unqualifiedClassName(null == o ? null : o.getClass());
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
        if (-1 != loc)
            name = name.substring(1 + loc);
        return name;
    }
    
    /** 
     * Calculate exact diffs and report missing and extra items.
     * This assumes the input List are not modified concurrently.
     * @param expectedListIn the List of expected results - treated as empty if null
     * @param actualListIn the List of actual results - treated as empty if null
     * @param extraListOut the List for any actual results not expected - ignored if null
     * @param missingListOut the List for any expected results not found - ignored if null
     * */
	public static void makeDiffs(
		List expectedListIn,
		List actualListIn,
		List missingListOut,
        List extraListOut) {
		if ((null == missingListOut) && (null == extraListOut)) {
			return;
		}
		if (null == expectedListIn) {
			expectedListIn = Collections.EMPTY_LIST;
		}
		if (null == actualListIn) {
			actualListIn = Collections.EMPTY_LIST;
		}
        if ((0 == actualListIn.size()) && (0 == expectedListIn.size()) ) {
            return;
        }
		BitSet actualExpected = new BitSet();
		for (Object expect : expectedListIn) {
			int loc = actualListIn.indexOf(expect);
			if (-1 == loc) {
				if (null != missingListOut) {
					missingListOut.add(expect);
				}
			} else {
				actualExpected.set(loc);
			}
		}
		if (null != extraListOut) {
			for (int i = 0; i < actualListIn.size(); i++) {
				if (!actualExpected.get(i)) {
					extraListOut.add(actualListIn.get(i));
				}
			}
		}
	}
    // XXX unit test for makeSoftDiffs
    /** 
     * Calculate potentially "soft" diffs using 
     * Comparator.compare(expected, actual).
     * This shallow-copies and sorts the input Lists.
     * @param expectedListIn the List of expected results - treated as empty if null
     * @param actualListIn the List of actual results - treated as empty if null
     * @param extraListOut the List for any actual results not expected - ignored if null
     * @param missingListOut the List for any expected results not found - ignored if null
     * @param comparator the Comparator for comparisons - not null
     * @throws IllegalArgumentException if comp is null
     */
    public static void makeSoftDiffs(  // XXX no intersect or union on collections???
        List expectedListIn,
        List actualListIn,
        List missingListOut,
        List extraListOut,
        Comparator comparator) {
        if ((null == missingListOut) && (null == extraListOut)) {
            return;
        }
        if (null == comparator) {
            throw new IllegalArgumentException("null comparator");
        }
        if (null == expectedListIn) {
            expectedListIn = Collections.EMPTY_LIST;
        }
        if (null == actualListIn) {
            actualListIn = Collections.EMPTY_LIST;
        }
        if ((0 == actualListIn.size()) && (0 == expectedListIn.size()) ) {
            return;
        }

		List expected = new ArrayList(expectedListIn);
        expected.sort(comparator);

		List actual = new ArrayList(actualListIn);
        actual.sort(comparator);
        Iterator actualIter = actual.iterator();        
        Object act = null;
        
        if (missingListOut != null) {
        	missingListOut.addAll(expectedListIn);
        }
        if (extraListOut != null) {
        	extraListOut.addAll(actualListIn);
        }
        
        // AMC: less efficient, but simplified implementation. Needed since messages can
        // now match on text content too, and the old algorithm did not cope with two expected
        // messages on the same line, but with different text content.
        while (actualIter.hasNext()) {
        	act = actualIter.next();
			for (Object exp : expected) {
				// if actual matches expected remove actual from extraListOut, and
				// remove expected from missingListOut
				int diff = comparator.compare(exp, act);
				if (diff == 0) {
					extraListOut.remove(act);
					missingListOut.remove(exp);
				} else if (diff > 0) {
					// since list is sorted, there can be no more matches...
					break;
				}
			}
        }
        
//        while (((null != act) || actualIter.hasNext())
//             && ((null != exp) || expectedIter.hasNext())) {
//            if (null == act) {
//                act = actualIter.next();
//            }
//            if (null == exp) {
//                exp = expectedIter.next();
//            }
//            int diff = comparator.compare(exp, act);
//            if (0 > diff) {          // exp < act
//                if (null != missingListOut) {
//                    missingListOut.add(exp);
//                    exp = null;
//                }
//            } else if (0 < diff) {   // exp > act
//                if (null != extraListOut) {
//                    extraListOut.add(act);
//                    act = null;
//                }
//            } else { // got match of actual to expected
//                // absorb all actual matching expected (duplicates)
//                while ((0 == diff) && actualIter.hasNext()) {
//                    act = actualIter.next();
//                    diff = comparator.compare(exp, act);
//                }
//                if (0 == diff) {
//                    act = null;
//                }
//                exp = null;
//            }
//        }
//        if (null != missingListOut) {
//             if (null != exp) {
//                missingListOut.add(exp);
//             }
//            while (expectedIter.hasNext()) {
//                 missingListOut.add(expectedIter.next());
//            }
//        }
//        if (null != extraListOut) {
//            if (null != act) {
//                extraListOut.add(act);
//            }
//            while (actualIter.hasNext()) {
//                 extraListOut.add(actualIter.next());
//            }
//        }
    }    
    public static class FlattenSpec {
        /** 
         * This tells unflatten(..) to throw IllegalArgumentException
         * if it finds two contiguous delimiters.
         */
        public static final String UNFLATTEN_EMPTY_ERROR 
            = "empty items not permitted when unflattening";
        /** 
         * This tells unflatten(..) to skip empty items when unflattening
         * (since null means "use null")
         */
        public static final String UNFLATTEN_EMPTY_AS_NULL 
            = "unflatten empty items as null";
            
        /** 
         * This tells unflatten(..) to skip empty items when unflattening
         * (since null means "use null")
         */
        public static final String SKIP_EMPTY_IN_UNFLATTEN
            = "skip empty items when unflattening";
        
        /** 
         * For Ant-style attributes: "item,item" (with escaped commas).
         * There is no way when unflattening to distinguish 
         * values which were empty from those which were null,
         * so all are unflattened as empty.
         */
        public static final FlattenSpec COMMA
            = new FlattenSpec(null, "", "\\", ",", null, "") {
                public String toString() { return "FlattenSpec.COMMA"; }
            };
            
        /** this attempts to mimic ((List)l).toString() */
        public static final FlattenSpec LIST
            = new FlattenSpec("[", "", null, ", ", "]", UNFLATTEN_EMPTY_ERROR) {
                public String toString() { return "FlattenSpec.LIST"; }
            };
            
        /** how toString renders null values */
        public static final String NULL = "<null>";
        private static String r(String s) {
            return (null == s ? NULL : s);
        }

        public final String prefix;
        public final String nullFlattened;
        public final String escape;
        public final String delim;
        public final String suffix;
        public final String emptyUnflattened;
        private transient String toString; 

        public FlattenSpec(
            String prefix, 
            String nullRendering, 
            String escape,
            String delim, 
            String suffix,
            String emptyUnflattened) {
            this.prefix = prefix;
            this.nullFlattened = nullRendering;
            this.escape = escape;
            this.delim = delim;
            this.suffix = suffix;
            this.emptyUnflattened = emptyUnflattened;
            throwIaxIfNull(emptyUnflattened, "use UNFLATTEN_EMPTY_AS_NULL");
        }
        
        public String toString() {
            if (null == toString) {
                toString = "FlattenSpec("
                    + "prefix=" + r(prefix)
                    + ", nullRendering=" + r(nullFlattened)
                    + ", escape=" + r(escape)
                    + ", delim=" + r(delim)
                    + ", suffix=" + r(suffix)
                    + ", emptyUnflattened=" + r(emptyUnflattened)
                    + ")";
            }
            return toString;
        }
    }
} // class LangUtil

//  --------- java runs using Ant
//	/**
//	 * Run a Java command separately.
//	 * @param className the fully-qualified String name of the class 
//     *         with the main method to run
//	 * @param classpathFiles the File to put on the classpath
//	 * @param args to the main method of the class
//     * @param outSink the PrintStream for the output stream - may be null
//	 */
//	public static void oldexecuteJava(
//		String className,
//		File[] classpathFiles,
//		String[] args,
//        PrintStream outSink) {
//        Project project = new Project();
//        project.setName("LangUtil.executeJava(" + className + ")");
//        Path classpath = new Path(project, classpathFiles[0].getAbsolutePath());
//        for (int i = 1; i < classpathFiles.length; i++) {
//			classpath.addExisting(new Path(project, classpathFiles[i].getAbsolutePath()));
//		}        
//
//        Commandline cmds = new Commandline();
//        cmds.addArguments(new String[] {className});
//        cmds.addArguments(args);      
//
//        ExecuteJava runner = new ExecuteJava();
//        runner.setClasspath(classpath);
//        runner.setJavaCommand(cmds);        
//        if (null != outSink) {
//            runner.setOutput(outSink); // XXX todo
//        }
//        runner.execute(project);
//	}

//     public static void executeJava(
//        String className,
//        File dir,
//        File[] classpathFiles,
//        String[] args,
//        PrintStream outSink) {
//        StringBuffer sb = new StringBuffer();
//        
//        sb.append("c:/apps/jdk1.3.1/bin/java.exe -classpath \"");
//        for (int i = 0; i < classpathFiles.length; i++) {
//            if (i < 0) {
//                sb.append(";");
//            }
//            sb.append(classpathFiles[i].getAbsolutePath());            
//        }        
//        sb.append("\" -verbose " + className);
//        for (int i = 0; i < args.length; i++) {
//            sb.append(" " + args[i]);
//		}
//        Exec exec = new Exec();
//        Project project = new Project();
//        project.setProperty("ant.home", "c:/home/wes/aj/aspectj/modules/lib/ant");
//        System.setProperty("ant.home", "c:/home/wes/aj/aspectj/modules/lib/ant");
//        exec.setProject(new Project());
//        exec.setCommand(sb.toString());
//        exec.setDir(dir.getAbsolutePath());
//        exec.execute();
//    }
//     public static void execJavaProcess(
//        String className,
//        File dir,
//        File[] classpathFiles,
//        String[] args,
//        PrintStream outSink) throws Throwable {
//        StringBuffer sb = new StringBuffer();
//        
//        sb.append("c:\\apps\\jdk1.3.1\\bin\\java.exe -classpath \"");
//        for (int i = 0; i < classpathFiles.length; i++) {
//            if (i > 0) {
//                sb.append(";");
//            }
//            sb.append(classpathFiles[i].getAbsolutePath());            
//        }        
//        sb.append("\" -verbose " + className);
//        for (int i = 0; i < args.length; i++) {
//            sb.append(" " + args[i]);
//        }
//        String command = sb.toString();
//        System.err.println("launching process: " + command);
//        Process process = Runtime.getRuntime().exec(command);
//        // huh? err/out
//        InputStream errStream = null; 
//        InputStream outStream = null; 
//        Throwable toThrow = null;
//        int result = -1;
//        try {
//            System.err.println("waiting for process: " + command);
//            errStream = null; // process.getErrorStream();
//            outStream = null; // process.getInputStream(); // misnamed - out
//            result = process.waitFor();
//            System.err.println("Done waiting for process: " + command);
//            process.destroy();
//        } catch (Throwable t) {
//            toThrow = t;
//        } finally {
//            if (null != outStream) {
//                FileUtil.copy(outStream, System.out, false);
//                try { outStream.close(); }
//                catch (IOException e) {}
//            }
//            if (null != errStream) {
//                FileUtil.copy(errStream, System.err, false);
//                try { errStream.close(); }
//                catch (IOException e) {}
//            }
//        }
//        if (null != toThrow) {
//            throw toThrow;
//        }
//    }
//        try {
//            // show the command
//            log(command, Project.MSG_VERBOSE);
//
//            // exec command on system runtime
//            Process proc = Runtime.getRuntime().exec(command);
//
//            if (out != null)  {
//                fos = new PrintWriter(new FileWriter(out));
//                log("Output redirected to " + out, Project.MSG_VERBOSE);
//            }
//
//            // copy input and error to the output stream
//            StreamPumper inputPumper =
//                new StreamPumper(proc.getInputStream(), Project.MSG_INFO);
//            StreamPumper errorPumper =
//                new StreamPumper(proc.getErrorStream(), Project.MSG_WARN);
//
//            // starts pumping away the generated output/error
//            inputPumper.start();
//            errorPumper.start();
//
//            // Wait for everything to finish
//            proc.waitFor();
//            inputPumper.join();
//            errorPumper.join();
//            proc.destroy();
//
//            // close the output file if required
//            logFlush();
//
//            // check its exit value
//            err = proc.exitValue();
//            if (err != 0) {
//                if (failOnError) {
//                    throw new BuildException("Exec returned: " + err, getLocation());
//                } else {
//                    log("Result: " + err, Project.MSG_ERR);
//                }
//            }
//        } catch (IOException ioe) {
//            throw new BuildException("Error exec: " + command, ioe, getLocation());
//        } catch (InterruptedException ex) {}
//


