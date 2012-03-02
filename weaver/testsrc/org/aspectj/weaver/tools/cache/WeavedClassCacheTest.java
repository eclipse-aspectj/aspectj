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

import junit.framework.TestCase;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.tools.GeneratedClassHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 */
public class WeavedClassCacheTest extends TestCase {
	String FAKE_CLASS = "com.example.foo.Bar";
	byte[] FAKE_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

	public class MemoryCacheBacking implements CacheBacking {
		HashMap<String, CachedClassEntry> cache = new HashMap<String, CachedClassEntry>();

		public String[] getKeys(String regex) {
			Set<String> keys = cache.keySet();
			List<String> matches = new LinkedList<String>();
			for (String key : keys) {
				if (key.matches(regex)) {
					matches.add(key);
				}
			}
			return matches.toArray(new String[0]);
		}

		public void remove(CachedClassReference ref) {
			cache.remove(ref.getKey());
		}

		public void clear() {
			cache.clear();
		}

		public CachedClassEntry get(CachedClassReference ref) {
			return cache.get(ref.getKey());
		}

		public void put(CachedClassEntry entry) {
			cache.put(entry.getKey(), entry);
		}
	}

	MemoryCacheBacking memoryBacking = new MemoryCacheBacking();

	IMessageHandler messageHandler = new IMessageHandler() {
		public boolean handleMessage(IMessage message) throws AbortException {
			return true;
		}

		public boolean isIgnoring(IMessage.Kind kind) {
			return true;
		}

		public void dontIgnore(IMessage.Kind kind) {
		}

		public void ignore(IMessage.Kind kind) {
		}
	};

	public class TestGeneratedClassHandler implements GeneratedClassHandler {
		public int accepts = 0;
		public List<String> classesISaw = new LinkedList<String>();

		public void acceptClass(String name, byte[] bytes) {
			accepts++;
			classesISaw.add(name);
		}
	}

	TestGeneratedClassHandler generatedClassHandler = new TestGeneratedClassHandler();

	CacheKeyResolver resolver = new DefaultCacheKeyResolver();

	private WeavedClassCache createCache() throws Exception {
		return new WeavedClassCache(generatedClassHandler, messageHandler, "test", memoryBacking, resolver);
	}

	private void reset() throws Exception {
		memoryBacking.cache.clear();
		generatedClassHandler.accepts = 0;
		generatedClassHandler.classesISaw.clear();
	}

	public void testGetCachingClassHandler() throws Exception {
		WeavedClassCache cache = createCache();
		GeneratedClassHandler newHandle = cache.getCachingClassHandler();
		assertTrue(generatedClassHandler != newHandle);
		assertTrue(newHandle instanceof GeneratedCachedClassHandler);
	}


	public void testExistingGeneratedClassesPassedThroughHandler() throws Exception {
		String classA = "com.generated.A";
		String classB = "com.generated.B";
		reset();
		memoryBacking.put(new CachedClassEntry(resolver.generatedKey(classA), FAKE_BYTES, CachedClassEntry.EntryType.GENERATED));
		memoryBacking.put(new CachedClassEntry(resolver.generatedKey(classB), FAKE_BYTES, CachedClassEntry.EntryType.GENERATED));
		createCache();
		assertEquals(2, generatedClassHandler.accepts);
		for (String cName : generatedClassHandler.classesISaw) {
			assertTrue("Got: " + cName, cName.equals(classA) || cName.equals(classB));
		}
	}

	public void testCache() throws Exception {
		reset();
		WeavedClassCache cache = createCache();
		CacheStatistics stats = cache.getStats();
		CachedClassReference ref = cache.createCacheKey(FAKE_CLASS, FAKE_BYTES);
		assertNull(cache.get(ref));
		cache.put(ref, FAKE_BYTES);
		assertNotNull(cache.get(ref));

		assertEquals(new String(FAKE_BYTES), new String(cache.get(ref).getBytes()));

		assertEquals(1, cache.getWeavedClasses().length);
		assertEquals(ref.getKey(), cache.getWeavedClasses()[0].getKey());

		ref = cache.createGeneratedCacheKey(FAKE_CLASS);
		assertNull(cache.get(ref));
		cache.put(ref, FAKE_BYTES);
		assertNotNull(cache.get(ref));
		assertEquals(new String(FAKE_BYTES), new String(cache.get(ref).getBytes()));

		assertEquals(1, cache.getGeneratedClasses().length);
		assertEquals(ref.getKey(), cache.getGeneratedClasses()[0].getKey());

		assertEquals(4, stats.getHits());
		assertEquals(2, stats.getMisses());


	}

	public void testRemove() throws Exception {
		reset();
		WeavedClassCache cache = createCache();
		CachedClassReference ref = cache.createCacheKey(FAKE_CLASS, FAKE_BYTES);
		assertNull(cache.get(ref));
		cache.put(ref, FAKE_BYTES);
		assertNotNull(cache.get(ref));
		cache.remove(ref);
		assertNull(cache.get(ref));
	}

}
