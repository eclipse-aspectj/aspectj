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
package telecom;
import java.util.Vector;
import java.util.Enumeration;

/**
 * A call supports the process of a customer trying to
 * connect to others.
 */
public class Call {

    private Customer caller, receiver;
    private Vector connections = new Vector();

    /**
     * Create a new call connecting caller to receiver
     * with a new connection. This should really only be
     * called by Customer.call(..)
     */
    public Call(Customer caller, Customer receiver) {
        this.caller = caller;
        this.receiver = receiver;
        Connection c;
        if (receiver.localTo(caller)) {
	    c = new Local(caller, receiver);
        } else {
	    c = new LongDistance(caller, receiver);
        }
        connections.addElement(c);
    }

    /**
     * picking up a call completes the current connection
     * (this means that you shouldnt merge calls until
     * they are completed)
     */
    public void pickup() {
    	Connection connection = (Connection)connections.lastElement();
        connection.complete();
    }


    /**
     * Is the call in a connected state?
     */
    public boolean isConnected(){
	return ((Connection)connections.lastElement()).getState()
            == Connection.COMPLETE;
    }

    /**
     * hanging up a call drops the connection
     */
    public void hangup(Customer c) {
        for(Enumeration e = connections.elements(); e.hasMoreElements();) {
	    ((Connection)e.nextElement()).drop();
        }
    }

    /**
     * is Customer c one of the customers in this call?
     */
    public boolean includes(Customer c){
	boolean result = false;
	for(Enumeration e = connections.elements(); e.hasMoreElements();) {
	    result = result || ((Connection)e.nextElement()).connects(c);
	}
	return result;
    }

    /**
     * Merge all connections from call 'other' into 'this'
     */
    public void merge(Call other){
	for(Enumeration e = other.connections.elements(); e.hasMoreElements();){
	    Connection conn = (Connection)e.nextElement();
	    other.connections.removeElement(conn);
	    connections.addElement(conn);
	}
    }
}
