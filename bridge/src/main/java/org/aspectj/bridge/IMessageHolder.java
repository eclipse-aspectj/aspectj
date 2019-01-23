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

import java.util.List;

/**
 * Hold and query a collection of messages.
 */
public interface IMessageHolder extends IMessageHandler { // XXX do not extend - mix instead
	// XXX go to LT EQ GT GE LE rather than simple orGreater
	/** value for orGreater parameter */
	public static final boolean ORGREATER = true;

	/** value for orGreater parameter */
	public static final boolean EQUAL = false;

	/**
	 * Tell whether this holder has any message of this kind (optionally or greater).
	 * 
	 * @param kind the IMessage.Kind to check for - accept any if null
	 * @param orGreater if true, also any greater than the target kind as determined by IMessage.Kind.COMPARATOR
	 * @return true if this holder has any message of this kind, or if orGreater and any message has a greater kind, as determined
	 *         by IMessage.Kind.COMPARATOR
	 */
	boolean hasAnyMessage(IMessage.Kind kind, boolean orGreater);

	/**
	 * Count the messages currently held by this holder. Pass null to get all kinds.
	 * 
	 * @param kind the IMessage.Kind expected, or null for all messages
	 * @param orGreater if true, also any greater than the target kind as determined by IMessage.Kind.COMPARATOR
	 * @return number of IMessage held (now) by this holder
	 */
	int numMessages(IMessage.Kind kind, boolean orGreater);

	/**
	 * Get all messages or those of a specific kind. Pass null to get all kinds.
	 * 
	 * @param kind the IMessage.Kind expected, or null for all messages
	 * @param orGreater if true, also get any greater than the target kind as determined by IMessage.Kind.COMPARATOR
	 * @return IMessage[] of messages of the right kind, or IMessage.NONE
	 */
	IMessage[] getMessages(IMessage.Kind kind, boolean orGreater);

	/** @return unmodifiable List view of underlying collection of IMessage */
	List<IMessage> getUnmodifiableListView();

	/**
	 * Clear any messages.
	 * 
	 * @throws UnsupportedOperationException if message list is read-only
	 */
	void clearMessages() throws UnsupportedOperationException;
}
