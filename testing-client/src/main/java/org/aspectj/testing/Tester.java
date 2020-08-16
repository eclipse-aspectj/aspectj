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

package org.aspectj.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.Message;
import org.aspectj.util.LangUtil;

/**
 * Testing client interface for checking results and reporting
 * to a delegate IMessageHandler.
 * Harnesses providing this interface for test clients must
 * set it up by calling
 * {@link #setBASEDIR(File)}
 * {@link #setMessageHandler(IMessageHandler)} and
 * {@link #clear()} for each test, as appropriate.
 * (That means that IMessageHandler must be loaded from a class
 * loader common to the harness and Tester.)
 * If clients submit a failing check, this registers the message
 * and throws an AbortException holding the message; this
 * AbortException <b>will not</b> have the correct stack trace;
 * all the information should be encoded in the message.
 * Find any original exception thrown in the message itself.
 */
  // XXX consider creating exception for new API throwFailure(String m)
public class Tester {
    /** delegate for reporting results */
    private static IMessageHandler messageHandler;

    /** base directory for calculating relative paths to event files */
    private static File BASEDIR;

    /**
     * collection of notes submitted
     */
    private static Set<String> notes;

    /** <code>List</code> to hold events submitted. */
    private static List<String> actualEvents = new ArrayList<>();

    /** <code>List</code> to hold events we expect. */
    private static List<String> expectedEvents = new ArrayList<>();

    static {
        setBASEDIR(new File("."));
        setMessageHandler(IMessageHandler.SYSTEM_ERR);
        clear();
    }

    /**
     * Set directory used for calculating relative paths
     * (currently only to an events file)
     * @param baseDir the File for an existing directory
     */
    public static void setBASEDIR(File baseDir) {
        if (null == baseDir) throw new IllegalArgumentException("null baseDir");
        if (!baseDir.isDirectory()) throw new IllegalArgumentException("not a directory: " + baseDir);
        BASEDIR = baseDir;
    }

    public static File getBASEDIR() {
        return BASEDIR;
    }


    /**
     * Set the message handler used for this Tester.
     * When given a message of kind FAIL, this handler
     * must complete abruptly or return false (i.e., not handled completely)
     * so the Tester throws an AbortException.
     * @see checkFailed(..).
     */
    public static void setMessageHandler(IMessageHandler handler) {
        if (null == handler) throw new IllegalArgumentException("null handler");
        if (messageHandler != handler) messageHandler = handler;
    }


    public static void clear() {
        clearNotes();
        clearEvents();
    }

    /** XXX deprecated #clear() */
    public static void clearNotes() {
        notes = new HashSet<>();
    }

    /** XXX deprecated #clear() */
    public static void clearEvents() {
        actualEvents = new ArrayList<>();
        expectedEvents = new ArrayList<>();
    }


    /** Add an actual event */
    public static void event(String s) {
        actualEvents.add(s);
    }

    /**
     * Add a note to {@link #notes}.
     * @param note Message to add.
     * XXX deprecated event(String)
     */
    public static void note(String note) {
        notes.add(note);
    }

    /**
     * Checks that <code>note</code> was added using {@link #note},
     * and fails using <code>note.toString()</code> is it wasn't found.
     *
     * @param note Message that should've been added using {@link #note}.
     * XXX deprecated checkEvent(String)
     */
    public static void check(String note) {
        check(note, "expected note \"" + note.toString() + "\"");
    }

    /**
     * Checks that <code>note</code> was added using {@link #note},
     * and fails using <code>message</code> is it wasn't found.
     *
     * @param note    Message that should've been added using {@link #note}.
     * @param message Message with which to fail if <code>node</code>
     *                wasn't added.
     */
    public static void check(String note, String message) {
        check(notes.contains(note), message);
    }

    /**
     * Reports that <code>t</code> shouldn't have been thrown.
     * using <code>t</code> as the message.
     *
     * @param t Thrown exception.
     * @see #throwable(Throwable,String)
     */
    public static void throwable(Throwable t) {
        throwable(t, null);
    }


    /**
     * Reports that <code>t</code> shouldn't have been thrown.
     * using <code>msg</code> as the message.
     *
     * @param thrown   Thrown exception.
     * @param msg Message with which to report error.
     */
    public static void throwable(Throwable thrown, String msg) {
        handle(msg, thrown, true);
   }

    /**
     * Report the error <code>message</code> unconditionally.
     *
     * @param message Error to report.
     */
    public static void checkFailed(String message) {
        handle(message, null, true);
    }

