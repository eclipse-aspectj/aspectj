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

class Ship extends SpaceObject {

    pointcut helmCommandsCut(Ship ship):
        target(ship) && ( call(void rotate(int))     ||
			  call(void thrust(boolean)) ||
			  call(void fire()) );


    /**
     * Energy and Damage are key values in the state of a ship.  Energy is
     * basically about fuel, and damage is about how bad a shape we are in.
     *
     * The energy related values are:
     * <ul>
     *   <li>MAX_ENERGY</li>
     *   <li>BULLET_ENERGY</li>
     *   <li>ACCELERATION_ENERGY_FACTOR</li>
     *   <li>energy</li>
     * </ul>
     * The damage related values are:
     * <ul>
     *   <li>MAX_DAMAGE</li>
     *   <li>BULLET_DAMAGE</li>
     *   <li>COLLISION_DAMAGE_FACTOR</li>
     *   <li>damage</li>
     * </ul>
     * Finally, REPAIR_RATE is the rate at which energy is consumed to fix
     * damage.
     *
     */
    private static final int    MAX_ENERGY = 100;
    private static final int    BULLET_ENERGY= 2;
    private static final double ACCELERATION_COST_FACTOR = 0.05;

    //XXX was private
    static final int    MAX_DAMAGE = 100;
    private static final int    BULLET_DAMAGE = 15;
    private static final double COLLISION_DAMAGE_FACTOR = 0.1;

    private static final double REPAIR_RATE = 0.08;


    private static final int    EXPLOSION_LENGTH = 10;

    static final int    BULLET_SPEED = 10;

    static final int    CLOCKWISE = 1;
    static final int    STOP = 0;
    static final int    COUNTERCLOCKWISE = (-1);

    static final double DEFAULT_ANGULAR_VELOCITY = 0.2;
    static final double DEFAULT_ACCELERATION = .4;

    static private final int SIZE = 30;     //Can't be changed for now!!!

    private double    energy;               // range: 0 to MAX_ENERGY
    private double    damage;               // range: 0 to MAX_DAMAGE
    private double    orientation;          // in degrees
    private double    angularVel;           // in ???
    private double    xAcc, yAcc, rAcc;     //
    private int       countdown;            // remaining explosion time

    private Pilot     pilot;

    Ship(Game theGame, double xPos, double yPos, double orientation) {
        super(theGame, xPos, yPos, 0, 0);
        xAcc = 0;
        yAcc = 0;
        this.orientation = orientation;
        angularVel = 0;

        energy = MAX_ENERGY;
        damage = 0;
        countdown = EXPLOSION_LENGTH;
    }


    int getSize()            { return SIZE; }

    double getEnergy()       { return energy; }
    double getDamage()       { return damage; }
    double getOrientation()  { return orientation; }
    double getRAcc()         { return rAcc; }

    Pilot getPilot()         { return pilot; }
    void  setPilot (Pilot p) { pilot = p; }

    float getEnergyLevel() {
        return (float)energy / (float)MAX_ENERGY;
    }
    float getDamageLevel() {
        return (float)damage / (float)MAX_DAMAGE;
    }

    /** returns false if energy is out, otherwise decrements energy by amount
     * and returns true
     */
    boolean expendEnergy(double amount) {
        if (amount <= energy) {
            energy -= amount;
            return true;
        }
        else
            return false;
    }

    /** increments damage by amount and handles the destruction of a ship if
     * damage reaches MAX_DAMAGE.
     */
    void inflictDamage(double amount) {
        if (amount < 0)     // shouldn't happen
            return;
        damage = Math.min(MAX_DAMAGE, damage + amount);
        if (damage == MAX_DAMAGE)
            setIsAlive(false);
    }

    /** repairs some damage
     */
    void repairDamage(double amount) {
        if (amount < 0)     // shouldn't happen
            return;
        if (damage == 0)
            return;
        damage = Math.max(0, damage - amount);
    }

    public void clockTick() {
        if (! isAlive())  {
            //
            // If we aren't alive, but we are still in the registry, it means
            // we are exploding.  countdown counts the length of the explosion.
            //
            if (--countdown == 0)
                die();
        }
        else {
            if (angularVel != 0) {
                orientation += angularVel;
                xAcc = rAcc * Math.cos(orientation);
                yAcc = rAcc * Math.sin(orientation);
            }
            setXVel(getXVel() + xAcc);
            setYVel(getYVel() + yAcc);

            //expend energy
            if (!expendEnergy(rAcc * ACCELERATION_COST_FACTOR))
                rAcc = xAcc = yAcc = 0;

            // fix damage
            if (energy > 10 && damage > REPAIR_RATE) {
                expendEnergy(REPAIR_RATE);
                repairDamage(REPAIR_RATE);
            }
        }
        super.clockTick();
    }

