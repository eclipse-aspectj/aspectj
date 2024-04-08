package org.aspectj.weaver.tools.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.CRC32;

import org.aspectj.weaver.Dump;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Abraham Nevado (lucierna) initial implementation
 ********************************************************************************/

public class SimpleCache {

	private static final String SAME_BYTES_STRING = "IDEM";
	static final byte[] SAME_BYTES = SAME_BYTES_STRING.getBytes(StandardCharsets.UTF_8);

	private final Map<String, byte[]> cacheMap;
	private boolean enabled = false;

	// cache for generated classes
	private Map<String, byte[]> generatedCache;
	private static final String GENERATED_CACHE_SUBFOLDER = "panenka.cache";
	private static final String GENERATED_CACHE_SEPARATOR = ";";

	public static final String IMPL_NAME = "shared";

	protected SimpleCache(String folder, boolean enabled) {
		this.enabled = enabled;

		cacheMap = Collections.synchronizedMap(StoreableCachingMap.init(folder));

		if (enabled) {
			String generatedCachePath = folder + File.separator + GENERATED_CACHE_SUBFOLDER;
			File f = new File(generatedCachePath);
			if (!f.exists()) {
				f.mkdir();
			}
			generatedCache = Collections.synchronizedMap(StoreableCachingMap.init(generatedCachePath, 0));
		}
	}

	/**
	 * Get bytes for given class from cache. If necessary, define and initialise the class first.
	 *
	 * @param classname        name of class to be retrieved from the cache
	 * @param bytes            class bytes (used to calculate cache key)
	 * @param loader           class loader
	 * @param protectionDomain protection domain
	 *
	 * @return {@code null}, if the cache is disabled or if it contains no entry for the given class. An
	 * {@code Optional<byte[]>} value, if the cache knows about the class. The optional will be empty, if the cache entry
	 * represents and unwoven class, i.e. its bytes are identical to the original bytes.
	 */
	@SuppressWarnings("OptionalAssignedToNull")
	public Optional<byte[]> getAndInitialize(
		String classname,
		byte[] bytes,
		ClassLoader loader,
		ProtectionDomain protectionDomain
	)
	{
		if (!enabled) {
			// Cache disabled
			return null;
		}
		byte[] res = get(classname, bytes);
		if (Arrays.equals(SAME_BYTES, res)) {
			// Cache hit: unwoven class
			return Optional.empty();
		}
		if (res != null) {
			// Cache hit: woven class
			initializeClass(classname, res, loader, protectionDomain);
			return Optional.of(res);
		}
		// Cache miss
		return null;
	}

	private byte[] get(String classname, byte[] bytes) {
		String key = generateKey(classname, bytes);
		return cacheMap.get(key);
	}

	public void put(String classname, byte[] origbytes, byte[] wovenbytes) {
		if (!enabled) {
			return;
		}

		String key = generateKey(classname, origbytes);

		if (wovenbytes == null || Arrays.equals(origbytes, wovenbytes)) {
			cacheMap.put(key, SAME_BYTES);
			return;
		}
		cacheMap.put(key, wovenbytes);
	}

	private String generateKey(String classname, byte[] bytes) {
		CRC32 checksum = new CRC32();
		checksum.update(bytes);
		long crc = checksum.getValue();
		classname = classname.replace("/", ".");
		return classname + "-" + crc;

	}

	private static class StoreableCachingMap extends HashMap {

		// TODO: This class extends a raw HashMap, but instances of this class are assigned to fields
		//       Map<String, byte[]> cacheMap and Map<String, byte[]> generatedCache without casts. However, we cannot
		//       simply declare 'extends HashMap<String, byte[]>', because 'put' writes String values (paths) when given
		//       byte[] ones, while 'get' geturns byte[] ones, which is inconsistent. I.e., superficially the class behaves
		//       like a Map<String, byte[]>, while not really being one. This is ugly and hard to understand.

		private final String folder;
		private static final String CACHENAMEIDX = "cache.idx";

		private long lastStored = System.currentTimeMillis();
		private static final int DEF_STORING_TIMER = 60000; //ms
		private final int storingTimer;

		private transient Trace trace;

		private void initTrace() {
			trace = TraceFactory.getTraceFactory().getTrace(StoreableCachingMap.class);
		}

		private StoreableCachingMap(String folder, int storingTimer) {
			this.folder = folder;
			initTrace();
			this.storingTimer = storingTimer;
		}

		public static StoreableCachingMap init(String folder) {
			return init(folder, DEF_STORING_TIMER);
		}

		public static StoreableCachingMap init(String folder, int storingTimer) {
			File file = new File(folder + File.separator + CACHENAMEIDX);
			if (file.exists()) {
				try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
					// Deserialize the object
					StoreableCachingMap sm = (StoreableCachingMap) in.readObject();
					sm.initTrace();
					return sm;
				}
				catch (Exception e) {
					Trace trace = TraceFactory.getTraceFactory().getTrace(StoreableCachingMap.class);
					trace.error("Error reading Storable Cache", e);
				}
			}

			return new StoreableCachingMap(folder, storingTimer);

		}

		@Override
		public Object get(Object obj) {
			try {
				if (super.containsKey(obj)) {
					String path = (String) super.get(obj);
					if (path.equals(SAME_BYTES_STRING)) {
						return SAME_BYTES;
					}
					return readFromPath(path);
				}
				else {
					return null;
				}
			}
			catch (IOException e) {
				trace.error("Error reading key:" + obj.toString(), e);
				Dump.dumpWithException(e);
			}
			return null;
		}

