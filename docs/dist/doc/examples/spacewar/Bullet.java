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


Bullet.java
Part of the Spacewar game.

*/

package spacewar;

class Bullet extends SpaceObject {

    static private final int SIZE = 3;        //Can't be changed for now!!!
    static private int LIFETIME = 50;

    private int lifeLeft;

    Bullet (Game theGame, double xP, double yP, double xV, double yV) {
        super(theGame, xP, yP, xV, yV);
        lifeLeft = LIFETIME;
    }

    int getSize() { return SIZE; }

    void handleCollision(SpaceObject obj) {
        die();
    }

    void clockTick() {
        if (--lifeLeft == 0)
            die();
        super.clockTick();
    }
}
