/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/


package figures;

import java.awt.*;
import java.awt.geom.*;

public class Box extends ShapeFigureElement {
    private Point _p0;
    private Point _p1;
    private Point _p2;
    private Point _p3;

    public Box(int x0, int y0, int width, int height) {
        _p0 = new Point(x0, y0);
        _p1 = new Point(x0+width, y0);
        _p2 = new Point(x0+width, y0+height);
        _p3 = new Point(x0, y0+height);
    }

    public Point getP0() { return _p0; }
    public Point getP1() { return _p1; }
    public Point getP2() { return _p2; }
    public Point getP3() { return _p3; }

    public void move(int dx, int dy) {
        _p0.move(dx, dy);
        _p1.move(dx, dy);
        _p2.move(dx, dy);
        _p3.move(dx, dy);
    }

    public void checkBoxness() {
        if ((_p0.getX() == _p3.getX()) &&
            (_p1.getX() == _p2.getX()) &&
            (_p0.getY() == _p1.getY()) &&
            (_p2.getY() == _p3.getY()))
          return;
        throw new IllegalStateException("This is not a square.");
    }

    public String toString() {
        return "Box(" + _p0 + ", " + _p1 + ", " + _p2 + ", " + _p3 + ")";
    }

    public Shape getShape() {
        return new Rectangle(getP1().getX(),
                             getP1().getY(),
                             getP3().getX() - getP1().getX(),
                             getP3().getY() - getP1().getY());
    }
}

