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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

/**
 */
public class DefaultCacheKeyResolverTest extends TestCase {
	byte[] FAKE_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	String FAKE_CLASS = "com.example.foo.Bar";

	DefaultCacheKeyResolver resolver = new DefaultCacheKeyResolver();

	class BasicTestCL extends ClassLoader {
		public BasicTestCL () {
			super();
		}
	}

	class URLTestCL extends URLClassLoader {
		public URLTestCL(URL... urls) {
			super(urls);
		}
	}


	public void testNonURLClassLoaderScope() throws Exception {
		String scope = resolver.createClassLoaderScope(new BasicTestCL(), Collections.<String>emptyList());
		assertTrue(scope.startsWith(BasicTestCL.class.getSimpleName()));
	}

	public void testCreateURLClassLoaderScope() throws Exception {
		URL testURLA = new URL("http://example.com");
		URL testURLB = new URL("file:///tmp");
		URL testURLC = new URL("ftp://ftp.example.com");
		URLTestCL A = new URLTestCL(testURLA);
		URLTestCL AB = new URLTestCL(testURLA, testURLB);
		URLTestCL BC = new URLTestCL(testURLB, testURLC);
		URLTestCL BC2 = new URLTestCL(testURLC, testURLB);
		String[] a = {"one", "two", "three", "four"};
		String[] a2 = {"one", "two", "three"};
		String scopeAa = resolver.createClassLoaderScope(A, Arrays.asList(a));
		String scopeABa = resolver.createClassLoaderScope(AB, Arrays.asList(a));
		String scopeBCa = resolver.createClassLoaderScope(BC, Arrays.asList(a));
		String scopeBC2a = resolver.createClassLoaderScope(BC2, Arrays.asList(a));
		String scopeAa2 = resolver.createClassLoaderScope(A, Arrays.asList(a2));
		String scopeABa2 = resolver.createClassLoaderScope(AB, Arrays.asList(a2));
		String scopeBCa2 = resolver.createClassLoaderScope(BC, Arrays.asList(a2));
		String scopeBC2a2 = resolver.createClassLoaderScope(BC2, Arrays.asList(a2));

		assertFalse(scopeAa.equals(scopeABa));
		assertFalse(scopeAa.equals(scopeBCa));
		assertFalse(scopeABa.equals(scopeBCa));
		assertTrue(scopeBC2a.equals(scopeBCa));
		assertFalse(scopeAa.equals(scopeAa2));
		assertFalse(scopeABa.equals(scopeABa2));
		assertFalse(scopeBCa.equals(scopeBCa2));
		assertFalse(scopeBC2a.equals(scopeBC2a2));


	}


	public void testCreateGeneratedCacheKey() throws Exception {
		CachedClassReference ref = resolver.generatedKey(FAKE_CLASS);
		assertTrue(ref.getKey().startsWith(FAKE_CLASS));
		assertTrue(ref.getKey().matches(resolver.getGeneratedRegex()));
		assertEquals(FAKE_CLASS, resolver.keyToClass(ref.getKey()));
	}

	public void testCreateCacheKey() throws Exception {
		// crc hashing
		CachedClassReference ref = resolver.weavedKey(FAKE_CLASS, FAKE_BYTES);
		assertTrue("key " + ref.getKey() + " does not match " + resolver.getWeavedRegex(), ref.getKey().matches(resolver.getWeavedRegex()));
		String className = resolver.keyToClass(ref.getKey());
		assertEquals("class " + FAKE_CLASS + " != " + className, FAKE_CLASS, className);
	}

}
