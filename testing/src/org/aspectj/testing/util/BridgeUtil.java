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

package org.aspectj.testing.util;

import java.io.File;
import java.util.Comparator;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.RunValidator;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LineReader;

/**
 * 
 */
public class BridgeUtil {

    private static final String INDENT = "    ";

    /** result value when writeMessage is passed null */
    private static final String NULL_MESSAGE_OUTPUT = "<null message output>";
    
    /** result value when readMessage is passed null */
    private static final IMessage NULL_MESSAGE_INPUT = null;
    
    private static final String KIND_DELIM = ": \"";
    private static final String MESSAGE_DELIM = "\" - ";

    
    public static ISourceLocation makeSourceLocation(LineReader reader) {
        LangUtil.throwIaxIfNull(reader, "reader");
        int line = reader.getLineNumber();
        
        return new SourceLocation(reader.getFile(), line, line, 0);
    }


    /**
     * Method readSourceLocation.
     * @param sourceLocStr
     * @return ISourceLocation
     */
    private static ISourceLocation readSourceLocation(String sourceLocStr) {
        return BridgeUtil.makeSourceLocation(sourceLocStr);
    }
    public static IMessage makeMessage(String message, IMessage.Kind kind, 
                                       Throwable thrown, LineReader reader) {
        ISourceLocation sl = (null == reader ? null : MessageUtil.makeSourceLocation(reader));
        if (null == kind) kind = IMessage.INFO;
        return new Message(message, kind, thrown, sl);        
    }

    /**
     * Read a message from a string written by writeMessage(IMessage).
     * Does not handle exceptions at all or source location well. XXX
     * @param message the String representation of a message
     * @return IMessage
     */
    public static IMessage readMessage(String message) {
       if (null == message) {
            return NULL_MESSAGE_INPUT;
       }
       if (NULL_MESSAGE_OUTPUT.equals(message)) {
            return null;
       }
       int kindEnd = message.indexOf(KIND_DELIM);
       int messageEnd = message.indexOf(MESSAGE_DELIM);
       int messageStart = kindEnd+KIND_DELIM.length();
       int sourceLocStart = messageEnd+MESSAGE_DELIM.length();
       String kindStr = message.substring(0, kindEnd);
       String text = message.substring(messageStart, messageEnd);
       String sourceLocStr = message.substring(sourceLocStart);
       IMessage.Kind kind = MessageUtil.getKind(kindStr);
       ISourceLocation loc = readSourceLocation(sourceLocStr);
       return new Message(text, kind, null, loc);
    }
    

    /**
     * Write a message to a string to be read by readMessage(String)
     * @param message the String representation of a message
     * @return IMessage
     */
    public static String writeMessage(IMessage message) {
        if (null == message) {
            return NULL_MESSAGE_OUTPUT;
        }
        return message.getKind() 
            + KIND_DELIM 
            + message.getMessage() 
            + MESSAGE_DELIM
            + message.getISourceLocation(); // XXX implement
    }
    

    public static class Comparators {        
        /** 
         * This is a weak ordering
         * so use only for sorts, not to maintain maps.
         * It returns 0 if one file path is a suffix of the other
         * or a case-insensitive string comparison otherwise.
         */
        public static final Comparator WEAK_File = new Comparator() {
            public int compare(Object o1, Object o2) {
                File one = (File) o1;
                File two = (File) o2;
                if (null == one) {
                    return (null == two ? 0 : 1);
                } else if (null == two) {
                    return -1;
                } else if (one == two) {
                    return 0;
                } else {
                    String s1 = FileUtil.weakNormalize(one.getPath());
                    String s2 = FileUtil.weakNormalize(two.getPath());
                    if (s1.endsWith(s2) || s2.endsWith(s1)) {
                        return 0;
                    } else {
                        return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
                    }
                }
        }};
        /** 
         * Ordering ignores filename,
         * so use only for sorts, not to maintain maps
         */
        public static final Comparator WEAK_ISourceLocation = new Comparator() {
            public int compare(Object o1, Object o2) {
                ISourceLocation one = (ISourceLocation) o1;
                ISourceLocation two = (ISourceLocation) o2;
                if (null == one) {
                    return (null == two ? 0 : 1);
                } else if (null == two) {
                    return -1;
                } else if (one == two) {
                    return 0;
                } else { // XXX start with File to make strong
                    int i1 = one.getLine();
                    int i2 = two.getLine(); 
                    return i1 - i2;
//                    // defer subcomparisons until messages complete                                       
//                    if (i1 != i2) {
//                        return i1 - i2;
//                    } else {
//                        i1 = one.getColumn();
//                        i2 = two.getColumn();
//                        if (i1 != i2) {
//                            return i1 - i2;
//                        } else {
//                            i1 = one.getEndLine();
//                            i2 = two.getEndLine();
//                            return i1 - i2;
//                        }
//                    }
                }
        }};

