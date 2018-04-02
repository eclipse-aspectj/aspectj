/*******************************************************************************
 * Copyright (c) 2005,2018 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Java 1.5 adapter for class pre processor
 * 
 * @author Alexandre Vasseur
 * @author Andy Clement
 */
public class ClassPreProcessorAgentAdapter implements ClassFileTransformer {

	private static ClassPreProcessor classPreProcessor;

	static {
		try {
			classPreProcessor = new Aj();
			classPreProcessor.initialize();
		} catch (Exception e) {
			throw new ExceptionInInitializerError("could not initialize JSR163 preprocessor due to: " + e.toString());
		}
	}

	/**
	 * Invokes the weaver to modify some set of input bytes.
	 * 
	 * @param loader the defining class loader
	 * @param className the name of class being loaded
	 * @param classBeingRedefined is set when hotswap is being attempted
	 * @param protectionDomain the protection domain for the class being loaded
	 * @param bytes the incoming bytes (before weaving)
	 * @return the woven bytes
	 */
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] bytes) throws IllegalClassFormatException {
		if (classBeingRedefined != null) {
			System.err.println("INFO: (Enh120375):  AspectJ attempting reweave of '" + className + "'");
			classPreProcessor.prepareForRedefinition(loader, className);
		}
		return classPreProcessor.preProcess(className, bytes, loader, protectionDomain);
	}
}
