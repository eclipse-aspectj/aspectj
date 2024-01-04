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

import java.util.Vector;
import java.util.Enumeration;


class MethodState {

    Vector threads=new Vector();

    void enterInThread (Thread t) {
	threads.addElement(t);
    }

    void exitInThread(Thread t) {
	threads.removeElement(t);
    }

    boolean hasOtherThreadThan(Thread t) {
	Enumeration e = threads.elements();
	while (e.hasMoreElements())
	    if (e.nextElement() != t)
		return(true);
	return (false);
    }

}
