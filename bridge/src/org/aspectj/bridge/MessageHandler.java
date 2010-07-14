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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This handler accumulates messages. To control messages accumulated, clients can ignore messages of a given kind, or install a
 * listener/interceptor. The interceptor handles all messages (even null) first, and can halt further processing/accumlation by
 * returning true. Clients can obtain messages accumulated using the get... methods. XXX this does not permit messages to be
 * removed.
 * 
 * @author PARC
 * @author Andy Clement
 */
public class MessageHandler implements IMessageHolder {

	/** messages accumulated */
	protected final ArrayList<IMessage> messages;
	/** kinds of messages to be ignored */
	protected final ArrayList<IMessage.Kind> ignoring;
	/** result of handleMessage(..) for messages not accumulated (ignored) */
	protected boolean handleMessageResult;
	/** listener which can halt processing by returning true */
	protected IMessageHandler interceptor;

	/**
	 * same as MessageHandler(false)
	 */
	public MessageHandler() {
		this(false);
	}

	/**
	 * @param accumulateOnly the result of handleMessage (i.e., if true, then only accumulate messages - stop processing
	 */
	public MessageHandler(boolean accumulateOnly) {
		messages = new ArrayList<IMessage>();
		ignoring = new ArrayList<IMessage.Kind>();
		init(accumulateOnly);
		ignore(IMessage.WEAVEINFO); // Off by default, need to explicitly be enabled (see -showWeaveInfo)
	}

	/**
	 * Initialize this, removing any messages accumulated, kinds being ignored, or interceptor. Assume that this should return false
	 * from handleMessage(..).
	 */
	public void init() {
		init(false);
	}

	/**
	 * Initialize this, removing any messages accumulated, kinds being ignored, or interceptor.
	 * 
	 * @param accumulateOnly boolean value returned from handleMessage after accumulating in list
	 */
	public void init(boolean accumulateOnly) {
		handleMessageResult = accumulateOnly;
		if (0 < messages.size()) {
			messages.clear();
		}
		if (0 < ignoring.size()) {
			boolean ignoringWeaveMessages = isIgnoring(IMessage.WEAVEINFO);
			ignoring.clear();
			if (ignoringWeaveMessages) {
				ignore(IMessage.WEAVEINFO);
			}
		}
		if (null != interceptor) {
			interceptor = null;
		}
	}

	/**
	 * Clear the messages without changing other behavior.
	 */
	public void clearMessages() {
		if (0 < messages.size()) {
			messages.clear();
		}
	}

	// ---------------------- IMessageHandler implementation
	/**
	 * This implementation accumulates message. If an interceptor is installed and returns true (message handled), then processing
	 * halts and the message is not accumulated.
	 * 
	 * @see org.aspectj.bridge.IMessageHandler#handleMessage(IMessage)
	 * @return true on interception or the constructor value otherwise
	 */
	public boolean handleMessage(IMessage message) {
		if ((null != interceptor) && (interceptor.handleMessage(message))) {
			return true;
		}
		if (null == message) {
			throw new IllegalArgumentException("null message");
		}
		if (!ignoring.contains(message.getKind())) {
			messages.add(message);
		}
		return handleMessageResult;
	}

	/**
	 * @return true if this kind has been flagged to be ignored.
	 * @see #ignore(IMessage.Kind)
	 * @see org.aspectj.bridge.IMessageHandler#isIgnoring(Kind)
	 */
	public boolean isIgnoring(IMessage.Kind kind) {
		return ((null != kind) && (ignoring.contains(kind)));
	}

	// ---------------------- end of IMessageHandler implementation

	/**
	 * Set a message kind to be ignored from now on
	 */
	public void ignore(IMessage.Kind kind) { // XXX sync
		if ((null != kind) && (!ignoring.contains(kind))) {
			ignoring.add(kind);
		}
	}

	/**
	 * Remove a message kind from the list of those ignored from now on.
	 */
	public void dontIgnore(IMessage.Kind kind) {
		if (null != kind) {
			ignoring.remove(kind);
		}
	}

	/**
	 * @see org.aspectj.bridge.IMessageHolder#hasAnyMessage(Kind, boolean)
	 */
	public boolean hasAnyMessage(final IMessage.Kind kind, final boolean orGreater) {
		if (null == kind) {
			return (0 < messages.size());
		}
		if (!orGreater) {
			for (IMessage m : messages) {
				if (kind == m.getKind()) {
					return true;
				}
			}
		} else {
			for (IMessage m : messages) {
				if (kind.isSameOrLessThan(m.getKind())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return number of messages accumulated of a given kind
	 */
	public int numMessages(IMessage.Kind kind, final boolean orGreater) {
		if (null == kind) {
			return messages.size();
		}
		int result = 0;
		if (!orGreater) {
			for (IMessage m : messages) {
				if (kind == m.getKind()) {
					result++;
				}
			}
		} else {
			for (IMessage m : messages) {
				if (kind.isSameOrLessThan(m.getKind())) {
					result++;
				}
			}
		}
		return result;
	}

	/**
	 * @see org.aspectj.bridge.IMessageHolder#getUnmodifiableListView()
	 */
	public List<IMessage> getUnmodifiableListView() {
		return Collections.unmodifiableList(messages);
	}

	/**
	 * Get all messages or those of a specific kind. Pass null to get all kinds.
	 * 
	 * @param kind the IMessage.Kind expected, or null for all messages
	 * @return IMessage[] of messages of the right kind
	 */
	public IMessage[] getMessages(IMessage.Kind kind, final boolean orGreater) {
		if (null == kind) {
			return messages.toArray(IMessage.RA_IMessage);
		}
		ArrayList<IMessage> result = new ArrayList<IMessage>();
		if (!orGreater) {
			for (IMessage m : messages) {
				if (kind == m.getKind()) {
					result.add(m);
				}
			}
		} else {
			for (IMessage m : messages) {
				if (kind.isSameOrLessThan(m.getKind())) {
					result.add(m);
				}
			}
		}
		if (0 == result.size()) {
			return IMessage.RA_IMessage;
		}
		return result.toArray(IMessage.RA_IMessage);
	}

	/**
	 * @return array of error messages, or IMessage.NONE
	 */
	public IMessage[] getErrors() {
		return getMessages(IMessage.ERROR, false);
	}

	/**
	 * @return array of warning messages, or IMessage.NONE
	 */
	public IMessage[] getWarnings() {
		return getMessages(IMessage.WARNING, false);
	}

	/**
	 * Set the interceptor which gets any message before we process it.
	 * 
	 * @param interceptor the IMessageHandler passed the message. Pass null to remove the old interceptor.
	 */
	public void setInterceptor(IMessageHandler interceptor) {
		this.interceptor = interceptor;
	}

	/**
	 * @return String containing list of messages
	 */
	public String toString() {
		if (0 == messages.size()) {
			return "MessageHandler: no messages";
		} else {
			return "MessageHandler: " + messages;
		}

	}

}
