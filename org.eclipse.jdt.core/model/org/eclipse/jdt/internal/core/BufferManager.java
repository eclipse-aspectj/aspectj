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

import java.util.Enumeration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;

/**
 * The buffer manager manages the set of open buffers.
 * It implements an LRU cache of buffers.
 */
public class BufferManager implements IBufferFactory {

	protected static BufferManager DEFAULT_BUFFER_MANAGER;

	/**
	 * LRU cache of buffers. The key and value for an entry
	 * in the table is the identical buffer.
	 */
	protected OverflowingLRUCache openBuffers = new BufferCache(60);

/**
 * Creates a new buffer manager.
 */
public BufferManager() {
}
/**
 * Adds a buffer to the table of open buffers.
 */
protected void addBuffer(IBuffer buffer) {
	openBuffers.put(buffer.getOwner(), buffer);
}
/**
 * @see IBufferFactory#createBuffer(IOpenable)
 */
public IBuffer createBuffer(IOpenable owner) {
	IJavaElement element = (IJavaElement)owner;
	IResource resource = element.getResource();
	return 
		new Buffer(
			resource instanceof IFile ? (IFile)resource : null, 
			owner, 
			element.isReadOnly());
}

/**
 * Returns the open buffer associated with the given owner,
 * or <code>null</code> if the owner does not have an open
 * buffer associated with it.
 */
public IBuffer getBuffer(IOpenable owner) {
	return (IBuffer)openBuffers.get(owner);
}
/**
 * Returns the default buffer manager.
 */
public synchronized static BufferManager getDefaultBufferManager() {
	if (DEFAULT_BUFFER_MANAGER == null) {
		DEFAULT_BUFFER_MANAGER = new BufferManager();
	}
	return DEFAULT_BUFFER_MANAGER;
}
/**
 * Returns the default buffer factory.
 */
public IBufferFactory getDefaultBufferFactory() {
	return this;
}
/**
 * Returns an enumeration of all open buffers.
 * <p> 
 * The <code>Enumeration</code> answered is thread safe.
 *
 * @see OverflowingLRUCache
 * @return Enumeration of IBuffer
 */
public Enumeration getOpenBuffers() {
	synchronized (openBuffers) {
		openBuffers.shrink();
		return openBuffers.elements();
	}
}


/**
 * Removes a buffer from the table of open buffers.
 */
protected void removeBuffer(IBuffer buffer) {
	openBuffers.remove(buffer.getOwner());
}
}
