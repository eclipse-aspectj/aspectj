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

/**
 * Default factory for creating the backing and resolving classes.
 */
public class DefaultCacheFactory implements CacheFactory {
	public CacheKeyResolver createResolver() {
		return new DefaultCacheKeyResolver();
	}

	public CacheBacking createBacking(String scope) {
		return DefaultFileCacheBacking.createBacking(scope);
	}
}
