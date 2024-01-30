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

public abstract class ShapeFigureElement implements FigureElement {
    public abstract void move(int dx, int dy);

    public abstract Shape getShape();

    public Rectangle getBounds() {
        return getShape().getBounds();
    }

    public boolean contains(Point2D p) {
        return getShape().contains(p);
    }

    public Color getLineColor() {
        return Color.black;
    }

    public Color getFillColor() {
        return Color.red;
    }

    public final void paint(Graphics2D g2) {
        Shape shape = getShape();
        g2.setPaint(getFillColor());
        g2.fill(shape);
        g2.setPaint(getLineColor());
        g2.draw(shape);
    }
}
