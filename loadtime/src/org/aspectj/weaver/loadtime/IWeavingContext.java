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
 * This class adds support to AspectJ for an OSGi environment
 * 
 * @author David Knibb
 */
public interface IWeavingContext {
	
	/**
	 * Allows the standard ClassLoader.getResources() mechanisms to be
	 * replaced with a different implementation.
	 * In an OSGi environment, this will allow for filtering to take 
	 * place on the results of ClassLoader.getResources(). In a non-OSGi
	 * environment, ClassLoader.getResources should be returned.
	 * @param name the name of the resource to search for
	 * @return an enumeration containing all of the matching resources found
	 * @throws IOException
	 */
	public Enumeration getResources(String name) throws IOException;
	
	/**
	 * In an OSGi environment, determin which bundle a URL originated from.
	 * In a non-OSGi environment, implementors should return <code>null<code>.
	 * @param url
	 * @return
	 */
	public String getBundleIdFromURL(URL url);

}