        /** 
         * Ordering ignores filename and message
         * so use only for sorts, not to maintain maps
         */
        public static final Comparator WEAK_IMessage = new Comparator() {
            public int compare(Object o1, Object o2) {
                IMessage one = (IMessage) o1;
                IMessage two = (IMessage) o2;
                if (null == one) {
                    return (null == two ? 0 : 1);
                } else if (null == two) {
                    return -1;
                } else if (one == two) {
                    return 0;
                } else {
                    IMessage.Kind kind1 = one.getKind();
                    IMessage.Kind kind2= two.getKind();
                    if (kind1 != kind2) {
                        return IMessage.Kind.COMPARATOR.compare(kind1, kind2);
                    } else {
                        ISourceLocation sl1 = one.getISourceLocation();
                        ISourceLocation sl2 = two.getISourceLocation();
                        return WEAK_ISourceLocation.compare(sl1, sl2);
                    }
                }
        }};
        
    }

    public static SourceLocation makeSourceLocation(String input) { // XXX only for testing, not production
        return makeSourceLocation(input, (File) null);
    }
    
    public static SourceLocation makeSourceLocation(String input, String path) { 
        return makeSourceLocation(input, (null == path ? null : new File(path)));        
    }
    
	/** attempt to create a source location from the input */
    public static SourceLocation makeSourceLocation(String input, File defaultFile) { 
    /*
     * Forms interpreted:
     * # - line
     * file - file
     * file:# - file, line
     * #:# - if defaultFile is not null, then file, line, column
     * file:#:# - file, line, column
     * file:#:#:? - file, line, column, message
     */
        SourceLocation result = null;
        if ((null == input) || (0 == input.length())) {
            if (null == defaultFile) {
                return null;
            } else {
                return new SourceLocation(defaultFile, 0, 0, 0);
            }
        }
        input = input.trim();
        
        String path = null;
        int line = 0;
        int endLine = 0;
        int column = 0;
        String message = null;
        
        // first try line only
        line = convert(input);
        if (-1 != line) {
            return new SourceLocation(defaultFile, line, line, 0);
        }
        
        // if not a line - must be > 2 characters
        if (3 > input.length()) {
            return null; // throw new IllegalArgumentException("too short: " + input);
        }
        final String fixTag = "FIXFIX";
        if (input.charAt(1) == ':') { // windows drive ambiguates ":" file:line:col separator
            input = fixTag + input.substring(0,1) + input.substring(2);
        }
        // expecting max: path:line:column:message
        // if 1 colon, delimits line (to second colon or end of string)
        // if 2 colon, delimits column (to third colon or end of string)
        // if 3 colon, delimits column (to fourth colon or end of string)
        // todo: use this instead??
        final int colon1 = input.indexOf(":",2); // 2 to get past windows drives...
        final int colon2 = (-1 == colon1?-1:input.indexOf(":", colon1+1));
        final int colon3 = (-1 == colon2?-1:input.indexOf(":", colon2+1));
        String s;
        if (-1 == colon1) {      // no colon; only path (number handled above)
            path = input;
        } else {                 // 1+ colon => file:line // XXX later or line:column
            path = input.substring(0, colon1);
            s = input.substring(colon1+1,(-1!=colon2?colon2:input.length())).trim();
            line = convert(s);
            if (-1 == line) {
               return null;
               //line = "expecting line(number) at \"" + line + "\" in " + input;
               //throw new IllegalArgumentException(line);
            } else if (-1 != colon2) { // 2+ colon => col
                s = input.substring(colon2+1,(-1!=colon3?colon3:input.length())).trim();
                column = convert(s);
                if (-1 == column) {
                   return null;
                   //col = "expecting col(number) at \"" + col + "\" in " + input;
                   //throw new IllegalArgumentException(col);
                } else if (-1 != colon3) { // 3 colon => message
                    message = input.substring(colon3+1); // do not trim message
                }
            }
        }

        if (path.startsWith(fixTag)) {
            int len = fixTag.length();
            path = path.substring(len, 1+len) + ":" + 
                    path.substring(1+len);
        }
        if ((endLine == 0) && (line != 0)) {
            endLine = line;
        }
        // XXX removed message/comment
        return new SourceLocation(new File(path), line, endLine, column);
    }