    /**
     * Check that <code>expectedNotes</code> is equal to {@link #notes}
     * , fail with <code>msg</code> and create a new instance of {@link #notes}.
     * <i>NOTE: <code>expectedNotes</code> is a <code>String</code>, so
     * it must match with {@link java.util.HashSet#toString()}</i>.
     *
     * @param expectedNotes <code>String</code> we expect
     *                      {@link #notes} to match.
     * @param msg           Message with which to fail.
     */
    public static void checkAndClear(String expectedNotes, String msg) {
        checkEqual(notes, expectedNotes, msg);
        clearNotes();
    }

    /**
     * Reports an error using <code>message</code> if
     * <code>test == false</code>.
     *
     * @param test    Determines whether we call {@link #checkFailed}.
     * @param message Message to pass {@link #checkFailed} if
     *                <code>test == false</code>.
     */
    public static void check(boolean test, String message) {
        if (!test) checkFailed(message);
    }

    /**
     * Checks that the values of <code>value</code> and
     * <code>expectedValue</code> are equal.  Both or either
     * can be null. Calls {@link #checkFailed} with <code>message</code>
     * if the arrays aren't equal.
     *
     * @param value         One test set.
     * @param expectedValue The other test set.
     * @param message       Message with which to fail.
     */
    public static void checkEqual(Object[] value,
                                  Object[] expectedValue,
                                  String message)
    {
        if (value == null) {
            if (expectedValue == null) return;
            checkFailed(message+" null array found");
            return;
        }
        int n = value.length;
        if (n != expectedValue.length) {
            checkFailed(message+" expected array of length "+expectedValue.length
                                +" got "+ n);
            return;
        }
        for(int i=0; i<n; i++) {
            if (!value[i].equals(expectedValue[i])) {
                checkFailed(message+": "+value[i]+" != "+
                            expectedValue[i]+" at index "+i);
            }
        }
    }

    /**
     * Fails if <code>s == t</code>.
     *
     * @param s a known value.
     * @param t another known value.
     */
    public static void checkNotEqual(boolean s, boolean t) {
        checkNotEqual(s, t, s + " shouldn't equal " + t);
    }
    /**
     * Fails with message <code>msg</code> if <code>s == t</code>.
     *
     * @param s   a known value.
     * @param t   another known value.
     * @param msg the failure message.
     */
    public static void checkNotEqual(boolean s, boolean t, String msg) {
        if (s == t) checkFailed(msg);
    }

    /**
     * Fails if <code>s == t</code>.
     *
     * @param s a known value.
     * @param t another known value.
     */
    public static void checkNotEqual(byte s, byte t) {
        checkNotEqual(s, t, s + " shouldn't equal " + t);
    }
    /**
     * Fails with message <code>msg</code> if <code>s == t</code>.
     *
     * @param s   a known value.
     * @param t   another known value.
     * @param msg the failure message.
     */
    public static void checkNotEqual(byte s, byte t, String msg) {
        if (s == t) checkFailed(msg);
    }

    /**
     * Fails if <code>s == t</code>.
     *
     * @param s a known value.
     * @param t another known value.
     */
    public static void checkNotEqual(char s, char t) {
        checkNotEqual(s, t, s + " shouldn't equal " + t);
    }
    /**
     * Fails with message <code>msg</code> if <code>s == t</code>.
     *
     * @param s   a known value.
     * @param t   another known value.
     * @param msg the failure message.
     */
    public static void checkNotEqual(char s, char t, String msg) {
        if (s == t) checkFailed(msg);
    }

    /**
     * Fails if <code>s == t</code>.
     *
     * @param s a known value.
     * @param t another known value.
     */
    public static void checkNotEqual(short s, short t) {
        checkNotEqual(s, t, s + " shouldn't equal " + t);
    }
    /**
     * Fails with message <code>msg</code> if <code>s == t</code>.
     *
     * @param s   a known value.
     * @param t   another known value.
     * @param msg the failure message.
     */
    public static void checkNotEqual(short s, short t, String msg) {
        if (s == t) checkFailed(msg);
    }

    /**
     * Fails if <code>s == t</code>.
     *
     * @param s a known value.
     * @param t another known value.
     */
    public static void checkNotEqual(int s, int t) {
        checkNotEqual(s, t, s + " shouldn't equal " + t);
    }
    /**
     * Fails with message <code>msg</code> if <code>s == t</code>.
     *
     * @param s   a known value.
     * @param t   another known value.
     * @param msg the failure message.
     */
    public static void checkNotEqual(int s, int t, String msg) {
        if (s == t) checkFailed(msg);
    }

