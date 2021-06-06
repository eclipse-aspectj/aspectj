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
