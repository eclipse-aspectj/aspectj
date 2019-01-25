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
 * Facility for overriding the default CacheKeyResolver
 * and CacheBacking; an implementing factory must be set
 * on the {@link WeavedClassCache} before the
 * {@link org.aspectj.weaver.tools.WeavingAdaptor} is
 * configured.
 */
public interface CacheFactory {
	CacheKeyResolver createResolver();

	CacheBacking createBacking(String scope);
}
