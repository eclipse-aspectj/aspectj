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


package org.aspectj.ajde.internal;

import org.aspectj.ajde.ErrorHandler;

public class DebugErrorHandler implements ErrorHandler {

    public void handleWarning(String message) {
        System.err.println("> WARNING: " + message);
    }

    public void handleError(String errorMessage) {
        handleError(errorMessage, null);
    }  

    public void handleError(String message, Throwable t) {
    	System.err.println("> ERROR: " + message);
        throw (RuntimeException)t;
    }
}
