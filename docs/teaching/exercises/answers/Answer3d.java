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

import support.Log;

import figures.*;

aspect Answer3d {

    private boolean Point.inGroup = false;

    before(Point p): 
            execution(void Group.add(FigureElement)) 
            && args(p) {
        if (p.inGroup) {
            throw new IllegalStateException();
        } else {
            p.inGroup = true;
        }
    }
}
