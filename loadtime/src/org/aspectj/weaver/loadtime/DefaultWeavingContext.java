/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   David Knibb         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Use in non-OSGi environment
 * 
 * @author David Knibb
 */
public class DefaultWeavingContext implements IWeavingContext {
	
	protected ClassLoader loader;
	
	public DefaultWeavingContext(){
		loader = getClass().getClassLoader();
	}

	/**
	 * Construct a new WeavingContext to use the specifed ClassLoader
	 * This is the constructor which should be used.
	 * @param loader
	 */
	public DefaultWeavingContext(ClassLoader loader) {
		this.loader = loader;
	}

	/**
	 * Same as ClassLoader.getResources()
	 */
	public Enumeration getResources(String name) throws IOException {
		return loader.getResources(name);
	}

	/**
	 * @return null as we are not in an OSGi environment (therefore no bundles)
	 */
	public String getBundleIdFromURL(URL url) {
		return null;
	}

	/**
	 * @return classname@hashcode
	 */
	public String getClassLoaderName() {
    	return ((loader!=null)?loader.getClass().getName()+"@"+loader.hashCode():"null");
	}

}
