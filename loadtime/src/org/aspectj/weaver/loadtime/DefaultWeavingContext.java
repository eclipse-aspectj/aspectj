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

import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;
import org.aspectj.weaver.tools.WeavingAdaptor;

/**
 * Use in non-OSGi environment
 * 
 * @author David Knibb
 */
public class DefaultWeavingContext implements IWeavingContext {

	protected ClassLoader loader;
	private String shortName;

	private static Trace trace = TraceFactory.getTraceFactory().getTrace(DefaultWeavingContext.class);

	/**
	 * Construct a new WeavingContext to use the specifed ClassLoader
	 * This is the constructor which should be used.
	 * @param loader
	 */
	public DefaultWeavingContext(ClassLoader loader) {
		super();
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
		return "";
	}

	/**
	 * @return classname@hashcode
	 */
	public String getClassLoaderName() {
    	return ((loader!=null)?loader.getClass().getName()+"@"+Integer.toHexString(System.identityHashCode(loader)):"null");
	}

	/**
	 * @return filename
	 */
	public String getFile(URL url) {
    	return url.getFile();
	}

	/**
	 * @return unqualifiedclassname@hashcode 
	 */
	public String getId () {
		if (shortName == null) {
			shortName = getClassLoaderName().replace('$','.');
			int index = shortName.lastIndexOf(".");
			shortName = shortName.substring(index + 1);
		}
		return shortName;
	}
	
	public String getSuffix () {
		return getClassLoaderName();
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

	/**
	 * Simply call weaving adaptor back to parse aop.xml
	 *
	 * @param weaver
	 * @param loader
	 */
	public List getDefinitions(final ClassLoader loader, final WeavingAdaptor adaptor) {
	        if (trace.isTraceEnabled()) trace.enter("getDefinitions",this,new Object[] { loader, adaptor });
	
	        List definitions = ((ClassLoaderWeavingAdaptor)adaptor).parseDefinitions(loader);
	        
	        if (trace.isTraceEnabled()) trace.exit("getDefinitions",definitions);
			return definitions;
	    }
}
