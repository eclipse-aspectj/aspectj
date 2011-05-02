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

public class AjTest extends TestCase {

	public void testAj() {
		// Aj aj =
		new Aj();
	}

	public void testAjIWeavingContext() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		IWeavingContext weavingContext = new DefaultWeavingContext(loader);
		// Aj aj =
		new Aj(weavingContext);
	}

	public void testInitialize() {
		Aj aj = new Aj();
		aj.initialize();
	}

	public void testPreProcess() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		Aj aj = new Aj();
		aj.preProcess("Junk", new byte[] {}, loader, null);
	}

	public void testGetNamespace() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		Aj aj = new Aj();
		String namespace = aj.getNamespace(loader);
		assertEquals("Namespace should be empty", "", namespace);
	}

	public void testGeneratedClassesExist() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		Aj aj = new Aj();
		boolean exist = aj.generatedClassesExist(loader);
		assertFalse("There should be no generated classes", exist);
	}

	public void testFlushGeneratedClasses() {
		ClassLoader loader = new URLClassLoader(new URL[] {}, null);
		Aj aj = new Aj();
		aj.flushGeneratedClasses(loader);
		boolean exist = aj.generatedClassesExist(loader);
		assertFalse("There should be no generated classes", exist);
	}

}
