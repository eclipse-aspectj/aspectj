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

/**
 * Customers have a unique id (name in this case for didactic purposes
 * but it could be telephone number) and area code.
 * They also have protocol for managing calls: call, pickup, etc.
 */
public class Customer {

    private String name;
    private int areacode;
    private Vector calls = new Vector();

    /**
     * unregister a call
     */
    protected void removeCall(Call c){
	calls.removeElement(c);
    }

    /**
     * register a call
     */
    protected void addCall(Call c){
	calls.addElement(c);
    }

    /**
     * Make a new customer with given name
     */
    public Customer(String name, int areacode) {
        this.name = name;
        this.areacode = areacode;
    }

    /**
     * String rendition of customer
     */
    public String toString() {
        return name + "(" + areacode + ")";
    }

    /**
     * what area is the customer in?
     */
    public int getAreacode(){
	return areacode;
    }

    /**
     * Is the other customer in the same area?
     */
    public boolean localTo(Customer other){
	return areacode == other.areacode;
    }

    /**
     * Make a new call to receiver
     */
    public Call call(Customer receiver) {
        Call call = new Call(this, receiver);
        addCall(call);
        return call;
    }

    /**
     * pick up a call
     */
    public void pickup(Call call) {
        call.pickup();
        addCall(call);
    }

    /**
     * hang up a call
     */
    public void hangup(Call call) {
	call.hangup(this);
	removeCall(call);
    }

    /**
     * Merge a pair of calls -- conference them
     * PRE: call1.includes(this)
     *      call2.includes(this)
     *      call1.connected()
     *      call2.connected()
     * POST: call1 includes all customers connected by call1@pre and call2@pre
     */
    public void merge(Call call1, Call call2){
	call1.merge(call2);
	removeCall(call2);
    }
}
