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


*/

package spacewar;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

class Player extends Pilot implements KeyListener {

    private KeyMapping keyMapping;

    /** current rotation key */
    private int        rotation_direction = Ship.STOP; // current rotation key

    /** current thrust */
    private boolean    thrust_on = false;

    Player(Game theGame, int number) {
        super(theGame,number);

        if (getNumber() == 1)
            keyMapping = KeyMapping.keyMapping1;
        else if (getNumber() == 2)
            keyMapping = KeyMapping.keyMapping2;

    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        boolean consumed = true;

        if (keyCode == keyMapping.fire) {
            ship.fire();
        }
        else if (keyCode == keyMapping.thrust && !thrust_on) {
            ship.thrust(true);
            thrust_on = true;
        }
        else if (keyCode == keyMapping.right &&
                 rotation_direction != Ship.COUNTERCLOCKWISE) {
            //start rotating clockwise unless already rotating in the
            //opposite direction
            rotation_direction = Ship.CLOCKWISE;
            ship.rotate(Ship.CLOCKWISE);
        }
        else if (keyCode == keyMapping.left &&
                 rotation_direction != Ship.CLOCKWISE) {
            //start rotating counterclockwise unless already rotating in the
            //opposite direction
            rotation_direction = Ship.COUNTERCLOCKWISE;
            ship.rotate(Ship.COUNTERCLOCKWISE);
        }
        else {
            consumed = false;
        }

        if (consumed) e.consume();
    }

    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == keyMapping.thrust) {
            ship.thrust(false);                 //engine off
            thrust_on = false;
        }
        else if (keyCode == keyMapping.right &&
                 rotation_direction == Ship.CLOCKWISE
                 ||
                 keyCode == keyMapping.left &&
                 rotation_direction == Ship.COUNTERCLOCKWISE) {
            ship.rotate(Ship.STOP);             //stop rotation
            rotation_direction = Ship.STOP;
        }
    }

    public void keyTyped(KeyEvent e) {
        // have to implement this because it's in KeyListener
    }
}

class KeyMapping {

    static final KeyMapping keyMapping1 =
        new KeyMapping(KeyEvent.VK_LEFT,
                       KeyEvent.VK_RIGHT,
                       KeyEvent.VK_UP,
                       KeyEvent.VK_SPACE);

    static final KeyMapping keyMapping2 =
        new KeyMapping(KeyEvent.VK_X,
                       KeyEvent.VK_V,
                       KeyEvent.VK_D,
                       KeyEvent.VK_ALT);

    int left, right, thrust, fire;

    KeyMapping(int k_left, int k_right, int k_thrust, int k_fire) {
        left   = k_left;
        right  = k_right;
        thrust = k_thrust;
        fire   = k_fire;
    }
}
