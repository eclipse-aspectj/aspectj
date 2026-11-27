/* *******************************************************************
 * Copyright (c) 2006-2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassLoaderRepository;
import org.aspectj.apache.bcel.util.SoftHashMap;

import junit.framework.TestCase;

public class ClassLoaderRepositoryTest extends TestCase {
	private File f;
	private ZipFile zf;
	private Enumeration<? extends ZipEntry> entries;
	private SoftHashMap<URL,JavaClass> map;
	private boolean currentUseSharedCache;

	/**
	 * This test will use aspectjtools simply as a test archive of class files.
	 */
	public void setUp() throws Exception {
		f = new File("../lib/aspectj/lib/aspectjtools.jar");
		assertTrue("Couldn't find aspectjtools to test.  Tried: "+f.getAbsolutePath(),f.exists());
		zf = new ZipFile(f);
		entries = zf.entries();
		map = getSharedMap(); // The map we really want to test from inside the ClassLoaderRepository
		currentUseSharedCache = ClassLoaderRepository.useSharedCache;
	}

	private ClassLoaderRepository setupRepository() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ClassLoader res = new URLClassLoader(new URL[]{f.toURI().toURL()},cl);
		ClassLoaderRepository rep = new ClassLoaderRepository(res);
		return rep;
	}

	public void tearDown() {
		new ClassLoaderRepository((ClassLoader) null).reset();
		ClassLoaderRepository.useSharedCache = currentUseSharedCache;
	}

	 private void compareTwoRepositories(boolean useSharedCache) throws Exception {
		ClassLoaderRepository.useSharedCache = useSharedCache;
		ClassLoaderRepository rep1 = setupRepository();
		ClassLoaderRepository rep2 = setupRepository();
		int i = 0;
		while (entries.hasMoreElements()) {
			ZipEntry zfe = entries.nextElement();
			String classfileName = zfe.getName();
			if (classfileName.endsWith(".class")) {
				String clazzname = classfileName.substring(0,classfileName.length()-6).replace('/','.');
				// twice by each
				rep1.loadClass(clazzname);
				rep1.loadClass(clazzname);
				rep2.loadClass(clazzname);
				rep2.loadClass(clazzname);
				i++;
			}
		}

		long[] rep1Stats = rep1.reportStats();
		long[] rep2Stats = rep2.reportStats();

		// Typical outputs if you dump the stats:
		// System.err.println("Successfully compared "+i+" entries!!");
		// System.err.println(rep1.report());
		// System.err.println(rep2.report());
		// Successfully compared 5315 entries!! (i=5315)
		// rep1: BCEL repository report. (shared cache) Total time spent loading: 494ms. Time spent manipulating URLs: 180ms. Classes loaded: 5315. Shared cache size: 5315 Shared cache (hits/missDueToEviction): (15945/0).
		// rep2: BCEL repository report. (shared cache) Total time spent loading: 0ms. Time spent manipulating URLs: 168ms. Classes loaded: 0. Shared cache size: 5315 Shared cache (hits/missDueToEviction): (15945/0).

		if (useSharedCache) {
			// This should result in loading all the classes once and then hitting the cache three times:
			// - first cache hit is the second load in the first repository
			// - second and third cache hits are the loads on the second repository (because the cache is *shared*)
			assertEquals("Should have loaded one of each class from the jar in first repository",i,rep1Stats[ClassLoaderRepository.STATS_CLASSES_LOADED]);
			assertEquals("No local cache hits in first repository",0,rep1Stats[ClassLoaderRepository.STATS_CACHE_HITS_LOCAL]);
			assertEquals("Second load from repo should always hit cache",3*i,rep1Stats[ClassLoaderRepository.STATS_CACHE_HITS_SHARED]);
			assertEquals("Should have loaded none in second repository",0,rep2Stats[ClassLoaderRepository.STATS_CLASSES_LOADED]);
			assertEquals("No local cache hits in second repository",0,rep2Stats[ClassLoaderRepository.STATS_CACHE_HITS_LOCAL]);
			assertEquals("Loads from second repo should always hit cache",3*i,rep2Stats[ClassLoaderRepository.STATS_CACHE_HITS_SHARED]);
		} else {
			// This should result in loading all the classes once in each repository, then hitting the local cache for the second round of loads:
			assertEquals("Should have loaded one of each class from the jar in first repository",i,rep1Stats[ClassLoaderRepository.STATS_CLASSES_LOADED]);
			assertEquals("No shared cache hits in first repository",0,rep1Stats[ClassLoaderRepository.STATS_CACHE_HITS_SHARED]);
			assertEquals("Second load from repo should always hit local cache in first repository",i,rep1Stats[ClassLoaderRepository.STATS_CACHE_HITS_LOCAL]);
			assertEquals("Should have loaded one of each class from the jar in second repository",i,rep2Stats[ClassLoaderRepository.STATS_CLASSES_LOADED]);
			assertEquals("Loads from second repo should always hit local cache in second repository",i,rep2Stats[ClassLoaderRepository.STATS_CACHE_HITS_LOCAL]);
			assertEquals("No shared cache hits in second repository",0,rep2Stats[ClassLoaderRepository.STATS_CACHE_HITS_SHARED]);
		}
	}

