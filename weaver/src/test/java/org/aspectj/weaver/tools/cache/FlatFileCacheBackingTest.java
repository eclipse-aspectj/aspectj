/*******************************************************************************
 * Copyright (c) 2012 VMware, Inc.
 * 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *  Lyor Goldstein
 *******************************************************************************/

package org.aspectj.weaver.tools.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import org.aspectj.weaver.tools.cache.AbstractIndexedFileCacheBacking.IndexEntry;

/**
 * @author Lyor Goldstein
 */
public class FlatFileCacheBackingTest extends AsynchronousFileCacheBackingTestSupport {
	public FlatFileCacheBackingTest() {
		super();
	}

	@Override
	protected FlatFileCacheBacking createFileBacking(File dir) {
		return new FlatFileCacheBacking(dir);
	}

	public void testReadIndex() throws IOException {
		IndexEntry[] entries = { createIgnoredEntry("ignored"), createIndexEntry("weaved", false, false, bytes, bytes),
				createIndexEntry("generated", true, false, bytes, bytes) };
		File indexFile = getIndexFile();
		writeIndex(indexFile, entries);
		Map<String, File> dataFiles = createDataFiles(entries);

		File cacheDir = getCacheDir();
		AsynchronousFileCacheBacking cache = createFileBacking(cacheDir);
		Map<String, IndexEntry> indexMap = cache.getIndexMap();
		assertEquals("Mismatched index size", entries.length, indexMap.size());

		Map<String, byte[]> bytesMap = cache.getBytesMap();
		assertEquals("Mismatched bytes size", dataFiles.size() /* the ignored one has no file */, bytesMap.size());

		for (IndexEntry entry : entries) {
			String key = entry.key;
			assertNotNull("Missing entry for key=" + key, indexMap.get(key));

			if (entry.ignored) {
				assertNull("Unexpected bytes for ignored key=" + key, bytesMap.get(key));
			} else {
				assertArrayEquals("Mismatched contents for key=" + key, bytes, bytesMap.get(key));
			}
		}
	}

	public void testIgnoredBadCrcDataFiles() throws Exception {
		IndexEntry[] entries = { createIndexEntry("weaved-goodData", false, false, bytes, bytes),
				createIndexEntry("badData-weaved", false, false, bytes, bytes),
				createIndexEntry("generated-goodData", true, false, bytes, bytes),
				createIndexEntry("badData-generated", true, false, bytes, bytes) };
		File indexFile = getIndexFile();
		writeIndex(indexFile, entries);

		Map<String, File> dataFiles = createDataFiles(entries);
		long newCrc = generateNewBytes();
		assertTrue("Bad new CRC", newCrc != (-1L));

		Map<String, File> badFiles = new TreeMap<>();
		for (IndexEntry entry : entries) {
			String key = entry.key;
			if (key.startsWith("badData")) {
				File file = dataFiles.get(key);
				OutputStream out = new FileOutputStream(file);
				try {
					out.write(bytes);
				} finally {
					out.close();
				}
				dataFiles.remove(key);
				badFiles.put(key, file);
			}
		}

		File cacheDir = getCacheDir();
		FlatFileCacheBacking cache = createFileBacking(cacheDir);
		Map<String, IndexEntry> indexMap = cache.getIndexMap();
		assertEquals("Mismatched index size", dataFiles.size(), indexMap.size());

		Map<String, byte[]> bytesMap = cache.getBytesMap();
		assertEquals("Mismatched bytes size", dataFiles.size(), bytesMap.size());

		for (Map.Entry<String, File> badEntry : badFiles.entrySet()) {
			String key = badEntry.getKey();
			assertFalse("Unexpectedly indexed: " + key, indexMap.containsKey(key));
			assertFalse("Unexpectedly loaded: " + key, bytesMap.containsKey(key));

			File file = badEntry.getValue();
			assertFalse("Unexpectedly still readable: " + key, file.canRead());
		}
	}

	public void testSkipMissingDataFileOnReadIndex() throws IOException {
		IndexEntry[] entries = { createIndexEntry("weaved-noData", false, false, null, null),
				createIndexEntry("withData-weaved", false, false, bytes, bytes),
				createIndexEntry("generated-noData", true, false, null, null),
				createIndexEntry("withData-generated", true, false, bytes, bytes) };
		File indexFile = getIndexFile();
		writeIndex(indexFile, entries);

		Map<String, File> dataFiles = new TreeMap<>();
		for (IndexEntry entry : entries) {
			String key = entry.key;
			if (key.startsWith("withData")) {
				dataFiles.put(key, createDataFile(entry, bytes));
			}
		}

		File cacheDir = getCacheDir();
		FlatFileCacheBacking cache = createFileBacking(cacheDir);
		Map<String, IndexEntry> indexMap = cache.getIndexMap();
		assertEquals("Mismatched index size", dataFiles.size(), indexMap.size());

		Map<String, byte[]> bytesMap = cache.getBytesMap();
		assertEquals("Mismatched bytes size", dataFiles.size(), bytesMap.size());

		for (IndexEntry entry : entries) {
			String key = entry.key;
			if (key.startsWith("withData")) {
				assertTrue("Not indexed: " + key, indexMap.containsKey(key));
				assertTrue("Not loaded: " + key, bytesMap.containsKey(key));
			} else {
				assertFalse("Unexpectedly indexed: " + key, indexMap.containsKey(key));
				assertFalse("Unexpectedly loaded: " + key, bytesMap.containsKey(key));
			}
		}
	}

}
