/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

 
package org.aspectj.tools.ajbrowser.ui;

import org.aspectj.ajde.IdeUIAdapter;
import org.aspectj.tools.ajbrowser.BrowserManager;

/**
 * AjBrowser implementation if IdeUIAdapter which displays the provided
 * information in the status bar at the bottom of the AjBrowser GUI.
 */
public class BrowserUIAdapter implements IdeUIAdapter {
	
	public void displayStatusInformation(String message) {
		BrowserManager.getDefault().setStatusInformation(message);
	}
}
