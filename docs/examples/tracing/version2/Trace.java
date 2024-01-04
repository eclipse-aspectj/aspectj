/*
Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.
*/

package tracing.version2;

import java.io.PrintStream;

/**
 *
 * This class provides support for printing trace messages into a stream.
 * Trace messages are printed before and after constructors and methods
 * are executed.
 * It defines one abstract crosscut for injecting that tracing functionality
 * into any application classes.
 * To use it, provide a subclass that concretizes the abstract crosscut.
 */
abstract aspect Trace {

    /*
     * Functional part
     */

    /**
     * There are 3 trace levels (values of TRACELEVEL):
     * 0 - No messages are printed
     * 1 - Trace messages are printed, but there is no indentation
     *     according to the call stack
     * 2 - Trace messages are printed, and they are indented
     *     according to the call stack
     */
    public static int TRACELEVEL = 2;
    protected static PrintStream stream = System.err;
    protected static int callDepth = 0;

    /**
     * Initialization.
     */
    public static void initStream(PrintStream s) {
        stream = s;
    }

    protected static void traceEntry(String str) {
        if (TRACELEVEL == 0) return;
        if (TRACELEVEL == 2) callDepth++;
        printEntering(str);
    }

    protected static void traceExit(String str) {
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


    /*
     * Crosscut part
     */

    /**
     * Application classes - left unspecified.
     * Subclasses should concretize this pointcut with class names.
     */
    abstract pointcut myClass();
    /**
     * The constructors in those classes.
     */
    pointcut myConstructor(): myClass() && execution(new(..));
    /**
     * The methods of those classes.
     */
    pointcut myMethod(): myClass() && execution(* *(..));

    /**
     * Prints trace messages before and after executing constructors.
     */
    before(): myConstructor() {
        traceEntry("" + thisJoinPointStaticPart.getSignature());
    }
    after(): myConstructor() {
        traceExit("" + thisJoinPointStaticPart.getSignature());
    }

    /**
     * Prints trace messages before and after executing methods.
     */
    before(): myMethod() {
        traceEntry("" + thisJoinPointStaticPart.getSignature());
    }
    after(): myMethod() {
        traceExit("" + thisJoinPointStaticPart.getSignature());
    }
}
