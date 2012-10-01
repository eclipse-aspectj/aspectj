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
 * A typed reference to a cached class entry. The key to any
 * cache entry is a simple string, but that string may contain
 * some specialized encoding. This class handles all of that
 * encoding.
 * <p/>
 * External users of the cache should not be able to create these
 * objects manually.
 */
public class CachedClassReference {
	static enum EntryType {
		GENERATED,
		WEAVED,
		IGNORED,
	}

	private final String key;
	private final String className;

	protected CachedClassReference(String key, CacheKeyResolver resolver) {
		this(key, resolver.keyToClass(key));
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

	@Override
	public int hashCode() {
		return getKey().hashCode() + getClassName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        CachedClassReference	other=(CachedClassReference) obj;
		if (getKey().equals(other.getKey())
		 && getClassName().equals(other.getClassName())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return getClassName() + "[" + getKey() + "]";
	}
}
