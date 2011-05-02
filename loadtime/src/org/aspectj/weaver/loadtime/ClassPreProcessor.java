/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.security.ProtectionDomain;

/**
 * Generic class pre processor interface that allows to separate the AspectJ 5 load time weaving from Java 5 JVMTI interfaces for
 * further use on Java 1.3 / 1.4
 * 
 * @author Alexandre Vasseur
 */
public interface ClassPreProcessor {

	/**
	 * Post constructor initialization, usually empty
	 */
	void initialize();

	/**
	 * Weave
	 * 
	 * @param className
	 * @param bytes
	 * @param classLoader
	 * @param a protection domain that may be used for defining extraneous classes generated as part of modifying the one passed in
	 * @return
	 */
	byte[] preProcess(String className, byte[] bytes, ClassLoader classLoader, ProtectionDomain protectionDomain);
}