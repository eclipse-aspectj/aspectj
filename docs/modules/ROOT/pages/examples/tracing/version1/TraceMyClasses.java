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

/**
 *
 * This class connects the tracing functions in the Trace class with
 * the constructors and methods in the application classes.
 *
 */
import tracing.TwoDShape;
import tracing.Circle;
import tracing.Square;
import tracing.ExampleMain;

aspect TraceMyClasses {
    /**
     * Application classes.
     */
    pointcut myClass(): within(TwoDShape) || within(Circle) || within(Square);
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
    before (): myConstructor() {
        Trace.traceEntry("" + thisJoinPointStaticPart.getSignature());
    }
    after(): myConstructor() {
        Trace.traceExit("" + thisJoinPointStaticPart.getSignature());
    }

    /**
     * Prints trace messages before and after executing methods.
     */
    before (): myMethod() {
        Trace.traceEntry("" + thisJoinPointStaticPart.getSignature());
    }
    after(): myMethod() {
        Trace.traceExit("" + thisJoinPointStaticPart.getSignature());
    }

    /**
     * A main function for testing the trace aspect.
     */
    public static void main(String[] args) {
        Trace.TRACELEVEL = 2;
        Trace.initStream(System.err);
        ExampleMain.main(args);
    }
}

