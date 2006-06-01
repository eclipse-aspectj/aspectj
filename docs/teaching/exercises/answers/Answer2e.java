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

import figures.*;

import java.awt.Rectangle;

aspect Answer2e {
    void around(Point p, int dx, int dy):
            target(p) && call(void move(int, int)) && args(dx, dy) {
        int preX = p.getX();
        int preY = p.getY();

        proceed(p, dx, dy);

        int postX = p.getX();
        int postY = p.getY();

        if ((postX != preX + dx) || (postY != preY + dy)) {
            throw new IllegalStateException("point didn't move properly");
        }
    }
}
