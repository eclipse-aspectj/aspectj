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

import figures.FigureElement;
import figures.Group;
import java.awt.Rectangle;

aspect Answer4a {
    private Rectangle wholeCanvas =
        new Rectangle(FigureElement.MIN_VALUE, FigureElement.MIN_VALUE,
                      FigureElement.MAX_VALUE - FigureElement.MIN_VALUE,
                      FigureElement.MAX_VALUE - FigureElement.MIN_VALUE);

    Rectangle around(): execution(Rectangle Group.getBounds()) {
	return wholeCanvas;
    }
}
