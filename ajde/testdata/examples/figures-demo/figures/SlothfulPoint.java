/*
Copyright (c) 2002 Palo Alto Research Center Incorporated. All Rights Reserved.
 */

package figures;

import java.awt.*;
import java.awt.geom.*;

/**
 * This class makes mistakes to be caught by invariant checkers.
 */
public class SlothfulPoint extends ShapeFigureElement {
    private int _x;
    private int _y;

    public SlothfulPoint(int x, int y) {
    }

    public void setX(int x) { 
    	_x = x; 
    }
    
    public void setY(int y) { 
    	_y = y; 
    }

    public void move(int dx, int dy) {
	//_x += dx;
 	//_y += dy;
    }

    public String toString() {
        return "SlothfulPoint";
    }

    public Shape getShape() {
	return new Ellipse2D.Float((float)_x,
                                   (float)_y, 1.0f, 1.0f);
    }
}

