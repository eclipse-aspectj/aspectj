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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implement messages. This implementation is immutable if ISourceLocation is immutable.
 */
public class Message implements IMessage {
	private final String message;
	private final IMessage.Kind kind;
	private final Throwable thrown;
	private final ISourceLocation sourceLocation;
	private final String details;
	private final List<ISourceLocation> extraSourceLocations;
	private final boolean declared; // Is it a DEOW ?
	private final int id;
	private final int sourceStart, sourceEnd;

	/**
	 * Create a (compiler) error or warning message
	 * 
	 * @param message the String used as the underlying message
	 * @param location the ISourceLocation, if any, associated with this message
	 * @param isError if true, use IMessage.ERROR; else use IMessage.WARNING
	 */
	public Message(String message, ISourceLocation location, boolean isError) {
		this(message, (isError ? IMessage.ERROR : IMessage.WARNING), null, location);
	}

	public Message(String message, ISourceLocation location, boolean isError, ISourceLocation[] extraSourceLocations) {
		this(message, "", (isError ? IMessage.ERROR : IMessage.WARNING), location, null,
				(extraSourceLocations.length > 0 ? extraSourceLocations : null));
	}

	/**
	 * Create a message, handling null values for message and kind if thrown is not null.
	 * 
	 * @param message the String used as the underlying message
	 * @param kind the IMessage.Kind of message - not null
	 * @param thrown the Throwable, if any, associated with this message
	 * @param sourceLocation the ISourceLocation, if any, associated with this message
	 * @param details descriptive information about the message
	 * @throws IllegalArgumentException if message is null and thrown is null or has a null message, or if kind is null and thrown
	 *         is null.
	 */
	public Message(String message, String details, IMessage.Kind kind, ISourceLocation sourceLocation, Throwable thrown,
			ISourceLocation[] extraSourceLocations) {
		this(message, details, kind, sourceLocation, thrown, extraSourceLocations, false, 0, -1, -1);
	}

	public Message(String message, String details, IMessage.Kind kind, ISourceLocation sLoc, Throwable thrown,
			ISourceLocation[] otherLocs, boolean declared, int id, int sourcestart, int sourceend) {
		this.details = details;
		this.id = id;
		this.sourceStart = sourcestart;
		this.sourceEnd = sourceend;
		this.message = ((message != null) ? message : ((thrown == null) ? null : thrown.getMessage()));
		this.kind = kind;
		this.sourceLocation = sLoc;
		this.thrown = thrown;
		if (otherLocs != null) {
			this.extraSourceLocations = Collections.unmodifiableList(Arrays.asList(otherLocs));
		} else {
			this.extraSourceLocations = Collections.emptyList();
		}
		if (null == this.kind) {
			throw new IllegalArgumentException("null kind");
		}
		if (null == this.message) {
			throw new IllegalArgumentException("null message");
		}
		this.declared = declared;
	}

	/**
	 * Create a message, handling null values for message and kind if thrown is not null.
	 * 
	 * @param message the String used as the underlying message
	 * @param kind the IMessage.Kind of message - not null
	 * @param thrown the Throwable, if any, associated with this message
	 * @param sourceLocation the ISourceLocation, if any, associated with this message
	 * @throws IllegalArgumentException if message is null and thrown is null or has a null message, or if kind is null and thrown
	 *         is null.
	 */
	public Message(String message, IMessage.Kind kind, Throwable thrown, ISourceLocation sourceLocation) {
		this(message, "", kind, sourceLocation, thrown, null);
	}

	/** @return the kind of this message */
	public IMessage.Kind getKind() {
		return kind;
	}

	/** @return true if kind == IMessage.ERROR */
	public boolean isError() {
		return kind == IMessage.ERROR;
	}

	/** @return true if kind == IMessage.WARNING */
	public boolean isWarning() {
		return kind == IMessage.WARNING;
	}

	/** @return true if kind == IMessage.DEBUG */
	public boolean isDebug() {
		return kind == IMessage.DEBUG;
	}

	public boolean isTaskTag() {
		return kind == IMessage.TASKTAG;
	}

	/**
	 * @return true if kind == IMessage.INFO
	 */
	public boolean isInfo() {
		return kind == IMessage.INFO;
	}

	/** @return true if kind == IMessage.ABORT */
	public boolean isAbort() {
		return kind == IMessage.ABORT;
	}

	/** Caller can verify if this message came about because of a DEOW */
	public boolean getDeclared() {
		return declared;
	}

	/**
	 * @return true if kind == IMessage.FAIL
	 */
	public boolean isFailed() {
		return kind == IMessage.FAIL;
	}

	/** @return non-null String with simple message */
	final public String getMessage() {
		return message;
	}

	/** @return Throwable associated with this message, or null if none */
	final public Throwable getThrown() {
		return thrown;
	}

	/** @return ISourceLocation associated with this message, or null if none */
	final public ISourceLocation getSourceLocation() {
		return sourceLocation;
	}

	public String toString() {
		return MessageUtil.renderMessage(this, false);
	}

	public String getDetails() {
		return details;
	}

	public List<ISourceLocation> getExtraSourceLocations() {
		return extraSourceLocations;
	}

	public int getID() {
		return id;
	}

	public int getSourceStart() {
		return sourceStart;
	}

	public int getSourceEnd() {
		return sourceEnd;
	}
}
