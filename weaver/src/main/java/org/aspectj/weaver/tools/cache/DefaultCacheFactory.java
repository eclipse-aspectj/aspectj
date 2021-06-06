/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
