/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde;

import org.aspectj.ajde.ui.*;

public class NullIdeUIAdapter implements IdeUIAdapter {
	
	public void displayStatusInformation(String message) {
		System.out.println("> status message: " + message);
	}
	
    public void resetGUI() {
        // not implemented
    }
}
