/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.tools.ajbrowser.core;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.swing.ErrorDialog;
import org.aspectj.tools.ajbrowser.BrowserManager;

/**
 * Error handler used by AjBrowser. Handles errors and warnings by
 * producing an error/warning dialog.
 */
public class BrowserErrorHandler {

	public static void handleWarning(String message) {
		JOptionPane.showMessageDialog(BrowserManager.getDefault()
				.getRootFrame(), message, "AJBrowser Warning",
				JOptionPane.WARNING_MESSAGE);
	}

	public static void handleError(String errorMessage) {
		handleError(errorMessage, null);
	}

	public static void handleError(String message, Throwable t) {
		String stack = getStackTraceAsString(t);
		ErrorDialog errorDialog = new ErrorDialog(Ajde.getDefault()
				.getRootFrame(), "AJBrowser Error", t, message, stack);
		errorDialog.setVisible(true);
	}

	private static String getStackTraceAsString(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		if (t != null) {
			t.printStackTrace(new PrintWriter(stringWriter));
			return stringWriter.getBuffer().toString();
		} 
		return "<no stack trace available>";
	}

}
