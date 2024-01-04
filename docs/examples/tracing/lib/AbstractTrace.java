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

import java.io.PrintStream;
import org.aspectj.lang.JoinPoint;


/**
 * This class provides support for printing trace messages into a stream. 
 * The trace messages consist of the class name, method name (if method)
 * and the list of parameter types.<P>
 * The class is thread-safe. Different threads may use different output streams
 * by simply calling the method initStream(myStream).<P>
 * This class should be extended.
 * It defines 3 abstract crosscuts for injecting the tracing functionality 
 * into any constructors and methods of any application classes.<P>
 *
 * One example of using this class might be
 * <PRE>
 * import tracing.lib.AbstractTrace;
 * aspect TraceMyClasses extends AbstractTrace of eachJVM() {
 *   pointcut classes(): within(TwoDShape) | within(Circle) | within(Square);
 *   pointcut constructors(): executions(new(..));
 *   pointcut methods(): executions(!abstract * *(..))
 * }
 * </PRE>
 * (Make sure .../aspectj/examples is in your classpath)
 */
public abstract aspect AbstractTrace {

    /**
     * Application classes - left unspecified.
     * Subclasses should concretize this crosscut with class names.
     */
    abstract pointcut classes();
    /**
     * Constructors - left unspecified.
     * Subclasses should concretize this crosscut with constructors.
     */
    abstract pointcut constructors();
    /**
     * Methods - left unspecified.
     * Subclasses should concretize this crosscut with method names.
     */
    abstract pointcut methods();

    before(): classes() && constructors() {   
	doTraceEntry(thisJoinPoint, true);
    }
    after(): classes() && constructors() {   
	doTraceExit(thisJoinPoint,  true);
    }

    before(): classes() && methods() {
	doTraceEntry(thisJoinPoint, false);
    }
    after(): classes() && methods() {
	doTraceExit(thisJoinPoint,  false);
    }

    /*
     * From here on, it's an ordinary class implementation.
     * The static state is thread-safe by using ThreadLocal variables.
     */

    /**
     * This method initializes this thread's trace output stream.
     * By default, the output stream is System.err, and it is the same for
     * all threads. In multithreaded applications, you may want to define
     * different output streams for the different threads. For doing it,
     * simply call this method in the beginning of each thread's main loop,
     * giving it different output streams.
     */
    public void initStream(PrintStream _stream) {
	setStream(_stream);
    }


    private ThreadLocal stream = new ThreadLocal() {
	    protected Object initialValue() {
		return System.err;
	    }
	};
    private ThreadLocal callDepth = new ThreadLocal() {
	    protected Object initialValue() {
		return new Integer(0);
	    }
	};

    private PrintStream getStream() { 
	return (PrintStream)stream.get(); 
    }
    private void setStream(PrintStream s) { 
	stream.set(s); 
    }
    private int  getCallDepth() { 
	return ((Integer)(callDepth.get())).intValue();
    }
    private void setCallDepth(int n) { 
	callDepth.set(new Integer(n)); 
    }

    private void doTraceEntry (JoinPoint jp, boolean isConstructor) {
	setCallDepth(getCallDepth() + 1);
	printEntering(jp, isConstructor);
    }

    private void doTraceExit (JoinPoint jp,  boolean isConstructor) {
	printExiting(jp, isConstructor);
	setCallDepth(getCallDepth() - 1);
    }

    private void printEntering (JoinPoint jp, boolean isConstructor) {
	printIndent();
	getStream().print("--> ");
	getStream().print(jp);
	//	printParameterTypes(jp);
	getStream().println();
    }

    private void printExiting (JoinPoint jp, boolean isConstructor) {
	printIndent();
	getStream().print("<--  ");
	getStream().print(jp);
	//	printParameterTypes(jp);
	getStream().println();
    }

//      private void printParameterTypes(JoinPoint jp) {
//  	Class[]  ptypes = jp.parameterTypes;

//  	getStream().print("(");
//  	for (int i = 0; i < ptypes.length; i++) {
//  	    getStream().print(ptypes[i].getName());
//  	    if (i < ptypes.length - 1) getStream().print(", ");
//  	}
//  	getStream().print(")");
//      }

    private void printIndent() {
	for (int i = 0; i < getCallDepth(); i++)
	    getStream().print(" ");
    }

    /**
     * This method is not being used.
     * It's being included solely for illustrating how to access and use
     * the information in JoinPoint.
     * If you want, you can replace the calls to printParameterTypes (above)
     * by calls to this method.
     */
//      private void printParameters(JoinPoint jp) {
//  	Class[]  ptypes = jp.parameterTypes;
//  	String[] pnames = jp.parameterNames;
//  	Object[] params = jp.parameters;

//  	getStream().print("(");
//  	for (int i = 0; i < ptypes.length; i++) {
//  	    getStream().print(ptypes[i].getName() + " " +
//  			      pnames[i]           + "=" +
//  			      params[i]);
//  	    if (i < ptypes.length - 1) getStream().print(", ");
//  	}
//  	getStream().print(")");
//      }

}