    /**
     * Fails if <code>s == t</code>.
     *
     * @param s a known value.
     * @param t another known value.
     */
    public static void checkNotEqual(long s, long t) {
        checkNotEqual(s, t, s + " shouldn't equal " + t);
    }
    /**
     * Fails with message <code>msg</code> if <code>s == t</code>.
     *
     * @param s   a known value.
     * @param t   another known value.
     * @param msg the failure message.
     */
    public static void checkNotEqual(long s, long t, String msg) {
        if (s == t) checkFailed(msg);
    }

    /**
     * Fails if <code>s == t</code>.
     *
     * @param s a known value.
     * @param t another known value.
     */
    public static void checkNotEqual(float s, float t) {
        checkNotEqual(s, t, s + " shouldn't equal " + t);
    }
    /**
     * Fails with message <code>msg</code> if <code>s == t</code>.
     *
     * @param s   a known value.
     * @param t   another known value.
     * @param msg the failure message.
     */
    public static void checkNotEqual(float s, float t, String msg) {
        if (s == t) checkFailed(msg);
    }

    /**
     * Fails if <code>s == t</code>.
     *
     * @param s a known value.
     * @param t another known value.
     */
    public static void checkNotEqual(double s, double t) {
        checkNotEqual(s, t, s + " shouldn't equal " + t);
    }
    /**
     * Fails with message <code>msg</code> if <code>s == t</code>.
     *
     * @param s   a known value.
     * @param t   another known value.
     * @param msg the failure message.
     */
    public static void checkNotEqual(double s, double t, String msg) {
        if (s == t) checkFailed(msg);
    }

