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

package answers;

import figures.FigureElement;
import figures.Group;
import java.awt.Rectangle;

aspect Answer4c {
    private Rectangle Group.cache = null;

    Rectangle around(Group g):
            execution(Rectangle Group.getBounds())
            && this(g) {
        if (g.cache == null) {
            g.cache = proceed(g);
        }
        return g.cache;
    }

    before(Group g):
            call(void move(int, int))
            && target(g) {
        g.cache = null;
    }
}
