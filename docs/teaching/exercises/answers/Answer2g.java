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

import figures.*;

aspect Answer2g {
    int around(int val): (set(int Point._x) || set(int Point._y))
                         && args(val) {
       return proceed(trim(val));
    }

    private int trim(int val) {
        return Math.max(Math.min(val, FigureElement.MAX_VALUE),
                        FigureElement.MIN_VALUE);
    }
}
