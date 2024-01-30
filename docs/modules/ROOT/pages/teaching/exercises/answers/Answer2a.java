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

import figures.Point;
import figures.FigureElement;

public aspect Answer2a {
    before(int newValue): set(int Point.*) && args(newValue) {
        if (newValue < 0) {
            throw new IllegalArgumentException("too small");
        }
    }
}
