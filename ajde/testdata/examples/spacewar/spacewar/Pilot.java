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

package spacewar;


/**
 * Pilot is the abstract superclass of Player and Robot.
 *
 */

abstract class Pilot {
    private Game game;
    private int  number;
    protected Ship ship = null;

    Game getGame()   { return game; }
    int  getNumber() { return number; }
    Ship getShip()   { return ship; }

    void setShip(Ship s) { ship = s; }

    Pilot (Game g, int n) {
        super();
        game   = g;
        number = n;
    }
}
