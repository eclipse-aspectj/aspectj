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
 * This simulation subclass implements AbstractSimulation.report(..)
 * 
 */
public class BillingSimulation extends AbstractSimulation {

    public static void main(String[] args){
	System.out.println("\n... Billing simulation 2 ...\n");
	simulation = new BillingSimulation();
	simulation.run();
    }

    /**
     * Print a report of the connection time and the bill for customer
     */
    protected void report(Customer c){
        Timing t = Timing.aspectOf();
        Billing b = Billing.aspectOf();
        System.out.println(c + " has been connected for "
			   + t.getTotalConnectTime(c)
			   + " seconds and has a bill of "
			   + b.getTotalCharge(c));
    }
}

