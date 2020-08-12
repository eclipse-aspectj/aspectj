/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.aspectj.apache.bcel.util.ClassLoaderRepository;

import junit.framework.TestCase;

/** NOT YET INCLUDED IN A FULL TEST RUN - WORK IN PROGRESS CHECKING CLASSLOADERREPOSITORY OPTIMIZATIONS */
public class ClassLoaderRepositoryTest extends TestCase {
	private File f;
	private ZipFile zf;
	private Enumeration entries;
	private Map map;
	
	public void setUp() throws Exception {
    	f = new File("../lib/aspectj/lib/aspectjtools.jar");
		assertTrue("Couldn't find aspectjtools to test.  Tried: "+f.getAbsolutePath(),f.exists());
		zf = new ZipFile(f);		
		entries = zf.entries();
//		ClassLoaderRepository.sharedCacheCompactFrequency = 16384;
		map = getSharedMap();
	}
	
	public void tearDown() {
		 new ClassLoaderRepository((ClassLoader) null).reset();
	}
	
	private ClassLoaderRepository setupRepository() throws Exception {
    	ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ClassLoader res = new URLClassLoader(new URL[]{f.toURL()},cl);
		ClassLoaderRepository rep = new ClassLoaderRepository(res);
		return rep;
	}
	
	 private void compareTwoRepositories() throws Exception {
		ClassLoaderRepository rep1 = setupRepository();
		ClassLoaderRepository rep2 = setupRepository();
		int i = 0;
		while (entries.hasMoreElements()) {
			ZipEntry zfe = (ZipEntry)entries.nextElement();
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
		System.err.println("Successfully compared "+i+" entries!!");
		System.err.println(rep1.report());
		System.err.println(rep2.report());
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
		 Enumeration entries;
		 
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
				int i = 0;
				while (entries.hasMoreElements()) {
					ZipEntry zfe = (ZipEntry)entries.nextElement();
					String classfileName = zfe.getName();
					if (classfileName.endsWith(".class")) {
						String clazzname = classfileName.substring(0,classfileName.length()-6).replace('/','.');
						rep.loadClass(clazzname);
						rep.loadClass(clazzname);
						i++;
					}
				}				 
				System.err.println("Thread finished: "+rep.report());
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
    	compareTwoRepositories();
	 }
		 
	 public void testSharedUrlRepository() throws  Exception {
		ClassLoaderRepository.useSharedCache=true;
		compareTwoRepositories();
//		ClassLoaderRepository.compactSharedCache();
    }
	 
	public void testPurgeUrlRepository() throws  Exception {		
		ClassLoaderRepository.useSharedCache = true;
		ClassLoaderRepository rep = setupRepository();
		Reference ref = null;
		
		while (ref==null && entries.hasMoreElements()) {
			ZipEntry zfe = (ZipEntry)entries.nextElement();
			String classfileName = zfe.getName();
			if (classfileName.endsWith(".class")) {
				String clazzname = classfileName.substring(0,classfileName.length()-6).replace('/','.');
				
				rep.loadClass(clazzname);
				assertEquals("expected one entry in shared URL cache "+map.size()+": "+map, 1, map.size());
				ref = (Reference)map.values().iterator().next();
				ref.clear();
				ref.enqueue();
				map.size();//force purge
			}
		}
		assertEquals("expected empty shared URL cache "+map.size(), 0, map.size());
	}
	
	public void testAutoPurgeUrlRepository() throws  Exception {		
		ClassLoaderRepository.useSharedCache = true;
		assertEquals("expected empty shared URL cache "+map.size(), 0, map.size());
		ClassLoaderRepository rep = setupRepository();
		Reference ref = null;
		int i = 0;
		
		while (i<3 && entries.hasMoreElements()) {
			ZipEntry zfe = (ZipEntry)entries.nextElement();
			String classfileName = zfe.getName();
			if (classfileName.endsWith(".class")) {
				String clazzname = classfileName.substring(0,classfileName.length()-6).replace('/','.');
				
				rep.loadClass(clazzname);
				ref = (Reference)map.values().iterator().next();
				ref.clear();
				ref.enqueue();
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
	
	private Map getSharedMap() throws Exception {
		return (Map)getSharedMapField() .get(null);
	}
}

