/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde;

import org.aspectj.ajde.ui.*;

public class NullIdeUIAdapter implements IdeUIAdapter {
	private final static boolean debugTests = false;
	public void displayStatusInformation(String message) {
		if (debugTests) System.out.println("NullIde>" + message);
	}
	
    public void resetGUI() {
        // not implemented
    }
}
