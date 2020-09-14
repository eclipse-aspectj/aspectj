/* *******************************************************************
 * Copyright (c) 2012 VMware, Inc. custard
 * 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 * Lyor Goldstein
 * ******************************************************************/

package org.aspectj.weaver.tools.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Map;
import java.util.TreeMap;

import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * Uses a &quot;flat&quot; files model to store the cached instrumented classes
 * and aspects - i.e., each class/aspect is stored as a <U>separate</U> (binary)
 * file. This is a good mechanism when the number of instrumented class is
 * relatively small (a few 10's). The reason for it is that scanning a folder
 * that has many files in it quickly becomes an I/O bottleneck. Also, some
 * O/S-es may impose internal limits on the maximum number of &quot;children&quot;
 * a folder node may have. On the other hand, it is much faster (again, for
 * small number of instrumented classes) than the ZIP cache since each class/aspect
 * is represented by a single file - thus adding/removing/modifying it is easier. 
 * 
 * @author Lyor Goldstein
 */
public class FlatFileCacheBacking extends AsynchronousFileCacheBacking {
    private static final AsynchronousFileCacheBackingCreator<FlatFileCacheBacking>    defaultCreator=
            new AsynchronousFileCacheBackingCreator<FlatFileCacheBacking>() {
                public FlatFileCacheBacking create(File cacheDir) {
                    return new FlatFileCacheBacking(cacheDir);
                }
        };
    public FlatFileCacheBacking(File cacheDir) {
        super(cacheDir);
    }

    public static final FlatFileCacheBacking createBacking (File cacheDir) {
        return createBacking(cacheDir, defaultCreator);
    }

    @Override
    protected Map<String, byte[]> readClassBytes(Map<String, IndexEntry> indexMap, File cacheDir) {
        return readClassBytes(indexMap, cacheDir.listFiles());
    }

    protected Map<String, byte[]> readClassBytes (Map<String,IndexEntry> indexMap, File[] files) {
        Map<String, byte[]> result= new TreeMap<>();
        if (LangUtil.isEmpty(files)) {
            return result;
        }

        for (File file : files) {
            if (!file.isFile()) {
                continue;   // skip sub-directories - we expect flat files
            }

            String  key=file.getName();
            if (INDEX_FILE.equalsIgnoreCase(key)) {
                continue;   // skip the index itself if found
            }

            IndexEntry  entry=indexMap.get(key);
            if ((entry == null) || entry.ignored) {    // if not in index or ignored then remove it
                if ((logger != null) && logger.isTraceEnabled()) {
                    logger.info("readClassBytes(" + key + ") remove orphan/ignored: " + file.getAbsolutePath());
                }
                FileUtil.deleteContents(file);
                continue;
            }

            try {
                byte[]  bytes=FileUtil.readAsByteArray(file);
                long    crc=crc(bytes);
                if (crc != entry.crcWeaved) {
                    throw new StreamCorruptedException("Mismatched CRC - expected=" + entry.crcWeaved + "/got=" + crc);
                }

                result.put(key, bytes);
                if ((logger != null) && logger.isTraceEnabled()) {
                    logger.debug("readClassBytes(" + key + ") cached from " + file.getAbsolutePath());
                }
            } catch(IOException  e) {
                if ((logger != null) && logger.isTraceEnabled()) {
                    logger.error("Failed (" + e.getClass().getSimpleName() + ")"
                               + " to read bytes from " + file.getAbsolutePath()
                               + ": " + e.getMessage());
                }
                indexMap.remove(key);   // no need for the entry if no file - force a re-write of its bytes
                FileUtil.deleteContents(file);  // assume some kind of corruption
                continue;
            }
        }

        return result;
    }
    
    @Override
    protected IndexEntry resolveIndexMapEntry (File cacheDir, IndexEntry ie) {
        File cacheEntry = new File(cacheDir, ie.key);
        if (ie.ignored || cacheEntry.canRead()) {
            return ie;
        } else {
            return null;
        }
    }

    @Override
    protected void writeClassBytes (String key, byte[] bytes) throws Exception {
        File    dir=getCacheDirectory(), file=new File(dir, key);
        FileOutputStream    out=new FileOutputStream(file);
        try {
            out.write(bytes);
        } finally {
            out.close();
        }
    }

    @Override
    protected void removeClassBytes (String key) throws Exception {
        File        dir=getCacheDirectory(), file=new File(dir, key);
        if (file.exists() && (!file.delete())) {
            throw new StreamCorruptedException("Failed to delete " + file.getAbsolutePath());
        }
    }

}
