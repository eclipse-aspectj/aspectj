/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Kew (vmware)         	initial implementation
 *   Lyor Goldstein (vmware)	add support for weaved class being re-defined
 *******************************************************************************/

package org.aspectj.weaver.tools.cache;

import org.aspectj.weaver.tools.GeneratedClassHandler;

/**
 * Handler for generated classes; such as Shadowed closures, etc. This wraps the normal
 * generated class handler with caching
 */
public class GeneratedCachedClassHandler implements GeneratedClassHandler {
	private final WeavedClassCache cache;
	private final GeneratedClassHandler nextGeneratedClassHandler;

	public GeneratedCachedClassHandler(WeavedClassCache cache, GeneratedClassHandler nextHandler) {
		this.cache = cache;
		this.nextGeneratedClassHandler = nextHandler;
	}

	public void acceptClass (String name, byte[] originalBytes, byte[] wovenBytes) {
		// The cache expects classNames in dot form
		CachedClassReference ref = cache.createGeneratedCacheKey(name.replace('/', '.'));
		cache.put(ref, originalBytes, wovenBytes);
		if (nextGeneratedClassHandler != null) {
			nextGeneratedClassHandler.acceptClass(name, originalBytes, wovenBytes);
		}
	}
}
