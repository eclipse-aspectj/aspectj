/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

public class TraceMessageHandler implements IMessageHandler {

	private Trace trace;

	public TraceMessageHandler () {
		this(TraceFactory.getTraceFactory().getTrace(TraceMessageHandler.class));
	}

	/**
	 * Used for testing
	 *
	 */
	protected TraceMessageHandler (Trace t) {
		this.trace = t;
	}
	
	/* Ignore this and defer all decisions about what we log to the Trace
	 * configuration
	 */
	public void dontIgnore(Kind kind) {
	}

	public boolean handleMessage(IMessage message) throws AbortException {
		if (message instanceof WeaveMessage) {
			trace.debug(render(message));
		}
		else if (message.isDebug()) {
			trace.debug(render(message));
		}
		else if (message.isInfo()) {
			trace.info(render(message));
		}
		else if (message.isWarning()) {
			trace.warn(render(message),message.getThrown());
		}
		else if (message.isError()) {
			trace.error(render(message),message.getThrown());
		}
		else if (message.isFailed()) {
			trace.fatal(render(message),message.getThrown());
		}
		else if (message.isAbort()) {
			trace.fatal(render(message),message.getThrown());
		}
		else return false;
		
		return true;
	}

	/* Ignore this and defer all decisions about what we log to the Trace
	 * configuration
	 */
	public boolean isIgnoring(Kind kind) {
		return false;
	}

    protected String render(IMessage message) {
        return message.toString();    
    }

}
