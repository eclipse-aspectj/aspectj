/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package answers;

import figures.FigureElement;
import figures.Group;
import figures.Point;
import java.awt.Rectangle;

aspect Answer4e {
    private Rectangle Group.cache = null;
    private Group FigureElement.enclosingGroup = null;

    before(FigureElement p, Group g): 
	    execution(void add(FigureElement)) 
	    && args(p) 
	    && target(g) {
        p.enclosingGroup = g;
    }

    Rectangle around(Group g):
            execution(Rectangle Group.getBounds()) 
	    && this(g) {
        if (g.cache == null) {
            g.cache = proceed(g);
        }
        return g.cache;
    }

    before(Point p): 
	    set(* Point.*) 
	    && target(p) {
        FigureElement fe = p;
        while (fe.enclosingGroup != null) {
            fe.enclosingGroup.cache = null;
            fe = fe.enclosingGroup;
        }
    }
}
