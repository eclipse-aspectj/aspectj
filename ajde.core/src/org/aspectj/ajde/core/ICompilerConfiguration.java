/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.ajde.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface that contains all the configuration required for the 
 * compiler to be able to perform a build
 */
public interface ICompilerConfiguration {
	
	/**
	 * Returns the table of the current custom java options. 
	 * <p>
	 * For a complete description of the configurable options, see 
	 * {@link org.aspectj.ajde.core.JavaOptions#getDefaultJavaOptions}
	 * or {@link org.aspectj.org.eclipse.jdt.core.IJavaProject#getOptions(boolean)}
	 * </p>
	 * 
	 * @return table of current settings of all options 
	 *   (key type: <code>String</code>; value type: <code>String</code>)
	 * @see org.aspectj.ajde.core.JavaOptions#getDefaultJavaOptions or
	 * org.aspectj.org.eclipse.jdt.core.IJavaProject#getOptions(boolean)
	 */
	public Map /*String --> String */getJavaOptionsMap();
	
	/**
	 * The non-standard options, typically prefaced with -X when used 
	 * with a command line compiler. The default is no non-standard 
	 * options. Options should be separated by a space, for example 
	 * "-showWeaveInfo -XnoInline"
	 */
	public String getNonStandardOptions();
	
	/**
	 * @return a list of those files to include in the build
	 */
    public List /*String*/ getProjectSourceFiles();

    /**
     * @return the classpath to use
     */
    public String getClasspath();

    /**
     * @return the IOutputLocationManager associated with this 
     * compiler configuration
     */
    public IOutputLocationManager getOutputLocationManager();

	/**
	 * @return the set of input path elements for this compilation.
	 * Set members should be of the type java.io.File.
	 * An empty set or null is acceptable for this option.
	 * From -inpath
	 */
    public Set /*java.io.File*/ getInpath();
    
	/**
	 * @return the output jar file for the compilation results.
	 * Return null to leave classfiles unjar'd in output directory
	 * From -outjar
	 */
    public String getOutJar();

	/**
	 * @return the set of aspect jar files to be used for the compilation.
	 * Returning null or an empty set disables this option. Set members
	 * should be of type java.io.File.
	 * From -aspectpath
	 */
    public Set /*java.io.File*/ getAspectPath();
    
	/**
	 * Get the set of non-Java resources for this compilation.
	 * Set members should be of type java.io.File.
	 * An empty set or null is acceptable for this option.
	 * 
	 * @return map from unique resource name to absolute path to source 
	 * resource (String to File)
	 */
    public Map /*String --> java.io.File */getSourcePathResources();
	
}
