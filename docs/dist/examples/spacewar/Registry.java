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


Registry.java
Part of the Spacewar system.

*/

package spacewar;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * The Registry keeps track of all the space objects that are floating around.
 * It basically supports register, unregister and contents type operations.
 *
 * The synchronization is done by the RegistrySynchronization aspect.
 */

class Registry {

    private Hashtable table;
    private Game      game;

    Game getGame() { return game; }

    Registry (Game theGame) {
        game = theGame;
        table = new Hashtable();
    }


    void register(SpaceObject object) {
        table.put(object, object);
    }

    void unregister(SpaceObject object) {
        table.remove(object);
    }

    /*
     * It is an invariant of the design that only two points in SpaceObject
     * should call register and unregister.  This aspect enforces that.
     *
     * Unfortunately, in the current compiler, we get a static warning when
     * there are no illegal calls that this advice has no targets.  That will
     * be fixed in a future release.  For the time being the dummy method
     * just below this fixes that.
     */
    static aspect RegistrationProtection {
        after() returning():
            (call(void Registry.register(SpaceObject)) ||
             call(void Registry.unregister(SpaceObject))) &&
            !(within(SpaceObject) && (withincode(new(..)) ||
                                      withincode(void die()))) {
            throw new IllegalAccessError(
               "This is an illegal call to " + thisJoinPoint + "\n" +
               "Only the constructor and the die() on SpaceObject\n" +
               "should call the primitive registry operations.");
        }
    }

    void dummy() {        // see comment above
        register(getObjects()[0]);
        unregister(getObjects()[0]);
    }


    SpaceObject[] getObjects() {
        SpaceObject[] allObjects = new SpaceObject[table.size()];
        Enumeration elements = table.elements();
        for(int i = 0; elements.hasMoreElements(); i++) {
            allObjects[i] = (SpaceObject)(elements.nextElement());
        }
        return allObjects;
    }

    Ship[] getShips() {
        //
        // First we have to put just the Ships into a vector, then we can put
        // them into an array of exactly the right length.
        //
        Ship[]  arrayOfShips;
        Vector vectorOfShips = new Vector();
        Enumeration elements = table.elements();
        while (elements.hasMoreElements()) {
            Object object = elements.nextElement();
            if (object instanceof Ship) {
                vectorOfShips.addElement(object);
            }
        }

        arrayOfShips = new Ship[(vectorOfShips.size())];
        vectorOfShips.copyInto(arrayOfShips);
        return arrayOfShips;
    }

    Hashtable getTable() { return table; }

    //
    // The protocol for clockTick is that it automatically cascades.
    //
    void clockTick() {
        Enumeration elements = table.elements();
        while (elements.hasMoreElements()) {
            ((SpaceObject)elements.nextElement()).clockTick();
        }
    }
}

