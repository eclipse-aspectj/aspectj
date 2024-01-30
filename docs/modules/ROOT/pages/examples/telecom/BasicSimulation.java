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
 * This simulation subclass implements AbstractSimulation.run(..)
 * with  a test script for the telecom system with only the
 * basic objects.
 */
public class BasicSimulation extends AbstractSimulation {

    public static void main(String[] args){
	simulation = new BasicSimulation();
	simulation.run();
    }

    protected void report(Customer c) { }

}
