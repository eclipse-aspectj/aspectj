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
import org.aspectj.util.FileUtil;

import java.io.File;
import java.util.zip.CRC32;

/**
 */
public class DefaultFileCacheBackingTest extends TestCase {
	File root = null;
	byte[] FAKE_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	String FAKE_CLASS = "com.example.foo.Bar";
	CacheKeyResolver resolver = new DefaultCacheKeyResolver();
	CachedClassReference fakeRef = resolver.weavedKey(FAKE_CLASS, FAKE_BYTES);


	public void setUp() throws Exception {
		if (root == null) {
			File tempFile = File.createTempFile("aspectj", "test");
			File tempDir = tempFile.getParentFile();
			root = new File(tempDir, "aspectj-test-cache");
		}
	}

	public void tearDown() throws Exception {
		FileUtil.deleteContents(root);
		root = null;
	}

	public void testCreateBacking() throws Exception {
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root, resolver);
		assertNotNull(backing);
		assertTrue(root.exists());
		assertTrue(root.isDirectory());
	}

	public void testClear() {
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root, resolver);
		backing.put(new CachedClassEntry(fakeRef, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED));
		assertNotNull(backing.get(fakeRef));
		backing.clear();
		assertNull(backing.get(fakeRef));
	}

	private CachedClassEntry createTestEntry(String key) {
		return new CachedClassEntry(new CachedClassReference(key, key), FAKE_BYTES, CachedClassEntry.EntryType.WEAVED);
	}

	public void testGetKeys() throws Exception {
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root, resolver);
		backing.put(createTestEntry("apple"));
		backing.put(createTestEntry("apply"));
		backing.put(createTestEntry("orange"));
		String[] matches = backing.getKeys("app.*");
		assertEquals(2, matches.length);
		matches = backing.getKeys("orange");
		assertEquals(1, matches.length);
		assertEquals("orange", matches[0]);
	}

	public void testPut() throws Exception {
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root, resolver);
		backing.put(new CachedClassEntry(fakeRef, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED));
		File cachedFile = new File(root, fakeRef.getKey());
		assertTrue(cachedFile.exists());
		assertTrue(cachedFile.isFile());
		assertEquals(FAKE_BYTES.length, cachedFile.length());
	}

	private boolean indexEntryExists(String key, long expectedCRC) throws Exception {
		long storedCRC = 0;
		DefaultFileCacheBacking.IndexEntry[] index = DefaultFileCacheBacking.readIndex(new File(root, DefaultFileCacheBacking.INDEX_FILE));
		if (index == null) {
			throw new NullPointerException("No index at " + root.getAbsolutePath());
		}
		for (DefaultFileCacheBacking.IndexEntry ie : index) {
			if (ie.key.equals(key)) {
				storedCRC = ie.crc;
				if (!ie.ignored) {
					assertEquals(expectedCRC, storedCRC);
				}
				return true;
			}
		}
		return false;
	}

	public void testGet() throws Exception {
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root, resolver);
		assertNull(backing.get(fakeRef));
		backing.put(new CachedClassEntry(fakeRef, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED));
		File cachedFile = new File(root, fakeRef.getKey());
		assertTrue(cachedFile.isFile());
		assertEquals(FAKE_BYTES.length, cachedFile.length());
		CRC32 expectedCRC = new CRC32();
		expectedCRC.update(FAKE_BYTES);
		assertTrue(indexEntryExists(fakeRef.getKey(), expectedCRC.getValue()));
		CachedClassEntry entry = backing.get(fakeRef);
		assertEquals(FAKE_BYTES.length, entry.getBytes().length);

	}

	public void testRemove() throws Exception {
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root, resolver);
		backing.put(new CachedClassEntry(fakeRef, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED));
		File cachedFile = new File(root, fakeRef.getKey());
		assertTrue(cachedFile.exists());
		assertTrue(cachedFile.isFile());
		CRC32 expectedCRC = new CRC32();
		expectedCRC.update(FAKE_BYTES);
		assertTrue(indexEntryExists(fakeRef.getKey(), expectedCRC.getValue()));
		backing.remove(fakeRef);
		cachedFile = new File(root, fakeRef.getKey());
		assertFalse("CacheFile Still exists!" + cachedFile.getAbsolutePath(), cachedFile.exists());
		assertFalse(cachedFile.isFile());
		assertFalse(indexEntryExists(fakeRef.getKey(), expectedCRC.getValue()));
	}


	public void testMultiFile() throws Exception {
		CachedClassEntry entry;
		File cachedFile;
		CRC32 expectedCRC = new CRC32();
		expectedCRC.update(FAKE_BYTES);
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root, resolver);
		// add weaved
		CachedClassReference wref = resolver.weavedKey(FAKE_CLASS + "WEAVED", FAKE_BYTES);
		entry = new CachedClassEntry(wref, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED);
		backing.put(entry);
		cachedFile = new File(root, wref.getKey());
		assertTrue(cachedFile.exists());
		assertTrue(cachedFile.isFile());
		assertTrue(indexEntryExists(wref.getKey(), expectedCRC.getValue()));

		// add generated
		CachedClassReference gref = resolver.generatedKey(FAKE_CLASS + "GENERATED");
		entry = new CachedClassEntry(gref, FAKE_BYTES, CachedClassEntry.EntryType.GENERATED);
		backing.put(entry);
		cachedFile = new File(root, gref.getKey());
		assertTrue(cachedFile.exists());
		assertTrue(cachedFile.isFile());
		assertTrue(indexEntryExists(gref.getKey(), expectedCRC.getValue()));

		// add ignored
		CachedClassReference iref = resolver.generatedKey(FAKE_CLASS + "IGNORED");
		entry = new CachedClassEntry(iref, FAKE_BYTES, CachedClassEntry.EntryType.IGNORED);
		backing.put(entry);
		cachedFile = new File(root, iref.getKey());
		assertFalse(cachedFile.exists());
		assertTrue(indexEntryExists(iref.getKey(), expectedCRC.getValue()));

		backing.remove(wref);
		backing.remove(gref);
		backing.remove(iref);
	}

}
