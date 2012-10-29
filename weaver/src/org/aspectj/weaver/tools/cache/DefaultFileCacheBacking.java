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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.util.Map;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;


/**
 * Naive File-Backed Class Cache with no expiry or application
 * centric invalidation.
 * <p/>
 * Enabled with the system property, "aj.weaving.cache.dir"
 * If this system property is not set, no caching will be
 * performed.
 * <p/>
 * A CRC checksum is stored alongside the class file to verify
 * the bytes on read. If for some reason there is an error
 * reading either the class or crc file, or if the crc does not
 * match the class data the cache entry is deleted.
 * <p/>
 * An alternate implementation of this could store the class file
 * as a jar/zip directly, which would have the required crc; as
 * a first pass however it is somewhat useful to view these files
 * in expanded form for debugging.
 */
public class DefaultFileCacheBacking extends AbstractIndexedFileCacheBacking {
	private final Map<String, IndexEntry> index;

	private static final Object LOCK = new Object();

	protected DefaultFileCacheBacking(File cacheDir) {
		super(cacheDir);
		index = readIndex();
	}

	public static final DefaultFileCacheBacking createBacking(File cacheDir) {
		if (!cacheDir.exists()) {
			if (!cacheDir.mkdirs()) {
				MessageUtil.error("Unable to create cache directory at " + cacheDir.getName());
				return null;
			}
		} else if (!cacheDir.isDirectory()) {
			MessageUtil.error("Not a cache directory at " + cacheDir.getName());
			return null;
		}

		if (!cacheDir.canWrite()) {
			MessageUtil.error("Cache directory is not writable at " + cacheDir.getName());
			return null;
		}
		return new DefaultFileCacheBacking(cacheDir);
	}

    @Override
	protected Map<String, IndexEntry> getIndex() {
		return index;
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

	private void removeIndexEntry(String key) {
		synchronized (LOCK) {
			index.remove(key);
			writeIndex();
		}
	}

	private void addIndexEntry(IndexEntry ie) {
		synchronized (LOCK) {
			index.put(ie.key, ie);
			writeIndex();
		}
	}

    @Override
	protected Map<String, IndexEntry> readIndex() {
		synchronized (LOCK) {
			return super.readIndex();
		}
	}

	@Override
	protected void writeIndex() {
		synchronized (LOCK) {
			super.writeIndex();
		}
	}

	public void clear() {
		File	cacheDir=getCacheDirectory();
		int		numDeleted=0;
		synchronized (LOCK) {
			numDeleted = FileUtil.deleteContents(cacheDir);
		}

		if ((numDeleted > 0) && (logger != null) && logger.isTraceEnabled()) {
			logger.info("clear(" + cacheDir + ") deleted");
		}
	}

	public static CacheBacking createBacking(String scope) {
		String cache = System.getProperty(WEAVED_CLASS_CACHE_DIR);
		if (cache == null) {
			return null;
		}

		File cacheDir = new File(cache, scope);
		return createBacking(cacheDir);
	}

	@Override
	public String[] getKeys(final String regex) {
		File	cacheDirectory = getCacheDirectory();
		File[] files = cacheDirectory.listFiles(new FilenameFilter() {
				public boolean accept(File file, String s) {
					if (s.matches(regex)) {
						return true;
					}
					return false;
				}
			});
		if (LangUtil.isEmpty(files)) {
			return EMPTY_KEYS;
		}
		String[] keys = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			keys[i] = files[i].getName();
		}
		return keys;
	}

	public CachedClassEntry get(CachedClassReference ref, byte[] originalBytes) {
		File cacheDirectory = getCacheDirectory();
		String	refKey=ref.getKey();
		File cacheFile = new File(cacheDirectory, refKey);
		IndexEntry ie = index.get(refKey);
		if (ie == null) {
			// no index, delete
			delete(cacheFile);
			return null;
		}

		// check if original file changed
		if (crc(originalBytes) != ie.crcClass) {
			delete(cacheFile);
			return null;
		}
		
		if (ie.ignored) {
			return new CachedClassEntry(ref, WeavedClassCache.ZERO_BYTES, CachedClassEntry.EntryType.IGNORED);
		}

		if (cacheFile.canRead()) {
			byte[] bytes = read(cacheFile, ie.crcWeaved);
			if (bytes != null) {
				if (!ie.generated) {
					return new CachedClassEntry(ref, bytes, CachedClassEntry.EntryType.WEAVED);
				} else {
					return new CachedClassEntry(ref, bytes, CachedClassEntry.EntryType.GENERATED);
				}
			}
		}

		return null;
	}

	public void put(CachedClassEntry entry, byte[] originalBytes) {
		File	cacheDirectory = getCacheDirectory();
		String	refKey = entry.getKey();
		IndexEntry ie = index.get(refKey);
		File 	cacheFile = new File(cacheDirectory, refKey);
		final boolean	writeEntryBytes;
		// check if original bytes changed or the ignored/generated flags
		if ((ie != null)
		 && ((ie.ignored != entry.isIgnored()) || (ie.generated != entry.isGenerated()) || (crc(originalBytes) != ie.crcClass))) {
			delete(cacheFile);
			writeEntryBytes = true;
		} else {
			writeEntryBytes = !cacheFile.exists();
		}

		if (writeEntryBytes) {
			ie = createIndexEntry(entry, originalBytes);
			if (!entry.isIgnored()) {
				ie.crcWeaved = write(cacheFile, entry.getBytes());
			}
			addIndexEntry(ie);
		}
	}

	public void remove(CachedClassReference ref) {
		File	cacheDirectory = getCacheDirectory();
		String	refKey = ref.getKey();
		File	cacheFile = new File(cacheDirectory, refKey);
		synchronized (LOCK) {
			removeIndexEntry(refKey);
			delete(cacheFile);
		}
	}

	@Override
	protected void delete(File file) {
		synchronized (LOCK) {
			super.delete(file);
		}
	}

	protected byte[] read(File file, long expectedCRC) {
		byte[]	bytes=null;
		synchronized (LOCK) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				bytes = FileUtil.readAsByteArray(fis);
			} catch (Exception e) {
				if ((logger != null) && logger.isTraceEnabled()) {
					logger.warn("read(" + file.getAbsolutePath() + ")"
							+ " failed (" + e.getClass().getSimpleName() + ")"
							+ " to read contents: " + e.getMessage(), e);
				}
			} finally {
				close(fis, file);
			}

			// delete the file if there was an exception reading it or mismatched crc
			if ((bytes == null) || (crc(bytes) != expectedCRC)) {
				delete(file);
				return null;
			}
		}

		return bytes;
	}

	protected long write(File file, byte[] bytes) {
		synchronized (LOCK) {
			if (file.exists()) {
				return -1L;
			}
			OutputStream out = null;
			try {
				out = new FileOutputStream(file);
				out.write(bytes);
			} catch (Exception e) {
				if ((logger != null) && logger.isTraceEnabled()) {
					logger.warn("write(" + file.getAbsolutePath() + ")"
							+ " failed (" + e.getClass().getSimpleName() + ")"
							+ " to write contents: " + e.getMessage(), e);
				}
				// delete the file if there was an exception writing it
				delete(file);
				return -1L;
			} finally {
				close(out, file);
			}

			return crc(bytes);
		}
	}

}
