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


EnergyPacketProducer.java
Part of the Spacewar system.

 This implementation creates booby-trapped packets 20% of the time.

*/

package spacewar;


class EnergyPacketProducer extends Thread {
    private final static int MIN = -20;
    private final static int MAX = 80;
    private final static int EXPECTEDINTERVAL = 15;

    private Game game;

    Game getGame() { return game; }

    EnergyPacketProducer(Game theGame) {
        super("EnergyPacketProducer");
        game = theGame;
    }

    public void run() {
        while(true) {
            produceAPacket();
            waitForABit();
        }
    }

    void waitForABit() {
        try { Thread.sleep((int)(Math.random() * EXPECTEDINTERVAL * 2000)); }
        catch (InterruptedException e) {}
    }

    void produceAPacket() {
        EnergyPacket pkt =
            new EnergyPacket(game,
                             Math.random() * getGame().getWidth(),
                             Math.random() * getGame().getHeight(),
                             Math.random() * 2 - 1,
                             Math.random() * 2 - 1,
                             Math.random() * (MAX - MIN) + MIN);
    }
}
