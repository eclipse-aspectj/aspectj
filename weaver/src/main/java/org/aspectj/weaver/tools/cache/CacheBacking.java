/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Kew (vmware)         	initial implementation
 *   Lyor Goldstein (vmware)	add support for weaved class being re-defined
 *******************************************************************************/
package org.aspectj.weaver.tools.cache;

/**
 * Interface for the backing to the cache; usually a file,
 * but could be an in-memory backing for testing.
 * <p>
 * aspectj and jvmti provide no suitable guarantees
 * on locking for class redefinitions, so every implementation
 * must have a some locking mechanism to prevent invalid reads.
 * </p>
 */
public interface CacheBacking {
	/**
	 * Return a list of keys which match the given
	 * regex.
	 *
	 * @param regex
	 * @return
	 */
	String[] getKeys(String regex);

	/**
	 * Remove an entry from the cache
	 *
	 * @param ref
	 */
	void remove(CachedClassReference ref);

	/**
	 * Clear the entire cache
	 */
	void clear();

	/**
	 * Get a cache entry
	 *
	 * @param ref entry to retrieve
	 * @param originalBytes Pre-weaving class bytes - required in order to
	 * ensure that the cached entry refers to the same original class
	 * @return the cached bytes or null, if the entry does not exist
	 */
	CachedClassEntry get(CachedClassReference ref, byte[] originalBytes);

	/**
	 * Put an entry in the cache
	 *
	 * @param entry key of the entry
	 * @param originalBytes Pre-weaving class bytes - required in order to
	 * ensure that the cached entry refers to the same original class
	 */
	void put(CachedClassEntry entry, byte[] originalBytes);
}
