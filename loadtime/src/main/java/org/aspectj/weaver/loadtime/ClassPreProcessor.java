/*******************************************************************************
 * Copyright (c) 2005,2018 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.security.ProtectionDomain;

/**
 * Generic class pre processor interface that allows to separate the AspectJ 5 load time weaving from Java 5 JVMTI interfaces for
 * further use on Java 1.3 / 1.4
 *
 * @author Alexandre Vasseur
 * @author Andy Clement
 */
public interface ClassPreProcessor {

	/**
	 * Post constructor initialization, usually empty
	 */
	void initialize();

	/**
	 * @param className        the name of the class in the internal form of fully qualified class and interface names as
	 *                         defined in <i>The Java Virtual Machine Specification</i>. For example,
	 *                         <code>"java/util/List"</code>.
	 * @param bytes            the input byte buffer in class file format - must not be modified
	 * @param classLoader      the defining loader of the class to be transformed, may be {@code null} if the bootstrap
	 *                         loader
	 * @param protectionDomain the protection domain of the class being defined or redefined
	 *
	 * @return a well-formed class file buffer (weaving result), or {@code null} if no weaving was performed
	 */
	byte[] preProcess(String className, final byte[] bytes, ClassLoader classLoader, ProtectionDomain protectionDomain);

	void prepareForRedefinition(ClassLoader loader, String className);
}
