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

package org.aspectj.weaver.tools.cache;

import java.io.File;

import junit.framework.TestCase;

/**
 */
public class SimpleClassCacheTest extends TestCase {
	byte[] FAKE_BYTES_V1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	byte[] FAKE_BYTES_V2 = {1, 1, 2, 3, 4, 5, 6, 7, 8, 9};

	byte[] FAKE_WOVEN_BYTES_V1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10};
	byte[] FAKE_WOVEN_BYTES_V2 = {1, 1, 2, 3, 4, 5, 6, 7, 8, 9,10};


	private SimpleCache createCache() throws Exception {
		return new SimpleCache(System.getProperty("java.io.tmpdir"),true);
	}


	public void testCache() throws Exception {
		String classA = "com.generated.A";
		SimpleCache cache = createCache();
		
		cache.put(classA, FAKE_BYTES_V1, FAKE_WOVEN_BYTES_V1);
		
		
		// Test the returned woven bytes are the original one
		byte result[] = cache.getAndInitialize(classA, FAKE_BYTES_V1, null, null);
		for(int i = 0; i < result.length; i ++){
			assertEquals("Cached version byte[" +i+"] should be equal to the original woven classe",result[i],FAKE_WOVEN_BYTES_V1[i]);
		}
		
		// Assure the class is properly backed up in the backing folder
		File f = new File (System.getProperty("java.io.tmpdir") + File.separator + "com.generated.A-1164760902");
		assertTrue("Class should be backed up to backing folder, with te CRC:1164760902 ",f.exists());

	}
	
	public void testDifferentVersionCache() throws Exception {
		String classA = "com.generated.A";
		SimpleCache cache = createCache();
		cache.put(classA, FAKE_BYTES_V1, FAKE_WOVEN_BYTES_V1);
		cache.put(classA, FAKE_BYTES_V2, FAKE_WOVEN_BYTES_V2);
		
		// Test the returned woven bytes are the original one for v1
		byte result[] = cache.getAndInitialize(classA, FAKE_BYTES_V1, null, null);
		for(int i = 0; i < result.length; i ++){
			assertEquals("Cached version v1 byte[" +i+"] should be equal to the original woven classe",result[i],FAKE_WOVEN_BYTES_V1[i]);
		}
		
		// Test the returned woven bytes are the original one for v2
		result = cache.getAndInitialize(classA, FAKE_BYTES_V2, null, null);
		for(int i = 0; i < result.length; i ++){
			assertEquals("Cached version v2 byte[" +i+"] should be equal to the original woven classe",result[i],FAKE_WOVEN_BYTES_V2[i]);
		}
	}
}