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

public class Line extends ShapeFigureElement {
    private Point _p1;
    private Point _p2;

    public Line(Point p1, Point p2) {
        _p1 = p1;
        _p2 = p2;
    }

    public Point getP1() { return _p1; }
    public Point getP2() { return _p2; }

    public void move(int dx, int dy) {
        _p1.move(dx, dy);
        _p2.move(dx, dy);
    }

    public String toString() {
        return "Line(" + _p1 + ", " + _p2 + ")";
    }

    /**
     * Used to determine if this line {@link contains(Point2D)} a point.
     */
    final static int THRESHHOLD = 5;

    /**
     * Returns <code>true</code> if the point segment distance is less than
     * {@link THRESHHOLD}.
     */
    public boolean contains(Point2D p) {
        return getLine2D().ptLineDist(p) < THRESHHOLD;
    }

    private Line2D getLine2D() {
        return new Line2D.Float((float)getP1().getX(),
                                (float)getP1().getY(),
                                (float)getP2().getX(),
                                (float)getP2().getY());
    }

    public Shape getShape() {
        return getLine2D();
    }
}

