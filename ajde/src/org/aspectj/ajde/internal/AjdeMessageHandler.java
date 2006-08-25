/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - moved into separate class
 *******************************************************************/
package org.aspectj.ajde.internal;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.TaskListManager;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessage.Kind;

/**
 * IMessageHandler used by Ajde. No messages are stored
 * within the handler as it delegates to the TaskListManager.
 * By default IMessage.INFO and IMessage.WEAVEINFO messages
 * are ignored.
 */
public class AjdeMessageHandler implements IMessageHandler {

	private TaskListManager taskListManager;
	private List ignoring;
	
	public AjdeMessageHandler() {
        ignoring = new ArrayList();
        ignore(IMessage.INFO);
        ignore(IMessage.WEAVEINFO);
		this.taskListManager = Ajde.getDefault().getTaskListManager();
	}	

	public boolean handleMessage(IMessage message) throws AbortException {
        IMessage.Kind kind = message.getKind(); 
        if (kind == IMessage.ABORT) return handleAbort(message);
        if (isIgnoring(kind)) {
            return true;
        }	
		taskListManager.addSourcelineTask(message);
		return true;
	}
	
	private boolean handleAbort(IMessage abortMessage) {
		throw new AbortException(abortMessage);
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
	
}
