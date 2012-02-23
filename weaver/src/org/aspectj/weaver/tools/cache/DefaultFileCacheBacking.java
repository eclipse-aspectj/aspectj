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

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FileUtil;

import java.io.*;
import java.util.HashMap;
import java.util.zip.CRC32;


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
public class DefaultFileCacheBacking implements CacheBacking {
	public static final String WEAVED_CLASS_CACHE_DIR = "aj.weaving.cache.dir";
	public static final String INDEX_FILE = "cache.idx";

	public static class IndexEntry implements Serializable {
		public String key;
		public boolean generated;
		public boolean ignored;
		public long crc;
	}

	private final File cacheDirectory;
	private final CacheKeyResolver resolver;
	private final HashMap<String, IndexEntry> index = new HashMap<String, IndexEntry>();

	private static final Object LOCK = new Object();

	protected DefaultFileCacheBacking(File cacheDirectory, CacheKeyResolver resolver) {
		this.cacheDirectory = cacheDirectory;
		this.resolver = resolver;
		readIndex();
	}

	public static CacheBacking createBacking(File cacheDir, CacheKeyResolver resolver) {
		if (!cacheDir.exists()) {
			if (!cacheDir.mkdirs()) {
				MessageUtil.error("Unable to create cache directory at " + cacheDir.getName());
				return null;
			}
		}
		if (!cacheDir.canWrite()) {
			MessageUtil.error("Cache directory is not writable at " + cacheDir.getName());
			return null;
		}
		return new DefaultFileCacheBacking(cacheDir, resolver);
	}

	public static IndexEntry[] readIndex(File indexFile) {
		IndexEntry[] iea = new IndexEntry[0];
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			if (!indexFile.canRead()) {
				return iea;
			}
			fis = new FileInputStream(indexFile);
			ois = new ObjectInputStream(fis);
			iea = (IndexEntry[]) ois.readObject();
		} catch (Exception e) {
			delete(indexFile);
		} finally {
			close(fis, indexFile);
			close(ois, indexFile);
		}
		return iea;
	}

	private void readIndex() {
		synchronized (LOCK) {
			IndexEntry[] idx = readIndex(new File(cacheDirectory, INDEX_FILE));
			for (IndexEntry ie : idx) {
				File cacheFile = new File(cacheDirectory, ie.key);
				if (cacheFile.canRead() || ie.ignored) {
					index.put(ie.key, ie);
				}
			}
		}
	}

	private void writeIndex() {
		synchronized (LOCK) {
			if (!cacheDirectory.exists())
				cacheDirectory.mkdirs();
			File indexFile = new File(cacheDirectory, INDEX_FILE);
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;
			try {
				delete(indexFile);
				fos = new FileOutputStream(indexFile);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(index.values().toArray(new IndexEntry[0]));
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				close(fos, indexFile);
				close(oos, indexFile);
			}
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

	public void clear() {
		synchronized (LOCK) {
			FileUtil.deleteContents(cacheDirectory);
		}
	}

	public static CacheBacking createBacking(String scope, CacheKeyResolver resolver) {
		String cache = System.getProperty(WEAVED_CLASS_CACHE_DIR);
		if (cache == null) {
			return null;
		}

		File cacheDir = new File(cache, scope);
		return createBacking(cacheDir, resolver);
	}

	public String[] getKeys(final String regex) {
		File[] files = cacheDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File file, String s) {
				if (s.matches(regex)) {
					return true;
				}
				return false;
			}
		});
		if (files == null) {
			return new String[0];
		}
		String[] keys = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			keys[i] = files[i].getName();
		}
		return keys;
	}

	public CachedClassEntry get(CachedClassReference ref) {
		IndexEntry ie = index.get(ref.getKey());
		if (ie != null && ie.ignored) {
			return new CachedClassEntry(ref, WeavedClassCache.ZERO_BYTES, CachedClassEntry.EntryType.IGNORED);
		}
		File cacheFile = new File(cacheDirectory, ref.getKey());
		if (cacheFile.canRead()) {
			if (ie == null) {
				// no index, delete
				delete(cacheFile);
				return null;
			}
			byte[] bytes = read(cacheFile, ie.crc);
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

	public void put(CachedClassEntry entry) {
		File cacheFile = new File(cacheDirectory, entry.getKey());
		if (!cacheFile.exists()) {
			IndexEntry ie = new IndexEntry();
			ie.key = entry.getKey();
			ie.generated = entry.isGenerated();
			ie.ignored = entry.isIgnored();
			if (!entry.isIgnored()) {
				ie.crc = write(cacheFile, entry.getBytes());
			}
			addIndexEntry(ie);
		}
	}

	public void remove(CachedClassReference ref) {
		synchronized (LOCK) {
			File cacheFile = new File(cacheDirectory, ref.getKey());
			removeIndexEntry(ref.getKey());
			delete(cacheFile);
		}
	}

	protected byte[] read(File file, long expectedCRC) {
		CRC32 checksum = new CRC32();
		synchronized (LOCK) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				byte[] bytes = FileUtil.readAsByteArray(fis);
				checksum.update(bytes);
				if (checksum.getValue() == expectedCRC) {
					return bytes;
				}
			} catch (FileNotFoundException e) {
				// file disappeared
				MessageUtil.error("File not found " + file.getName());
			} catch (IOException e) {
				MessageUtil.error("Error reading cached class " + e.getLocalizedMessage());
			} finally {
				close(fis, file);
			}
			// delete the file if there was an exception reading it
			// or the expected checksum does not match
			delete(file);
		}
		return null;
	}

	protected long write(File file, byte[] bytes) {
		if (file.exists()) {
			return -1;
		}
		synchronized (LOCK) {
			if (file.exists()) {
				return -1;
			}
			OutputStream out = null;
			ObjectOutputStream crcOut = null;
			CRC32 checksum = new CRC32();
			try {
				out = new FileOutputStream(file);
				out.write(bytes);
				checksum.update(bytes);
				return checksum.getValue();
			} catch (FileNotFoundException e) {
				MessageUtil.error("Error writing (File Not Found) " + file.getName() + ": " + e.getLocalizedMessage());
			} catch (IOException e) {
				MessageUtil.error("Error writing " + file.getName());

			} finally {
				close(out, file);
			}
			// delete the file if there was an exception writing it
			delete(file);
		}
		return -1;
	}

	protected static void delete(File file) {
		if (file.exists()) {
			file.delete();
		}
	}

	protected static void close(OutputStream out, File file) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				// error
				MessageUtil.error("Error closing write file " + file.getName());
			}
		}
	}

	protected static void close(InputStream in, File file) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// error
				MessageUtil.error("Error closing read file " + file.getName());
			}
		}
	}
}
