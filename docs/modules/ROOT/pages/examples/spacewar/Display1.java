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


Display1.java
Part of the Spacewar system.
*/

package spacewar;


import java.awt.Graphics;
import java.awt.Color;
import java.util.Random;

/**
 * This is the standard display aspect.
 */
class Display1 extends Display {
    /*
     * Here's the color scheme for the game.  No other places in this file
     * should say Color.xxx.  Instead, that color should be given a symbolic
     * name here.
     */
    private static Color backgroundColor        = Color.black;
    private static Color player1ShipColor       = Color.white;
    private static Color player2ShipColor       = Color.gray;
    private static Color robotShipColor         = new Color(0xa00000);
    private static Color flameColor             = Color.red;
    private static Color shipExplosionColor     = Color.red;
    private static Color bulletColor            = Color.green;
    private static Color energyPacketOuterColor = Color.blue;
    private static Color energyPacketInnerColor = new Color(0x7070FF);
    private static Color statusLabelsColor      = Color.white;
    private static Color statusMeterBorderColor = Color.white;
    private static Color energyStatusMeterColor = Color.blue;
    private static Color damageStatusMeterColor = Color.red;


    Display1(Game game) {
        super(game);
        frame.setLocation(20, 20);
    }

    void noticeSizeChange() {
        super.noticeSizeChange();
        setBackground(backgroundColor);
    }

    void paintObjects(Graphics g) {
        SpaceObject[] objects = game.getRegistry().getObjects();
        final int len = objects.length;
        for (int i = 0; i < len; i++) {
            objects[i].paint(g);
        }
    }

    static aspect SpaceObjectPainting {

        abstract private void SpaceObject.paint(Graphics g);

        /*
         * Ships are by far and away the most complex of the space Objects
         * to paint.  First off, we need to set the color when the ship
         * is made.
         */
        private Color Ship.color;

        after(Pilot pilot) returning (Ship ship): call(Ship Game.newShip(Pilot)) && args(pilot) {
            if (pilot.getNumber() == 1)
                ship.color = player1ShipColor;
            else if (pilot.getNumber() == 2)
                ship.color = player2ShipColor;
            else
                ship.color = robotShipColor;
        }

        private void Ship.paint(Graphics g) {
            final double PI  = Math.PI;
            int[] radius     = {15, 12, -4, 12, -9, -15, -9};
            double[] angle   = {0, PI * 3/4, 0, -PI * 3/4, PI/8, 0, -PI/8};
            int[] x;
            int[] y;

            Random  random = new Random();

            if (this.getDamage() >= this.MAX_DAMAGE) {
                int lines = 20;
                x = new int[lines];
                y = new int[lines];
                g.setColor(shipExplosionColor);
                for (int i = 0; i < lines; i++) {
                    x[i] = (int)(this.getXPos()) + random.nextInt() % 20;
                    y[i] = (int)(this.getYPos()) + random.nextInt() % 20;
                }
                for (int i = 0; i < lines; i++)
                    g.drawLine(x[i], y[i], x[(i + 1) % lines], y[(i + 1) % lines]);
            } else {
                x = new int[7];
                y = new int[7];

                g.setColor(this.color);

                radius[5] += random.nextInt() % 3;
                // convert coordinates from polar to cartesian
                for (int i = 0; i < 7; i++) {
                    x[i] = (int)
                        (this.getXPos() +
                         Math.cos(this.getOrientation() + angle[i]) * radius[i]);
                    y[i] = (int)
                        (this.getYPos() +
                         Math.sin(this.getOrientation() + angle[i]) * radius[i]);
                }

                // draw the body as a polygon
                g.drawPolygon(x, y, 4);

                // if the ship is accelerating, draw in a flame
                if (this.getRAcc() != 0) {
                    g.setColor(flameColor);
                    g.drawLine(x[4], y[4], x[5], y[5]);
                    g.drawLine(x[5], y[5], x[6], y[6]);
                }
            }
        }

        /*
         * Bullets
         */
        private void Bullet.paint(Graphics g) {
            g.setColor(bulletColor);
            g.fillOval((int)this.getXPos() - 1,
                       (int)this.getYPos() - 1,
                       3,
                       3);
        }

        /*
         * energy packets
         */
        private void EnergyPacket.paint(Graphics g) {
            g.setColor(energyPacketOuterColor);
            g.fillOval((int)this.getXPos() - 5,
                       (int)this.getYPos() - 5,
                       10, 10);
            g.setColor(energyPacketInnerColor);
            g.fillOval((int)this.getXPos() - 2,
                       (int)this.getYPos() - 2,
                       3, 3);
        }
    }


    void paintStatus(Graphics g) {
        int left1 = 60;
        int top1  = 0;

        int left2 = 200;
        int top2  = 0;

        g.setColor(statusLabelsColor);
        g.drawString("energy:", 5, top1 + 15);
        g.drawString("damage:", 5, top1 + 30);

        if (getPilot1() != null)
            paintLevels(g, getPilot1().getShip(), top1, left1);
        if (getPilot2() != null)
            paintLevels(g, getPilot2().getShip(), top2, left2);
    }

    static void paintLevels(Graphics g, Ship ship, int top, int left) {
        if (ship == null)
            return;
        else if (ship.isAlive()) {
            g.setColor(statusMeterBorderColor);
            g.drawRect(left, top + 6,  101, 10);
            g.drawRect(left, top + 21, 101, 10);
            g.setColor(energyStatusMeterColor);
            g.fillRect(left + 1, top + 7,  (int)(ship.getEnergyLevel()*100), 9);
            g.setColor(damageStatusMeterColor);
            g.fillRect(left + 1, top + 22, (int)(ship.getDamageLevel()*100), 9);
        }
        else {
            g.setColor(damageStatusMeterColor);
            g.drawString("Ship is destroyed", left+1, top+15);
        }
    }
}
