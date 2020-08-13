/**
 * Copyright (c) 2012 VMware, Inc.
 *
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 * Lyor Goldstein
 */

package org.aspectj.weaver.tools.cache;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.aspectj.util.FileUtil;
import org.aspectj.weaver.tools.cache.AbstractIndexedFileCacheBacking.IndexEntry;

/**
 * 
 */
public class ZippedFileCacheBackingTest extends AsynchronousFileCacheBackingTestSupport {
	private File	zipTestFile;

    public ZippedFileCacheBackingTest() {
        super();
    }

    public void testReadIndex () throws Exception {
        IndexEntry[]    entries={
                createIgnoredEntry("ignored"),
                createIndexEntry("weaved", false, false, bytes, bytes),
                createIndexEntry("generated", true, false, bytes, bytes)
            };
        File	indexFile=getIndexFile();
        writeIndex(indexFile, entries);

        Map<String,byte[]>  entriesMap= new TreeMap<>();
        for (IndexEntry ie : entries) {
            if (ie.ignored) {
                continue;
            }

            entriesMap.put(ie.key, bytes);
        }

        File	zipFile=getZipFile();
        ZippedFileCacheBacking.writeZipClassBytes(zipFile, entriesMap);

        File							cacheDir=getCacheDir();
        AsynchronousFileCacheBacking    cache=createFileBacking(cacheDir);
        Map<String, IndexEntry>			indexMap=cache.getIndexMap();
        assertEquals("Mismatched index size", entries.length, indexMap.size());

        Map<String, byte[]> bytesMap=cache.getBytesMap(); 
        assertEquals("Mismatched bytes size", entriesMap.size() /* the ignored one has no file */, bytesMap.size());
        
        for (IndexEntry entry : entries) {
            String  key=entry.key;
            assertNotNull("Missing entry for key=" + key, indexMap.get(key));
            
            if (entry.ignored) {
                assertNull("Unexpected bytes for ignored key=" + key, bytesMap.get(key));
            } else {
                assertArrayEquals("Mismatched contents for key=" + key, bytes, bytesMap.get(key));
            }
        }
    }

    public void testReadWriteZipClassBytes () throws IOException {
        Map<String,byte[]>  entriesMap= new TreeMap<>();
        for (int    index=0; index < Byte.SIZE; index++) {
            String  name="classBytes#" + index;
            random.nextBytes(bytes);
            entriesMap.put(name, bytes);
        }

        File	zipFile=getZipFile();
        ZippedFileCacheBacking.writeZipClassBytes(zipFile, entriesMap);

        Map<String, byte[]> bytesMap=ZippedFileCacheBacking.readZipClassBytes(zipFile);
        assertEquals("Mismatched recovered entries size", entriesMap.size(), bytesMap.size());
        for (Map.Entry<String,byte[]> bytesEntry : entriesMap.entrySet()) {
            String  key=bytesEntry.getKey();
            byte[]  expected=bytesEntry.getValue(), actual=bytesMap.get(key);
            assertArrayEquals("Mismatched data for " + key, expected, actual);
        }
    }

    public void testReadClassBytes () throws IOException {
        IndexEntry[]    entries={
                createIgnoredEntry("ignoredReadClassBytes"),
                createIndexEntry("weavedReadClassBytes", false, false, bytes, bytes),
                createIndexEntry("generatedReadClassBytes", true, false, bytes, bytes)
            };
        File	indexFile=getIndexFile();
        writeIndex(indexFile, entries);

        long newCrc=generateNewBytes();
        assertTrue("Bad new CRC", newCrc != (-1L));

        Map<String,byte[]>  entriesMap= new TreeMap<>();
        for (IndexEntry ie : entries) {
            if (ie.ignored) {
                continue;
            }

            entriesMap.put(ie.key, bytes);
        }

        File	zipFile=getZipFile();
        ZippedFileCacheBacking.writeZipClassBytes(zipFile, entriesMap);

        File							cacheDir=getCacheDir();
        AsynchronousFileCacheBacking    cache=createFileBacking(cacheDir);
        Map<String, IndexEntry> 		indexMap=cache.getIndexMap();
        assertEquals("Mismatched index size", 1 /* only the ignored entry */, indexMap.size());

        Map<String, byte[]> bytesMap=cache.getBytesMap(); 
        assertEquals("Non empty data bytes", 0, bytesMap.size());
        assertFalse("Zip file not deleted: " + zipFile, zipFile.canRead());
    }

    protected File getZipFile () {
    	if (zipTestFile == null) {
    		File	cacheDir=getCacheDir();
    		zipTestFile = new File(cacheDir, ZippedFileCacheBacking.ZIP_FILE);
    	}

    	return zipTestFile;
    }

    @Override
	protected void cleanupCache() {
		if (zipTestFile != null) {
			if (FileUtil.deleteContents(zipTestFile) > 0) {
				System.out.println("Deleted " + zipTestFile);
			}
			zipTestFile = null;
		}

		super.cleanupCache();
	}

    @Override
    protected ZippedFileCacheBacking createFileBacking(File dir) {
        return new ZippedFileCacheBacking(dir);
    }
}