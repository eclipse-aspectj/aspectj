package org.aspectj.apache.bcel.classfile.tests;

import java.net.URL;
import java.net.URLClassLoader;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.ClassLoaderRepository;

import junit.framework.TestCase;

/*
 * Tests create a simple classloader repository configuration and check sharing of information.
 */
public class ClassloaderRepositoryTest extends TestCase {

	private ClassLoaderRepository rep1,rep2;
	
	public void setUp() throws Exception {
		super.setUp();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ClassLoader cl1 = new URLClassLoader(new URL[]{},cl);
		ClassLoader cl2 = new URLClassLoader(new URL[]{},cl);
		rep1 = new ClassLoaderRepository(cl1);
		rep2 = new ClassLoaderRepository(cl2);
	}

	// Retrieve string 5 times from same repository, 4 hits should be from local cache
	public void testLocalCacheWorks() throws ClassNotFoundException {		
		ClassLoaderRepository.useSharedCache=false;
		JavaClass jc = rep1.loadClass("java.lang.String");
		jc = rep1.loadClass("java.lang.String");
		jc = rep1.loadClass("java.lang.String");
		jc = rep1.loadClass("java.lang.String");
		jc = rep1.loadClass("java.lang.String");
		assertTrue("Should have used local cache 4 times: "+reportLocalCacheHits(rep1),reportLocalCacheHits(rep1)==4);
	}

	// Retrieve string 5 times from same repository, 4 hits should be from local cache
	public void testSharedCacheWorksOnOne() throws ClassNotFoundException {		
		ClassLoaderRepository.useSharedCache=true;
		JavaClass jc = rep1.loadClass("java.lang.String");
		jc = rep1.loadClass("java.lang.String");
		jc = rep1.loadClass("java.lang.String");
		jc = rep1.loadClass("java.lang.String");
		jc = rep1.loadClass("java.lang.String");
		assertTrue("Should have used local cache 4 times: "+reportSharedCacheHits(rep1),reportSharedCacheHits(rep1)==4);
	}

	// Retrieve String through one repository then load again through another, should be shared cache hit
	public void testSharedCacheWorks() throws ClassNotFoundException {		
		ClassLoaderRepository.useSharedCache=true;
		JavaClass jc = rep1.loadClass("java.lang.String");
		jc = rep2.loadClass("java.lang.String");
		assertTrue("Should have retrieved String from shared cache: "+reportSharedCacheHits(rep1),
				reportSharedCacheHits(rep1)==1);
	}
	
	// Shared cache OFF, shouldn't get a shared cache hit
	public void testSharedCacheCanBeDeactivated() throws ClassNotFoundException {		
		try {
			ClassLoaderRepository.useSharedCache=false;
			JavaClass jc = rep1.loadClass("java.lang.String");
			jc = rep2.loadClass("java.lang.String");
			assertTrue("Should not have retrieved String from shared cache: "+
					reportSharedCacheHits(rep1),
				    reportSharedCacheHits(rep1)==0);
		} finally {
			ClassLoaderRepository.useSharedCache=true;
		}
	}
	
	public void tearDown() throws Exception {
		super.tearDown();
		System.err.println("Rep1: "+rep1.reportStats());
		System.err.println("Rep2: "+rep2.reportStats());
		rep1.reset();
		rep2.reset();
	}

	private long reportLocalCacheHits(ClassLoaderRepository rep) {
		return rep.reportStats()[5];
	}

	private long reportSharedCacheHits(ClassLoaderRepository rep) {
		return rep.reportStats()[3];
	}

}

