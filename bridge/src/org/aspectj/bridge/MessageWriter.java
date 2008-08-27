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

package org.aspectj.bridge;

import java.io.PrintWriter;

/**
 * An IMessageHandler implementation that writes all to a PrintWriter.
 * Clients may set this up to throw AbortException for FAIL or ERROR messages.
 * Subclasses may control whether messages are printed and how they
 * are rendered by overriding render(IMessage).
 */
public class MessageWriter implements IMessageHandler {
    
    protected PrintWriter writer;
    protected boolean abortOnFailure;
    public MessageWriter(PrintWriter writer, boolean abortOnFailure) {
        this.writer = (null != writer ? writer : new PrintWriter(System.out));
        this.abortOnFailure = abortOnFailure;
    }
    
    /**
     * Handle message by printing and
     * (if abortOnFailure) throwing an AbortException if 
     * the messages is a failure or an abort (but not for errors).
	 * @see org.aspectj.bridge.IMessageHandler#handleMessage(IMessage)
	 */
	public boolean handleMessage(IMessage message) throws AbortException {
        if ((null != message) && !isIgnoring(message.getKind())) {
            String result = render(message);
            if (null != result) {
                writer.println(result);
                writer.flush();
                if (abortOnFailure
                    && (message.isFailed() || message.isAbort())) {
                    throw new AbortException(message);
                }
            }
        }
		return true;
	}
    
    /**
	 * @see org.aspectj.bridge.IMessageHandler#isIgnoring(org.aspectj.bridge.IMessage.Kind)
	 */
	public boolean isIgnoring(IMessage.Kind kind) { 
        // XXX share MessageHandler implementation in superclass
		return false;
	}

    /**
     * No-op
     * @see org.aspectj.bridge.IMessageHandler#isIgnoring(org.aspectj.bridge.IMessage.Kind)
     * @param kind
     */
    public void dontIgnore(IMessage.Kind kind) {
        
    }

    /**
     * No-op
     * @see org.aspectj.bridge.IMessageHandler#ignore(org.aspectj.bridge.IMessage.Kind)
     * @param kind
     */
	public void ignore(IMessage.Kind kind) {
	}

    /** @return null to not print, or message rendering (including newlines) */
    protected String render(IMessage message) {
        return message.toString();    
    }

}
