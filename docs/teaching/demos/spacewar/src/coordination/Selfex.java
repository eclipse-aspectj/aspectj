/* -*- Mode: Java; -*-

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

package coordination;


import java.lang.String;

class Selfex implements Exclusion {
    String methodName;
    Thread thread;
    int count = 0;

    Selfex (String _methodName) {
	methodName = _methodName;
    }

    public boolean testExclusion (String _methodName) {
	if (count == 0)
	    return(true);
	return (thread == Thread.currentThread());
    }

    public void enterExclusion (String _methodName) {
	count++;
	thread = Thread.currentThread();    // note that if count wasn't 0
	// we aren't changing thread
    }

    public void exitExclusion (String _methodName) {
	count--;
	if (count == 0)                   // not stricly necessary, but...
	    thread = null;
    }

    public void printNames() {
	System.out.println("Selfex name: " + methodName);
    }

}