    // XXX reconsider convert if used in production code
    /**
     * Convert String to int using ascii and optionally
     * tolerating text
     * @param s the String to convert
     * @param permitText if true, pick a sequence of numbers 
     *         within a possibly non-numeric String
     * @param last if permitText, then if this is true the
     *         last sequence is used - otherwise the first is used
     * XXX only default u.s. encodings..
     * @return -1 or value if a valid, totally-numeric positive string 0..MAX_WIDTH 
     */
    private static int convert(String s) {
        return convert(s, false, false);
    }
    
	// XXX reconsider convert if used in production code
    /**
     * Convert String to int using ascii and optionally
     * tolerating text
     * @param s the String to convert
     * @param permitText if true, pick a sequence of numbers 
     *         within a possibly non-numeric String
     * @param last if permitText, then if this is true the
     *         last sequence is used - otherwise the first is used
     * XXX only default u.s. encodings..
     * @return -1 or value if a valid, positive string 0..MAX_WIDTH 
     */
    private static int convert(String s, boolean permitText, 
        boolean first) { 
        int result = -1;
        int last = -1;
        int max = s.length(); 
        boolean reading = false;
        for (int i = 0; i < max; i++) {
            char c = s.charAt(i);
            if ((c >= '0') && (c <= '9')) {
                if (-1 == result) { // prefix loop
                    result = 0;
                    reading = true;
                }
                result = ((result * 10) + (c - '0'));
            } else if (!permitText) {
                return -1;
            } else if (reading) { // from numeric -> non-numeric
                if (first) {
                    return result;
                } else {
                    last = result;
                }
                reading = false;
            }
        }
        if (permitText && !first && (-1 != last) && (-1 == result)) {
            result = last;
        }
        return ((0 < result) && (result < ISourceLocation.MAX_LINE) ? result : -1);
    }

    private BridgeUtil() {}

    /** @return String for status header, counting children passed/failed */
    public static String childString(IRunStatus runStatus, int numSkips, int numIncomplete) {
        if (null == runStatus) {
            return "((RunStatus) null)";
        }
        if (0 > numSkips) {
            numSkips = 0;
        }
        if (0 > numIncomplete) {
            numIncomplete = 0;
        }
        StringBuffer sb = new StringBuffer();
        if (RunValidator.NORMAL.runPassed(runStatus)) {
            sb.append("PASS ");
        } else {
            sb.append("FAIL ");
        }
        Object id = runStatus.getIdentifier();
        if (null != id) {
            sb.append(id.toString() + " ");
        }
        IRunStatus[] children = runStatus.getChildren();
        final int numChildren = (null == children ? 0 : children.length);
        final int numTests = numIncomplete + numChildren + numSkips;
        int numFails = 0;
        if (!LangUtil.isEmpty(children)) {
            for (int i = 0; i < children.length; i++) {
                if (!RunValidator.NORMAL.runPassed(children[i])) {
                    numFails++;
                }
			}
        }
        final int numPass = children.length - numFails;
        sb.append(numTests + " tests");
        if (0 < numTests) {
            sb.append(" (");
        }
        if (0 < numSkips) {
            sb.append(numSkips + " skipped");
            if (0 < (numFails + numPass + numIncomplete)) {
                sb.append(", ");
            }
        }
        if (0 < numIncomplete) {
            sb.append(numIncomplete + " incomplete");
            if (0 < (numFails + numPass)) {
                sb.append(", ");
            }
        }
        if (0 < numFails) {
            sb.append(numFails + " failed");
            if (0 < numPass) {
                sb.append(", ");
            }
        }
        if (0 < numPass) {
            sb.append(numPass + " passed)");
        } else if (0 < numTests) {
            sb.append(")");
        }
        return sb.toString().trim();
    }

	/** @return String for status header */
	public static String toShortString(IRunStatus runStatus) {
        if (null == runStatus) {
            return "((RunStatus) null)";
        }
        StringBuffer sb = new StringBuffer();
        if (RunValidator.NORMAL.runPassed(runStatus)) {
            sb.append("PASS ");
        } else {
            sb.append("FAIL ");
        }
        Object id = runStatus.getIdentifier();
        if (null != id) {
            sb.append(id.toString() + " ");
        }
        sb.append(MessageUtil.renderCounts(runStatus));
        return sb.toString().trim();
	}

}
