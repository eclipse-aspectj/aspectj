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

package answers;

import support.Log;

import figures.Point;
import figures.Group;
import figures.FigureElement;

aspect Answer3c {
    private Group Point.enclosingGroup = null;

    before(Point p, Group g):
        execution(void add(FigureElement)) && args(p) && target(g) {
        p.enclosingGroup = g;
    }

    before(Point p):
        call(void move(int, int)) && target(p) {
        Log.log("moving as a part of " + p.enclosingGroup);
    }

}
