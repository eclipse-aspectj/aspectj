/*
Copyright (c) 2001-2002 Palo Alto Research Center Incorporated. All Rights Reserved.
 */

package figures;

import java.awt.*;
import java.awt.geom.*;

public interface FigureElement {
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 500;

    public abstract void move(int dx, int dy);

    public abstract Rectangle getBounds();

    public abstract boolean contains(Point2D p);

    public abstract void paint(Graphics2D g2);
}
