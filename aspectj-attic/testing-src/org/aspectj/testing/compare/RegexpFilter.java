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

// todo: non-distribution license?
package org.aspectj.testing.compare;

import org.aspectj.testing.compare.Regexp;

import java.util.*;
import java.io.*;


/** utility class for logging */
class D {
    public static boolean LOG = false;
    public static void log(String s) { if (LOG) System.err.println("## " + s); }
    static {
        try {
            LOG = (null != System.getProperty("DEBUG"));
        } catch (Throwable t) {
            // ignore
        }
    }
}

/** utility class for handling errors */
class ErrHandler {
    public static final ErrHandler DEFAULT = new ErrHandler();
    public static final Action INFO = new Action("info") {
            public void invoke(String message) { System.out.println("INFO: " + message); } };
    public static final Action WARN = new Action("warn") {
            public void invoke(String message) { System.err.println("WARNING: " + message); } };
    public static final Action HALT = new Action("halt") {
            public void invoke(String message) { throw new RuntimeException(message); } };
    public static final Action ABORT = new Action("abort") {
            public void invoke(String message) { throw new Error(message); } };
    public static final Action EXIT = new Action("exit") {
            public void invoke(String message) { System.err.println(message); System.exit(1); } };
    abstract static class Action { 
        protected final String name;
        private Action(String name) {
            this.name = name.toLowerCase().trim();
        }
        abstract public void invoke(String message);
        public String toString() { return name; }
    }
    public static final void handleErr(String message, Throwable t) { 
        DEFAULT.handle(message, t);
    }
    public static final void handleErr(String message) { 
        DEFAULT.handle(message);
    }
    public static final void handleErr(String message, Action suggestion) { 
        DEFAULT.handle(message, suggestion);
    }
    public void handle(String message) { handle(message, INFO); }
    public void handle(String message, Throwable t) { 
        String eMessage = (null == t ? "" : 
                           t.getClass().getName() + ": " + t.getMessage());
        handle(message + eMessage, HALT);
    }
    /**
     * The default implementation just takes the suggested action
     * @param message the String to pass to any Action
     * @param suggestion the Action proposed by the caller
     */
    public void handle(String message, Action suggestion) {
        suggestion.invoke(message);
    }
}

/* old comments, not correct:
 * <li>test line against all registered select statements
 *   to get all the matching (replace) operations (unsupported)</li>
 * The algorithm is greedy in that if the user requests a line
 * and the default is no-output, it will read lines from the input
 * until one is matched (or EOF).
 */

/**  
 * Process files in a minimal version of sed:
 * <li>read line using superclass LineNumberReader.readLine()</li>
 * <li>Preprocess with case and white space operations</li>
 * <li>run all the replace operations on the input, in order</li>
 * <li>return the line.</li>
 * Using anything but the <code>readLine()</code> method will circumvent
 * the regular expression replacement processing.
 */
public class RegexpFilter {
    protected static final String[] NO_STRINGS = new String[]{};

    // ---------------------------------------------- static methods
    /**
     * Process file (or System.in) like sed.
     * This only calls <code>RegexpFilterReader.main(args)</code>.
     * @param args  same as for init(String[], RegexpFilter)
     */
    public static void main(String[] args) throws IOException {
        RegexpFilterReader.main(args);
    }

    // todo: move String -> String[] (commas) out into utility
    /**
     * Initialize a RegexpFilter based on command-line style arguments
     * in a single String. (Otherwise, same as 
     *  <code>init(String[], RegexpFilter)</code>)
     * The Strings are separated at , (unless \ escaped) and trimmed.
     * Note that the escape characters are removed from before the ,.
     * @param spec the String to break into String[]
     * @param toSet the RegexpFilter to initialize - if null, construct one from
     *              the file argument or stdin if there is no file argument.
     */
    public static RegexpFilter init(String arg, RegexpFilter toSet) {
        if ((null == arg) || (1 > arg.length())) {
            return init(NO_STRINGS, toSet);
        } 
        StringTokenizer st = new StringTokenizer(arg, ",");
        Vector result = new Vector();
        String last = null;
        String next;
        while (st.hasMoreTokens()) {
            next = st.nextToken();
            if (next.endsWith("\\") && (st.hasMoreTokens())) {
                next = next.substring(0, next.length()-1);
                last = last == null ? next : last + next;
                continue;
            }
            if (null != last) {
                next = last + next;
                last = null;
            }
            result.add(next.trim());
        }
        String[] args = new String[result.size()];
        result.copyInto(args);
        return RegexpFilter.init(args, toSet);
    }

