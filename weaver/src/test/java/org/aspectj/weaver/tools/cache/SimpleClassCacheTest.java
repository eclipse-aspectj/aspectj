/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Abraham Nevado (lucierna) initial implementation
 ********************************************************************************/

package org.aspectj.weaver.tools.cache;

import junit.framework.TestCase;

import java.io.File;

public class SimpleClassCacheTest extends TestCase {
	byte[] FAKE_BYTES_V1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	byte[] FAKE_BYTES_V2 = { 1, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	byte[] FAKE_WOVEN_BYTES_V1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	byte[] FAKE_WOVEN_BYTES_V2 = { 1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	private SimpleCache createCache() {
		return new SimpleCache(System.getProperty("java.io.tmpdir"), true);
	}

	public void testCache() {
		String classA = "com.generated.A";
		SimpleCache cache = createCache();
		cache.put(classA, FAKE_BYTES_V1, FAKE_WOVEN_BYTES_V1);

		// Returned woven bytes are the original ones
		byte[] result = cache.getAndInitialize(classA, FAKE_BYTES_V1, null, null).orElse(null);
		assertNotNull(result);
		for (int i = 0; i < result.length; i++)
			assertEquals(
				"Cached version byte[" + i + "] should be equal to the original woven class",
				result[i], FAKE_WOVEN_BYTES_V1[i]
			);

		// Class is properly backed up
		File f = new File(System.getProperty("java.io.tmpdir") + File.separator + "com.generated.A-1164760902");
		assertTrue(
			"Class should be backed up with CRC 1164760902",
			f.exists()
		);
	}

	public void testDifferentVersionCache() {
		String classA = "com.generated.A";
		SimpleCache cache = createCache();
		cache.put(classA, FAKE_BYTES_V1, FAKE_WOVEN_BYTES_V1);
		cache.put(classA, FAKE_BYTES_V2, FAKE_WOVEN_BYTES_V2);

		// Returned woven bytes are the original ones for v1
		byte[] result = cache.getAndInitialize(classA, FAKE_BYTES_V1, null, null).orElse(null);
		assertNotNull(result);
		for (int i = 0; i < result.length; i++)
			assertEquals(
				"Cached version v1 byte[" + i + "] should be equal to the original woven class",
				result[i], FAKE_WOVEN_BYTES_V1[i]
			);

		// Returned woven bytes are the original ones for v2
		result = cache.getAndInitialize(classA, FAKE_BYTES_V2, null, null).orElse(null);
		assertNotNull(result);
		for (int i = 0; i < result.length; i++)
			assertEquals(
				"Cached version v2 byte[" + i + "] should be equal to the original woven class",
				result[i], FAKE_WOVEN_BYTES_V2[i]
			);
	}

	public void testCacheMiss() {
		String classA = "com.generated.A";
		SimpleCache cache = createCache();

		// Woven bytes not found in cache
		assertNull(cache.getAndInitialize(classA, FAKE_BYTES_V1, null, null));
	}

	public void testCacheHitUnwoven() {
		String classA = "com.generated.A";
		SimpleCache cache = createCache();
		cache.put(classA, FAKE_BYTES_V1, SimpleCache.SAME_BYTES);

		// Returned woven bytes are null, indicating an unwoven class
		byte[] result = cache.getAndInitialize(classA, FAKE_BYTES_V1, null, null).orElse(null);
		assertNull(result);
	}
}
