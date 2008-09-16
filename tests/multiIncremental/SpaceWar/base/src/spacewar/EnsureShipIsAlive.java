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

Ship.java
Part of the Spacewar system.

*/

package spacewar;

/**
 * This aspect makes sure that the ship is alive before performing any console
 * commands.
 *
 */
aspect EnsureShipIsAlive {
    void around (Ship ship): Ship.helmCommandsCut(ship) {
        if ( ship.isAlive() ) {
            proceed(ship);
        }
    }
}
