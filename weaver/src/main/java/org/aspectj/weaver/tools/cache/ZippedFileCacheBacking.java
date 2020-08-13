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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * Uses a ZIP file to store the instrumented classes/aspects - each one as a
 * <U>separate</U> {@link ZipEntry}. This mechanism is suitable for relatively
 * large numbers of instrumented classes/aspects (100's and more) since it
 * holds all of them in a single (ZIP) file. The down side is that any
 * modifications to the cache require re-writing the entire ZIP file. This
 * can cause the ZIP file to become corrupted if interrupted in mid-update,
 * thus requiring the re-population of the cache on next application activation
 * (though the overhead in this case is not prohibitvely high...)
 */
public class ZippedFileCacheBacking extends AsynchronousFileCacheBacking {
    public static final String ZIP_FILE = "cache.zip";
    private static final AsynchronousFileCacheBackingCreator<ZippedFileCacheBacking>    defaultCreator=
            new AsynchronousFileCacheBackingCreator<ZippedFileCacheBacking>() {
                public ZippedFileCacheBacking create(File cacheDir) {
                    return new ZippedFileCacheBacking(cacheDir);
                }
        };

    private final File  zipFile;
    public ZippedFileCacheBacking(File cacheDir) {
        super(cacheDir);
        zipFile = new File(cacheDir, ZIP_FILE);
    }

    public File getZipFile () {
        return zipFile;
    }

    public static final ZippedFileCacheBacking createBacking (File cacheDir) {
        return createBacking(cacheDir, defaultCreator);
    }

    @Override
    protected void writeClassBytes(String key, byte[] bytes) throws Exception {
        File                  outFile=getZipFile();
        Map<String,byte[]>    entriesMap;
        try {
            entriesMap = readZipClassBytes(outFile);
        } catch(Exception e) {
            if ((logger != null) && logger.isTraceEnabled()) {
                logger.warn("writeClassBytes(" + outFile + ")[" + key + "]"
                          + " failed (" + e.getClass().getSimpleName() + ")"
                          + " to read current data: " + e.getMessage(),
                            e);
            }
            
            FileUtil.deleteContents(outFile);
            return;
        }

        if (entriesMap.isEmpty()) {
            entriesMap = Collections.singletonMap(key, bytes);
        } else {
            entriesMap.put(key, bytes);
        }

        try {
            writeZipClassBytes(outFile, entriesMap);
        } catch(Exception e) {
            if ((logger != null) && logger.isTraceEnabled()) {
                logger.warn("writeClassBytes(" + outFile + ")[" + key + "]"
                          + " failed (" + e.getClass().getSimpleName() + ")"
                          + " to write updated data: " + e.getMessage(),
                            e);
            }
            
            FileUtil.deleteContents(outFile);
        }
    }

    @Override
    protected void removeClassBytes(String key) throws Exception {
        File                  outFile=getZipFile();
        Map<String,byte[]>    entriesMap;
        try {
            entriesMap = readZipClassBytes(outFile);
        } catch(Exception e) {
            if ((logger != null) && logger.isTraceEnabled()) {
                logger.warn("removeClassBytes(" + outFile + ")[" + key + "]"
                          + " failed (" + e.getClass().getSimpleName() + ")"
                          + " to read current data: " + e.getMessage(),
                            e);
            }
            
            FileUtil.deleteContents(outFile);
            return;
        }

        if (!entriesMap.isEmpty()) {
            if (entriesMap.remove(key) == null) {
                return; // not in the data file to begin with so nothing to update
            }
        }

        try {
            writeZipClassBytes(outFile, entriesMap);
        } catch(Exception e) {
            if ((logger != null) && logger.isTraceEnabled()) {
                logger.warn("removeClassBytes(" + outFile + ")[" + key + "]"
                          + " failed (" + e.getClass().getSimpleName() + ")"
                          + " to write updated data: " + e.getMessage(),
                            e);
            }
            
            FileUtil.deleteContents(outFile);
        }
    }

    @Override
    protected Map<String, byte[]> readClassBytes(Map<String, IndexEntry> indexMap, File cacheDir) {
        File                dataFile=new File(cacheDir, ZIP_FILE);
        Map<String,byte[]>  entriesMap;
        boolean             okEntries=true;
        try {
            entriesMap = readZipClassBytes(dataFile);
        } catch(Exception e) {
            if ((logger != null) && logger.isTraceEnabled()) {
                logger.warn("Failed (" + e.getClass().getSimpleName() + ")"
                          + " to read zip entries from " + dataFile
                          + ": " + e.getMessage(),
                          e);
            }

            entriesMap = new TreeMap<>();
            okEntries = false;
        }

        if (!syncClassBytesEntries(dataFile, indexMap, entriesMap)) {
            okEntries = false;
        }

        if (!okEntries) {
            FileUtil.deleteContents(dataFile);

            if (!entriesMap.isEmpty()) {
                entriesMap.clear();
            }
        }

        syncIndexEntries(dataFile, indexMap, entriesMap);

        return entriesMap;
    }

    // remove all non-ignored entries that have no class bytes
    protected Collection<String> syncIndexEntries (File dataFile, Map<String, IndexEntry> indexMap, Map<String,byte[]>  entriesMap) {
        Collection<String>  toDelete=null;
        for (Map.Entry<String, IndexEntry> ie : indexMap.entrySet()) {
            String      key=ie.getKey();
            IndexEntry  indexEntry=ie.getValue();
            if (indexEntry.ignored) {
                continue;   // ignored entries have no class bytes
            }

            if (entriesMap.containsKey(key)) {
                continue;
            }

            if ((logger != null) && logger.isTraceEnabled()) {
                logger.debug("syncIndexEntries(" + dataFile + ")[" + key + "] no class bytes");
            }

            if (toDelete == null) {
                toDelete = new TreeSet<>();
            }
            toDelete.add(key);
        }

        if (toDelete == null) {
            return Collections.emptySet();
        }

        for (String key : toDelete) {
            indexMap.remove(key);
        }

        return toDelete;
    }

    // check if all class bytes entries are valid
    protected boolean syncClassBytesEntries (File dataFile, Map<String, IndexEntry> indexMap, Map<String,byte[]>  entriesMap) {
        boolean okEntries=true;

        for (Map.Entry<String,byte[]> bytesEntry : entriesMap.entrySet()) {
            String      key=bytesEntry.getKey();
            IndexEntry  indexEntry=indexMap.get(key);
            // ignored entries should have no bytes
            if ((indexEntry == null) || indexEntry.ignored) {
                if ((logger != null) && logger.isTraceEnabled()) {
                    logger.debug("syncClassBytesEntries(" + dataFile + ")[" + key + "] bad index entry");
                }

                okEntries = false;
                continue;
            }

            long    crc=crc(bytesEntry.getValue());
            if (crc != indexEntry.crcWeaved) {
                if ((logger != null) && logger.isTraceEnabled()) {
                    logger.debug("syncClassBytesEntries(" + dataFile + ")[" + key + "]"
                               + " mismatched CRC - expected=" + indexEntry.crcWeaved + "/got=" + crc);
                }

                indexMap.remove(key);
                okEntries = false;
                continue;
            }
        }

        return okEntries;
    }

    @Override
    protected IndexEntry resolveIndexMapEntry(File cacheDir, IndexEntry ie) {
        if (cacheDir.exists()) {
            return ie;  // we will take care of non-existing index entries in the readClassBytes method
        } else {
            return null;
        }
    }

    public static final Map<String,byte[]> readZipClassBytes (File file) throws IOException {
        if (!file.canRead()) {
            return Collections.emptyMap();
        }

        Map<String,byte[]>      result= new TreeMap<>();
        byte[]                  copyBuf=new byte[4096];
        ByteArrayOutputStream   out=new ByteArrayOutputStream(copyBuf.length);
        ZipFile                 zipFile=new ZipFile(file);
        try {
            for (Enumeration<? extends ZipEntry> entries=zipFile.entries(); (entries != null) && entries.hasMoreElements(); ) {
                ZipEntry    e=entries.nextElement();
                String      name=e.getName();
                if (LangUtil.isEmpty(name)) {
                    continue;
                }

                out.reset();

                InputStream zipStream=zipFile.getInputStream(e);
                try {
                    for (int    nRead=zipStream.read(copyBuf); nRead != (-1); nRead=zipStream.read(copyBuf)) {
                        out.write(copyBuf, 0, nRead);
                    }
                } finally {
                    zipStream.close();
                }

                byte[]  data=out.toByteArray(), prev=result.put(name, data);
                if (prev != null) {
                    throw new StreamCorruptedException("Multiple entries for " + name);
                }
            }
        } finally {
            zipFile.close();
        }

        return result;
    }
    
    public static final void writeZipClassBytes (File file, Map<String,byte[]> entriesMap) throws IOException {
        if (entriesMap.isEmpty()) {
            FileUtil.deleteContents(file);
            return;
        }

        File	zipDir=file.getParentFile();
        if ((!zipDir.exists()) && (!zipDir.mkdirs())) {
            throw new IOException("Failed to create path to " + zipDir.getAbsolutePath());
        }

        ZipOutputStream zipOut=new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file), 4096));
        try {
            for (Map.Entry<String,byte[]> bytesEntry : entriesMap.entrySet()) {
                String      key=bytesEntry.getKey();
                byte[]      bytes=bytesEntry.getValue();
                zipOut.putNextEntry(new ZipEntry(key));
                zipOut.write(bytes);
                zipOut.closeEntry();
            }
        } finally {
            zipOut.close();
        }
    }
}
