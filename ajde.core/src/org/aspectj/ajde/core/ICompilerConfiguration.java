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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajdt.internal.core.builder.CompilerConfigurationChangeFlags;

/**
 * Interface that contains all the configuration required for the compiler to be able to perform a build
 */
public interface ICompilerConfiguration extends CompilerConfigurationChangeFlags {

	/**
	 * Returns the table of the current custom java options.
	 * <p>
	 * For a complete description of the configurable options, see {@link org.aspectj.ajde.core.JavaOptions#getDefaultJavaOptions}
	 * or {@link org.aspectj.org.eclipse.jdt.core.IJavaProject#getOptions(boolean)}
	 * </p>
	 * 
	 * @return table of current settings of all options (key type: <code>String</code>; value type: <code>String</code>)
	 * @see org.aspectj.ajde.core.JavaOptions#getDefaultJavaOptions or
	 *      org.aspectj.org.eclipse.jdt.core.IJavaProject#getOptions(boolean)
	 */
	public Map /* String --> String */getJavaOptionsMap();

	/**
	 * The non-standard options, typically prefaced with -X when used with a command line compiler. The default is no non-standard
	 * options. Options should be separated by a space, for example "-showWeaveInfo -XnoInline"
	 */
	public String getNonStandardOptions();

	/**
	 * @return a list of those files to include in the build
	 */
	public List<String> getProjectSourceFiles();

	/**
	 * @return a list of those files that should be used to configure a build
	 */
	public List<String> getProjectXmlConfigFiles();

	/**
	 * Return a subset of those files we'd get on getProjectSourceFiles() - the subset that have changed since the last build. If
	 * someone else has already worked out what needs rebuilding, we don't need to do it again by checking all of the
	 * projectSourceFiles(). Returning an empty list means nothing has changed, returning null means you have no idea what changed
	 * and the compiler should work it out.
	 * 
	 * @return a subset of those files that would be returned on getProjectSourceFiles() that have actually *changed*
	 */
	public List /* File */getProjectSourceFilesChanged();

	/**
	 * @return the classpath to use
	 */
	public String getClasspath();

	/**
	 * @return the IOutputLocationManager associated with this compiler configuration
	 */
	public IOutputLocationManager getOutputLocationManager();

	/**
	 * @return the set of input path elements for this compilation. Set members should be of the type java.io.File. An empty set or
	 *         null is acceptable for this option. From -inpath
	 */
	public Set<File> getInpath();

	/**
	 * @return the output jar file for the compilation results. Return null to leave classfiles unjar'd in output directory From
	 *         -outjar
	 */
	public String getOutJar();

	/**
	 * @return the set of aspect jar files to be used for the compilation. Returning null or an empty set disables this option. Set
	 *         members should be of type java.io.File. From -aspectpath
	 */
	public Set<File> getAspectPath();

	/**
	 * Get the set of non-Java resources for this compilation. Set members should be of type java.io.File. An empty set or null is
	 * acceptable for this option.
	 * 
	 * @return map from unique resource name to absolute path to source resource (String to File)
	 */
	public Map /* String --> java.io.File */getSourcePathResources();

	/**
	 * Returns a set of bit flags indicating what has changed in the configuration since it was previously read. This allows the
	 * compiler to avoid repeating computation for values that are the same as before.
	 * 
	 * @return set of bit flags, see the constants in @link {@link CompilerConfigurationChangeFlags}. If unsure return EVERYTHING
	 */
	public int getConfigurationChanges();

	/**
	 * Called by AspectJ once it has processed the configuration object and is ready to do a build. The configuration object may or
	 * may not be interested in this event. It probably will be if it is correctly tracking changes and answering
	 * getConfigurationChanges() with something other than EVERYTHING.
	 */
	public void configurationRead();

	/**
	 * Return a List (Strings) of the directory elements on the classpath that are likely to contain modified .class files since the
	 * previous build and must be checked. This would be used in the situation where a project has a dependency on another project
	 * and the dependency is captured by inclusion of one project on the classpath for the other. When the first project is built,
	 * we need to check the classpath element on the second projects classpath that represents the bin folder of the first project.
	 * By explicitly returning a list here we can avoid checking EVERYTHING.
	 * 
	 * @return a list of modified elements that should be checked (can be empty) or null if unknown (and in which case every
	 *         classpath element will be checked)
	 */
	public List getClasspathElementsWithModifiedContents();

	//
	// /**
	// * @return the location where the state can be persisted across eclipse restarts. eg. f:/foo/bar/goo
	// */
	// public String getLocationForPersistingProjectState();

	/**
	 * Return the encoding to use for this project. Return null if the platform default should be used. Example return value "UTF-8"
	 */
	public String getProjectEncoding();

}
