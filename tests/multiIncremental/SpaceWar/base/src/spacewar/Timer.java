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


Timer.java
Part of the Spacewar system.

*/

package spacewar;


class Timer extends Thread {

    private final static int TICK_PERIOD = 40;  // time between ticks in millis

    private Game game;

    Game getGame() { return game; }

    Timer (Game theGame) {
        super("Timer");
        game = theGame;
    }

    public void run() {
        long t1, tdiff;
        while (true) {
            t1 = System.currentTimeMillis();
            getGame().clockTick();
            tdiff = System.currentTimeMillis() - t1;
            if (tdiff < TICK_PERIOD) {
                try {
                    sleep (Math.max(0 , TICK_PERIOD - tdiff));
                }
                catch (InterruptedException e) { }
            }
        }
    }
}
