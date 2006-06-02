/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * 	   AMC 01.20.2003 extended to support AspectJ 1.1 options
 * ******************************************************************/


package org.aspectj.ajde;

import java.io.*;
import java.util.*;

import org.aspectj.tools.ajc.AjcTests;
import org.aspectj.util.FileUtil;

/**
 * @author	Mik Kersten
 */
public class NullIdeProperties implements ProjectPropertiesAdapter {

	private String testProjectPath = "";
	private List buildConfigFiles = new ArrayList();

	private Set inJars;
	private Set inpath;
	private Set sourceRoots;
	private Set aspectPath;
	private String outJar;
	private String outputPath = "bin";

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
        return testProjectPath 
            + File.pathSeparator 
            + System.getProperty("sun.boot.class.path")
            + File.pathSeparator 
            + AjcTests.aspectjrtClasspath();  
    }

    public String getOutputPath() {
    	return testProjectPath + "/" + outputPath; 
    }
    
    public void setOutputPath(String outputPath) {
    	this.outputPath = outputPath;
    }

    public OutputLocationManager getOutputLocationManager() {
    	return null;
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
    
    public void setInpath( Set path) { this.inpath = path; }
    
    public Set getInJars( ) {
    	return inJars;
    }
    
    public Set getInpath( ) {
    	return inpath;
    }
    
	public Map getSourcePathResources() {
		Map map = new HashMap();

		/* Allow the user to override the testProjectPath by using sourceRoots */ 
		File[] srcBase;
		if (sourceRoots == null || sourceRoots.isEmpty()) {
			srcBase = new File[] { new File(getProjectSourcePath()) };
		}
		else {
			srcBase = new File[sourceRoots.size()];
			sourceRoots.toArray(srcBase);
		}
		
		for (int j = 0; j < srcBase.length; j++) {
			File[] fromResources = FileUtil.listFiles(srcBase[j], new FileFilter() {
				public boolean accept(File pathname) {
					String name = pathname.getName().toLowerCase();
					return !name.endsWith(".class") && !name.endsWith(".java") && !name.endsWith(".aj");
				}
			});
			for (int i = 0; i < fromResources.length; i++) {
				String normPath = FileUtil.normalizedPath(fromResources[i] ,srcBase[j]);
				map.put(normPath, fromResources[i]);

			}
		}
		
		return map;
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
