/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Kew (vmware)         initial implementation
 *******************************************************************************/

package org.aspectj.weaver.tools.cache;

/**
 * A typed reference to a cached class entry. The key to any
 * cache entry is a simple string, but that string may contain
 * some specialized encoding. This class handles all of that
 * encoding.
 * <p/>
 * External users of the cache should not be able to create these
 * objects manually.
 */
public class CachedClassReference {
	enum EntryType {
		GENERATED,
		WEAVED,
		IGNORED,
	}

	private final String key;
	private final String className;

	protected CachedClassReference(String key, CacheKeyResolver resolver) {
		this.key = key;
		this.className = resolver.keyToClass(key);
	}

	/**
	 * Protected to allow only the WeavedClassCache initialization rights
	 *
	 * @param key	   encoded key of the class
	 * @param className the classname
	 */
	protected CachedClassReference(String key, String className) {
		this.key = key;
		this.className = className;
	}

	public String getKey() {
		return key;
	}

	public String getClassName() {
		return className;
	}
}
