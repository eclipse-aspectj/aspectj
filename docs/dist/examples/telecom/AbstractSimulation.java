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

public abstract class AbstractSimulation {

    public static AbstractSimulation simulation;

    /**
     * Creates objects and puts them to work.
     */
    public void run() {
        Customer jim = new Customer("Jim", 650);
        Customer mik = new Customer("Mik", 650);
        Customer crista = new Customer("Crista", 415);

        say("jim calls mik...");
        Call c1 = jim.call(mik);
        wait(1.0);
        say("mik accepts...");
        mik.pickup(c1);
        wait(2.0);
        say("jim hangs up...");
        jim.hangup(c1);
        report(jim);
        report(mik);
        report(crista);

        say("mik calls crista...");
        Call c2 = mik.call(crista);
        say("crista accepts...");
        crista.pickup(c2);
        wait(1.5);
        say("crista hangs up...");
        crista.hangup(c2);
        report(jim);
        report(mik);
        report(crista);
    }

    /**
     * Print a report of the connection time for customer
     */
    abstract protected void report(Customer c);

    /**
     * Wait 0.1 seconds per "second" for simulation
     */
    protected static void wait(double seconds) {
        Object dummy = new Object();
        synchronized (dummy) {
	    //XXX cheat and only wait 0.1 seconds per second
            try {dummy.wait((long)(seconds*100)); }
            catch (Exception e) {}
        }
    }

    /**
     * Put a message on standard output
     */
    protected static void say(String s){
	System.out.println(s);
    }

}
