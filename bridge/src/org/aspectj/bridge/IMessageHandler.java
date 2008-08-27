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
 * Handle messages, logging and/or aborting as appropriate.
 * Implementations define which messages are logged and whether
 * the handler aborts the process.  
 * For messages that are costly to construct, clients may query 
 * {@link #isIgnoring(IMessage.Kind)}
 * to avoid construction if the message will be ignored.
 * Clients passing messages to an IMessageHandler should not
 * interfere with aborts by catching AbortException unless
 * otherwise required by their logic or the message handler.
 */
public interface IMessageHandler {
	/** print all to System.err and throw AbortException on failure or abort messages */
	public static final IMessageHandler SYSTEM_ERR =
		new MessageWriter(new PrintWriter(System.err, true), true);

	/** print all to System.out but do not throw AbortException on failure or abort messages */
	public static final IMessageHandler SYSTEM_OUT =
		new MessageWriter(new PrintWriter(System.out, true), false);

	/** Throw exceptions for anything with ERROR or greater severity */
	public static final IMessageHandler THROW =
		new IMessageHandler() {
			public boolean handleMessage(IMessage message) {
				if (message.getKind().compareTo(IMessage.ERROR) >= 0) {
					throw new AbortException(message);
				} else {
					return SYSTEM_OUT.handleMessage(message);
				}
			}
			public boolean isIgnoring(IMessage.Kind kind) {
				return false;
			}
            public void dontIgnore(IMessage.Kind kind) {
                
            }
			public void ignore(IMessage.Kind kind) {
			}
		};

	/** 
	 * Handle message, by reporting and/or throwing an AbortException.
	 * @param message the IMessage to handle - never null
	 * @return true if this message was handled by this handler
	 * @throws IllegalArgumentException if message is null
	 * @throws AbortException depending on handler logic.
	 */
	boolean handleMessage(IMessage message) throws AbortException;

	/**
	 * Signal clients whether this will ignore messages of a given type.
	 * Clients may use this to avoid constructing or sending certain messages.
	 * @return true if this handler is ignoring all messages of this type
	 */
	boolean isIgnoring(IMessage.Kind kind);

    /**
     * Allow fine grained configuration after initialization. Minaly used in LTW. Most of the
     * implementation can have this method be a no-op.
     *  
     * @param kind
     */
    void dontIgnore(IMessage.Kind kind);
    
    /**
     * Allow fine grained configuration after initialization. 
     *  
     * @param kind
     */
    void ignore(IMessage.Kind kind);
}
