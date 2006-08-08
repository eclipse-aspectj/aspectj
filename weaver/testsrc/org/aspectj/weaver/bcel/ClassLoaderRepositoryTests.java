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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.aspectj.apache.bcel.util.ClassLoaderRepository;

/** NOT YET INCLUDED IN A FULL TEST RUN - WORK IN PROGRESS CHECKING CLASSLOADERREPOSITORY OPTIMIZATIONS */
public class ClassLoaderRepositoryTests extends TestCase {

	 public void testRepositorySharing() throws  Exception {
    	ClassLoaderRepository.useSharedCache=false;
    	File f = new File("../lib/aspectj/lib/aspectjtools.jar");
    	ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ClassLoader cl1 = new URLClassLoader(new URL[]{f.toURL()},cl);
		ClassLoader cl2 = new URLClassLoader(new URL[]{f.toURL()},cl);
		ClassLoaderRepository rep1 = new ClassLoaderRepository(cl1);
		ClassLoaderRepository rep2 = new ClassLoaderRepository(cl2);
		try {
			assertTrue("Couldnt find aspectjtools to test.  Tried: "+f.getAbsolutePath(),f.exists());
			ZipFile zf = new ZipFile(f);
			int i = 0;
			Enumeration entries = zf.entries();
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
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		System.err.println(rep1.reportAllStatistics());
		System.err.println(rep2.reportAllStatistics());
    }
    
	 
	
}
