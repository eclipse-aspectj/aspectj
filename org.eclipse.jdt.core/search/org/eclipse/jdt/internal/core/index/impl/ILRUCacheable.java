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
package org.eclipse.jdt.internal.core.index.impl;

/**
 * Types implementing this interface can occupy a variable amount of space
 * in an LRUCache.  Cached items that do not implement this interface are
 * considered to occupy one unit of space.
 *
 * @see LRUCache
 */
public interface ILRUCacheable {
	/**
	 * Returns the space the receiver consumes in an LRU Cache.  The default space
	 * value is 1.
	 *
	 * @return int Amount of cache space taken by the receiver
	 */
	public int getCacheFootprint();
}
