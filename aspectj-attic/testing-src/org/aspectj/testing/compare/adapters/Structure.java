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

package org.aspectj.testing.compare.adapters;

import org.aspectj.testing.compare.GenericTreeNode;
import org.aspectj.testing.compare.GenericTreeNodesVisitorI;
import org.aspectj.testing.compare.GenericTreeNodeListOrdererFactoryI;
import org.aspectj.testing.compare.GenericTreeNodeListOrdererI;
import org.aspectj.testing.compare.adapters.StructureGenericTreeNodeFactory;

// XXX compiler import org.aspectj.asm.StructureNode;
// XXX wes move to ajde tests import org.aspectj.tools.ajde.StructureUtilities;

import java.util.*;
import java.io.*;

/**
 * Compare or print (serialized) structure tree/graph(s).
 * Mostly thread-safe, except that (todo for now) PRINT_SINK is a mutable static
 * variable settable by the arguments.
 * See {@link #main(String[]) main} for usage.
 */
public class Structure {
    // ---------------------------------- static fields
    // PRINT_SINK as static permits static inner class singleton visitors to reflect output sink policy
    /** WARNING: print sink changeable at runtime (affects all instances of class!) */
    public static PrintStream PRINT_SINK = System.err;

    /** singleton factory to sort by string */
    protected static final GenericTreeNodeListOrdererFactoryI STRING_SORTER_FACTORY
        = new StringSortFactory();
    /** singleton factory to sort by string */
    protected static final GenericTreeNodeListOrdererI STRING_SORTER
        = new StringSort();
    /** singleton to sort by string */
    protected static final Comparator STRING_COMP = new StringComparator();
    /** default visitor printer expects only one of pair and render each parent as tab */
    protected static final GenericTreeNodesVisitorI PRINT_EACH = new PrintEach();
    /** visitor printer prints long version of both input (todo: not redirected by toOut) */
    protected static final GenericTreeNodesVisitorI PRINT_ALL = GenericTreeNode.PRINTALL;
    /** visitor printer prints long version of both input if non-matching (todo: not redirected by toOut) */
    protected static final GenericTreeNodesVisitorI PRINT_ERR = GenericTreeNode.PRINTERR;
    /** parms: default list of files used if no files given */
    public static final String DEFAULT_LST = "default.lst";
    /** parms: default argument list if none given */
    public static final String[] DEFAULT_ARGS = new String[]
    { "-d" , "classes" , "-workingdir", "ajworkingdir", "-argfile", DEFAULT_LST};

    // ---------------------------------- static methods
    /**
     * Print and/or compare Structure trees.
     * One (actual) can be compiled at the same time;
     * either (expected or actual) may be read in from a serialized tree.
     * Supported arguments:
     * <table>
     * <tr><td>{ajc compiler args}</td>
     *     <td>The set of compiler arguments, to compile a new tree</td></tr>
     * <tr><td>-expect {file}.ser</td>
     *     <td>The old Structure tree to read in and compare with new</td></tr>
     * <tr><td>-actual {file}.ser</td>
     *     <td>The new Structure tree to read in (i.e., no compile)</td></tr>
     * <tr><td>-save {file}.ser</td>
     *     <td>Serialize the results of the compile to {file}.ser.</td></tr>
     * <tr><td>-printall</td>
     *     <td>Print all pairs in long format</td></tr>
     * <tr><td>-printeach</td>
     *     <td>Print each item in short format
     *         - used when only one tree is available</td></tr>
     * <tr><td>-printerr</td>
     *     <td>Print pairs of items that do not match
     *         - used when only both trees are available</td></tr>
     * <tr><td>-sortString</td>
     *     <td>before comparing, do a string-sort on each child list.</td></tr>
     * <tr><td>-toOut</td>
     *     <td>Redirect output from System.err to System.out (for all instances of this class)</td></tr>
     * <tr><td>-notest</td>
     *     <td>Do not run test (e.g., just compile and save)</td></tr>
     * </table>
     * @param args the String[] of arguments for this test - defaults supplied if empty.
     */
    public static void main(String[] args) {
        new Structure().runTest(args);
    }

