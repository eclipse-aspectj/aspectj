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

aspect Answer2d {
    void around(int val): (set(int Point._x) || set(int Point._y))
                          && args(val) {
        if (val < 0)
            val = 0;
        proceed(val);
    }
}
