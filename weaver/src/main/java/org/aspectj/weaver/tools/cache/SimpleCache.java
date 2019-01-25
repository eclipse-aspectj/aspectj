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
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

import org.aspectj.weaver.Dump;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;


/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abraham Nevado (lucierna) initial implementation
 ********************************************************************************/

public class SimpleCache {

	private static final String SAME_BYTES_STRING = "IDEM";
	private static final byte[] SAME_BYTES = SAME_BYTES_STRING.getBytes();

	private Map<String, byte[]> cacheMap;
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
			File f = new File (generatedCachePath);
			if (!f.exists()){
				f.mkdir();
			}
			generatedCache = Collections.synchronizedMap(StoreableCachingMap.init(generatedCachePath,0));
		}
	}

	public byte[] getAndInitialize(String classname, byte[] bytes,
			ClassLoader loader, ProtectionDomain protectionDomain) {
		if (!enabled) {
			return null;
		}
		byte[] res = get(classname, bytes);

		if (Arrays.equals(SAME_BYTES, res)) {
			return bytes;
		} else {
			if (res != null) {
				initializeClass(classname, res, loader, protectionDomain);
			}
			return res;
		}

	}

	private byte[] get(String classname, byte bytes[]) {
		String key = generateKey(classname, bytes);

		byte[] res = cacheMap.get(key);
		return res;
	}

	public void put(String classname, byte[] origbytes, byte[] wovenbytes) {
		if (!enabled) {
			return;
		}

		String key = generateKey(classname, origbytes);

		if (Arrays.equals(origbytes, wovenbytes)) {
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
		return new String(classname + "-" + crc);

	}

	private static class StoreableCachingMap extends HashMap {
		private String folder;
		private static final String CACHENAMEIDX = "cache.idx";
		
		private long lastStored = System.currentTimeMillis();
		private static int DEF_STORING_TIMER = 60000; //ms
		private int storingTimer;
		
		private transient Trace trace;
		private void initTrace(){
			trace = TraceFactory.getTraceFactory().getTrace(StoreableCachingMap.class);
		}
		
//		private StoreableCachingMap(String folder) {
//			this.folder = folder;
//			initTrace();
//		}
		
		private StoreableCachingMap(String folder, int storingTimer){
			this.folder = folder;
			initTrace();
			this.storingTimer = storingTimer;
		}
		
		public static StoreableCachingMap init(String folder) {
			return init(folder,DEF_STORING_TIMER);
			
		}
		
		public static StoreableCachingMap init(String folder, int storingTimer) {
			File file = new File(folder + File.separator + CACHENAMEIDX);
			if (file.exists()) {
				try {
					ObjectInputStream in = new ObjectInputStream(
							new FileInputStream(file));
					// Deserialize the object
					StoreableCachingMap sm = (StoreableCachingMap) in.readObject();
					sm.initTrace();
					in.close();
					return sm;
				} catch (Exception e) {
					Trace trace = TraceFactory.getTraceFactory().getTrace(StoreableCachingMap.class);
					trace.error("Error reading Storable Cache", e);
				}
			}

			return new StoreableCachingMap(folder,storingTimer);

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
				} else {
					return null;
				}
			} catch (IOException e) {
				trace.error("Error reading key:"+obj.toString(),e);
				Dump.dumpWithException(e);
			}
			return null;
		}

		@Override
		public Object put(Object key, Object value) {
			try {
				String path = null;
				byte[] valueBytes = (byte[]) value;
				
				if (Arrays.equals(valueBytes, SAME_BYTES)) {
					path = SAME_BYTES_STRING;
				} else {
					path = writeToPath((String) key, valueBytes);
				}
				Object result = super.put(key, path);
				storeMap();
				return result;
			} catch (IOException e) {
				trace.error("Error inserting in cache: key:"+key.toString() + "; value:"+value.toString(), e);
				Dump.dumpWithException(e);
			}
			return null;
		}
		
		

		public void storeMap() {
			long now = System.currentTimeMillis();
			if ((now - lastStored ) < storingTimer){
				return;
			}
			File file = new File(folder + File.separator + CACHENAMEIDX);;
			try {
				ObjectOutputStream out = new ObjectOutputStream(
						new FileOutputStream(file));
				// Deserialize the object
				out.writeObject(this);
				out.close();
				lastStored = now;
			} catch (Exception e) {
				trace.error("Error storing cache; cache file:"+file.getAbsolutePath(), e);
				Dump.dumpWithException(e);
			}
		}

		private byte[] readFromPath(String fullPath) throws IOException {
			FileInputStream is = null ;
			try{
				is = new FileInputStream(fullPath);
			}
			catch (FileNotFoundException e){
				//may be caused by a generated class that has been stored in generated cache but not saved at cache folder
				System.out.println("FileNotFoundExceptions: The aspectj cache is corrupt. Please clean it and reboot the server. Cache path:"+this.folder );
				e.printStackTrace();
				return null;
			}
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
			is.close();
			return buffer.toByteArray();

		}

		private String writeToPath(String key, byte[] bytes) throws IOException {
			String fullPath = folder + File.separator + key;
			FileOutputStream fos = new FileOutputStream(fullPath);
			fos.write(bytes);
			fos.flush();
			fos.close();
			return fullPath;
		}

	}

	private void initializeClass(String className, byte[] bytes,
			ClassLoader loader, ProtectionDomain protectionDomain) {
		String[] generatedClassesNames = getGeneratedClassesNames(className,bytes);

		if (generatedClassesNames == null) {
			return;
		}
		for (String generatedClassName : generatedClassesNames) {

			byte[] generatedBytes = get(generatedClassName, bytes);
			
			if (protectionDomain == null) {
				defineClass(loader, generatedClassName, generatedBytes);
			} else {
				defineClass(loader, generatedClassName, generatedBytes,
						protectionDomain);
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
		} else {
			String storedClasses = new String(storedBytes);
			storedClasses += GENERATED_CACHE_SEPARATOR + generatedClassName;
			generatedCache.put(key, storedClasses.getBytes());
		}
	}

	private Method defineClassMethod = null;
	private Method defineClassWithProtectionDomainMethod = null;

	private void defineClass(ClassLoader loader, String name, byte[] bytes) {

		Object clazz = null;

		try {
			if (defineClassMethod == null) {
				defineClassMethod = ClassLoader.class.getDeclaredMethod(
						"defineClass", new Class[] { String.class,
								bytes.getClass(), int.class, int.class });
			}
			defineClassMethod.setAccessible(true);
			clazz = defineClassMethod.invoke(loader, new Object[] { name,
					bytes, new Integer(0), new Integer(bytes.length) });
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof LinkageError) {
				e.printStackTrace();
			} else {
				System.out.println("define generated class failed"
						+ e.getTargetException());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Dump.dumpWithException(e);
		}
	}

	private void defineClass(ClassLoader loader, String name, byte[] bytes,
			ProtectionDomain protectionDomain) {

		Object clazz = null;

		try {
			// System.out.println(">> Defining with protection domain " + name +
			// " pd=" + protectionDomain);
			if (defineClassWithProtectionDomainMethod == null) {
				defineClassWithProtectionDomainMethod = ClassLoader.class
						.getDeclaredMethod("defineClass", new Class[] {
								String.class, bytes.getClass(), int.class,
								int.class, ProtectionDomain.class });
			}
			defineClassWithProtectionDomainMethod.setAccessible(true);
			clazz = defineClassWithProtectionDomainMethod.invoke(loader,
					new Object[] { name, bytes, Integer.valueOf(0),
							new Integer(bytes.length), protectionDomain });
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof LinkageError) {
				e.printStackTrace();
				// is already defined (happens for X$ajcMightHaveAspect
				// interfaces since aspects are reweaved)
				// TODO maw I don't think this is OK and
			} else {
				e.printStackTrace();
			}
		}catch (NullPointerException e) {
			System.out.println("NullPointerException loading class:"+name+".  Probabily caused by a corruput cache. Please clean it and reboot the server");
		} catch (Exception e) {
			e.printStackTrace();
			Dump.dumpWithException(e);
		}

	}

}
