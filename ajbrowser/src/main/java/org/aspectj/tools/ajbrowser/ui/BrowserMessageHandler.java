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
package org.aspectj.tools.ajbrowser.ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.IUIBuildMessageHandler;
import org.aspectj.ajde.ui.swing.ErrorDialog;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;

/**
 * MessageHandler used by AjBrowser that displays ERROR messages with exceptions and ABORT messages in an error dialog. Other
 * messages are displayed by the MessageHandlerPanel. By default INFO and WEAVEINFO messages are ignored.
 */
public class BrowserMessageHandler implements IUIBuildMessageHandler {

	private List<IMessage.Kind> ignoring;
	private List<IMessage> messages;

	public BrowserMessageHandler() {
		ignoring = new ArrayList<>();
		messages = new ArrayList<>();
		ignore(IMessage.INFO);
		ignore(IMessage.WEAVEINFO);
	}

	public boolean handleMessage(IMessage message) throws AbortException {
		Kind messageKind = message.getKind();
		if (isIgnoring(messageKind)) {
			return true;
		}
		if (messageKind.equals(IMessage.ABORT) || (message.getThrown() != null)) {
			String stack = getStackTraceAsString(message.getThrown());
			ErrorDialog errorDialog = new ErrorDialog(Ajde.getDefault().getRootFrame(), "AJDE Error", message.getThrown(),
					message.getMessage(), stack);
			errorDialog.setVisible(true);
			return true;
		}
		messages.add(message);
		return true;
	}

	public void dontIgnore(Kind kind) {
		if (null != kind) {
			ignoring.remove(kind);
		}
	}

	public boolean isIgnoring(Kind kind) {
		return ((null != kind) && (ignoring.contains(kind)));
	}

	public void ignore(Kind kind) {
		if ((null != kind) && (!ignoring.contains(kind))) {
			ignoring.add(kind);
		}
	}

	public List<IMessage> getMessages() {
		return messages;
	}

	private String getStackTraceAsString(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		if (t != null) {
			t.printStackTrace(new PrintWriter(stringWriter));
			return stringWriter.getBuffer().toString();
		}
		return "<no stack trace available>";
	}

	public void reset() {
		messages.clear();
	}

}
