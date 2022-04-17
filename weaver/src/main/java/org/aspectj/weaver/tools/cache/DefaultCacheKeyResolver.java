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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Naive default class and classloader hashing implementation useful
 * for some multi-classloader environments.
 * <p>
 * This implementation creates classloader scopes of the form:<br>
 * "ExampleClassLoaderName.[crc hash]"
 * </p>
 * <p>
 * And weaved class keys of the form:<br>
 * "com.foo.BarClassName.[bytes len][crc].weaved"
 * </p>
 * <p>
 * And generated class keys of the form:<br>
 * "com.foo.BarClassName$AjClosure.generated
 * </p>
 */
public class DefaultCacheKeyResolver implements CacheKeyResolver {
	public static final String GENERATED_SUFFIX = ".generated";
	public static final String WEAVED_SUFFIX = ".weaved";

	/**
	 * Create a scope from a set of urls and aspect urls. Creates scope
	 * of the form "ExampleClassLoaderName.[md5sum]" or
	 * "ExampleClassLoaderName.[crc]"
	 *
	 * @param cl	  the classloader which uses the cache, can be null
	 * @param aspects the aspects
	 * @return a unique string for URLClassloaders, otherwise a non-unique classname
	 */
	public String createClassLoaderScope(ClassLoader cl, List<String> aspects) {
		String name = cl != null ? cl.getClass().getSimpleName() : "unknown";

		List<String> hashableStrings = new LinkedList<>();
		StringBuilder hashable = new StringBuilder(256);

		// Add the list of loader urls to the hash list
		if (cl instanceof URLClassLoader) {
			URL[] urls = ((URLClassLoader) cl).getURLs();
			for (URL url : urls) {
				hashableStrings.add(url.toString());
			}
		}

		hashableStrings.addAll(aspects);
		Collections.sort(hashableStrings);
		for (String url : hashableStrings) {
			hashable.append(url);
		}
		String hash = null;
		byte[] bytes = hashable.toString().getBytes();
		hash = crc(bytes);

		return name + '.' + hash;
	}

	private String crc(byte[] input) {
		CRC32 crc32 = new CRC32();
		crc32.update(input);
		return String.valueOf(crc32.getValue());
	}

	public String getGeneratedRegex() {
		return ".*" + GENERATED_SUFFIX;
	}

	public String getWeavedRegex() {
		return ".*" + WEAVED_SUFFIX;
	}


	/**
	 * Converts a cache key back to a className
	 *
	 * @param key to convert
	 * @return className, e.g. "com.foo.Bar"
	 */
	public String keyToClass(String key) {
		if (key.endsWith(GENERATED_SUFFIX)) {
			return key.replaceAll(GENERATED_SUFFIX + "$", "");
		}
		if (key.endsWith(WEAVED_SUFFIX)) {
			return key.replaceAll("\\.[^.]+" + WEAVED_SUFFIX, "");
		}
		return key;
	}

	public CachedClassReference weavedKey(String className, byte[] original_bytes) {
		String hash = crc(original_bytes);
		return new CachedClassReference(className + "." + hash + WEAVED_SUFFIX, className);

	}

	public CachedClassReference generatedKey(String className) {
		return new CachedClassReference(className + GENERATED_SUFFIX, className);
	}

}