    /**
     * Initialize a RegexpFilter based on command-line style arguments.
     * This is the only way (currently) to set up a RegexpFilter.
     * syntax: <code>{file | {-i|-t|-b|-s <pattern>|-s <patternFile>}..}</code>
     * (for booleans, use lowercase to enable, uppercase to disable).
     * @param args  the String[] containing file to input plus any number of...
     *              <li>-i "ignore": ignore case</li>
     *              <li>-t "trim"  : ignore leading and trailing white space</li>
     *              <li>-b "blanks": ignore differences in all white space</li>
     *              <li>-s "{s/pattern/expression/};...": 
     *                      replace pattern in lines with expression</li>
     *              <li>-S <file>  : same as s, but read commands from file</li>
     * @param toSet the RegexpFilter to initialize - if null, construct one from
     *              the file argument or stdin if there is no file argument.
     */
    public static RegexpFilter init(String[] args, RegexpFilter toSet) {
        final String syntax = " - syntax: {file | {-i|-t|-b|-s <pattern>|-s <patternFile>}..}";
        RegexpFilter result = (null != toSet ? toSet : new RegexpFilter());
        if ((null != args) && (0 < args.length)) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ((null == arg) || (1 > arg.length())) continue;
                if (arg.startsWith("-"))  {
                    switch (arg.charAt(1)) {
                    case 'i' : result.ignoreCase = true; break;
                    case 'I' : result.ignoreCase = false; break;
                    case 'b' : result.collapseWhitespace = true; break;
                    case 'B' : result.collapseWhitespace = false; break;
                    case 't' : result.trimWhitespace = true; break;
                    case 'T' : result.trimWhitespace = false; break;
                    case 's' : ++i; 
                        if (i < args.length) {
                            result.getOperationList().addOperation(args[i]); 
                        } else {
                            String err = "need arg after -s " + syntax;
                            ErrHandler.handleErr(err, ErrHandler.WARN); 
                        }
                    break; 
                    case 'S' : ++i; 
                        if (i < args.length) {
                            result.getOperationList().addFile(args[i]); 
                        } else {
                            String err = "need arg after -s " + syntax;
                            ErrHandler.handleErr(err, ErrHandler.WARN); 
                        }
                    break; 
                    default: 
                        String err = "unrecognized flag : " + arg + syntax;
                        ErrHandler.handleErr(err, ErrHandler.WARN); 
                        break;
                    }
                } else if (null != result) {
                    ErrHandler.handleErr("unexpected arg " + arg + syntax, ErrHandler.WARN); 
                    break;
                } else { // unflagged argument, need file - should be input file
                    File _file = new File(arg);
                    if (_file.exists() && _file.canRead()) {
                        result.setFile(_file);
                    }
                }
            } // reading args
        }  // have args
        return result;
    } // init
    
    // ---------------------------------------------- instance fields
    /** ignore case by converting lines to upper case */
    protected boolean ignoreCase  = false;
    /** collapse internal whitespace by converting to space character */
    protected boolean collapseWhitespace  = true;
    /** trim leading and trailing whitespace from lines before comparison */
    protected boolean trimWhitespace  = false;
    /** replace input per replace operations */
    protected boolean replace  = false;
    /** operations to process the file with */
    protected OperationList operations;
    /** handler for our errors*/
    protected ErrHandler handler  = ErrHandler.DEFAULT;
    /** the File to use */
    protected File file = null;

    // ---------------------------------------------- constructors
    /** no getter/setters yet, so construct using 
     * <code>static RegexpFilter init(String[],RegexpFilter)</code> 
     */
    protected RegexpFilter() { }

    // ---------------------------------------------- instance methods
   

    /**
     * Set a file for this RegexpFilter.
     * This makes command-line initialization easier.
     * @param file the File to set for this RegexpFilter
     */
    public void setFile(File file) { this.file = file; } 

    /**
     * Return file this RegexpFilter was initialized with.
     * @return the File this RegexpFilter was initialized with (may be null).
     */
    public File getFile() { return file; } 
 
    /**
     * Lazy construction of operations list
     */
    protected OperationList getOperationList() {
        if (null == operations) {
            operations = new OperationList();
            replace = true;
        }
        return operations;
    }

    /**
     * Process line, applying case and whitespace operations
     * before delegating to replace.
     * @param string the String to proces
     * @return the String as processed
     */
    protected String process(String string) {
        String label = "process(\"" + string + "\")";
        D.log(label);
        if (null == string) return null;
        String result = string;
        if (ignoreCase) {
            result = result.toUpperCase();
        }
        if (trimWhitespace) {
            result = result.trim();
        }
        if (collapseWhitespace) {
            final StringBuffer collapse = new StringBuffer();
            StringTokenizer tokens = new StringTokenizer(result);
            boolean hasMoreTokens = tokens.hasMoreTokens();
            while (hasMoreTokens) {
                collapse.append(tokens.nextToken());
                hasMoreTokens = tokens.hasMoreTokens();
                if (hasMoreTokens) {
                    collapse.append(" ");
                }
            }
            result = collapse.toString();
        }
        if (replace) {
            result = getOperationList().replace(result);
            D.log(label + " result " + result);
        }
        return result;
    }

    /** 
     * container for ReplaceOperations constructs on add,
     * runs operations against input.
     */
    class OperationList {
        final ArrayList list;
        public OperationList() { 
            list = new ArrayList();
        }

        /**
         * Run input through all the operations in this list
         * and return the result.
         * @param input the String to process
         * @return the String result of running input through all replace 
         *         operations in order.
         */
        public String replace(String input) {
            if (null == input) return null;
            Iterator operations = operations();
            while (operations.hasNext()) {
                ReplaceOperation operation = (ReplaceOperation) operations.next();
                input = operation.replace(input);
            }
            return input;
        }

        /**
         * Add operations read from file, one per line,
         * ignoring empty lines and # or // comments.
         * ';' delimits operations within a line as it does
         * for addOperation(String), so you must \ escape ;
         * in the search or replace segments
         */
        public void addFile(String path) { 
            if (null == path) {
                handler.handle("null path", ErrHandler.ABORT);
            } else {
                File file = new File(path);
                if (!file.exists() && file.canRead()) {
                    handler.handle("invalid path: " + path, ErrHandler.ABORT);
                } else {
                    BufferedReader reader = null;
                    int lineNumber = 0;
                    String line  = null;
                    try {
                        reader = new BufferedReader(new FileReader(file));
                        while (null != (line = reader.readLine())) {
                            lineNumber++;
                            int loc = line.indexOf("#");
                            if (-1 != loc) {
                                line = line.substring(0,loc);
                            }
                            loc = line.indexOf("//");
                            if (-1 != loc) {
                                line = line.substring(0,loc);
                            }
                            line = line.trim();
                            if (1 > line.length()) continue;
                            addOperation(line);
                        }
                    } catch (IOException e) {
                        String message ="Error processing file " + path 
                            + " at line " + lineNumber + ": \"" + line + "\""
                            + ": " + e.getClass().getName() + ": " + e.getMessage() ;
                        handler.handle(message, ErrHandler.ABORT);
                    } finally {
                        try  { 
                            if (reader != null) reader.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
            }
        }

        /**
         * Add operation to list, emitting warning and returning false if not created.
         * Add multiple operations at once by separating with ';'
         * (so any ; in search or replace must be escaped).
         * @param operation a String acceptable to 
         *                  <code>ReplaceOperation.makeReplaceOperation(String, ErrHandler)</code>,
         *                  of the form sX{search}X{replace}X{g};..
         * @return false if not all added.
         */
        public boolean addOperation(String operation) { 
            StringTokenizer st = new StringTokenizer(operation, ";", false);
            String last = null;
            ReplaceOperation toAdd;
            boolean allAdded = true;
            while (st.hasMoreTokens()) {
                // grab tokens, accumulating if \ escapes ; delimiter 
                String next = st.nextToken();
                if (next.endsWith("\\") && (st.hasMoreTokens())) {
                    next = next.substring(0, next.length()-1);
                    last = (last == null ? next : last + next);
                    continue;
                }
                if (null != last) {
                    next = last + next;
                    last = null;
                }
                toAdd = ReplaceOperation.makeReplaceOperation(next, handler);
                if (null != toAdd) {
                    list.add(toAdd);
                } else {
                    String label = "RegexpFilter.OperationList.addOperation(\"" + operation + "\"): ";
                    handler.handle(label + " input not accepted " , ErrHandler.WARN);
                    if (allAdded) allAdded = false;
                }
            }
            return allAdded;
        }

        /**
         * @return an Iterator over the list of ReplaceOperation
         */
        public Iterator operations() {
            return list.iterator();
        }
    } // class OperationList
} // class RegexpFilter

/**
 * Encapsulate a search/replace operation which uses a RegExp.
 */
class ReplaceOperation {
    /**
     * This accepts a sed-like substitute command, except that
     * the delimiter character may not be used anywhere in the 
     * search or replace strings, even if escaped.  You may use
     * any delimiter character.
     * Note that although g (replace-globally) is supported as input, 
     * it is ignored in this implementation.
     * @param operation a String of the form sX{search}X{replace}X{g}
     */
    public static ReplaceOperation makeReplaceOperation(String operation, ErrHandler handler) {
        ReplaceOperation result = null;
        StringBuffer err = (null == handler ? null : new StringBuffer());
        final String syntax = "sX{search}X{replace}X{g}";
        // todo: use Point p = isValidOperation(operation);
        if (null == operation) {
            if (null != err) err.append("null operation");
        } else if (5 > operation.length()) {
            if (null != err) err.append("empty operation");
        } else if (!operation.startsWith("s")) {
            if (null != err) err.append("expecting s: " + syntax);
       } else {
            String sep = operation.substring(1,2);
            int mid = operation.indexOf(sep, 2);
            if (-1 == mid) {
                if (null != handler) err.append("expecting middle \"" + sep + "\": " + syntax);
            } else if (mid == 2) {
                if (null != handler) err.append("expecting search before middle \"" + sep + "\": " + syntax);
            } else {
                int end = operation.indexOf(sep, mid+1);
                if (-1 == end) {
                    if (null != handler) err.append("expecting final \"" + sep + "\": " + syntax);
                } else {
                    String search = operation.substring(2,mid);
                    if (!ReplaceOperation.isValidSearch(search)) {
                        if (null != handler) err.append("invalid search \"" + search + "\": " + syntax);
                    } else {
                        String replace = operation.substring(mid+1,end);
                        if (!ReplaceOperation.isValidReplace(replace)) {
                            if (null != handler) err.append("invalid replace \"" + replace + "\": " + syntax);
                        } else {
                            result = new ReplaceOperation(search, replace, operation.endsWith("g"), handler);
                        }
                    }
                }
            }
        }
        if ((0 < err.length()) && (null != handler)) {
            err.append(" operation=\"" + operation + "\"");
            handler.handle(err.toString(), ErrHandler.HALT);
        }
        return result;
    }

    /**
     * Return true if the input string represents a valid search operation
     * @param replace the String representing a search expression
     */
    protected static boolean isValidSearch(String search) { // todo: too weak to be useful now
        return ((null != search) && (0 < search.length()));
    }

    /**
     * Return Point x=mid, y=end if the input string represents a valid search operation
     * @param search the String representing a search expression
    protected static Point isValidOperation(String search) {
        if (null != search) {
            final int length = search.length();
            if (5 < length) {
                String sep = search.substring(2,3);
                int mid = search.indexOf(sep, 3);
                if (3 < mid) {
                    int end = search.indexOf(sep, mid+1);
                    if ((end == length-1) 
                        || ((end == length-2) 
                            && search.endsWith("g"))) {
                        return new Point(mid, end);
                    }
                }
            }
        }
        return null;
    }
     */

    /**
     * Return true if the input string represents a valid replace operation
     * @param replace the String representing a replace expression
     */
    protected static boolean isValidReplace(String replace) { // todo: too weak to be useful now
        boolean result = (null != replace); 
        return result;
    } // isValidReplace

    // ------------------------------------------------- instance members
    /** If true, repeat replace as often as possible (todo: repeat not supported) */
    protected final boolean repeat;
    /** search pattern */
    protected final String search;
    /** replace pattern */
    protected final String replace;
    /** regexp processor */
    protected final Regexp regexp;
    /** replace buffer (read-only) */
    protected final char[] replaceBuffer;
    /** error handler */
    protected final ErrHandler handler;

    // ------------------------------------------------- constructors
    private ReplaceOperation(String search, String replace, boolean repeat, ErrHandler handler) {
        this.search = search; 
        this.replace = replace; 
        this.replaceBuffer = replace.toCharArray();
        this.repeat = repeat; 
        this.handler = (null != handler ? handler : ErrHandler.DEFAULT);
        this.regexp = RegexpFactory.makeRegexp();
        try {
            this.regexp.setPattern(search);
        } catch (Exception e) {
            this.handler.handle("setting search=" + search, e);
        }
    }


    /**
     * Return true if the input would be matched by the search string of this ReplaceOperation.
     * @param input the String to compare
     * @return true if the input would be matched by the search string of this ReplaceOperation
     */
    public boolean matches(String input) {
        return ((null != input) && regexp.matches(input));
    } // matches

    /**
     * Replace any search text in input with replacement text, 
     * returning input if there is no match. More specifically, 
    * <li> emit unmatched prefix, if any</li>
    * <li> emit replacement text as-is, except that
    *   \[0-9] in the replacement text is replaced
    *   with the matching subsection of the input text</li>
    * <li> emit unmatched suffix, if any</li>
     * @param input the String to search and replace
     * @throws IllegalArgumentException if null == input 
     */
    public String replace(String input) {
        if (null == input) throw new IllegalArgumentException("null input");
        String label = "replace(\"" + input + "\") ";
        D.log(label);
        if (matches(input)) {
            StringBuffer buffer = new StringBuffer();
            final int length = replaceBuffer.length;
            Vector groups = regexp.getGroups(input);
            if ((null == groups) || (1 > groups.size())) {
                handler.handle(label + "matched but no groups? ");
                return input;
            }
            buffer.setLength(0);
            // group 0 is whole; if not same as input, print prefix/suffix
            String matchedPart = (String) groups.elementAt(0);
            final int matchStart = input.indexOf(matchedPart);
            final int matchEnd = matchStart + matchedPart.length();
            if (0 < matchStart) {
                buffer.append(input.substring(0, matchStart));
            }
            // true if \ escaping special char, esp. replace \[0-9]
            boolean specialChar = false; 
            for (int i = 0; i < length; i++) {
                char c = replaceBuffer[i];
                if (specialChar) { 
                    int value = Character.digit(c, 10); // only 0-9 supported
                    if ((0 <= value) && (value < groups.size())) {
                        buffer.append((String) groups.elementAt(value));
                    } else {
                        buffer.append(c);
                    }
                    specialChar = false;
                } else if ('\\' != c) {
                    D.log("." + c);
                    buffer.append(c);
                } else {
                    specialChar = true;
                }
            }
            if (specialChar) {
                handler.handle(label + "\\ without register: " + replace,
                               ErrHandler.ABORT);
            }
            if (matchEnd < input.length()) {
                buffer.append(input.substring(matchEnd));
            }
            input = buffer.toString();
        }
        return input;
    } // replace
} // class ReplaceOperation