//	 private void loadOnce() throws Exception {
//		ClassLoaderRepository rep = setupRepository();
//		while (entries.hasMoreElements()) {
//			ZipEntry zfe = (ZipEntry) entries.nextElement();
//			String classfileName = zfe.getName();
//			if (classfileName.endsWith(".class")) {
//				String clazzname = classfileName.substring(0,
//						classfileName.length() - 6).replace('/', '.');
//
//				rep.loadClass(clazzname);
//			}
//		}
//	}

	 public void testMultiThreaded() throws Throwable {
		 ClassLoaderRepository.useSharedCache=true;
//		 ClassLoaderRepository.sharedCacheCompactFrequency = 200;
		 //loadOnce();
		 TestThread threads[] = new TestThread[6];
		 for (int i=0; i<threads.length; i++) {
			 threads[i] = new TestThread((i%3)*1000);
			 threads[i].start();
		 }
		 for (TestThread thread : threads) {
			 thread.join();
			 if (thread.getFailure() != null) {
				 throw thread.getFailure();
			 }
		 }
	 }

	 private class TestThread extends Thread {
		 public Throwable failure = null;
		 Enumeration<? extends ZipEntry> entries;

		 // ensure the threads are loading DIFFERENT shared classes at the same time...
		 public TestThread(int skip) {
			entries = zf.entries();
			for (int i=0; i<skip && entries.hasMoreElements(); i++) {
				entries.nextElement();
			}
		 }

		 public void run() {
			 try {
				ClassLoaderRepository rep = setupRepository();
				while (entries.hasMoreElements()) {
					ZipEntry zfe = entries.nextElement();
					String classfileName = zfe.getName();
					if (classfileName.endsWith(".class")) {
						String clazzname = classfileName.substring(0,classfileName.length()-6).replace('/','.');
						rep.loadClass(clazzname);
						rep.loadClass(clazzname);
					}
				}
//				System.err.println("Thread finished: "+rep.report());
			 } catch (Throwable t) {
				 failure = t;
			 }
	 	}
		public Throwable getFailure() {
			return failure;
		}
	 }

	 public void testNotSharedRepository() throws  Exception {
    	ClassLoaderRepository.useSharedCache=false;
    	compareTwoRepositories(false);
	 }

	 public void testSharedUrlRepository() throws  Exception {
		compareTwoRepositories(true);
//		ClassLoaderRepository.compactSharedCache();
	}

	public void testPurgeUrlRepository() throws  Exception {
		ClassLoaderRepository.useSharedCache = true;
		ClassLoaderRepository rep = setupRepository();
		while (entries.hasMoreElements()) {
			ZipEntry zfe = (ZipEntry)entries.nextElement();
			String classfileName = zfe.getName();
			if (classfileName.endsWith(".class")) {
				String clazzname = classfileName.substring(0,classfileName.length()-6).replace('/','.');
				rep.loadClass(clazzname);
				assertEquals("expected one entry in shared URL cache "+map.size()+": "+map, 1, map.size());
				map.discardValueReferences(1);
				map.size(); // force purge
			}
		}
		assertEquals("expected empty shared URL cache "+map.size(), 0, map.size());
	}

	public void testAutoPurgeUrlRepository() throws  Exception {
		ClassLoaderRepository.useSharedCache = true;
		assertEquals("expected empty shared URL cache "+map.size(), 0, map.size());
		ClassLoaderRepository rep = setupRepository();
		int i = 0;

		while (i<3 && entries.hasMoreElements()) {
			ZipEntry zfe = entries.nextElement();
			String classfileName = zfe.getName();
			if (classfileName.endsWith(".class")) {
				String clazzname = classfileName.substring(0,classfileName.length()-6).replace('/','.');
				rep.loadClass(clazzname);
				map.discardValueReferences(1);
				i++;
			}
		}
		assertTrue("expected smaller shared URL cache "+map.size(), map.size()<3);
	}

	private Field getSharedMapField() throws Exception {
		Field field = ClassLoaderRepository.class.getDeclaredField("sharedCache");
		field.setAccessible(true);
		return field;
	}

	@SuppressWarnings({ "unchecked" })
	private SoftHashMap<URL,JavaClass> getSharedMap() throws Exception {
		return (SoftHashMap<URL,JavaClass>)getSharedMapField() .get(null);
	}
}

