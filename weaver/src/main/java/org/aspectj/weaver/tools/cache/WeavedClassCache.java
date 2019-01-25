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

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.tools.GeneratedClassHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Manages a cache of weaved and generated classes similar to Eclipse Equinox,
 * except designed to operate across multiple restarts of the JVM and with one
 * cache per classloader; allowing URLClassLoaders with the same set of URI
 * paths to share the same cache (with the default configuration).
 * <p/>
 * To enable the default configuration two system properties must be set:
 * <pre>
 *    "-Daj.weaving.cache.enabled=true"
 *    "-Daj.weaving.cache.dir=/some/directory"
 * </pre>
 * <p/>
 * The class cache is often something that application developers or
 * containers would like to manage, so there are a few interfaces for overriding the
 * default behavior and performing other management functions.
 * <p/>
 * {@link CacheBacking} <br/>
 * Provides an interface for implementing a custom backing store
 * for the cache; The default implementation in {@link DefaultFileCacheBacking}
 * provides a naive file-backed cache. An alternate implementation may ignore
 * caching until signaled explicitly by the application, or only cache files
 * for a specific duration. This class delegates the locking and synchronization
 * requirements to the CacheBacking implementation.
 * <p/>
 * {@link CacheKeyResolver} <br/>
 * Provides methods for creating keys from classes to be cached and for
 * creating the "scope" of the cache itself for a given classloader and aspect
 * list. The default implementation is provided by {@link DefaultCacheKeyResolver}
 * but an alternate implementation may want to associate a cache with a particular
 * application running underneath a container.
 * <p/>
 * This naive cache does not normally invalidate *any* classes; the interfaces above
 * must be used to implement more intelligent behavior. Cache invalidation
 * problems may occur in at least three scenarios:
 * <pre>
 *    1. New aspects are added dynamically somewhere in the classloader hierarchy; affecting
 *       other classes elsewhere.
 *    2. Use of declare parent in aspects to change the type hierarchy; if the cache
 *       has not invalidated the right classes in the type hierarchy aspectj may not
 *       be reconstruct the class incorrectly.
 *    3. Similarly to (2), the addition of fields or methods on classes which have
 *       already been weaved and cached could have inter-type conflicts.
 * </pre>
 */
public class WeavedClassCache {
	public static final String WEAVED_CLASS_CACHE_ENABLED = "aj.weaving.cache.enabled";
	public static final String CACHE_IMPL = SimpleCacheFactory.CACHE_IMPL;
	private static CacheFactory DEFAULT_FACTORY = new DefaultCacheFactory();
	public static final byte[] ZERO_BYTES = new byte[0];
	private final IMessageHandler messageHandler;
	private final GeneratedCachedClassHandler cachingClassHandler;
	private final CacheBacking backing;
	private final CacheStatistics stats;
	private final CacheKeyResolver resolver;
	private final String name;

	private static final List<WeavedClassCache> cacheRegistry = new LinkedList<WeavedClassCache>();

	protected WeavedClassCache(GeneratedClassHandler existingClassHandler,
							   IMessageHandler messageHandler,
							   String name,
							   CacheBacking backing,
							   CacheKeyResolver resolver) {
		this.resolver = resolver;
		this.name = name;
		this.backing = backing;
		this.messageHandler = messageHandler;
		// wrap the existing class handler with a caching version
		cachingClassHandler = new GeneratedCachedClassHandler(this, existingClassHandler);
		this.stats = new CacheStatistics();
		synchronized (cacheRegistry) {
			cacheRegistry.add(this);
		}
	}

	/**
	 * Creates a new cache using the resolver and backing returned by the DefaultCacheFactory.
	 *
	 * @param loader			   classloader for this cache
	 * @param aspects			  list of aspects used by the WeavingAdapter
	 * @param existingClassHandler the existing GeneratedClassHandler used by the weaver
	 * @param messageHandler	   the existing messageHandler used by the weaver
	 * @return
	 */
	public static WeavedClassCache createCache(ClassLoader loader, List<String> aspects, GeneratedClassHandler existingClassHandler, IMessageHandler messageHandler) {
		CacheKeyResolver resolver = DEFAULT_FACTORY.createResolver();
		String name = resolver.createClassLoaderScope(loader, aspects);
		if (name == null) {
			return null;
		}
		CacheBacking backing = DEFAULT_FACTORY.createBacking(name);
		if (backing != null) {
			return new WeavedClassCache(existingClassHandler, messageHandler, name, backing, resolver);
		}
		return null;
	}

	public String getName() {
		return name;
	}

