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


Robot.java
Part of the Spacewar system.

*/

package spacewar;

import java.util.Random;

/**
 * Robot is an automatic pilot that now has quite a bit of intelligence.
 * So, beware !
 */
class Robot extends Pilot implements Runnable {

    private static final int FIRE_INTERVAL  = 60;
    private static final int REBIRTH_DELAY  = 900;

    private final Random random = new Random();

    private Thread  runner;
    private boolean runnable = true;

    Robot(Game theGame, int number) {
        super(theGame, number);
    }

    void start() {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
        }
    }

    void destroy() {
        if (runner != null) {
            runnable = false;
            runner   = null;
        }
    }


    // A Robot tracks User-controlled ships and fires at them
    public void run() {
        Ship target = null;

        while(runnable) {
            // find target ship
            do {
                Ship[] potentials = getGame().getRegistry().getShips();
                if(potentials.length != 0)
                    target = potentials[Math.abs(random.nextInt() % potentials.length)];
                sleepForABit(25);
            } while (target == ship);
            // main loop
            int     currentRotation       = Ship.STOP;
            int     time;
            boolean currentlyAccelerating = false;
            double  dx, dy, angleA, angleB, theta, dtheta, d,
                targetVel, a, b, c, targetXVel, targetYVel;

            while(true) {
                sleepForABit(FIRE_INTERVAL);

                // if my ship is destroyed, give me a new one
                if (!ship.isAlive()) {
                    sleepForABit(REBIRTH_DELAY);
                    getGame().newShip(this);
                }

                // find direction and distance from target to me
                dx = ship.getXPos() - target.getXPos();
                if (dx < - getGame().getWidth() / 2)
                    dx += getGame().getWidth();
                if (dx > getGame().getWidth() / 2)
                    dx -= getGame().getWidth();
                dy = ship.getYPos() - target.getYPos();
                if (dy < - getGame().getHeight() / 2)
                    dy += getGame().getHeight();
                if (dy > getGame().getHeight() / 2)
                    dy -= getGame().getHeight();
                d = Math.sqrt(dx * dx + dy * dy);
                angleA = Math.atan(dy / dx);
                if (dx < 0)
                    angleA += Math.PI;

                // find relative velocity and trajectory of target
                targetXVel = target.getXVel() - ship.getXVel();
                targetYVel = target.getYVel() - ship.getYVel();
                targetVel = Math.sqrt(targetXVel * targetXVel +
                                      targetYVel * targetYVel);
                angleB = Math.atan(targetYVel / targetXVel);
                if (targetXVel < 0)
                    angleB+=Math.PI;

                // find angle between line to target and taget's direction of travel
                theta = (angleA - angleB) % (2 * Math.PI);
                if (theta < -Math.PI)
                    theta += 2 * Math.PI;
                if (theta > Math.PI)
                    theta -= 2 * Math.PI;

                // calculate time to bullet impact using law of cosines
                a = targetVel * targetVel + Ship.BULLET_SPEED * Ship.BULLET_SPEED;
                b = d * targetVel * Math.cos(theta);
                c = - d * d;
                time = (int)((-b + Math.sqrt(b * b - 4 * a * c)) / 2 / a);

                // calculate angle and distance to bullet impact location
                dx = targetXVel * time - dx;
                dy = targetYVel * time - dy;
                theta = Math.atan(dy / dx);
                if(dx < 0)
                    theta += Math.PI;

                // find desired change in rotation
                dtheta = (theta - ship.getOrientation()) % (2 * Math.PI);
                // find the shortest path to the desired orientation;
                if(dtheta < - Math.PI)
                    dtheta += 2 * Math.PI;
                if(dtheta > Math.PI)
                    dtheta -= 2 * Math.PI;

                // turn if nessecary
                if (dtheta > Ship.DEFAULT_ANGULAR_VELOCITY / 2) {
                    if (currentRotation != Ship.CLOCKWISE)
                        ship.rotate(currentRotation = Ship.CLOCKWISE);
                }
                else if (dtheta < -Ship.DEFAULT_ANGULAR_VELOCITY / 2) {
                    if (currentRotation != Ship.COUNTERCLOCKWISE)
                        ship.rotate(currentRotation = Ship.COUNTERCLOCKWISE);
                } // otherwise, fire, maybe even a burst
                else {
                    if(currentRotation != Ship.STOP)
                        ship.rotate(currentRotation = Ship.STOP);
                    if (random.nextInt() % 40 == 0) {
                        ship.fire();
                    }
                }

                // randomly accelerate
                if (currentlyAccelerating && random.nextInt() % 2 == 0)
                    ship.thrust(currentlyAccelerating = false);
                else {
                    if (ship.getXVel() == 0)
                        angleA = 0;
                    else
                        angleA = Math.atan(ship.getYVel() / ship.getXVel());

                    if (ship.getXVel() < 0)
                        angleA+=Math.PI;
                    angleB = (angleA - ship.getOrientation()) % (2 * Math.PI);
                    if (angleB < -Math.PI)
                        angleB += 2 * Math.PI;
                    if (angleB > Math.PI)
                        angleB -= 2 * Math.PI;
                    angleB = Math.abs(angleB);

                    // angleB now represents the angle between the ship's
                    // orientation and velocity vector.  This will be used to
                    // determine the probably that the ship will thrust to
                    // prevent ships from accelerating too much in one direction
                    if (random.nextInt() % (int)(12 * (Math.PI - angleB) + 1) == 0)
                        ship.thrust(currentlyAccelerating = true);
                }

                // switch targets if current one has been destroyed
                if (target.getDamage() == 100)
                    break;

                // randomly switch targets
                if (random.nextInt() % 4000 == 0)
                    break;
            }
        }
    }

    void sleepForABit (int time) {
        try {
            runner.sleep(time);
        }
        catch (InterruptedException e) {}
    }
}
