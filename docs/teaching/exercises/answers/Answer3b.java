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

aspect Answer3b {
    before():
	execution(void Group.add(FigureElement)) && args(Point) {
        Log.log("adding Point");
    }
}
