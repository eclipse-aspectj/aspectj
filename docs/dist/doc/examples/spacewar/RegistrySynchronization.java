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
 * This aspect ensures synchronized access to methods of the Registry in
 * the presence of several threads.
 *
 * It uses the Coordinator class, from the AspectJ coordination library.
 *
 * It uses a per-Registry coordination scheme, so there is one instance of
 * this class for each instance of the Registry class.  When this class is
 * constructed, it registers appropriate mutexes and selfexes using the
 * behavior inherited from Coordinator.
 *
 * The mutating methods (register and unregister) should be self-exclusive.
 * Each reader method should be mutually exclusive with the mutating
 * methods.  But the readers can run concurrently.  */
aspect RegistrySynchronization extends Coordinator perthis(this(Registry)) {

    protected pointcut synchronizationPoint():
        call(void Registry.register(..))   ||
        call(void Registry.unregister(..)) ||
        call(SpaceObject[] Registry.getObjects(..)) ||
        call(Ship[] Registry.getShips(..));
    
    public RegistrySynchronization() {
        addSelfex("register");
        addSelfex("unregister");

        addMutex(new String[] {"register", "unregister", "getObjects"});
        addMutex(new String[] {"register", "unregister", "getShips"});
    }

}