    // ---------------------------------- static util
    protected static final void log(String s) {
        final PrintStream sink = PRINT_SINK;
        if ((null != s) &&  (null != sink)) {
            sink.println("Structure: " + s);
        }
    }

    protected static void signal(String message) {
        log(message);
    }
    protected static void signal(String context, String event) {
        log(context + ": " + event);
    }
    protected static void signal(String context, Throwable event) {
        if (null == event) {
            log(context);
        } else {
            String msg = event.getMessage();
            log(context + ": " + msg);
            event.printStackTrace(PRINT_SINK);
        }
    }

    // ---------------------------------- instance fields
    /** args result: path to serialize actual tree to */
    protected String actualSavePath = null;
    /** args result: path of actual serialized tree */
    protected String actualPath = null;
    /** args result: path of expected serialized tree */
    protected String expectedPath = null;
    /** args result: false if not running comparisons */
    protected boolean doComparison = true;

    /** visitor to run - print or test (default is PRINT_ALL) */
    protected GenericTreeNodesVisitorI visitor = PRINT_ALL;
    /** this determines for each set of children whether/how to sort them */
    protected GenericTreeNodeListOrdererFactoryI sorter = null;

    // ---------------------------------- instance methods
    /** no-arg (default) constructor */
    public Structure() { }

    /**
     * Clear settings before running.
     * Use this unless you want to inherit settings from
     * a prior run.  You can specify new arguments that
     * overwrite the old settings.
     * @param args the String[] used for runTest(String[])
     * @see #clear()
     * @see #runTest(String[])
     */
    public synchronized void clearAndRunTest(String[] args) {
        actualSavePath = null;
        actualPath = null;
        expectedPath = null;
        doComparison = true;
        visitor = PRINT_ALL;
        runTest(args);
    }

    /**
     * Read and/or write and/or compare structure trees.
     * Any results are delivered by the comparison visitors.
     * The test process is as follows:
     * <li>processArgs(..)</li>
     * <li>saveActual(..) if saving actual to file</li>
     * <li>doComparison(..) of actual and expected per visitor/sorter options</li>
     * <p>If you run this consecutively, you'll inherit the values
     * of the last run that you do not overwrite
     * unless you invoke {@link #clear()}.  The method synchronization
     * will not prevent another thread from interrupting your
     * @param args the String[] defined by <code>main(String[] args)</code>
     */
    public synchronized void runTest(String[] args) {
        if (null == args) throw new IllegalArgumentException("null args");
        args = processArgs(args);
        if (null == args) throw new IllegalArgumentException("bad args");
// XXX compiler 
////        StructureNode lhsRoot = loadStructureNode(expectedPath);
////        StructureNode rhsRoot = loadStructureNode(actualPath);
////        if (null == rhsRoot) { // not serialized - try compile
////            // XXX wes move to ajde tests rhsRoot = StructureUtilities.buildStructureModel(args);
////        }
////        // save actual, if requested
////        saveActual(rhsRoot);
////        // do comparison (i.e., run test)
////        doComparison(lhsRoot, rhsRoot);
    }