		@Override
		public Object put(Object key, Object value) {
			try {
				String path;
				byte[] valueBytes = (byte[]) value;

				if (Arrays.equals(valueBytes, SAME_BYTES)) {
					path = SAME_BYTES_STRING;
				}
				else {
					path = writeToPath((String) key, valueBytes);
				}
				Object result = super.put(key, path);
				storeMap();
				return result;
			}
			catch (IOException e) {
				trace.error("Error inserting in cache: key:" + key + "; value:" + value.toString(), e);
				Dump.dumpWithException(e);
			}
			return null;
		}


		public void storeMap() {
			long now = System.currentTimeMillis();
			if ((now - lastStored) < storingTimer) {
				return;
			}
			File file = new File(folder + File.separator + CACHENAMEIDX);
			try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file.toPath()))) {
				// Deserialize the object
				out.writeObject(this);
				lastStored = now;
			}
			catch (Exception e) {
				trace.error("Error storing cache; cache file:" + file.getAbsolutePath(), e);
				Dump.dumpWithException(e);
			}
		}

		private byte[] readFromPath(String fullPath) throws IOException {
			try (
				FileInputStream is = new FileInputStream(fullPath);
				ByteArrayOutputStream buffer = new ByteArrayOutputStream()
			) {
				int nRead;
				byte[] data = new byte[16384];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
				return buffer.toByteArray();
			}
			catch (FileNotFoundException e) {
				// May be caused by a generated class that has been stored in generated cache but not saved in cache folder
				System.out.println("FileNotFoundExceptions: The aspectj cache is corrupt. Please clean it and reboot the server. Cache path:" + this.folder);
				e.printStackTrace();
				return null;
			}
		}

		private String writeToPath(String key, byte[] bytes) throws IOException {
			String fullPath = folder + File.separator + key;
			try (FileOutputStream fos = new FileOutputStream(fullPath)) {
				fos.write(bytes);
				fos.flush();
			}
			return fullPath;
		}

	}

	private void initializeClass(
		String className, byte[] bytes,
		ClassLoader loader, ProtectionDomain protectionDomain
	)
	{
		String[] generatedClassesNames = getGeneratedClassesNames(className, bytes);
		if (generatedClassesNames == null) {
			return;
		}
		for (String generatedClassName : generatedClassesNames) {
			byte[] generatedBytes = get(generatedClassName, bytes);
			if (protectionDomain == null) {
				defineClass(loader, generatedClassName, generatedBytes);
			}
			else {
				defineClass(loader, generatedClassName, generatedBytes, protectionDomain);
			}
		}
	}

	private String[] getGeneratedClassesNames(String className, byte[] bytes) {
		String key = generateKey(className, bytes);

		byte[] readBytes = generatedCache.get(key);
		if (readBytes == null) {
			return null;
		}
		String readString = new String(readBytes);
		return readString.split(GENERATED_CACHE_SEPARATOR);
	}

	public void addGeneratedClassesNames(String parentClassName, byte[] parentBytes, String generatedClassName) {
		if (!enabled) {
			return;
		}
		String key = generateKey(parentClassName, parentBytes);

		byte[] storedBytes = generatedCache.get(key);
		if (storedBytes == null) {
			generatedCache.put(key, generatedClassName.getBytes());
		}
		else {
			String storedClasses = new String(storedBytes);
			storedClasses += GENERATED_CACHE_SEPARATOR + generatedClassName;
			generatedCache.put(key, storedClasses.getBytes());
		}
	}

	private Method defineClassMethod = null;
	private Method defineClassWithProtectionDomainMethod = null;

	private void defineClass(ClassLoader loader, String name, byte[] bytes) {
		try {
			if (defineClassMethod == null) {
				// TODO: Replace by class definition strategy used in ClassLoaderWeavingAdaptor
				defineClassMethod = ClassLoader.class.getDeclaredMethod(
					"defineClass",
					String.class, bytes.getClass(), int.class, int.class
				);
				defineClassMethod.setAccessible(true);
			}
			defineClassMethod.invoke(loader, name, bytes, 0, bytes.length);
		}
		catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof LinkageError) {
				e.printStackTrace();
			}
			else {
				System.out.println("define generated class failed" + e.getTargetException());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Dump.dumpWithException(e);
		}
	}

	private void defineClass(ClassLoader loader, String name, byte[] bytes, ProtectionDomain protectionDomain) {
		try {
			if (defineClassWithProtectionDomainMethod == null) {
				// TODO: Replace by class definition strategy used in ClassLoaderWeavingAdaptor
				defineClassWithProtectionDomainMethod = ClassLoader.class.getDeclaredMethod(
					"defineClass",
					String.class, bytes.getClass(), int.class, int.class, ProtectionDomain.class
				);
				defineClassWithProtectionDomainMethod.setAccessible(true);
			}
			defineClassWithProtectionDomainMethod.invoke(loader, name, bytes, 0, bytes.length, protectionDomain);
		}
		catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof LinkageError) {
				e.printStackTrace();
				// is already defined (happens for X$ajcMightHaveAspect
				// interfaces since aspects are reweaved)
				// TODO maw I don't think this is OK and
			}
			else {
				e.printStackTrace();
			}
		}
		catch (NullPointerException e) {
			System.out.println("NullPointerException loading class:" + name + ".  Probabily caused by a corruput cache. Please clean it and reboot the server");
		}
		catch (Exception e) {
			e.printStackTrace();
			Dump.dumpWithException(e);
		}

	}

}
