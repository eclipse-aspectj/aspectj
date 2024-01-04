/*
Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.
*/

package telecom;

/**
 * The Timing aspect is concerned with the duration
 * of connections and with customer's cumulative
 * connection time.
 */
public aspect Timing {

    /**
     * Every Customer has a total connection time
     */
    public long Customer.totalConnectTime = 0;

    public long getTotalConnectTime(Customer cust) {
        return cust.totalConnectTime;
    }
    /**
     * Every connection has a timer
     */
    private Timer Connection.timer = new Timer();
    public Timer getTimer(Connection conn) { return conn.timer; }

    /**
     * Start the timer when call completed
     */
    after (Connection c): target(c) && call(void Connection.complete()) {
        getTimer(c).start();
    }

    /**
     * When to stop the timer
     */
    pointcut endTiming(Connection c): target(c) &&
        call(void Connection.drop());

    /**
     * Stop the timer when call dropped and update the involved parties
     */
    after(Connection c): endTiming(c) {
        getTimer(c).stop();
        c.getCaller().totalConnectTime += getTimer(c).getTime();
        c.getReceiver().totalConnectTime += getTimer(c).getTime();
    }
}
