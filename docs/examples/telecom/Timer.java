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
 * Simple timer machine used to record elapsed time
 */
public class Timer {
    public long startTime, stopTime;

    /**
     * set the start time
     */
    public void start() {
        startTime = System.currentTimeMillis();
        stopTime = startTime;
    }

    /**
     * set the end time
     */
    public void stop() {
        stopTime = System.currentTimeMillis();
    }

    /**
     * set how much time passed between last start and stop?
     */
    public long getTime() {
        return stopTime - startTime;
    }
}


