/*

Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.

|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|

*/

package tracing.version3;

import java.io.PrintStream;

/**
 *
 * This class provides support for printing trace messages into a stream. 
 * Trace messages are printed before and after constructors and methods
 * are executed.
 * The messages are appended with the string representation of the objects
 * whose constructors and methods are being traced.
 * It defines one abstract pointcut for injecting that tracing functionality 
 * into any application classes.
 *
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
    public static int TRACELEVEL = 0;
    protected static PrintStream stream = null;
    protected static int callDepth = 0;

    /**
     * Initialization.
     */
    public static void initStream(PrintStream s) {
        stream = s;
    }

    protected static void traceEntry(String str, Object o) {
        if (TRACELEVEL == 0) return;
        if (TRACELEVEL == 2) callDepth++;
        printEntering(str + ": " + o.toString());
    }

    protected static void traceExit(String str, Object o) {
        if (TRACELEVEL == 0) return;
        printExiting(str + ": " + o.toString());
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
     */
    abstract pointcut myClass(Object obj);
    /**
     * The constructors in those classes.
     */
    pointcut myConstructor(Object obj): myClass(obj) && execution(new(..));
    /**
     * The methods of those classes.
     */
    // toString is called from within our advice, so we shouldn't
    // advise its executions.  But if toString is overridden, even
    // this might not be enough, so we might want
    //    && !cflow(execution(String toString()))
    pointcut myMethod(Object obj): myClass(obj) && 
        execution(* *(..)) && !execution(String toString());

    before(Object obj): myConstructor(obj) {
        traceEntry("" + thisJoinPointStaticPart.getSignature(), obj);
    }
    after(Object obj): myConstructor(obj) {
        traceExit("" + thisJoinPointStaticPart.getSignature(), obj);
    }

    before(Object obj): myMethod(obj) {
        traceEntry("" + thisJoinPointStaticPart.getSignature(), obj);
    }
    after(Object obj): myMethod(obj) {
        traceExit("" + thisJoinPointStaticPart.getSignature(), obj);
    }
}
