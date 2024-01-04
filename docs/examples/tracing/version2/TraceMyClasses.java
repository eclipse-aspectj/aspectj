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

import tracing.TwoDShape;
import tracing.Circle;
import tracing.Square;
import tracing.ExampleMain;

/**
 *
 * This class concretizes the abstract crosscut in Trace,
 * applying the trace facility to these application classes.
 *
 */
public aspect TraceMyClasses extends Trace {
    pointcut myClass(): within(TwoDShape) || within(Circle) || within(Square);

    /**
     * A main function for testing the trace aspect.
     */
    public static void main(String[] args) {
        Trace.TRACELEVEL = 2;
        Trace.initStream(System.err);
        ExampleMain.main(args);
    }
}
