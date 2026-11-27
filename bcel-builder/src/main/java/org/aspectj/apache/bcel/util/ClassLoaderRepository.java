package org.aspectj.apache.bcel.util;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (https://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <https://www.apache.org/>.
 */

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;

/**
 * The repository maintains information about which classes have been loaded.
 *
 * It loads its data from the ClassLoader implementation passed into its constructor.
 *
 * @see org.aspectj.apache.bcel.Repository
 *
 * @version $Id: ClassLoaderRepository.java,v 1.13 2009/09/09 19:56:20 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @author David Dixon-Peugh
 */
public class ClassLoaderRepository implements Repository {
	private static java.lang.ClassLoader bootClassLoader = null;
	private ClassLoaderReference loaderRef;

	// Choice of cache...
	private WeakHashMap<URL, SoftReference<JavaClass>> localCache = new WeakHashMap<>();
	private static SoftHashMap<URL,JavaClass> sharedCache = new SoftHashMap<>(Collections.synchronizedMap(new HashMap<>()));

	// For fast translation of the classname *intentionally not static*
	private SoftHashMap<String,URL> nameMap = new SoftHashMap<>(new HashMap<>());

	public static boolean useSharedCache =
		System.getProperty("org.aspectj.apache.bcel.useSharedCache", "true").equalsIgnoreCase("true");

	// Cache not found classes as well to prevent unnecessary file I/O operations
	public static boolean useUnavailableClassesCache =
		System.getProperty("org.aspectj.apache.bcel.useUnavailableClassesCache", "false").equalsIgnoreCase("true");
	// Ignore cache clear requests to not build up the cache over and over again
	public static boolean ignoreCacheClearRequests =
		System.getProperty("org.aspectj.apache.bcel.ignoreCacheClearRequests", "false").equalsIgnoreCase("true");

	// Second cache for the unavailable classes
	private static Set<String> unavailableClasses = Collections.synchronizedSet(new HashSet<String>());

	private static int cacheHitsShared = 0;
	private long timeManipulatingURLs = 0L;
	private long timeSpentLoading = 0L;
	private int classesLoadedCount = 0;
	private int misses = 0;
	private int cacheHitsLocal = 0;
	private int unavailableClassesCacheHits = 0;
	private int missLocalEvicted = 0; // Misses in local cache access due to reference GC

	public ClassLoaderRepository(java.lang.ClassLoader loader) {
		this.loaderRef = new DefaultClassLoaderReference((loader != null) ? loader : getBootClassLoader());
	}

	public ClassLoaderRepository(ClassLoaderReference loaderRef) {
		this.loaderRef = loaderRef;
	}

	private static synchronized java.lang.ClassLoader getBootClassLoader() {
		if (bootClassLoader == null) {
			bootClassLoader = new URLClassLoader(new URL[0]);
		}
		return bootClassLoader;
	}



	/**
	 * Store a new JavaClass into this repository as a soft reference and return the reference
	 */
	private void storeClassAsReference(URL url, JavaClass clazz) {
		if (useSharedCache) {
			clazz.setRepository(null); // can't risk setting repository, we'll get in a pickle!
			sharedCache.put(url, clazz);
		} else {
			clazz.setRepository(this);
			localCache.put(url, new SoftReference<>(clazz));
		}
	}

	/**
	 * Store a new JavaClass into this Repository.
	 */
	public void storeClass(JavaClass clazz) {
		storeClassAsReference(toURL(clazz.getClassName()), clazz);
	}

	/**
	 * Remove class from repository
	 */
	public void removeClass(JavaClass clazz) {
		if (useSharedCache)
			sharedCache.remove(toURL(clazz.getClassName()));
		else
			localCache.remove(toURL(clazz.getClassName()));
	}

	/**
	 * Find an already defined JavaClass in the local cache.
	 */
	public JavaClass findClass(String className) {
		if (useSharedCache)
			return findClassShared(toURL(className));
		else
			return findClassLocal(toURL(className));
	}

	private JavaClass findClassLocal(URL url) {
		SoftReference<JavaClass> ref = localCache.get(url);
		if (ref != null) {
			JavaClass javaClass = ref.get();
			if (javaClass != null) {
				return javaClass;
			} else {
				missLocalEvicted++;
			}
		}
		return null;
	}

	/**
	 * Find an already defined JavaClass in the shared cache.
	 */
	private JavaClass findClassShared(URL url) {
		return (JavaClass) sharedCache.get(url);
	}

	private URL toURL(String className) {
		URL url = (URL) nameMap.get(className);
		if (url == null) {
			String classFile = className.replace('.', '/');
			url = loaderRef.getClassLoader().getResource(classFile + ".class");
			nameMap.put(className, url);
		}
		return url;
	}

	/**
	 * Lookup a JavaClass object from the classname provided.
	 */
	public JavaClass loadClass(String className) throws ClassNotFoundException {
		// Quick evaluation of unavailable classes to prevent unnecessary file I/O
		if (useUnavailableClassesCache && unavailableClasses.contains(className)) {
			unavailableClassesCacheHits++;
			throw new ClassNotFoundException(className + " not found.");
		}

		long time = System.currentTimeMillis();
		java.net.URL url = toURL(className);
		timeManipulatingURLs += (System.currentTimeMillis() - time);
		if (url == null) {
			if (useUnavailableClassesCache) {
				unavailableClasses.add(className);
			}
			throw new ClassNotFoundException(className + " not found - unable to determine URL");
		}

		JavaClass clazz = null;
		// Look in the appropriate cache
		if (useSharedCache) {
			clazz = findClassShared(url);
			if (clazz != null) {
				cacheHitsShared++;
				return clazz;
			}
		} else {
			clazz = findClassLocal(url);
			if (clazz != null) {
				cacheHitsLocal++;
				return clazz;
			}
		}

		// Didn't find it in either cache
		misses++;

		try {
			// Load it
			String classFile = className.replace('.', '/');
			InputStream is = (useSharedCache ? url.openStream() : loaderRef.getClassLoader().getResourceAsStream(
					classFile + ".class"));
			if (is == null) {
				if (useUnavailableClassesCache) {
					unavailableClasses.add(className);
				}
				throw new ClassNotFoundException(className + " not found using url " + url);
			}
			ClassParser parser = new ClassParser(is, className);
			clazz = parser.parse();

			// Cache it
			storeClassAsReference(url, clazz);

			timeSpentLoading += (System.currentTimeMillis() - time);
			classesLoadedCount++;
			return clazz;
		} catch (IOException e) {
			if (useUnavailableClassesCache) {
				unavailableClasses.add(className);
			}
			throw new ClassNotFoundException(e.toString());
		}
	}

	/**
	 * Produce a report on cache usage.
	 */
	public String report() {
		StringBuilder sb = new StringBuilder();
		sb.append("BCEL repository report.");
		if (useSharedCache)
			sb.append(" (shared cache)");
		else
			sb.append(" (local cache)");
		sb.append(" Total time spent loading: " + timeSpentLoading + "ms.");
		sb.append(" Time spent manipulating URLs: " + timeManipulatingURLs + "ms.");
		sb.append(" Classes loaded: " + classesLoadedCount + ".");
		if (useSharedCache) {
			sb.append(" Shared cache size: " + sharedCache.size());
			sb.append(" Shared cache (hits/missDueToEviction): (" + cacheHitsShared + "/" + sharedCache.evictedMisses() + ").");
		} else {
			sb.append(" Local cache size: " + localCache.size());
			sb.append(" Local cache (hits/missDueToEviction): (" + cacheHitsLocal + "/" + missLocalEvicted + ").");
		}
		return sb.toString();
	}

	// Positions in the array returned by reportStats()
	public final static int STATS_TIME_SPENT_LOADING = 0;
	public final static int STATS_TIME_MANIPULATING_URLS = 1;
	public final static int STATS_CLASSES_LOADED = 2;
	public final static int STATS_CACHE_HITS_SHARED = 3;
	public final static int STATS_MISS_SHARED_EVICTED = 4;
	public final static int STATS_CACHE_HITS_LOCAL = 5;
	public final static int STATS_MISS_LOCAL_EVICTED = 6;
	public final static int STATS_SHARED_CACHE_SIZE = 7;
	public final static int STATS_UNAVAILABLE_CLASSES_CACHE_HITS = 8;

	/**
	 * Returns an array of the stats, for testing. Use the STATS_ constants for the positions. The cache hits and misses of the
	 * shared cache, and the size of the shared cache, are shared by all repositories, the others are for this repository.
	 */
	public long[] reportStats() {
		long[] stats = new long[STATS_UNAVAILABLE_CLASSES_CACHE_HITS + 1];
		stats[STATS_TIME_SPENT_LOADING] = timeSpentLoading;
		stats[STATS_TIME_MANIPULATING_URLS] = timeManipulatingURLs;
		stats[STATS_CLASSES_LOADED] = classesLoadedCount;
		stats[STATS_CACHE_HITS_SHARED] = cacheHitsShared;
		stats[STATS_MISS_SHARED_EVICTED] = sharedCache.evictedMisses(); // misses in shared cache access due to reference GC
		stats[STATS_CACHE_HITS_LOCAL] = cacheHitsLocal;
		stats[STATS_MISS_LOCAL_EVICTED] = missLocalEvicted;
		stats[STATS_SHARED_CACHE_SIZE] = sharedCache.size();
		stats[STATS_UNAVAILABLE_CLASSES_CACHE_HITS] = unavailableClassesCacheHits;
		return stats;
	}

	/**
	 * Reset statistics and clear all caches
	 */
	public void reset() {
		timeManipulatingURLs = 0L;
		timeSpentLoading = 0L;
		classesLoadedCount = 0;
		cacheHitsLocal = 0;
		cacheHitsShared = 0;
		sharedCache.resetEvictedMisses();
		missLocalEvicted = 0;
		unavailableClassesCacheHits = 0;
		misses = 0;
		clear();
	}

	public JavaClass loadClass(Class<?> clazz) throws ClassNotFoundException {
		return loadClass(clazz.getName());
	}

	/** Clear all entries from the local cache */
	public void clear() {
		if (!ignoreCacheClearRequests) {
			if (useSharedCache) {
				sharedCache.clear();
			} else {
				localCache.clear();
			}
			unavailableClasses.clear();
		}
	}

}
