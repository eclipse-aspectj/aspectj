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


RegistrySynchronization.java
Part of the Spacewar system.

*/

package spacewar;

import coordination.Coordinator;

/**
 * This aspect ensures synchronized access to methods of the Game in the
 * presence of several threads.
 *
 * It uses the Coordinator class, from the AspectJ coordination library.
 * (This case is right on the borderline of being too simple to use the
 * coordination library, but we use it anyways to keep the similarity
 * with the RegistrySynchronizer.)
 *
 * It uses a per-Game coordination scheme, so there is one instance of
 * this class for each instance of the Game class.  When this class is
 * constructed, it registers appropriate mutexes and selfexes using
 * the behavior inherited from Coordinator.
 *
 * The coordination constraints for the Game are simple.  We just need to
 * make sure that newShip and handleCollisions are mutually exclusive.  That
 * ensures that they we can't destroy a ship that has just been replaced.
 */
aspect GameSynchronization extends Coordinator perthis(this(Game)) {

    protected pointcut synchronizationPoint():
	call(void Game.handleCollisions(..)) || call(Ship Game.newShip(..));

    public GameSynchronization() {
        addMutex(new String[] {"handleCollisions", "newShip"});
    }

}
