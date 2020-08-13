/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Lyor Goldstein (vmware)	add support for weaved class being re-defined
 *******************************************************************************/
package org.aspectj.weaver.tools.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.tools.cache.AbstractIndexedFileCacheBacking.IndexEntry;

/**
 */
public abstract class AsynchronousFileCacheBackingTestSupport
		extends AbstractCacheBackingTestSupport {
    private File  cacheDir, indexFile;
    protected final byte[]    bytes=new byte[Byte.MAX_VALUE];
    protected final Random    random=new Random(System.nanoTime());

	protected AsynchronousFileCacheBackingTestSupport() {
		super();
	}

	protected AsynchronousFileCacheBackingTestSupport(String name) {
		super(name);
	}

    @Override
    public void setUp () throws Exception {
    	super.setUp();
    	cleanupCache();
    	
        random.nextBytes(bytes);
    }

    @Override
    public void tearDown () throws Exception {
    	cleanupCache();
    	super.tearDown();
    }
   
    protected void cleanupCache() {
    	if (indexFile != null) {
    		if (FileUtil.deleteContents(indexFile) > 0) {
    			System.out.println("Deleted " + indexFile);
    		}
    		indexFile = null;
    	}

    	if (cacheDir != null) {
    		if (FileUtil.deleteContents(cacheDir) > 0) {
    			System.out.println("Deleted " + cacheDir);
    		}
    		cacheDir = null;
    	}
    }

    protected File getIndexFile () {
    	if (indexFile == null) {
    		File	parent=getCacheDir();
    		indexFile=new File(parent, AbstractIndexedFileCacheBacking.INDEX_FILE);
    	}

    	return indexFile;
    }

    protected File getCacheDir () {
    	if (cacheDir == null) {
    		File	targetDir=detectTargetFolder();
    		cacheDir = new File(targetDir, "dir-" + String.valueOf(Math.random()));
    	}
    	
    	return ensureFolderExists(cacheDir);
    }

    protected abstract AsynchronousFileCacheBacking createFileBacking (File dir);

    public void testDeleteIndexFileOnEmptyIndex () throws Exception {
        IndexEntry[]    entries={
                createIndexEntry("weaved-empty", false, false, bytes, bytes),
                createIndexEntry("generated-empty", true, false, bytes, bytes)
            };
        File	cacheIndex=getIndexFile();
        writeIndex(cacheIndex, entries);
        assertTrue("No initial index file available: " + cacheIndex, cacheIndex.canRead());

        AsynchronousFileCacheBacking    cache=createFileBacking(getCacheDir());
        // the call should read an empty index since no data files exist
        Map<String, IndexEntry>         indexMap=cache.getIndexMap();
        assertEquals("Mismatched index size", 0, indexMap.size());

        // no data files were created
        Map<String, byte[]> bytesMap=cache.getBytesMap(); 
        assertEquals("Mismatched bytes size", 0, bytesMap.size());
        
        writeIndex(cache.getIndexFile(), cache.getIndexEntries());

        assertFalse("Index file still available: " + cacheIndex, cacheIndex.canRead());
    }

    protected long generateNewBytes () {
        final long          CRC=AbstractCacheBacking.crc(bytes);
        long                crc=CRC;
        // 8 tries should be enough to find a non-matching CRC...
        for (int    index=0; (index < Byte.SIZE) && (CRC == crc) && (crc != -1L); index++) {
            random.nextBytes(bytes);
            crc = AbstractCacheBacking.crc(bytes);
        }
        assertTrue("Could not generate different CRC for " + CRC, crc != CRC);

        return crc;
    }

    protected Map<String, File> createDataFiles (IndexEntry ... entries) throws IOException {
        return createDataFiles(LangUtil.isEmpty(entries) ? Collections.<IndexEntry>emptyList() : Arrays.asList(entries));
    }

    protected Map<String, File> createDataFiles (Collection<? extends IndexEntry> entries) throws IOException {
        if (LangUtil.isEmpty(entries)) {
            return Collections.emptyMap();
        }

        Map<String, File>  files= new TreeMap<>();
        for (IndexEntry entry : entries) {
            File    file=createDataFile(entry);
            if (file != null) {
                files.put(entry.key, file);
            }
        }

        return files;
    }

    protected File createDataFile (IndexEntry entry) throws IOException {
        return createDataFile(entry, entry.ignored ? null : bytes);
    }

    protected File createDataFile (IndexEntry entry, byte[] dataBytes) throws IOException {
        return createDataFile(entry.key, dataBytes);
    }

    protected File createDataFile (String key, byte[] dataBytes) throws IOException {
        if (LangUtil.isEmpty(dataBytes)) {
            return null;
        }
        
        File    		parent=getCacheDir(), file=new File(parent, key);
        OutputStream    out=new FileOutputStream(file);
        try {
            out.write(dataBytes);
        } finally { 
            out.close();
        }

        return file;
    }

    protected static final IndexEntry createIgnoredEntry (String key) {
        return createIndexEntry(key, false, true, null, null);
    }

    protected static final IndexEntry createIndexEntry (String key, boolean generated, boolean ignored, byte[] bytes, byte[] originalBytes) {
        IndexEntry  entry=new IndexEntry();
        entry.key = key;
        entry.generated = generated;
        entry.ignored = ignored;
        if (ignored) {
            assertFalse(key + " ignored cannot be generated", generated);
        } else {
        	entry.crcClass = AbstractCacheBacking.crc(originalBytes);
            entry.crcWeaved = AbstractCacheBacking.crc(bytes);
        }
        
        return entry;
    }
}
