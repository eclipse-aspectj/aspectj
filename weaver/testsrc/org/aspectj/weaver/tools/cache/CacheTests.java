/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Kew (vmware)         initial implementation
 *******************************************************************************/

package org.aspectj.weaver.tools.cache;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 */
public class CacheTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(CacheTests.class.getName());
		suite.addTestSuite(SimpleClassCacheTest.class);
		suite.addTestSuite(WeavedClassCacheTest.class);
		suite.addTestSuite(DefaultCacheKeyResolverTest.class);
		suite.addTestSuite(DefaultFileCacheBackingTest.class);
		suite.addTestSuite(FlatFileCacheBackingTest.class);
		suite.addTestSuite(ZippedFileCacheBackingTest.class);
		return suite;
	}
}