	/**
	 * The Cache and be extended in two ways, through a specialized CacheKeyResolver and
	 * a specialized CacheBacking. The default factory used to create these classes can
	 * be set with this method. Since each weaver will create a cache, this method must be
	 * called before the weaver is first initialized.
	 *
	 * @param factory
	 */
	public static void setDefaultCacheFactory(CacheFactory factory) {
		DEFAULT_FACTORY = factory;
	}

	/**
	 * Created a key for a generated class
	 *
	 * @param className ClassName, e.g. "com.foo.Bar"
	 * @return the cache key, or null if no caching should be performed
	 */
	public CachedClassReference createGeneratedCacheKey(String className) {
		return resolver.generatedKey(className);
	}

	/**
	 * Create a key for a normal weaved class
	 *
	 * @param className	 ClassName, e.g. "com.foo.Bar"
	 * @param originalBytes Original byte array of the class
	 * @return a cache key, or null if no caching should be performed
	 */
	public CachedClassReference createCacheKey(String className, byte[] originalBytes) {
		return resolver.weavedKey(className, originalBytes);
	}

	/**
	 * Returns a generated class handler which wraps the handler this cache was initialized
	 * with; this handler should be used to make sure that generated classes are added
	 * to the cache
	 */
	public GeneratedClassHandler getCachingClassHandler() {
		return cachingClassHandler;
	}

	/**
	 * Has caching been enabled through the System property,
	 * WEAVED_CLASS_CACHE_ENABLED
	 *
	 * @return true if caching is enabled
	 */
	public static boolean isEnabled() {
		String enabled = System.getProperty(WEAVED_CLASS_CACHE_ENABLED);
		String impl = System.getProperty(CACHE_IMPL);
		return (enabled != null && (impl == null || !SimpleCache.IMPL_NAME.equalsIgnoreCase(impl) ) );
	}

	/**
	 * Put a class in the cache
	 *
	 * @param ref		 reference to the entry, as created through createCacheKey
	 * @param classBytes pre-weaving class bytes
	 * @param weavedBytes bytes to cache
	 */
	public void put(CachedClassReference ref, byte[] classBytes, byte[] weavedBytes) {
		CachedClassEntry.EntryType type = CachedClassEntry.EntryType.WEAVED;
		if (ref.getKey().matches(resolver.getGeneratedRegex())) {
			type = CachedClassEntry.EntryType.GENERATED;
		}
		backing.put(new CachedClassEntry(ref, weavedBytes, type), classBytes);
		stats.put();
	}

	/**
	 * Get a cache value
	 *
	 * @param ref reference to the cache entry, created through createCacheKey
	 * @param classBytes Pre-weaving class bytes - required to ensure that
	 * cached aspects refer to an unchanged original class
	 * @return the CacheEntry, or null if no entry exists in the cache
	 */
	public CachedClassEntry get(CachedClassReference ref, byte[] classBytes) {
		CachedClassEntry entry = backing.get(ref, classBytes);
		if (entry == null) {
			stats.miss();
		} else {
			stats.hit();
			if (entry.isGenerated()) stats.generated();
			if (entry.isWeaved()) stats.weaved();
			if (entry.isIgnored()) stats.ignored();
		}
		return entry;
	}

	/**
	 * Put a cache entry to indicate that the class should not be
	 * weaved; the original bytes of the class should be used.
	 *
	 * @param ref The cache reference
	 * @param classBytes The un-weaved class bytes
	 */
	public void ignore(CachedClassReference ref, byte[] classBytes) {
		stats.putIgnored();
		backing.put(new CachedClassEntry(ref, ZERO_BYTES, CachedClassEntry.EntryType.IGNORED), classBytes);
	}

	/**
	 * Invalidate a cache entry
	 *
	 * @param ref
	 */
	public void remove(CachedClassReference ref) {
		backing.remove(ref);
	}

	/**
	 * Clear the entire cache
	 */
	public void clear() {
		backing.clear();
	}

	/**
	 * Get the statistics associated with this cache, or
	 * null if statistics have not been enabled.
	 *
	 * @return
	 */
	public CacheStatistics getStats() {
		return stats;
	}

	/**
	 * Return a list of all WeavedClassCaches which have been initialized
	 *
	 * @return
	 */
	public static List<WeavedClassCache> getCaches() {
		synchronized (cacheRegistry) {
			return new LinkedList<WeavedClassCache>(cacheRegistry);
		}
	}

	protected void error(String message, Throwable th) {
		messageHandler.handleMessage(new Message(message, IMessage.ERROR, th, null));
	}

	protected void error(String message) {
		MessageUtil.error(messageHandler, message);
	}

	protected void info(String message) {
		MessageUtil.info(message);
	}

}
