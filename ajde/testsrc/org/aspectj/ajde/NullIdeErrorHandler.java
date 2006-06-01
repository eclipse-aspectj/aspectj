/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * ******************************************************************/

package org.aspectj.ajde;

/**
 * @author Mik Kersten
 */
public class NullIdeErrorHandler implements ErrorHandler {

	public void handleWarning(String message) {
		System.out.println("NullIde> warning: " + message);
	}

	public void handleError(String message) {
		System.out.println("NullIde> error: " + message);
	}

	public void handleError(String message, Throwable t) {
		System.out.println("NullIde> error: " + message);
		t.printStackTrace(System.out);
	}
}