    /**
     * First check to make sure we have enough energy to accelerate.  If
     * we do, then go ahead and do so.  Acceleration is in the direction
     * we are already facing (i.e. orientation).
     */
    void setAcceleration(double acc) {
        if (acc * ACCELERATION_COST_FACTOR <= energy) {
            rAcc = acc;
            xAcc = rAcc * Math.cos(orientation);
            yAcc = rAcc * Math.sin(orientation);
        }
    }

    /**
     * First check to make sure we have enough energy to rotate.  If
     * we do, then go ahead and do so.
     */
    void setAngularVelocity(double omega) {
        // changing direction of rotation takes energy
        if (!expendEnergy(Math.abs(omega - angularVel) / 2))
            return;
        //sets amount of degree rotation per clock tick, in radians;
        //clockwise is positive
        angularVel = omega;
    }

    /** affect rotation thrusters.  Direction can be one of {@link
     * #CLOCKWISE}, {@link #COUNTERCLOCKWISE}, or zero for turning off
     * the thrusters.
     */
    void rotate(int direction) {
        setAngularVelocity(
          direction == CLOCKWISE        ? DEFAULT_ANGULAR_VELOCITY :
          direction == COUNTERCLOCKWISE ? -DEFAULT_ANGULAR_VELOCITY :
          0);
    }

    /** turn on acceleration */
    void thrust(boolean onOff) {
        setAcceleration(onOff ? DEFAULT_ACCELERATION : 0);
    }

    /** create a bullet and fire it */
    void fire() {
        // firing a shot takes energy
        if (!expendEnergy(BULLET_ENERGY))
            return;

        //create a bullet object so it doesn't hit the ship that's firing it
        double xV = getXVel() + BULLET_SPEED * (Math.cos(orientation));
        double yV = getYVel() + BULLET_SPEED * (Math.sin(orientation));

        // create the actual bullet
        new Bullet(
         getGame(),
         (getXPos() + ((getSize()/2 + 2) * (Math.cos(orientation))) + xV),
         (getYPos() + ((getSize()/2 + 2) * (Math.sin(orientation))) + yV),
         xV,
         yV);
    }


    void handleCollision(SpaceObject obj) {
        if (obj instanceof Ship) {
            // should never be called. ship - ship collisions are handled in
            // Ship.bounce(Ship shipA, Ship shipB)
        }
        else if (obj instanceof Bullet) {
            inflictDamage(BULLET_DAMAGE);
        }
        else if (obj instanceof EnergyPacket) {
            double packetEnergy = ((EnergyPacket)obj).getEnergy();
            energy = Math.max(0, Math.min(energy + packetEnergy, MAX_ENERGY));
        }
        else {
            System.err.println("collision with UFO!");
        }
    }

    static void bounce(Ship shipA, Ship shipB) {
        double  dx, dy, denominator,
            xAccA, yAccA, xAccB, yAccB, damage,
            xComp, yComp, dvx, dvy;

        dx = Math.abs(shipA.getXPos() - shipB.getXPos());
        dy = Math.abs(shipA.getYPos() - shipB.getYPos());
        denominator = Math.sqrt(dx * dx + dy * dy);
        xComp = dx / denominator;
        yComp = dy / denominator;
        xAccA = shipB.getXVel() * xComp + shipA.getXVel() * (1 - xComp) -
            shipA.getXVel();
        yAccA = shipB.getYVel() * yComp + shipA.getYVel() * (1 - yComp) -
            shipA.getYVel();
        xAccB = shipA.getXVel() * xComp + shipB.getXVel() * (1 - xComp) -
            shipB.getXVel();
        yAccB = shipA.getYVel() * yComp + shipB.getYVel() * (1 - yComp) -
            shipB.getYVel();
        shipA.accelerate(xAccA, yAccA);
        shipB.accelerate(xAccB, yAccB);
        dvx = shipA.getXVel() - shipB.getXVel();
        dvy = shipA.getYVel() - shipA.getYVel();
        damage = COLLISION_DAMAGE_FACTOR * (dvx * dvx + dvy * dvy);
        shipA.inflictDamage(damage);
        shipB.inflictDamage(damage);

        // !!!
        // !!! poopers!  this does a local time warp.  this has to be a
        // !!! violation of the clockTick protocol
        // !!!
        while (Game.isCollision(shipA, shipB)) {
            shipA.clockTick();
            shipB.clockTick();
        }
    }
}
