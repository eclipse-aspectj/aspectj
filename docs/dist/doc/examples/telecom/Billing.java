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
 * The Billing aspect deals with... billing.
 * How much money did each connection cost?
 * How much money did each call cost?
 * How much money is being debited to a customer?
 * This aspect can be used by other parts of the system. (not in this example)
 *
 * Billing can depend many things, such as timing, the type of the connection,
 * some special discounts the customer has, special features, etc. In here,
 * it depends only on timing and on the type of the connection.
 */
public aspect Billing {
    // precedence required to get advice on endtiming in the right order
    declare precedence: Billing, Timing;

    public static final long LOCAL_RATE = 3;
    public static final long LONG_DISTANCE_RATE = 10;

    public Customer Connection.payer;
    public Customer getPayer(Connection conn) { return conn.payer; }
    /**
     * Caller pays for the call
     */
    after(Customer cust) returning (Connection conn):
        args(cust, ..) && call(Connection+.new(..)) {
        conn.payer = cust;
    }

    /**
     * Connections give the appropriate call rate
     */
    public abstract long Connection.callRate();


    public long LongDistance.callRate() { return LONG_DISTANCE_RATE; }
    public long Local.callRate() { return LOCAL_RATE; }


    /**
     * When timing stops, calculate and add the charge from the
     * connection time
     */
    after(Connection conn): Timing.endTiming(conn) {
        long time = Timing.aspectOf().getTimer(conn).getTime();
        long rate = conn.callRate();
        long cost = rate * time;
        getPayer(conn).addCharge(cost);
    }


    /**
     * Customers have a bill paying aspect with state
     */
    public long Customer.totalCharge = 0;
    public long getTotalCharge(Customer cust) { return cust.totalCharge; }

    public void Customer.addCharge(long charge){
        totalCharge += charge;
    }
}
