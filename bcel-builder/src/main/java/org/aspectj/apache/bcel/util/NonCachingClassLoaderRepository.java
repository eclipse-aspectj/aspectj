package org.aspectj.apache.bcel.util;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;

/**
 * The repository maintains information about which classes have been loaded.
 * 
 * It loads its data from the ClassLoader implementation passed into its constructor.
 * 
 * @see org.aspectj.apache.bcel.Repository
 * 
 * @version $Id: NonCachingClassLoaderRepository.java,v 1.6 2009/09/09 19:56:20 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @author David Dixon-Peugh
 * 
 */
public class NonCachingClassLoaderRepository implements Repository {
	private static java.lang.ClassLoader bootClassLoader = null;

	private final ClassLoaderReference loaderRef;
	private final Map<String, JavaClass> loadedClasses = new SoftHashMap();

	public static class SoftHashMap extends AbstractMap {
		private Map<Object, SpecialValue> map;
		private ReferenceQueue rq = new ReferenceQueue();

		public SoftHashMap(Map<Object, SpecialValue> map) {
			this.map = map;
		}

		public SoftHashMap() {
			this(new HashMap<>());
		}

		public SoftHashMap(Map<Object,SpecialValue> map, boolean b) {
			this(map);
		}

		class SpecialValue extends SoftReference {
			private final Object key;

			SpecialValue(Object k, Object v) {
				super(v, rq);
				this.key = k;
			}
		}

		private void processQueue() {
			SpecialValue sv = null;
			while ((sv = (SpecialValue) rq.poll()) != null) {
				map.remove(sv.key);
			}
		}

		@Override
		public Object get(Object key) {
			SpecialValue value = map.get(key);
			if (value == null)
				return null;
			if (value.get() == null) {
				// it got GC'd
				map.remove(value.key);
				return null;
			} else {
				return value.get();
			}
		}

		@Override
		public Object put(Object k, Object v) {
			processQueue();
			return map.put(k, new SpecialValue(k, v));
		}

		@Override
		public Set entrySet() {
			return map.entrySet();
		}

		@Override
		public void clear() {
			processQueue();
			Set<Object> keys = map.keySet();
			for (Object name : keys) {
				map.remove(name);
			}
		}

		@Override
		public int size() {
			processQueue();
			return map.size();
		}

		@Override
		public Object remove(Object k) {
			processQueue();
			SpecialValue value = map.remove(k);
			if (value == null)
				return null;
			if (value.get() != null) {
				return value.get();
			}
			return null;
		}
	}

	public NonCachingClassLoaderRepository(java.lang.ClassLoader loader) {
		this.loaderRef = new DefaultClassLoaderReference((loader != null) ? loader : getBootClassLoader());
	}

	public NonCachingClassLoaderRepository(ClassLoaderReference loaderRef) {
		this.loaderRef = loaderRef;
	}

	private static synchronized java.lang.ClassLoader getBootClassLoader() {
		if (bootClassLoader == null) {
			bootClassLoader = new URLClassLoader(new URL[0]);
		}
		return bootClassLoader;
	}

	/**
	 * Store a new JavaClass into this Repository.
	 */
	public void storeClass(JavaClass clazz) {
		synchronized (loadedClasses) {
			loadedClasses.put(clazz.getClassName(), clazz);
		}
		clazz.setRepository(this);
	}

	/**
	 * Remove class from repository
	 */
	public void removeClass(JavaClass clazz) {
		synchronized (loadedClasses) {
			loadedClasses.remove(clazz.getClassName());
		}
	}

	/**
	 * Find an already defined JavaClass.
	 */
	public JavaClass findClass(String className) {
		synchronized (loadedClasses) {
			return loadedClasses.getOrDefault(className, null);
		}
	}

	/**
	 * Clear all entries from cache.
	 */
	public void clear() {
		synchronized (loadedClasses) {
			loadedClasses.clear();
		}
	}

	/**
	 * Lookup a JavaClass object from the Class Name provided.
	 */
	public JavaClass loadClass(String className) throws ClassNotFoundException {

		JavaClass javaClass = findClass(className);
		if (javaClass != null) {
			return javaClass;
		}

		javaClass = loadJavaClass(className);
		storeClass(javaClass);

		return javaClass;
	}

	public JavaClass loadClass(Class clazz) throws ClassNotFoundException {
		return loadClass(clazz.getName());
	}

	private JavaClass loadJavaClass(String className) throws ClassNotFoundException {
		String classFile = className.replace('.', '/');
		try {
			InputStream is = loaderRef.getClassLoader().getResourceAsStream(classFile + ".class");

			if (is == null) {
				throw new ClassNotFoundException(className + " not found.");
			}

			ClassParser parser = new ClassParser(is, className);
			return parser.parse();
		} catch (IOException e) {
			throw new ClassNotFoundException(e.toString());
		}
	}

}
