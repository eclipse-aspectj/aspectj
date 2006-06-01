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

import figures.Group;
import figures.FigureElement;

public aspect Answer2b {
    before(FigureElement newValue):
            call(void Group.add(FigureElement))
            && args(newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("null not allowed");
        }
    }
}
