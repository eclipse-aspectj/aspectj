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

public interface FigureElement {
    public static final Rectangle MAX_BOUNDS = 
        new Rectangle(0, 0, 500, 500);

    public abstract void move(int dx, int dy);

    public abstract Rectangle getBounds();

    public abstract boolean contains(Point2D p);

    public abstract void paint(Graphics2D g2);
}
