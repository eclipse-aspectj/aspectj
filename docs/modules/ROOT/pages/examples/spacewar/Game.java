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


Game.java
Part of the Spacewar system.

*/

package spacewar;

import java.awt.Dimension;

/**
 * The Game class is the root of the spacewar game.  To start a spacewar
 * game, you can either call the main method, or instantiate this class
 * directly.
 *
 * Synchronization is done by the GameSynchronization aspect.
 */
public class Game extends Thread {

    /**
     * To run the game from top level, simply say Java Game, as usual.  Passing
     * an argument makes the game run in demo mode.  Without an argument it runs
     * in the normal player mode.
     */
    public static void main(String[] args) {
        if ( args.length == 0 )
            new Game("1").run();
        new Game(args[0]).run();
    }


    private Timer    timer;
    private EnergyPacketProducer ePP;

    private Registry registry;
    private Pilot    pilot1, pilot2;

    private Dimension screenSize = new Dimension(500, 500);

    Registry getRegistry() { return registry; }
    Pilot    getPilot1()   { return pilot1; }
    Pilot    getPilot2()   { return pilot2; }

    /** returns the width of the screen, delegating to screenSize */
    int getWidth()  { return screenSize.width;  }

    /** returns the height of the screen, delegating to screenSize */
    int getHeight() { return screenSize.height; }

    /**
     * To run the game, simply instantiate this class.  It runs in its own
     * thread.  You can instantiate multiple games at once.  For the time being
     * the only way to end the game is to exit from the Java VM.
     *
     * @param mode Controls whether the game runs in demo mode or not.  True
     *   means it is a demo, false means it runs in normal 2 player mode.
     */
    public Game(String mode) {
        timer    = new Timer(this);
        ePP      = new EnergyPacketProducer(this);
        registry = new Registry(this);
    }

    public void run() {
        timer.start();
        ePP.start();

        while(true) {
            try {
                newRobot(3);
                Thread.sleep(15000);
            }
            catch (InterruptedException e) {}
        }
    }


    /**
     * add a robot to the game.  This is a menu command.
     */
    void addRobot() {
        newRobot(3);
    }

    /**
     * resurrect the ships in the game.  This is a menu command.
     */
    void resetShips() {
        Ship[] ships = registry.getShips();

        for (int i = 0; i < ships.length; i++) {
            Ship ship = ships[i];
            Pilot pilot = ship.getPilot();
            newShip(pilot);
        }
    }

    /**
     * leave the game.  This is a menu command.
     */
    void quit() {
        System.exit(0);
    }

    void error(Object o) {
        System.err.println(o);
    }


    /**
     * returns a new player.  With {@link #newRobot} and {@link
     * #newShip}, the only ways to make a Player, a Robot, or a Ship.
     * The structural invariant is that there should be no calls to
     * new of one of these three classes outside these three methods.
     */
    Player newPlayer(int number) {
        Player player = new Player(this, number);
        newShip(player);
        return player;
    }

    /**
     * returns a new robot.  With {@link #newPlayer} and {@link
     * #newShip}, the only ways to make a Player, a Robot, or a Ship.
     * The structural invariant is that there should be no calls to
     * new of one of these three classes outside these three methods.
     */
    Robot newRobot(int number) {
        Robot robot = new Robot(this, number);
        newShip(robot);
        robot.start();
        return robot;
    }

    /**
     * returns a new ship.  With {@link #newRobot} and {@link
     * #newPlayer}, the only ways to make a Player, a Robot, or a
     * Ship.  The structural invariant is that there should be no
     * calls to new of one of these three classes outside these three
     * methods.
     */
    Ship newShip(Pilot pilot) {
        //
        // If there is an old ship (we're doing a reset), then remove it from
        // the registry.
        //
        Ship oldShip = pilot.getShip();
        if (! (oldShip == null))
            oldShip.die();

        Ship newShip = new Ship(this,
                                Math.random() * getWidth(),
                                Math.random() * getHeight(),
                                Math.random() * Math.PI * 2);
        pilot.setShip(newShip);
        newShip.setPilot(pilot);

        return newShip;
    }

    void clockTick() {
        registry.clockTick();
        handleCollisions();
    }

    // collision detection

    void handleCollisions() {
        SpaceObject[] objects = registry.getObjects();

        SpaceObject objI, objJ;
        for (int i = 0; i < objects.length; i++) {
            objI = objects[i];
            for (int j = i + 1; j < objects.length; j++) {
                objJ = objects[j];
                if (objI instanceof Bullet && objJ instanceof Bullet)
                    continue;
                if (isCollision(objI, objJ)) {
                    if (objI instanceof Ship && objJ instanceof Ship)
                        Ship.bounce((Ship)(objI), (Ship)(objJ));
                    else {
                        objI.handleCollision(objJ);
                        objJ.handleCollision(objI);
                    }
                }
            }
        }
    }

    /*
     * Is the distance between the two centers less than the sum of the two
     * radii.  This is a cheap and dirty (i.e. wrong) implementation of this.
     */
    static boolean isCollision(SpaceObject a, SpaceObject b) {
        return (Math.abs(a.getXPos() - b.getXPos()) +
                Math.abs(a.getYPos() - b.getYPos())) <
            (a.getSize()/2 + b.getSize()/2);
    }
}
