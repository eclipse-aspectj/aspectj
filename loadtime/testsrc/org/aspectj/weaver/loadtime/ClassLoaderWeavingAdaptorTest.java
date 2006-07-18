/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

public class ClassLoaderWeavingAdaptorTest extends TestCase {

	public void testClassLoaderWeavingAdaptor() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader,null);
	}

	public void testGetNamespace() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader,null);
		String namespace = adaptor.getNamespace();
		assertEquals("Namespace should be empty","",namespace);
	}

	public void testGeneratedClassesExistFor() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader,null);
		boolean exist = adaptor.generatedClassesExistFor("Junk");
		assertFalse("There should be no generated classes",exist);
	}

	public void testFlushGeneratedClasses() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader,null);
		adaptor.flushGeneratedClasses();
		boolean exist = adaptor.generatedClassesExistFor("Junk");
		assertFalse("There should be no generated classes",exist);
	}

}
