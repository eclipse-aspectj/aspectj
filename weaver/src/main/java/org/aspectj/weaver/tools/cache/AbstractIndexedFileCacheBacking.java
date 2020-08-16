/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Kew (vmware)         	initial implementation
 *   Lyor Goldstein (vmware)	add support for weaved class being re-defined
 *******************************************************************************/
package org.aspectj.weaver.tools.cache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.aspectj.util.LangUtil;

/**
 * Uses an <code>index</code> file to keep track of the cached entries
 */
public abstract class AbstractIndexedFileCacheBacking extends AbstractFileCacheBacking {
	/**
	 * Default name of cache index file - assumed to contain {@link IndexEntry}-s
	 */
	public static final String INDEX_FILE = "cache.idx";
	protected static final IndexEntry[]	EMPTY_INDEX=new IndexEntry[0];
	protected static final String[]	EMPTY_KEYS=new String[0];

	private final File	indexFile;

	protected AbstractIndexedFileCacheBacking(File cacheDir) {
		super(cacheDir);

        indexFile = new File(cacheDir, INDEX_FILE);
	}

    public File getIndexFile () {
        return indexFile;
    }

    public String[] getKeys(String regex) {
    	Map<String, IndexEntry>	index=getIndex();
    	if ((index == null) || index.isEmpty()) {
    		return EMPTY_KEYS;
    	}

        Collection<String>  matches= new LinkedList<>();
        synchronized(index) {
            for (String key : index.keySet()) {
                if (key.matches(regex)) {
                    matches.add(key);
                }
            }
        }

        if (matches.isEmpty()) {
            return EMPTY_KEYS;
        } else {
            return matches.toArray(new String[0]);
        }
    }

    protected Map<String, IndexEntry> readIndex () {
    	return readIndex(getCacheDirectory(), getIndexFile());
    }

    protected void writeIndex () {
    	writeIndex(getIndexFile());
    }

    protected void writeIndex (File file) {
    	try {
    		writeIndex(file, getIndex());
    	} catch(Exception e) {
    		if ((logger != null) && logger.isTraceEnabled()) {
    			logger.warn("writeIndex(" + file + ") " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
    		}
    	}
    }

    protected abstract Map<String, IndexEntry> getIndex ();

    protected Map<String, IndexEntry> readIndex (File cacheDir, File cacheFile) {
        Map<String, IndexEntry> indexMap= new TreeMap<>();
        IndexEntry[]            idxValues=readIndex(cacheFile);
        if (LangUtil.isEmpty(idxValues)) {
        	if ((logger != null) && logger.isTraceEnabled()) {
                logger.debug("readIndex(" + cacheFile + ") no index entries");
        	}
        	return indexMap;
        }

        for (IndexEntry ie : idxValues) {
            IndexEntry  resEntry=resolveIndexMapEntry(cacheDir, ie);
            if (resEntry != null) {
                indexMap.put(resEntry.key, resEntry);
            } else if ((logger != null) && logger.isTraceEnabled()) {
                logger.debug("readIndex(" + cacheFile + ") skip " + ie.key);
            }
        }

        return indexMap;
    }

    protected IndexEntry resolveIndexMapEntry (File cacheDir, IndexEntry ie) {
    	return ie;
    }

	public IndexEntry[] readIndex(File indexFile) {
		if (!indexFile.canRead()) {
			return EMPTY_INDEX;
		}

		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(indexFile));
			return (IndexEntry[]) ois.readObject();
		} catch (Exception e) {
			if ((logger != null) && logger.isTraceEnabled()) {
				logger.error("Failed (" + e.getClass().getSimpleName() + ")"
						   + " to read index from " + indexFile.getAbsolutePath()
						   + " : " + e.getMessage(), e);
			}
			delete(indexFile);
		} finally {
			close(ois, indexFile);
		}

		return EMPTY_INDEX;
	}

	protected void writeIndex (File indexFile, Map<String,? extends IndexEntry> index) throws IOException {
		writeIndex(indexFile, LangUtil.isEmpty(index) ? Collections.<IndexEntry>emptyList() : index.values());
	}

    protected void writeIndex (File indexFile, IndexEntry ... entries) throws IOException {
        writeIndex(indexFile, LangUtil.isEmpty(entries) ? Collections.<IndexEntry>emptyList() : Arrays.asList(entries));
    }

    protected void writeIndex (File indexFile, Collection<? extends IndexEntry> entries) throws IOException {
        File    indexDir=indexFile.getParentFile();
        if ((!indexDir.exists()) && (!indexDir.mkdirs())) {
            throw new IOException("Failed to create path to " + indexFile.getAbsolutePath());
        }

        int             numEntries=LangUtil.isEmpty(entries) ? 0 : entries.size();
        IndexEntry[]    entryValues=(numEntries <= 0) ? null : entries.toArray(new IndexEntry[numEntries]);
        // if no entries, simply delete the index file
        if (LangUtil.isEmpty(entryValues)) {
            if (indexFile.exists() && (!indexFile.delete())) {
                throw new StreamCorruptedException("Failed to clean up index file at " + indexFile.getAbsolutePath());
            }

            return;
        }

        ObjectOutputStream oos=new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile), 4096));
        try {
            oos.writeObject(entryValues);
        } finally {
            close(oos, indexFile);
        }
    }

    public static final IndexEntry createIndexEntry (CachedClassEntry classEntry, byte[] originalBytes) {
        if (classEntry == null) {
            return null;
        }

        IndexEntry  indexEntry = new IndexEntry();
        indexEntry.key = classEntry.getKey();
        indexEntry.generated = classEntry.isGenerated();
        indexEntry.ignored = classEntry.isIgnored();
    	indexEntry.crcClass = crc(originalBytes);
        if (!classEntry.isIgnored()) {
            indexEntry.crcWeaved = crc(classEntry.getBytes());
        }

        return indexEntry;
    }

	/**
	 * The default index entry in the index file 
	 */
	public static class IndexEntry implements Serializable, Cloneable {
		private static final long serialVersionUID = 756391290557029363L;

		public String key;
		public boolean generated;
		public boolean ignored;
		public long crcClass;
		public long crcWeaved;
		
		public IndexEntry () {
			super();
		}

		@Override
		public IndexEntry clone () {
			try {
				return getClass().cast(super.clone());
			} catch(CloneNotSupportedException e) {
				throw new RuntimeException("Failed to clone: " + toString() + ": " + e.getMessage(), e);
			}
		}

		@Override
		public int hashCode() {
			return (int) (key.hashCode()
				 + (generated ? 1 : 0)
				 + (ignored ? 1 : 0)
				 + crcClass
				 + crcWeaved);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (this == obj)
				return true;
			if (getClass() != obj.getClass())
				return false;

			IndexEntry	other=(IndexEntry) obj;
			if (this.key.equals(other.key)
			 && (this.ignored == other.ignored)
			 && (this.generated == other.generated)
			 && (this.crcClass == other.crcClass)
			 && (this.crcWeaved == other.crcWeaved)) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return key
				 + "[" + (generated ? "generated" : "ignored") + "]"
				 + ";crcClass=0x" + Long.toHexString(crcClass)
				 + ";crcWeaved=0x" + Long.toHexString(crcWeaved)
				 ;
		}
	}

}
