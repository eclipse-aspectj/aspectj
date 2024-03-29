/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   John Kew (vmware)         initial implementation
 *******************************************************************************/

package org.aspectj.weaver.tools.cache;

import java.util.List;

/**
 * Interface to allow alternate hashing schemes for weaved and
 * generated classes. While the DefaultCacheKeyResolver may be
 * a reasonable naive implementation, the management and invalidation
 * of the cache may be more usefully accomplished at the Application
 * or Container level.
 * <p>
 * The key is not a one-way hash; it must be convertible back to a
 * className and must match the regex for the type of key it is
 * (generated or weaved).
 * </p>
 */
public interface CacheKeyResolver {
	/**
	 * Create a key for the given className from a class generated by
	 * the weaver such that:
	 * <pre>
	 *    className == keyToClass(generatedKey(className)) holds
	 * and
	 *    generatedKey(className).matches(getGeneratedRegex()) == true
	 * </pre>
	 *
	 * @param className class to create a key for
	 * @return key for the class, or null if no caching should be performed
	 */
	CachedClassReference generatedKey(String className);

	/**
	 * Create a key for the given class name and byte array from the pre-weaved
	 * class such that
	 * <pre>
	 *    className == keyToClass(weavedKey(className, various_bytes)) holds
	 * and
	 *    weavedKey(className, various_bytes).matches(getWeavedRegex()) == true
	 * </pre>
	 *
	 * @param className	  class to create a key for
	 * @param original_bytes bytes of the pre-weaved class
	 * @return key for the class, or null if no caching should be performed
	 */
	CachedClassReference weavedKey(String className, byte[] original_bytes);

	/**
	 * Convert a key back to a className
	 *
	 * @param key cache key
	 * @return className
	 */
	String keyToClass(String key);

	/**
	 * Create a unique string for the given classpath and aspect list
	 *
	 * @param loader  Classloader for this adapter
	 * @param aspects list of aspects; either urls or class names handled by this adapter
	 * @return scope, or null, if no caching should be performed for this classloader
	 */
	String createClassLoaderScope(ClassLoader loader, List<String> aspects);

	/**
	 * Return a regex which matches all generated keys
	 *
	 * @return string regex
	 */
	String getGeneratedRegex();

	/**
	 * Return a regex which matches all weaved keys;
	 *
	 * @return string regex
	 */
	String getWeavedRegex();
}
