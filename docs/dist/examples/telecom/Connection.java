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

/**
 * Connections are circuits between customers
 * There are two kinds: local and long distance
 * see subclasses at the end of this file.
 */
public abstract class Connection {

    public static final int PENDING = 0;
    public static final int COMPLETE = 1;
    public static final int DROPPED = 2;

    Customer caller, receiver;
    private int state = PENDING;

    /**
     * Creatte a new Connection between a and b
     */
    Connection(Customer a, Customer b) {
        this.caller = a;
        this.receiver = b;
    }

    /**
     * what is the state of the connection?
     */
    public int getState(){
	return state;
    }

    /**
     * get the customer who initiated this connection
     */
    public Customer getCaller() { return caller; }

    /**
     * get the customer who received this connection
     */
    public Customer getReceiver() { return receiver; }

    /**
     * Called when a call is picked up.  This means the b side has picked up
     * and the connection should now complete itself and start passing data.
     */
    void complete() {
        state = COMPLETE;
        System.out.println("connection completed");
    }

    /**
     * Called when the connection is dropped from a call.  Is intended to
     * free up any resources the connection was consuming.
     */
    void drop() {
	state = DROPPED;
        System.out.println("connection dropped");
    }

    /**
     * Is customer c connected by this connection?
     */
    public boolean connects(Customer c){
	return (caller == c || receiver == c);
    }

}