   /**
     * Process arguments by extracting our arguments from callee arguments
     * and initializing the class accordingly.
     * {@link See main(String[])} for a list of valid arguments.
     * @param args the String[] adopted, elements shifted down to remove ours
     * @return a String[] containing args not relevant to us (i.e., for callee = compiler)
     */
    protected String[] processArgs(String[] args) {
        if ((null == args) || (1 > args.length)) {
            return processArgs(DEFAULT_ARGS);
        }
        int numFiles = 0;
        int numArgFiles = 0;
        final String SKIP = "skip";
        String calleeArg;
        int readIndex = 0;
        int writeIndex = 0;
        while (readIndex < args.length) {
            final String arg = args[readIndex];
            calleeArg = arg;
            // assume valid arg for callee unless shown to be ours
            if ((null == arg) || (0 == arg.length())) {
                signal("processArgs", "empty arg at index "+ readIndex);
                break;
            } else if (arg.startsWith("@") || "-argfile".equals(arg)) {
                numArgFiles++;
            } else if (arg.endsWith(".java")) {
                numFiles++;
            } else if (arg.startsWith("-")) {
                calleeArg = SKIP; // assume args are ours unless found otherwise
                if ("-toOut".equals(arg)) {
                    Structure.PRINT_SINK = System.out;
                } else if ("-notest".equals(arg)) {
                    doComparison = false;
                } else if ("-printall".equals(arg)) {
                    visitor = PRINT_ALL;
                } else if ("-printeach".equals(arg)) {
                    visitor = PRINT_EACH;
                } else if ("-printerr".equals(arg)) {
                    visitor = PRINT_ERR;
                } else if ("-sortString".equals(arg)) {
                    sorter = STRING_SORTER_FACTORY;
                } else {  // the rest of ours require a parm
                    readIndex++;
                    String next = ((readIndex < args.length)
                                   ? args[readIndex] : null);
                    boolean nextIsOption
                        = ((null != next) && next.startsWith("-"));
                    if ("-expect".equals(arg)) {
                        expectedPath = next;
                    } else if ("-actual".equals(arg)) {
                        actualPath = next;
                    } else if ("-save".equals(arg)) {
                        actualSavePath = next;
                    } else {
                        readIndex--;
                        calleeArg = arg; // ok, not ours - save
                    }
                    if ((calleeArg == SKIP)
                        && ((null == next) || (nextIsOption))) {
                        signal("processArgs", arg + " requires a parameter");
                        break;
                    }
                }
            }
            if (SKIP != calleeArg) {
                args[writeIndex++] = calleeArg;
            }
            readIndex++;
        }  // end of reading args[]
        if (readIndex < args.length) { // bad args[] - abort (see signals above)
            return null;
        }
        // if no input specified, supply default list file
        if ((0 == numFiles) && (0 == numArgFiles) && (null == actualPath)) {
            if (writeIndex+3 > args.length) {
                String[] result = new String[writeIndex+2];
                System.arraycopy(args, 0, result, 0, writeIndex);
                args = result;
            }
            args[writeIndex++] = "-argfile";
            args[writeIndex++] = DEFAULT_LST;
        }
        // if some args clipped (ours), clip array to actual (callee)
        if (writeIndex < args.length) {
            String[] result = new String[writeIndex];
            System.arraycopy(args, 0, result, 0, writeIndex);
            args = result;
        }
        return args;
    } // processArgs(String[])

// XXX compiler
//    /**
//     * Load any StructureNode tree at path, if possible
//     * @param path the String path to a serialized StructureNode
//     */
//    protected StructureNode loadStructureNode(String path) {
//        if (null == path) return null;
//        StructureNode result = null;
//        try {
//            FileInputStream stream = new FileInputStream(path);
//            ObjectInputStream ois  = new ObjectInputStream(stream);
//            Object o = ois.readObject();
//            Class oClass = (null == o ? null : o.getClass());
//            if (StructureNode.class.isAssignableFrom(oClass)) {
//                result = (StructureNode) o;
//            } else {
//                signal("loadStructureNode(\"" + path
//                       + "\") - wrong type: " + oClass);
//            }
//        } catch (Throwable t) {
//            signal("loadStructureNode(\"" + path + "\")", t);
//        }
//        return result;
//     }
//
//    /**
//     * Save any StructureNode tree to actualSavePath, if possible
//     * @param actual the StructureNode root of the actual tree to save
//     *        (ignored if null)
//     */
//    protected void saveActual(StructureNode actual) {
//        if ((null != actual) && (null != actualSavePath)) {
//            ObjectOutputStream p = null;
//            FileOutputStream ostream = null;
//            try {
//                ostream = new FileOutputStream(actualSavePath);
//                p = new ObjectOutputStream(ostream);
//                p.writeObject(actual);
//            } catch (Throwable e) {
//                signal("saveActual(\"" + actual + "\") -> "
//                       + actualSavePath, e);
//            } finally {
//                try {
//                    if (null != p) p.flush();
//                    if (null != ostream) ostream.close();
//                } catch (IOException o) {} // ignored
//            }
//        }
//    }

