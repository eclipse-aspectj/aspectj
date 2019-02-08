/*******************************************************************************
 * Copyright (c) 2012 Lucierna 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Abraham Nevado (lucierna) - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc171;

import java.io.File;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

// NOTE THIS IS ACTUALLY IN 1.7.2 - IT IS JUST THAT THE PATCH WAS CREATED AGAINST 1.7.1
public class NewFeatures extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testSharedCache() {
		this.runTest("Test Shared Cache");
		File cacheFolder =  new File(ajc.getSandboxDirectory().getAbsolutePath() + File.separator + "panenka.cache");
		assertTrue("Cache folder should be written when using share cache", cacheFolder.exists());		
		//Delete the cache from the ajc sandbox
		deleteFolder(cacheFolder);
	}

	public void testPerClassLoaderCache() {
		this.runTest("Test Per ClassLoader Cache");
		File cacheFolder =  new File(ajc.getSandboxDirectory().getAbsolutePath() + File.separator + "panenka.cache");
		assertFalse("Shared Cache Folder should not be present", cacheFolder.exists());		
	}
	
	public void testDefaultCachePerClassloader() {
		this.runTest("Test Default Cache Per ClassLoader");
		File cacheFolder =  new File(ajc.getSandboxDirectory().getAbsolutePath() + File.separator + "panenka.cache");
		assertFalse("By Default Per ClassLoader Cache should be used and not the shared one", cacheFolder.exists());		
	}

	// ///////////////////////////////////////

	private static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(NewFeatures.class);
	}

	@SuppressWarnings("unused")
	private JavaClass getMyClass(String className) throws ClassNotFoundException {
		return getClassFrom(ajc.getSandboxDirectory(), className);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("newfeatures-tests.xml");
//		return new File("../tests/src/org/aspectj/systemtest/ajc171/newfeatures-tests.xml");
	}

}