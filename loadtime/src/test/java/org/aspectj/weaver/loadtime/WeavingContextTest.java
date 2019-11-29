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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;

import org.aspectj.weaver.tools.WeavingAdaptor;

import junit.framework.TestCase;

public class WeavingContextTest extends TestCase {

	private boolean called;
	
	public void testWeavingContext() {
		URLClassLoader loader = new URLClassLoader(new URL[] {},null);
		IWeavingContext context = new TestWeavingContext(loader);
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader,context);
	}

	public void testGetResources() {
		URLClassLoader loader = new URLClassLoader(new URL[] {},null);
		IWeavingContext context = new TestWeavingContext(loader) {

			public Enumeration getResources(String name) throws IOException {
				called = true;
				return super.getResources(name);
			}
			
		};
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader,context);
		
		assertTrue("IWeavingContext not called",called);
	}

	public void testGetBundleIdFromURL() {
		URLClassLoader loader = new URLClassLoader(new URL[] {},null);
		IWeavingContext context = new TestWeavingContext(loader) {

			public String getBundleIdFromURL(URL url) {
				throw new UnsupportedOperationException();
			}
			
		};
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		try {
			adaptor.initialize(loader,context);
		}
		catch (UnsupportedOperationException ex) {
			fail("IWeavingContect.getBundleIdFromURL() is deprecated");
		}
	}

	public void testGetClassLoaderName() {
		URLClassLoader loader = new URLClassLoader(new URL[] {},null);
		IWeavingContext context = new TestWeavingContext(loader) {

			public String getClassLoaderName () {
				called = true;
				return super.getClassLoaderName();
			}
			
		};
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader,context);
		
		assertTrue("IWeavingContext not called",called);
	}

	public void testGetFile() throws IOException {
		File file = new File("../loadtime/testdata");
		URL fileURL = file.getCanonicalFile().toURL();
		URLClassLoader loader = new URLClassLoader(new URL[] { fileURL },null);
		IWeavingContext context = new TestWeavingContext(loader) {

			public String getFile (URL url) {
				called = true;
				return super.getFile(url);
			}
			
		};
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader,context);
		
		assertTrue("IWeavingContext not called",called);
	}

	public void testGetId() throws IOException {
		File file = new File("../loadtime/testdata");
		URL fileURL = file.getCanonicalFile().toURL();
		URLClassLoader loader = new URLClassLoader(new URL[] { fileURL },null);
		IWeavingContext context = new TestWeavingContext(loader) {

			public String getId () {
				called = true;
				return super.getId();
			}
			
		};
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader,context);
		
		assertTrue("IWeavingContext not called",called);
	}

	public void testGetDefinitions () throws Exception {
		File file = new File("../loadtime/testdata");
		URL fileURL = file.getCanonicalFile().toURL();
		URLClassLoader loader = new URLClassLoader(new URL[] { fileURL },null);
		IWeavingContext context = new TestWeavingContext(loader) {

			public List getDefinitions(ClassLoader loader, WeavingAdaptor adaptor) {
				called = true;
		        return super.getDefinitions(loader,adaptor);
			}
			
		};
		ClassLoaderWeavingAdaptor adaptor = new ClassLoaderWeavingAdaptor();
		adaptor.initialize(loader,context);
		
		assertTrue("getDefinitions not called",called);
	}
	
	private static class TestWeavingContext implements IWeavingContext {

		private ClassLoader loader;
		
		public TestWeavingContext (ClassLoader classLoader) {
			this.loader = classLoader;
		}
		
		public String getBundleIdFromURL(URL url) {
			return null;
		}

		public String getClassLoaderName() {
			return "ClassLoaderName";
		}
		
		public ClassLoader getClassLoader() { return this.loader;}

		public String getFile(URL url) {
			return "File";
		}

		public String getId() {
			return "Id";
		}

		public Enumeration getResources(String name) throws IOException {
			return loader.getResources(name);
		}

		public boolean isLocallyDefined(String classname) {
	        String asResource = classname.replace('.', '/').concat(".class");

	        URL localURL = loader.getResource(asResource);
	        if (localURL == null) return false;

			boolean isLocallyDefined = true;
	        ClassLoader parent = loader.getParent();
	        if (parent != null) {
	            URL parentURL = parent.getResource(asResource);
	            if (localURL.equals(parentURL)) isLocallyDefined =  false;
	        } 
	        return isLocallyDefined;
		}

		public List getDefinitions(ClassLoader loader, WeavingAdaptor adaptor) {
	        return ((ClassLoaderWeavingAdaptor)adaptor).parseDefinitions(loader);
		}
	}

	protected void setUp() throws Exception {
		super.setUp();

		this.called = false;
	}
	
}
