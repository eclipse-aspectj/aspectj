/*
Copyright (c) 2002 Palo Alto Research Center Incorporated. All Rights Reserved.
 */

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
