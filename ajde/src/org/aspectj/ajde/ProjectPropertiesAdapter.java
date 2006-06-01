/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC       initial implementation 
 *     AMC  01.20.2003  extended for AspectJ 1.1 compiler options
 * ******************************************************************/

 
 
package org.aspectj.ajde;

import java.util.*;

/**
 * @author	Mik Kersten
 */
public interface ProjectPropertiesAdapter {

    public String getProjectName();

    public String getRootProjectDir();

	public List getBuildConfigFiles();
	
	public String getDefaultBuildConfigFile();
	
	/**
	 * @return The last selected build configuration for this project.  If no selection
	 * 			is present the default build config file for this project is returned.
	 */ 
	public String getLastActiveBuildConfigFile();

    public List getProjectSourceFiles();

    public String getProjectSourcePath();

    public String getClasspath();

    public String getOutputPath();
    
    /** 
     * A non-null OutputLocationManager takes precedence over getOutputPath...
     * @return
     */
    public OutputLocationManager getOutputLocationManager();

//    public String getAjcWorkingDir();

	// @todo: move to build options
    public String getBootClasspath();
    
    // @todo: move all below to execution options
    public String getClassToExecute();

    public String getExecutionArgs();

    public String getVmArgs();
    
    // following methods added for AspectJ 1.1
	//-----------------------------------------
	
	/**
	 * Get the set of input jar files for this compilation.
	 * Set members should be of type java.io.File.
	 * An empty set or null is acceptable for this option.
	 * From -injars.
	 */
	public Set getInJars();
	
	/**
	 * Get the set of input path elements for this compilation.
	 * Set members should be of the type java.io.File.
	 * An empty set or null is acceptable for this option.
	 * From -injars
	 */
	public Set getInpath();
	
	/**
	 * Get the set of non-Java resources for this compilation.
	 * Set members should be of type java.io.File.
	 * An empty set or null is acceptable for this option.
	 * From -injars.
	 * 
	 * @return map from unique resource name to absolute path to source resource (String to File)
	 */
	public Map getSourcePathResources();
	
	/**
	 * Get the output jar file for the compilation results.
	 * Return null to leave classfiles unjar'd in output directory
	 * From -outjar
	 */
	public String getOutJar();
	
	/**
	 * Get a set of root source directories for the compilation.
	 * Set members should be of type java.io.File
	 * Returning null or an empty set disables the option.
	 * From -sourceroots
	 */
	public Set getSourceRoots();
	
	/**
	 * Get the set of aspect jar files to be used for the compilation.
	 * Returning null or an empty set disables this option. Set members
	 * should be of type java.io.File.
	 * From -aspectpath
	 */
	public Set getAspectPath();
}
