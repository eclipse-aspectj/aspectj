/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

import com.bea.jvm.ClassPreProcessor;
import com.bea.jvm.JVMFactory;

import junit.framework.TestCase;

public class JRockitAgentTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testJRockitAgent() {
		ClassPreProcessor preProcessor = new JRockitAgent();
		ClassPreProcessor expectedPreProcessor = JVMFactory.getJVM().getClassLibrary().getClassPreProcessor();
		assertEquals("JRocketAgent must be registered", expectedPreProcessor, preProcessor);
	}

	public void testPreProcess() {
		ClassPreProcessor preProcessor = new JRockitAgent();
		preProcessor.preProcess(null, "foo.Bar", new byte[] {});
	}

	public void testJrockitRecursionProtection() {
		if (LangUtil.is19VMOrGreater()) {
			// Skip test, not castable to URLClassLoader
			return;
		}
		URLClassLoader thisLoader = (URLClassLoader) getClass().getClassLoader();
		URL jrockit = FileUtil.getFileURL(new File("../lib/ext/jrockit/jrockit.jar"));
		URL[] urls = new URL[] {jrockit};
		 thisLoader = new URLClassLoader(urls, thisLoader);
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

		try {
			/* Needed by Commons Logging */
			Thread.currentThread().setContextClassLoader(thisLoader.getParent());

			ClassLoader loader = new JRockitClassLoader(thisLoader);

			Class.forName("java.lang.Object", false, loader);
			Class.forName("junit.framework.TestCase", false, loader);
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.toString());
		} finally {
			Thread.currentThread().setContextClassLoader(contextLoader);
		}
	}

	private class JRockitClassLoader extends ClassLoader {

		public final static boolean debug = false;

		private List path = new LinkedList();
		// private com.bea.jvm.ClassPreProcessor agent;
		private Object agent;
		private Method preProcess;

		public JRockitClassLoader(URLClassLoader clone) throws Exception {
			/* Use extensions loader */
			super(clone.getParent());

			URL[] urls = clone.getURLs();
			for (URL value : urls) {
				Object pathElement;
				URL url = value;
				if (debug)
					System.out.println("JRockitClassLoader.JRockitClassLoader() url=" + url.getPath());
				File file = new File(encode(url.getFile()));
				if (debug)
					System.out.println("JRockitClassLoader.JRockitClassLoader() file" + file);
				if (file.isDirectory())
					pathElement = file;
				else if (file.exists() && file.getName().endsWith(".jar"))
					pathElement = new JarFile(file);
				else
					throw new RuntimeException(file.getAbsolutePath());
				path.add(pathElement);
			}

			Class agentClazz = Class.forName("org.aspectj.weaver.loadtime.JRockitAgent", false, this);
			Object obj = agentClazz.newInstance();
			if (debug)
				System.out.println("JRockitClassLoader.JRockitClassLoader() obj=" + obj);
			this.agent = obj;
			byte[] bytes = new byte[] {};
			Class[] parameterTypes = new Class[] { java.lang.ClassLoader.class, java.lang.String.class, bytes.getClass() };
			preProcess = agentClazz.getMethod("preProcess", parameterTypes);
		}

		/* Get rid of escaped characters */
		private String encode(String s) {
			StringBuffer result = new StringBuffer();
			int i = s.indexOf("%");
			while (i != -1) {
				result.append(s.substring(0, i));
				String escaped = s.substring(i + 1, i + 3);
				s = s.substring(i + 3);
				Integer value = Integer.valueOf(escaped, 16);
				result.append(new Character((char) value.intValue()));
				i = s.indexOf("%");
			}
			result.append(s);
			return result.toString();
		}

		protected Class findClass(String name) throws ClassNotFoundException {
			if (debug)
				System.out.println("> JRockitClassLoader.findClass() name=" + name);
			Class clazz = null;
			try {
				clazz = super.findClass(name);
			} catch (ClassNotFoundException ex) {
				for (Iterator i = path.iterator(); clazz == null && i.hasNext();) {
					byte[] classBytes = null;
					try {
						Object pathElement = i.next();
						if (pathElement instanceof File) {
							File dir = (File) pathElement;
							String className = name.replace('.', '/') + ".class";
							File classFile = new File(dir, className);
							if (classFile.exists())
								classBytes = loadClassFromFile(name, classFile);
						} else {
							JarFile jar = (JarFile) pathElement;
							String className = name.replace('.', '/') + ".class";
							ZipEntry entry = jar.getEntry(className);
							if (entry != null)
								classBytes = loadBytesFromZipEntry(jar, entry);
						}

						if (classBytes != null) {
							clazz = defineClass(name, classBytes);
						}
					} catch (IOException ioException) {
						ex.printStackTrace();
					}
				}
			}

			if (debug)
				System.out.println("< JRockitClassLoader.findClass() name=" + name);
			return clazz;
		}

		private Class defineClass(String name, byte[] bytes) {
			if (debug)
				System.out.println("> JRockitClassLoader.defineClass() name=" + name);
			try {
				if (agent != null)
					preProcess.invoke(agent, new Object[] { this, name, bytes });
			} catch (IllegalAccessException iae) {
				iae.printStackTrace();
				throw new ClassFormatError(iae.getMessage());
			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
				throw new ClassFormatError(ite.getTargetException().getMessage());
			}
			if (debug)
				System.out.println("< JRockitClassLoader.defineClass() name=" + name);
			return super.defineClass(name, bytes, 0, bytes.length);
		}

		private byte[] loadClassFromFile(String name, File file) throws IOException {
			if (debug)
				System.out.println("JRockitClassLoader.loadClassFromFile() file=" + file);

			byte[] bytes;
			bytes = new byte[(int) file.length()];
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				bytes = readBytes(fis, bytes);
			} finally {
				if (fis != null)
					fis.close();
			}

			return bytes;
		}

		private byte[] loadBytesFromZipEntry(JarFile jar, ZipEntry entry) throws IOException {
			if (debug)
				System.out.println("JRockitClassLoader.loadBytesFromZipEntry() entry=" + entry);

			byte[] bytes;
			bytes = new byte[(int) entry.getSize()];
			InputStream is = null;
			try {
				is = jar.getInputStream(entry);
				bytes = readBytes(is, bytes);
			} finally {
				if (is != null)
					is.close();
			}

			return bytes;
		}

		private byte[] readBytes(InputStream is, byte[] bytes) throws IOException {
			for (int offset = 0; offset < bytes.length;) {
				int read = is.read(bytes, offset, bytes.length - offset);
				offset += read;
			}
			return bytes;
		}
	}
}
