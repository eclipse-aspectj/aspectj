/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * 	   AMC 01.20.2003 extended to support AspectJ 1.1 options
 * ******************************************************************/


package org.aspectj.ajde;

import java.io.File;
import java.util.*;

/**
 * @author	Mik Kersten
 */
public class NullIdeProperties implements ProjectPropertiesAdapter {

	private String testProjectPath = "";
	private List buildConfigFiles = new ArrayList();

	private Set inJars;
	private Set sourceRoots;
	private Set aspectPath;
	private String outJar;

	public NullIdeProperties(String testProjectPath) {
		this.testProjectPath = testProjectPath;
	}

	public List getBuildConfigFiles() {
		return buildConfigFiles;
	}
	
	public String getLastActiveBuildConfigFile() {
		return null;	
	}
	
	public String getDefaultBuildConfigFile() {
		return null;	
	}

    public String getProjectName() {
    	return "test";	
    }

    public String getRootProjectDir() {
    	return testProjectPath;
    }

    public List getProjectSourceFiles() {
    	return null;	
    }

    public String getProjectSourcePath() {
		return testProjectPath + "/src";   	
    }

    public String getClasspath() {
    	//XXX
    	// AMC - added in path separator since absence was causing
    	// build failures with invalid classpath
    	return testProjectPath + File.pathSeparator +
    		System.getProperty("sun.boot.class.path") + File.pathSeparator +  "../runtime/bin";	
    }

    public String getOutputPath() {
    	return testProjectPath + "/bin"; 
    }

    public String getAjcWorkingDir() {
    	return testProjectPath + "/ajworkingdir";	
    }
 
    public String getBootClasspath() {
    	return null;
    }
    
    public String getClassToExecute() {
    	return "figures.Main";	
    }

    public String getExecutionArgs() {
    	return null;
    }

    public String getVmArgs() {
    	return null;	
    }
    
    public void setInJars( Set jars ) { this.inJars = jars; }
    
    public Set getInJars( ) {
    	return inJars;
    }

	public void setOutJar( String jar ){ this.outJar = jar; }

    public String getOutJar() {
    	return outJar;
    }
    
    public void setSourceRoots( Set roots ) { this.sourceRoots = roots; }

    public Set getSourceRoots() {
    	return sourceRoots;
    }

	public void setAspectPath( Set path ) { this.aspectPath = path; }
	    
    public Set getAspectPath() {
    	return aspectPath;
    }
}
