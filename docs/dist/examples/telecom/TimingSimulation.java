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
public class TimingSimulation extends AbstractSimulation {

    public static void main(String[] args){
	System.out.println("\n... Timing simulation 2 ...\n");
	simulation = new TimingSimulation();
	simulation.run();
    }

    /**
     * Print a report of the connection time for customer
     */
    protected void report(Customer c){
        Timing t = Timing.aspectOf();
        System.out.println(c + " spent " + t.getTotalConnectTime(c));
    }

}
