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

Debug.java
Part of the Spacewar system.

*/

package spacewar;

import java.awt.Menu;
import java.awt.CheckboxMenuItem;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.Dimension;

/**
 * This aspect specifies debugging information to be output to the
 * information window.
 *
 * When the debug aspect is compiled in the Frame menu has several checkbox
 * items that can be used to control the amount of tracing information
 * displayed.  (By default the first three are off, because they generate
 * so much information.)
 *
 * There are two reasons to gather all this debugging code into an aspect
 * like this:
 *
 *   (1) It makes it easier to understand when it is all in one place.
 *
 *   (2) It means that we can "plug and debug".  We can enable/disable
 *       the debugging code simply by weaving or not weaving this
 *       aspect in.
 *
 * All in all, this is a lot better than the usual practice of writing
 * complex debugging code and then deleting it when the bug is found,
 * only to regret it a month later when a related bug surfaces.  (Or even
 * the same bug!)
 *
 * This file also defines a class InfoWin, which it uses to display all the
 * debugging information.
 */
aspect Debug {

    private static InfoWin infoWin = new InfoWin();

    private static Menu menu = new Menu("Debug");

    private static CheckboxMenuItem traceConstructors =
        new CheckboxMenuItem("trace constructors", false);
    private static CheckboxMenuItem traceInitializations =
        new CheckboxMenuItem("trace initializations", false);
    private static CheckboxMenuItem traceMethods =
        new CheckboxMenuItem("trace methods", false);
    private static CheckboxMenuItem traceClockTick =
        new CheckboxMenuItem("trace clock tick", false);
    private static CheckboxMenuItem traceRegistry =
        new CheckboxMenuItem("trace registry", true);
    private static CheckboxMenuItem traceFireCollideDamage =
        new CheckboxMenuItem("trace fire, collide, damage", true);

    after() returning (SWFrame frame): call(SWFrame+.new(..)) {
        menu.add(traceConstructors);
        menu.add(traceInitializations);
        menu.add(traceMethods);
        menu.add(traceClockTick);
        menu.add(traceRegistry);
        menu.add(traceFireCollideDamage);
        frame.getMenuBar().add(menu);
    }

    /*
     * all constructors
     */
    pointcut allConstructorsCut(): 
	call((spacewar.* && !(Debug+ || InfoWin+)).new(..));

    before(): allConstructorsCut() {
        if (traceConstructors.getState()) {
            infoWin.println("begin constructing " + thisJoinPoint.getSignature());
        }
    }

    after() returning: allConstructorsCut() {
        if (traceConstructors.getState()) {
            infoWin.println("done constructing " + thisJoinPoint.getSignature());
        }
    }

    /*
     * All dynamic initializations
     */
    pointcut allInitializationsCut(): 
	initialization((spacewar.* && !(Debug+ || InfoWin+)).new(..));

    before(): allInitializationsCut() {
        if (traceConstructors.getState()) {
            infoWin.println("begin initializing " + thisJoinPoint.getSignature());
        }
    }
    after() returning : allInitializationsCut() {
        if (traceConstructors.getState()) {
            infoWin.println("done initializing " + thisJoinPoint.getSignature());
        }
    }

    /*
     * all methods
     */
    pointcut allMethodsCut(): 
        execution(* (spacewar.* && !(Debug+ || InfoWin+)).*(..));

    before(): allMethodsCut() {
        if (traceMethods.getState()) {
            infoWin.println("entering " + thisJoinPoint.getSignature());
        }
    }
    after() returning : allMethodsCut() {
        if (traceMethods.getState()) {
            infoWin.println("exiting " + thisJoinPoint.getSignature());
        }
    }

    /*
     * clock ticks
     */
    after(Object obj) returning :
        (target(obj) && (target(Game) ||
			 target(Registry) ||
			 target(SpaceObject)))
        && call(void clockTick()) {
        if (traceClockTick.getState())
            infoWin.println("ticking " + obj);
    }

    /*
     * registry contents
     */
    after(Registry registry) returning :
        target(registry) && (call(void register(..)) ||
			     call(void unregister(..))) {
        if (traceRegistry.getState())
            infoWin.println(registry.getTable().size() +
                            " space objects in the registry.");
    }

    /*
     * fire, collide, damage
     */
    after() returning : call(void Ship.fire()) {
        if (traceFireCollideDamage.getState())
            infoWin.println("firing");
    }

    after(Ship ship, SpaceObject obj) returning :
	call(void handleCollision(SpaceObject)) && target(ship) && args(obj) {
        if (traceFireCollideDamage.getState())
            infoWin.println(ship + " collides with " + obj);
    }

    after(Ship shipA, Ship shipB) returning :
        execution(void Ship.bounce(Ship, Ship)) && args(shipA, shipB) {
        if (traceFireCollideDamage.getState())
            infoWin.println(shipA + " bounces with " + shipB);
    }

    before(Ship ship, double amount):
	call(void Ship.inflictDamage(double)) && target(ship) && args(amount) {
        if (traceFireCollideDamage.getState())
            if (amount > 0)
                infoWin.println(ship + "gets " +
                                amount + " damage (" +
                                ship.getDamage() + ")");
    }

}

class InfoWin {
    private Frame    frame;
    private TextArea info;

    InfoWin() {
        frame = new Frame("debugging info for spacewar game");
        info = new TextArea();
        info.setEditable(false);

        Dimension screenSize = frame.getToolkit().getScreenSize();
        frame.setSize(250, 600);
        frame.setLocation(screenSize.width - 250, 0);
        frame.add(info);
        frame.show();
        frame.toFront();
    }

    void clear() {
        info.setText("");
    }

    void println(String line) {
        info.append(line + "\n");
    }

    void print(String line) {
        info.append(line);
    }
}
