/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/


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

    public int getX() { return _x; }

    public int getY() { return _y; }

    public void setX(int x) { _x = x; }

    public void setY(int y) { _y = y; }

    public void move(int dx, int dy) {
        _x += dx;
        _y += dy;
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

