/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

 
 
package org.aspectj.ajde;

import java.util.List;

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

    public String getAjcWorkingDir();

	// @todo: move to build options
    public String getBootClasspath();
    
    // @todo: move all below to execution options
    public String getClassToExecute();

    public String getExecutionArgs();

    public String getVmArgs();
}
