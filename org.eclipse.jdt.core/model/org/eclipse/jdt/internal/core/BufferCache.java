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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.internal.core.util.LRUCache;

/**
 * An LRU cache of <code>IBuffers</code>.
 */
public class BufferCache extends OverflowingLRUCache {
/**
 * Constructs a new buffer cache of the given size.
 */
public BufferCache(int size) {
	super(size);
}
/**
 * Constructs a new buffer cache of the given size.
 */
public BufferCache(int size, int overflow) {
	super(size, overflow);
}
/**
 * Returns true if the buffer is successfully closed and
 * removed from the cache, otherwise false.
 *
 * <p>NOTE: this triggers an external removal of this buffer
 * by closing the buffer.
 */
protected boolean close(LRUCacheEntry entry) {
	IBuffer buffer= (IBuffer) entry._fValue;
	if (buffer.hasUnsavedChanges()) {
		return false;
	} else {
		buffer.close();
		return true;
	}
}
	/**
	 * Returns a new instance of the reciever.
	 */
	protected LRUCache newInstance(int size, int overflow) {
		return new BufferCache(size, overflow);
	}
}
