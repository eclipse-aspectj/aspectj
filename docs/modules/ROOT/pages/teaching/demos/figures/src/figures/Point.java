/*
Copyright (c) 2001-2002 Palo Alto Research Center Incorporated. All Rights Reserved.
 */

package figures;

import java.awt.*;
import java.awt.geom.*;

public class Point extends ShapeFigureElement {
    private int _x;
    private int _y;

    public Point(int x, int y) {
        _x = x;
        _y = y;
    }

    public int getX() { 
    	return _x; 
    }

    public void setX(int x) { 
    	_x = x; 
//		Canvas.updateHistory();
    }

    public int getY() { 
    	return _y; 
    }

    public void setY(int y) { 
    	_y = y; 
//    	Canvas.updateHistory();
    }

    public void move(int dx, int dy) {
 	 	setX(_x + dx);
      	setY(_y + dy);        
    }

    public String toString() {
        return "Point(" + _x + ", " + _y + ")";
    }

    /** The height of displayed {@link Point}s. */
    private final static int HEIGHT = 10;

    /** The width of displayed {@link Point}s. -- same as {@link HEIGHT}. */
    private final static int WIDTH  = Point.HEIGHT;

    public Shape getShape() {
	return new Ellipse2D.Float((float)getX()-Point.WIDTH/2,
                                   (float)getY()-Point.HEIGHT/2,
                                   (float)Point.HEIGHT,
                                   (float)Point.WIDTH);
    }
}

