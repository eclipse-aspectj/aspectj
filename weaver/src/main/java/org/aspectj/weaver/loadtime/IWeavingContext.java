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
import java.util.List;

import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.tools.WeavingAdaptor;

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
	Enumeration<URL> getResources(String name) throws IOException;

	/**
	 * In an OSGi environment, determine which bundle a URL originated from.
	 * In a non-OSGi environment, implementors should return <code>null</code>.
	 * @param url
	 * @return
	 * @deprecated use getFile() or getClassLoaderName()
	 */
	String getBundleIdFromURL(URL url);

	/**
	 * In an environment with multiple class loaders allows each to be
	 * identified using something safer and possibly shorter than toString
	 * @return name of the associated class loader
	 */
	String getClassLoaderName();

    ClassLoader getClassLoader();

	/**
	 * Format a URL
	 * @return filename
	 */
	String getFile(URL url);

	/**
	 * In an environment with multiple class loaders allows messages
	 * to identified according to the weaving context
	 * @return short name
	 */
	String getId();

	/**
	 * Return true if the classloader associated with this weaving context
	 * is the one that will define the class with the specified name.
	 * In a delegating classloader hierarchy this might check the parent won't
	 * define it and the child will - in OSGi it will do something else.
	 * @param classname name of the class, eg. "java.lang.String"
	 * @return true if the associated classloader will define the class
	 */
	boolean isLocallyDefined(String classname);

	/**
	 * Allow custom parsing of aop.xml or alternative mechanism for providing
	 * Definitions
	 *
	 * @param loader
	 * @param adaptor
	 * @return List containing 0 or more Definition instances
	 */
	List<Definition> getDefinitions(final ClassLoader loader, WeavingAdaptor adaptor);

}
