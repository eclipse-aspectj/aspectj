/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core;

import java.util.EventObject;

/**
 * A buffer changed event describes how a buffer has changed. These events are
 * used in <code>IBufferChangedListener</code> notifications.
 * <p>
 * For text insertions, <code>getOffset</code> is the offset
 * of the first inserted character, <code>getText</code> is the
 * inserted text, and <code>getLength</code> is 0.
 * </p>
 * <p>
 * For text removals, <code>getOffset</code> is the offset
 * of the first removed character, <code>getText</code> is <code>null</code>,
 * and <code>getLength</code> is the length of the text that was removed.
 * </p>
 * <p>
 * For replacements (including <code>IBuffer.setContents</code>), 
 * <code>getOffset</code> is the offset
 * of the first replaced character, <code>getText</code> is the replacement
 * text, and <code>getLength</code> is the length of the original text
 * that was replaced.
 * </p>
 * <p>
 * When a buffer is closed, <code>getOffset</code> is 0, <code>getLength</code>
 * is 0, and <code>getText</code> is <code>null</code>.
 * </p>
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * Instances of this class are automatically created by the Java model.
 * </p>
 *
 * @see IBuffer
 */
public class BufferChangedEvent extends EventObject {

	/**
	 * The length of text that has been modified in the buffer.
	 */
	private int length;

	/**
	 * The offset into the buffer where the modification took place.
	 */
	private int offset;

	/**
	 * The text that was modified.
	 */
	private String text;

/**
 * Creates a new buffer changed event indicating that the given buffer has changed.
 */
public BufferChangedEvent(IBuffer buffer, int offset, int length, String text) {
	super(buffer);
	this.offset = offset;
	this.length = length;
	this.text = text;
}
/**
 * Returns the buffer which has changed.
 *
 * @return the buffer affected by the change
 */
public IBuffer getBuffer() {
	return (IBuffer) source;
}
/**
 * Returns the length of text removed or replaced in the buffer, or
 * 0 if text has been inserted into the buffer.
 *
 * @return the length of the original text fragment modified by the 
 *   buffer change (<code> 0 </code> in case of insertion).
 */
public int getLength() {
	return this.length;
}
/**
 * Returns the index of the first character inserted, removed, or replaced
 * in the buffer.
 *
 * @return the source offset of the textual manipulation in the buffer
 */
public int getOffset() {
	return this.offset;
}
/**
 * Returns the text that was inserted, the replacement text,
 * or <code>null</code> if text has been removed.
 *
 * @return the text corresponding to the buffer change (<code> null </code>
 *   in case of deletion).
 */
public String getText() {
	return this.text;
}
}