    /**
     * Fails if <code>s == t</code>.
     *
     * @param s a known value.
     * @param t another known value.
     */
    public static void checkNotEqual(Object s, Object t) {
        checkNotEqual(s, t, s + " shouldn't equal " + t);
    }
    /**
     * Fails with message <code>msg</code> if <code>s == t</code>
     * or both <code>s</code> and <code>t</code> are <code>null</code>.
     *
     * @param s   a known value.
     * @param t   another known value.
     * @param msg the failure message.
     */
    public static void checkNotEqual(Object s, Object t, String msg) {
        if ((s != null && s.equals(t)) ||
            (t != null && t.equals(s)) ||
            (s == null && t == null)) {
            checkFailed(msg);
        }
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>"compare"</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @see   #checkEqual(int,int,String)
     */
    public static void checkEqual(int value, int expectedValue) {
        checkEqual(value, expectedValue, "compare");
    }

    /**
     * Fails if the passed in value is <code>null</code>.
     *
     * @param o    the expected non-null thing.
     * @param name the name of <code>o</code>.
     */
    public static void checkNonNull(Object o, String name) {
        if (o == null) checkFailed(name + " shouldn't be null");
    }

    /**
     * Compared <code>value</code> and <code>expectedValue</code>
     * and fails with <code>message</code> if they aren't equal.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @param msg           Message with which to fail.
     */
    public static void checkEqual(int value, int expectedValue, String message) {
        if (value == expectedValue) return;
        if (value < expectedValue) {
            message = message+": "+value+" < "+expectedValue;
        } else {
            message = message+": "+value+" > "+expectedValue;
        }
        checkFailed(message);
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>"compare"</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @see   #checkEqual(float,float,String)
     */
    public static void checkEqual(float value, float expectedValue) {
        checkEqual(value, expectedValue, "compare");
    }

    /**
     * Compared <code>value</code> and <code>expectedValue</code>
     * and fails with <code>message</code> if they aren't equal.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @param msg           Message with which to fail.
     */
    public static void checkEqual(float value, float expectedValue, String msg) {
        if (Float.isNaN(value) && Float.isNaN(expectedValue)) return;
        if (value == expectedValue) return;
        if (value < expectedValue) {
            msg = msg+": "+value+" < "+expectedValue;
        } else {
            msg = msg+": "+value+" > "+expectedValue;
        }
        checkFailed(msg);
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>"compare"</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @see   #checkEqual(long,long,String)
     */
    public static void checkEqual(long value, long expectedValue) {
        checkEqual(value, expectedValue, "compare");
    }

    /**
     * Compared <code>value</code> and <code>expectedValue</code>
     * and fails with <code>message</code> if they aren't equal.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @param msg           Message with which to fail.
     */
    public static void checkEqual(long value, long expectedValue, String msg) {
        if (value == expectedValue) return;
        if (value < expectedValue) {
            msg = msg+": "+value+" < "+expectedValue;
        } else {
            msg = msg+": "+value+" > "+expectedValue;
        }
        checkFailed(msg);
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>"compare"</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @see   #checkEqual(double,double,String)
     */
    public static void checkEqual(double value, double expectedValue) {
        checkEqual(value, expectedValue, "compare");
    }

    /**
     * Compared <code>value</code> and <code>expectedValue</code>
     * and fails with <code>message</code> if they aren't equal.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @param msg           Message with which to fail.
     */
    public static void checkEqual(double value, double expectedValue, String msg) {
        if (Double.isNaN(value) && Double.isNaN(expectedValue)) return;
        if (value == expectedValue) return;
        if (value < expectedValue) {
            msg = msg+": "+value+" < "+expectedValue;
        } else {
            msg = msg+": "+value+" > "+expectedValue;
        }
        checkFailed(msg);
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>"compare"</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @see   #checkEqual(short,short,String)
     */
    public static void checkEqual(short value, short expectedValue) {
        checkEqual(value, expectedValue, "compare");
    }

    /**
     * Compared <code>value</code> and <code>expectedValue</code>
     * and fails with <code>message</code> if they aren't equal.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @param msg           Message with which to fail.
     */
    public static void checkEqual(short value, short expectedValue, String msg) {
        if (value == expectedValue) return;
        if (value < expectedValue) {
            msg = msg+": "+value+" < "+expectedValue;
        } else {
            msg = msg+": "+value+" > "+expectedValue;
        }
        checkFailed(msg);
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>"compare"</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @see   #checkEqual(byte,byte,String)
     */
    public static void checkEqual(byte value, byte expectedValue) {
        checkEqual(value, expectedValue, "compare");
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>msg</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @param msg           Message with which to fail.
     */
    public static void checkEqual(byte value, byte expectedValue, String msg) {
        if (value == expectedValue) return;
        if (value < expectedValue) {
            msg = msg+": "+value+" < "+expectedValue;
        } else {
            msg = msg+": "+value+" > "+expectedValue;
        }
        checkFailed(msg);
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>"compare"</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @see   #checkEqual(char,char,String)
     */
    public static void checkEqual(char value, char expectedValue) {
        checkEqual(value, expectedValue, "compare");
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>msg</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @param msg           Message with which to fail.
     */
    public static void checkEqual(char value, char expectedValue, String msg) {
        if (value == expectedValue) return;
        if (value < expectedValue) {
            msg = msg+": "+value+" < "+expectedValue;
        } else {
            msg = msg+": "+value+" > "+expectedValue;
        }
        checkFailed(msg);
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>"compare"</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @see   #checkEqual(boolean,boolean,String)
     */
    public static void checkEqual(boolean value, boolean expectedValue) {
        checkEqual(value, expectedValue, "compare");
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>msg</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @param msg           Message with which to fail.
     */
    public static void checkEqual(boolean value, boolean expectedValue, String msg) {
        if (value == expectedValue) return;
        msg = msg+": "+value+" != "+expectedValue;
        checkFailed(msg);
    }

    /**
     * Checks whether the entries of <code>set</code> are equal
     * using <code>equals</code> to the corresponding String in
     * <code>expectedSet</code> and fails with message <code>msg</code>.
     *
     * @param set         Unkown set of values.
     * @param expectedSet Expected <code>String</code> of values.
     * @param msg         Message with which to fail.
     */
    public static void checkEqual(Collection<String> set, String expectedSet, String msg) {
        checkEqual(set, LangUtil.split(expectedSet), msg);
    }

    /**
     * Checks whether the entries of <code>set</code> are equal
     * using <code>equals</code> to the corresponding entry in
     * <code>expectedSet</code> and fails with message <code>msg</code>,
     * except that duplicate actual entries are ignored.
     * This issues fail messages for each failure; when
     * aborting on failure, only the first will be reported.
     *
     * @param set         Unkown set of values.
     * @param expectedSet Expected <code>String</code> of values.
     * @param msg         Message with which to fail.
     */
    public static void checkEqualIgnoreDups(Collection<String> set, String[] expected, String msg,
                                              boolean ignoreDups) {
        String[] diffs = diffIgnoreDups(set, expected, msg, ignoreDups);
        if (0 < diffs.length) {
            check(false, "" + Arrays.asList(diffs));
        }
//        for (int i = 0; i < diffs.length; i++) {
//			check(false, diffs[i]);
//		}
    }

    /** @return String[] of differences '{un}expected msg "..." {not} found' */
    private static String[] diffIgnoreDups(Collection<String> set, String[] expected, String msg,
        boolean ignoreDups) {
        ArrayList<String> result = new ArrayList<>();
        List<String> actual = new ArrayList<>(set);
        BitSet hits = new BitSet();
        for (int i = 0; i < expected.length; i++) {
            if (!actual.remove(expected[i])) {
                result.add(" expected " + msg + " \"" + expected[i] + "\" not found");
            } else {
                hits.set(i);
                if (ignoreDups) {
                   while (actual.remove(expected[i])) ; // remove all instances of it
                }
            }
        }
        for (String act: actual) {
            result.add(" unexpected " + msg + " \"" + act + "\" found");
		}
        return result.toArray(new String[0]);
    }

    /**
     * Checks whether the entries of <code>set</code> are equal
     * using <code>equals</code> to the corresponding entry in
     * <code>expectedSet</code> and fails with message <code>msg</code>.
     *
     * @param set         Unkown set of values.
     * @param expectedSet Expected <code>String</code> of values.
     * @param msg         Message with which to fail.
     */
    public static void checkEqual(Collection<String> set, String[] expected, String msg) {
        checkEqualIgnoreDups(set, expected, msg, false);
    }

    /**
     * Compares <code>value</code> and <code>expectedValue</code>
     * with failing message <code>"compare"</code>.
     *
     * @param value         Unkown value.
     * @param expectedValue Expected value.
     * @see   #checkEqual(Object,Object,String)
     */
    public static void checkEqual(Object value, Object expectedValue) {
        checkEqual(value, expectedValue, "compare");
    }

    /**
     * Checks whether the entries of <code>set</code> are equal
     * using <code>equals</code> to the corresponding String in
     * <code>expectedSet</code> and fails with message <code>msg</code>.
     *
     * @param set         Unkown set of values.
     * @param expectedSet Expected <code>String</code> of values.
     * @param msg         Message with which to fail.
     */
    public static void checkEqual(Object value, Object expectedValue, String msg) {
        if (value == null && expectedValue == null) return;
        if (value != null && value.equals(expectedValue)) return;
        msg = msg+": "+value+" !equals "+expectedValue;
        checkFailed(msg);
    }

    /**
     * Checks whether the entries of <code>set</code> are equal
     * using <code>equals</code> to the corresponding String in
     * <code>expectedSet</code> and fails with message <code>msg</code>.
     *
     * @param set         Unkown set of values.
     * @param expectedSet Expected <code>String</code> of values.
     * @param msg         Message with which to fail.
     */
    public static void checkEq(Object value, Object expectedValue, String msg) {
        if (value == expectedValue) return;
        msg = msg+": "+value+" != "+expectedValue;
        checkFailed(msg);
    }

     /** add expected events */
    public static void expectEvent(String s) {
        if (null != s) {
            expectedEvents.add(s);
        }
    }

     /** add expected events */
    public static void expectEvent(Object s) {
        if (null != s) {
            expectEvent(s.toString());
        }
    }

    /**
     * add expected events, parse out ; from string
     * Expect those messages in <code>s</code> separated by
     * <code>":;, "</code>.
     *
     * @param s String containg delimited,expected messages.
     */
    public static void expectEventsInString(String s) {
        if (null != s) {
            StringTokenizer tok = new StringTokenizer(s, ":;, ");
            while (tok.hasMoreTokens()) {
                expectEvent(tok.nextToken());
            }
        }
    }

    public static void expectEventsInString(String[] ra) {
        expectEvents((Object[]) ra);
    }

   /** add expected events */
    public static void expectEvents(Object[] events) {
        if (null != events) {
			for (Object event : events) {
				if (null != event) {
					expectEvent(event.toString());
				}
			}
        }
    }

    /** add expected events */
    public static void expectEvents(String[] events) {
        if (null != events) {
			for (String event : events) {
				if (null != event) {
					expectEvent(event.toString());
				}
			}
        }
    }

    /** check actual and expected have same members */
    public static void checkAllEvents() {
        checkAndClearEvents(expectedEvents.toArray(new String[0]));
    }

    /** also ignore duplicate actual entries for expected */
    public static void checkAllEventsIgnoreDups() {
        final boolean ignoreDups = true;
        final String[] exp = expectedEvents.toArray(new String[0]);
        checkEqualIgnoreDups(actualEvents, exp, "event", ignoreDups);
        clearEvents();
    }

    /** Check events, file is line-delimited.  If there is a non-match, signalls
     * a single error for the first event that does not match the next event in
     * the file.  The equivalence is {@link #checkEqualLists}.  Blank lines are
     * ignored.  lines that start with '//' are ignored.  */
    public static void checkEventsFromFile(String eventsFile) {
        // XXX bug reads into current expected and checks all - separate read and check
        try {
            File file = new File(getBASEDIR(), eventsFile);   // XXX TestDriver
            BufferedReader in = new BufferedReader(new FileReader(file));
            //final File parentDir = (null == file? null : file.getParentFile());
            String line;
            List<String> expEvents = new ArrayList<>();
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if ((line.length() < 1) || (line.startsWith("//"))) continue;
                expEvents.add(line);
            }
            in.close();
            checkEqualLists(actualEvents, expEvents, " from " + eventsFile);
        } catch (IOException ioe) {
            throwable(ioe);
        }
    }


    /** Check to see that two lists of strings are the same.  Order is important.
     *  Trimmable whitespace is not important.  Case is important.
     *
     * 	@param actual one list to check
     *  @param expected another list
     *  @param message a context string for the resulting error message if the test fails.
     */
    public static void checkEqualLists(List<String> actual, List<String> expected,
    					String message) {
       	Iterator<String> a = actual.iterator();
       	Iterator<String> e = expected.iterator();
        int ai = 0;
        int ei = 0;
       	for (; a.hasNext(); ) {
       	    if (! e.hasNext()) {
       	        checkFailed("unexpected [" + ai + "] \"" + a.next() + "\" " + message);
       	        return;
       	    }
       	    String a0 = a.next().trim();
       	    String e0 = e.next().trim();
       	    if (! a0.equals(e0)) {
       	        checkFailed("expected [" + ei + "] \"" + e0
                        + "\"\n  but found [" + ai + "] \"" + a0 + "\"\n  " + message);
       	        return;
       	    }
            ai++;
            ei++;
       	}
       	while (e.hasNext()) {
       	     checkFailed("expected [" + ei + "] \"" + e.next() + "\" " + message);
             ei++;
       	}
    }

    /** Check events, expEvents is space delimited */
    public static void checkEvents(String expEvents) {
        checkEqual(actualEvents, expEvents, "event");
    }

    /** Check events, expEvents is an array */
    public static void checkEvents(String[] expEvents) {
        checkEqual(actualEvents, expEvents, "event");
    }

    /** Check events and clear after check*/
    public static void checkAndClearEvents(String expEvents) {
        checkEvents(expEvents);
        clearEvents();
    }

    /** Check events and clear after check*/
    public static void checkAndClearEvents(String[] expEvents) {
        checkEvents(expEvents);
        clearEvents();
    }

    /** XXX deprecated */
    public static void printEvents() {  // XXX no clients?
		for (String actualEvent : actualEvents) {
			System.out.println(actualEvent);               // XXX System.out
		}
    }

    /**
     * Report an uncaught exeption as an error
     * @param thrown <code>Throwable</code> to print.
     * @see   #maxStackTrace
     */
    public void unexpectedExceptionFailure(Throwable thrown) {
        handle("unexpectedExceptionFailure", thrown, true);
    }

    /**
     * Handle message by delegation to message handler, doing
     * IMessage.FAIL if (fail || (thrown != null) and IMessage.INFO
     * otherwise.
     */

    private static void handle(String message, Throwable thrown, boolean fail) {
        final boolean failed = fail || (null != thrown);
        IMessage.Kind kind = (failed ? IMessage.FAIL : IMessage.INFO);
        IMessage m = new Message(message, kind, thrown, null);
        /*final boolean handled = */messageHandler.handleMessage(m);
    }
//    private static void resofhandle(String message, Throwable thrown, boolean fail) {
//     /* If FAIL and the message handler returns false (normally),
//     * Then this preserves "abort" semantics by throwing an
//     * abort exception.
//     */
//        if (failed) {
//            if (handled) {
//                String s = "Tester expecting handler to return false or "
//                            + "complete abruptly when passed a fail, for " + m;
//                m = new Message(s, IMessage.DEBUG, null, null);
//                messageHandler.handleMessage(m);
//            } else {
//                throw AbortException.borrowPorter(m);
//            }
//        }
//    }

}
