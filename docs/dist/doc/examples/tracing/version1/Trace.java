/*
Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.
*/

package tracing.version1;

import java.io.PrintStream;

/**
 *
 * This class provides some basic functionality for printing trace messages
 * into a stream.
 *
 */
public class Trace {
    /**
     * There are 3 trace levels (values of TRACELEVEL):
     * 0 - No messages are printed
     * 1 - Trace messages are printed, but there is no indentation
     *     according to the call stack
     * 2 - Trace messages are printed, and they are indented
     *     according to the call stack
     */
    public static int TRACELEVEL = 0;
    protected static PrintStream stream = null;
    protected static int callDepth = 0;

    /**
     * Initialization.
     */
    public static void initStream(PrintStream s) {
        stream = s;
    }

    /**
     * Prints an "entering" message. It is intended to be called in the
     * beginning of the blocks to be traced.
     */
    public static void traceEntry(String str) {
        if (TRACELEVEL == 0) return;
        if (TRACELEVEL == 2) callDepth++;
        printEntering(str);
    }

    /**
     * Prints an "exiting" message. It is intended to be called in the
     * end of the blocks to be traced.
     */
    public static void traceExit(String str) {
        if (TRACELEVEL == 0) return;
        printExiting(str);
        if (TRACELEVEL == 2) callDepth--;
    }

    private static void printEntering(String str) {
        printIndent();
        stream.println("--> " + str);
    }

    private static void printExiting(String str) {
        printIndent();
        stream.println("<-- " + str);
    }

    private static void printIndent() {
        for (int i = 0; i < callDepth; i++)
            stream.print("  ");
    }
}
