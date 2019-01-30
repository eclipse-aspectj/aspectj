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
package org.aspectj.ajde.core;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;

/**
 * Interface that handles messages sent from the compiler.
 * Implementations define which messages are logged and whether
 * the handler aborts the process.  
 */
public interface IBuildMessageHandler {

	/** 
	 * Handle message by reporting and/or throwing an AbortException.
	 * 
	 * @param message the IMessage to handle - never null
	 * @return true if this message was handled by this handler
	 * @throws IllegalArgumentException if message is null
	 * @throws AbortException depending on handler logic.
	 */
	boolean handleMessage(IMessage message) throws AbortException;

	/**
	 * Signal whether this will ignore messages of a given type.
	 * Clients may use this to avoid constructing or sending 
	 * certain messages.
	 * 
	 * @return true if this handler is ignoring all messages of this type
	 */
	boolean isIgnoring(IMessage.Kind kind);

    /**
     * Allow fine grained configuration after initialization. 
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