    /**
     * Compare two trees based on the settings for
     * the visitor and sorter.  All results should be
     * delivered by the visitor.
     * @param expected the StructureNode actual tree to compare
     * @param actual the StructureNode actual tree to compare
     */
    // XXX compiler
//    protected void doComparison(StructureNode expected, StructureNode actual) {
//        if (doComparison) {
//            final GenericTreeNodeFactoryI fact =
//                StructureGenericTreeNodeFactory.SINGLETON;
//            GenericTreeNode lhs = null;
//            if (expected != null) {
//                lhs = fact.createGenericTreeNode(expected, null);
//            }
//            GenericTreeNode rhs = null;
//            if (actual != null) {
//                rhs = fact.createGenericTreeNode(actual, null);
//            }
//            GenericTreeNode.traverse(lhs, rhs, sorter, visitor);
//        }
//    }

    /**
     * A visitor which prints each to the sink (if any).
     * If only one of the pair is not null,
     * render it using GenericTreeNode.shortString()
     */
    static class PrintEach implements GenericTreeNodesVisitorI {
        private PrintEach() {}
        public boolean visit(GenericTreeNode lhs,GenericTreeNode rhs) {
            PrintStream sink = PRINT_SINK;
            if (null != sink) {
                if ((lhs != null) && (rhs != null)) { // both
                    sink.println("[lhs=" + lhs + "] [rhs=" + rhs + "]");
                } else {
                    GenericTreeNode gtn = (null == lhs ? rhs : lhs);
                    if (null != gtn) {                // one
                        sink.println(gtn.shortString());
                    }
                }
            }
            return true;
        }
    }  // class PrintEach

    static class StringSortFactory implements GenericTreeNodeListOrdererFactoryI {
        /**
         * Produce the correct orderer for the children of the given GenericTreeNodes
         * This always produces the same StringSorter.
         * @return GenericTreeNodeListOrdererI for children, or null if none to be used
         */
        public GenericTreeNodeListOrdererI produce(GenericTreeNode lhs, GenericTreeNode rhs,
                                                   GenericTreeNodesVisitorI visitor) {
            return STRING_SORTER;
        }
    } // class StringSortFactory

    /**
     * sort input lists by Comparator <code>Structure.STRING_COMP</code>.
     */
    static class StringSort implements GenericTreeNodeListOrdererI {
        /**
         * Order input lists (not copies)
         * using the Comparator <code>Structure.STRING_COMP</code>.
         * @param lhs the List representing the left-hand-side
         *            which contains only GenericTreeNode
         * @param rhs the List representing the right-hand-side
         *            which contains only GenericTreeNode
         * @return two lists List[] (0 => lhs, 1 => rhs)
         */
        public List[] produceLists(List lhs, List rhs) {
            if (null != lhs) Collections.sort(lhs, STRING_COMP);
            if (null != rhs) Collections.sort(rhs, STRING_COMP);
            List[] result = new List[2];
            result[0] = lhs;
            result[1] = rhs;
            return result;
        }
    } // class CompSort

    /**
     * Comparator that imposes case-sensitive String order
     * based on GenericTreeNode.shortString() if both are
     * GenericTreeNode, or toString() otherwise.
     * If both are null, considered equal.
     * If one is null, the other is considered larger.
     */
    static class StringComparator implements Comparator {
        public int compare(Object lhs, Object rhs) {
            if (null == lhs) {
                return (null == rhs ? 0 : -1);
            } else if (null == rhs) {
                return 1;
            } else if ((lhs instanceof GenericTreeNode)
                       && (rhs instanceof GenericTreeNode)) {
                String lhsString = ((GenericTreeNode) lhs).shortString();
                String rhsString = ((GenericTreeNode) rhs).shortString();
                if (null == lhsString) {
                    return (null == rhsString ? 0 : rhsString.compareTo(lhsString));
                } else {
                    return lhsString.compareTo(rhsString);
                }
            } else {
                return lhs.toString().compareTo(rhs.toString());
            }
        }
    } // class StringComparator
} // class Structure

