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


Display2.java
Part of the Spacewar system.
*/

package spacewar;


import java.awt.Graphics;
import java.awt.Color;


/**
 * This is the cheap Display aspect.
 */
class Display2 extends Display {

    Display2(Game game) {
        super(game);
        frame.setLocation(540, 20);
    }

    void noticeSizeChange() {
        super.noticeSizeChange();
        setBackground(Color.darkGray);
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
         * to paint.
         */
        private Color Ship.color;

        after(Pilot pilot) returning (Ship ship): call(Ship Game.newShip(Pilot)) && args(pilot) {
            if (pilot.getNumber() == 1)
                ship.color = Color.white;
            else if (pilot.getNumber() == 2)
                ship.color = Color.gray;
            else
                ship.color = new Color(0xa00000);
        }

        private void Ship.paint(Graphics g) {
            if (this.getDamage() < this.MAX_DAMAGE) {
                double x        = this.getXPos();
                double y        = this.getYPos();
                double sinTheta = Math.sin(this.getOrientation());
                double cosTheta = Math.cos(this.getOrientation());

                g.setColor(color);
                g.drawLine((int)(x + 8*cosTheta), (int)(y + 8*sinTheta),
                           (int)(x - 8*cosTheta), (int)(y - 8*sinTheta));

                // if the ship is accelerating, draw thruster
                if (this.getRAcc() != 0) {
                    g.setColor(Color.red);
                    g.fillOval((int)(x - 8*cosTheta), (int)(y - 8*sinTheta), 6, 6);
                }
            }
        }

        private void Bullet.paint(Graphics g) {
            g.setColor(Color.green);
            g.fillOval((int)this.getXPos() - 1,
                       (int)this.getYPos() - 1,
                       3,
                       3);
        }

        private void EnergyPacket.paint(Graphics g) {
            g.setColor(Color.white);
            g.fillOval((int)this.getXPos() - 5,
                       (int)this.getYPos() - 5,
                       10,
                       10);
        }
    }

    void paintStatus(Graphics g) {
        int left1 = 60;
        int top1  = 0;

        int left2 = 200;
        int top2  = 0;

        g.setColor(Color.white);
        g.drawString("energy:", 5, top1 + 15);
        g.drawString("damage:", 5, top1 + 30);

        if (getPilot1() != null)
            paintLevels(g, getPilot1().getShip(), top1, left1);
        if (getPilot2() != null)
            paintLevels(g, getPilot2().getShip(), top2, left2);
    }

    void paintLevels(Graphics g, Ship ship, int top, int left) {
        if (ship == null)
            return;
        else if (ship.isAlive()) {
            g.drawString(Float.toString(ship.getEnergyLevel()*100), left+1, top+15);
            g.drawString(Float.toString(ship.getDamageLevel()*100), left+1, top+30);
        }
        else {
            g.setColor(Color.red);
            g.drawString("Ship is destroyed", left+1, top+15);
        }
    }
}
