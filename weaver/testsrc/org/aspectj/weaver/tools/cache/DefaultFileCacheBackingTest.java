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
 *   Lyor Goldstein (vmware)	add support for weaved class being re-defined
 *******************************************************************************/

package org.aspectj.weaver.tools.cache;

import java.io.File;
import java.util.zip.CRC32;

import org.aspectj.util.LangUtil;
import org.aspectj.weaver.tools.cache.AbstractIndexedFileCacheBacking.IndexEntry;

/**
 */
public class DefaultFileCacheBackingTest extends AbstractCacheBackingTestSupport {
	private final byte[] FAKE_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	private final String FAKE_CLASS = "com.example.foo.Bar";
	private final CacheKeyResolver resolver = new DefaultCacheKeyResolver();
	private final CachedClassReference fakeRef = resolver.weavedKey(FAKE_CLASS, FAKE_BYTES);
	private final String	fakeKey=fakeRef.getKey();

	public DefaultFileCacheBackingTest () {
		super();
	}

	public void testCreateBacking() throws Exception {
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root);
		assertNotNull(backing);
		assertTrue("Root folder not created: " + root, root.exists());
		assertTrue("Root folder not a directory: " + root, root.isDirectory());
	}

	public void testClear() {
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root);
		backing.put(new CachedClassEntry(fakeRef, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED), FAKE_BYTES);
		assertNotNull(backing.get(fakeRef, FAKE_BYTES));
		backing.clear();
		assertNull(backing.get(fakeRef, FAKE_BYTES));
	}

	private CachedClassEntry createTestEntry(String key) {
		return new CachedClassEntry(new CachedClassReference(key, key), FAKE_BYTES, CachedClassEntry.EntryType.WEAVED);
	}

	public void testGetKeys() throws Exception {
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root);
		backing.put(createTestEntry("apple"), FAKE_BYTES);
		backing.put(createTestEntry("apply"), FAKE_BYTES);
		backing.put(createTestEntry("orange"), FAKE_BYTES);
		String[] matches = backing.getKeys("app.*");
		assertEquals(2, matches.length);
		matches = backing.getKeys("orange");
		assertEquals(1, matches.length);
		assertEquals("orange", matches[0]);
	}

	public void testPut() throws Exception {
		CacheBacking backing = DefaultFileCacheBacking.createBacking(root);
		backing.put(new CachedClassEntry(fakeRef, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED), FAKE_BYTES);
		File cachedFile = new File(root, fakeKey);
		assertTrue(cachedFile.exists());
		assertTrue(cachedFile.isFile());
		assertEquals(FAKE_BYTES.length, cachedFile.length());
	}

	public void testGet() throws Exception {
		DefaultFileCacheBacking backing = DefaultFileCacheBacking.createBacking(root);
		assertNull(backing.get(fakeRef, FAKE_BYTES));
		backing.put(new CachedClassEntry(fakeRef, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED), FAKE_BYTES);
		File cachedFile = new File(root, fakeKey);
		assertTrue(cachedFile.isFile());
		assertEquals(FAKE_BYTES.length, cachedFile.length());
		CRC32 expectedCRC = new CRC32();
		expectedCRC.update(FAKE_BYTES);
		assertTrue(indexEntryExists(backing, fakeKey, expectedCRC.getValue()));
		CachedClassEntry entry = backing.get(fakeRef, FAKE_BYTES);
		assertEquals(FAKE_BYTES.length, entry.getBytes().length);
	}

	public void testRemove() throws Exception {
		DefaultFileCacheBacking backing = DefaultFileCacheBacking.createBacking(root);
		backing.put(new CachedClassEntry(fakeRef, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED), FAKE_BYTES);
		File cachedFile = new File(root, fakeKey);
		assertTrue("Cached file not found: " + cachedFile, cachedFile.exists());
		assertTrue("Cached file not a file: " + cachedFile, cachedFile.isFile());
		CRC32 expectedCRC = new CRC32();
		expectedCRC.update(FAKE_BYTES);
		assertTrue("Cached entry index not found", indexEntryExists(backing, fakeKey, expectedCRC.getValue()));
		backing.remove(fakeRef);

		assertFalse("CacheFile Still exists: " + cachedFile, cachedFile.exists());
		assertFalse("Cached file is a file: " + cachedFile, cachedFile.isFile());
		assertFalse("Cached entry index not removed", indexEntryExists(backing, fakeKey, expectedCRC.getValue()));
	}

	public void testMultiFile() throws Exception {
		CachedClassEntry entry;
		File cachedFile;
		CRC32 expectedCRC = new CRC32();
		expectedCRC.update(FAKE_BYTES);
		DefaultFileCacheBacking backing = DefaultFileCacheBacking.createBacking(root);
		// add weaved
		CachedClassReference wref = resolver.weavedKey(FAKE_CLASS + "WEAVED", FAKE_BYTES);
		entry = new CachedClassEntry(wref, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED);
		backing.put(entry, FAKE_BYTES);
		cachedFile = new File(root, wref.getKey());
		assertTrue(cachedFile.exists());
		assertTrue(cachedFile.isFile());
		assertTrue(indexEntryExists(backing, wref.getKey(), expectedCRC.getValue()));

		// add generated
		CachedClassReference gref = resolver.generatedKey(FAKE_CLASS + "GENERATED");
		entry = new CachedClassEntry(gref, FAKE_BYTES, CachedClassEntry.EntryType.GENERATED);
		backing.put(entry, FAKE_BYTES);
		cachedFile = new File(root, gref.getKey());
		assertTrue(cachedFile.exists());
		assertTrue(cachedFile.isFile());
		assertTrue(indexEntryExists(backing, gref.getKey(), expectedCRC.getValue()));

		// add ignored
		CachedClassReference iref = resolver.generatedKey(FAKE_CLASS + "IGNORED");
		entry = new CachedClassEntry(iref, FAKE_BYTES, CachedClassEntry.EntryType.IGNORED);
		backing.put(entry, FAKE_BYTES);
		cachedFile = new File(root, iref.getKey());
		assertFalse(cachedFile.exists());
		assertTrue(indexEntryExists(backing, iref.getKey(), expectedCRC.getValue()));

		backing.remove(wref);
		backing.remove(gref);
		backing.remove(iref);
	}

	public void testOriginalClassBytesChanged () {
		DefaultFileCacheBacking backing = DefaultFileCacheBacking.createBacking(root);
		backing.put(new CachedClassEntry(fakeRef, FAKE_BYTES, CachedClassEntry.EntryType.WEAVED), FAKE_BYTES);
		
		CachedClassEntry entry = backing.get(fakeRef, FAKE_BYTES);
		assertNotNull("No initial entry", entry);

		byte[]	newBytes=new byte[FAKE_BYTES.length];
		for (int index=0; index < FAKE_BYTES.length; index++) {
			newBytes[index] = (byte) (0 - FAKE_BYTES[index]);
		}

		entry = backing.get(fakeRef, newBytes);
		assertNull("Unexpected modified bytes entry: " + entry, entry);
		
		File cachedFile = new File(root, fakeKey);
		assertFalse("Cache file not removed", cachedFile.exists());
	}

	private boolean indexEntryExists(AbstractIndexedFileCacheBacking cache, String key, long expectedCRC) throws Exception {
		long storedCRC = 0L;
		IndexEntry[] index = cache.readIndex(new File(root, AbstractIndexedFileCacheBacking.INDEX_FILE));
		if (LangUtil.isEmpty(index)) {
			return false;
		}

		for (IndexEntry ie : index) {
			if (ie.key.equals(key)) {
				storedCRC = ie.crcWeaved;
				if (!ie.ignored) {
					assertEquals(expectedCRC, storedCRC);
				}
				return true;
			}
		}
		return false;
	}
}
