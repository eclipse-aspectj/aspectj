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

package tracing.lib;

import tracing.TwoDShape;
import tracing.Circle;
import tracing.Square;
import tracing.ExampleMain;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileNotFoundException;

aspect TraceMyClasses extends AbstractTrace {
    /**
     * The application classes
     */
    pointcut classes(): within(TwoDShape) || within(Circle) || within(Square);
    /**
     * The constructors in those classes - but only the ones with 3
     * arguments.
     */
    pointcut constructors(): execution(new(double, double, double));
    /**
     * This specifies all the message executions.
     */
    pointcut methods(): execution(* *(..));

    /**
     * A main function for testing the trace aspect.
     */
    public static void main(String[] _args) {
	final String[] args = _args;
	new Thread() {
		public void run() {
		    TraceMyClasses.aspectOf().initStream(System.err);
		    ExampleMain.main(args);
		}
	    }.start();

	new Thread() {
		public void run() {
		    try {
			TraceMyClasses.aspectOf().initStream(new PrintStream(new FileOutputStream("AJTRACETEST")));
		    }
		    catch (FileNotFoundException e) {}
		    ExampleMain.main(args);
		}
	    }.start();
    }
}
