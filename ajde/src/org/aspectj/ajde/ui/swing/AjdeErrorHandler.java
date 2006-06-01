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



package org.aspectj.ajde.ui.swing;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ErrorHandler;

public class AjdeErrorHandler implements ErrorHandler {

    public void handleWarning(String message) {
        JOptionPane.showMessageDialog(AjdeUIManager.getDefault().getRootFrame(),
                                      message,
                                      "AJDE Warning",
                                      JOptionPane.WARNING_MESSAGE);
    }

    public void handleError(String errorMessage) {
        handleError(errorMessage, null);
    }  

    public void handleError(String message, Throwable t) {
    	String stack = getStackTraceAsString(t);
        Ajde.getDefault().logEvent("Error: " + stack);
        ErrorDialog errorDialog = new ErrorDialog(AjdeUIManager.getDefault().getRootFrame(), "AJDE Error", t, message, stack);
        errorDialog.setVisible(true);
    }

    private String getStackTraceAsString(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        if (t != null) {
            t.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.getBuffer().toString();
        } else {
            return "<no stack trace available>";
        }
    }
}

