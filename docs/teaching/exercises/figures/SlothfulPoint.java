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

/**
 * This class makes mistakes to be caught by invariant checkers.
 */
public class SlothfulPoint extends ShapeFigureElement {
    private int _x;
    private int _y;

    public SlothfulPoint(int x, int y) {
    }

    public int getX() { return _x; }

    public int getY() { return _y; }

    public void setX(int x) { }

    public void setY(int y) { }

    public void move(int dx, int dy) {
        System.out.println("Slothful moving");
    }

    public String toString() {
        return "SlothfulPoint";
    }

    public Shape getShape() {
        return new Ellipse2D.Float((float)_x,
                                   (float)_y, 1.0f, 1.0f);
    }
}

