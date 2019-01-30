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
package org.aspectj.ajde.core.internal;

import org.aspectj.ajde.core.IBuildMessageHandler;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessage.Kind;

/**
 * Enables the messages from the compiler/weaver to be passed on
 * to the tool's implementation so they can handle it as they wish
 */
public class AjdeCoreMessageHandlerAdapter implements IMessageHandler {

	private IBuildMessageHandler handler;
	
	public AjdeCoreMessageHandlerAdapter(IBuildMessageHandler messageHandler) {
		this.handler = messageHandler;
	}
	
	public void dontIgnore(Kind kind) {
		handler.dontIgnore(kind);
	}

	public boolean handleMessage(IMessage message) throws AbortException {
		return handler.handleMessage(message);
	}

	public void ignore(Kind kind) {
		handler.ignore(kind);
	}

	public boolean isIgnoring(Kind kind) {
		return handler.isIgnoring(kind);
	}

}
