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
 *     Xerox/PARC     initial implementation 
 * 	   AMC 01.20.2003 extended for new AspectJ 1.1 project options
 * ******************************************************************/



package org.aspectj.tools.ajbrowser;

import java.util.*;
import java.io.*;
import org.aspectj.ajde.*;
import org.aspectj.ajde.ui.*;

public class BrowserProperties implements ProjectPropertiesAdapter {
    
    UserPreferencesAdapter preferencesAdapter = null;
    
    public BrowserProperties(UserPreferencesAdapter preferencesAdapter) {
    	this.preferencesAdapter = preferencesAdapter;
    }

    public String getLastOpenSourceFilePath() {
        return preferencesAdapter.getProjectPreference("editor.lastOpenFile");
    }

    public void setLastOpenSourceFilePath(String value) {
        preferencesAdapter.setProjectPreference("editor.lastOpenFile",value);
    }

    public String getLastOpenSourceLineNumber() {
        return preferencesAdapter.getProjectPreference("editor.lastOpenLineNumber");
    }

    public void setLastOpenSourceLineNumber(String value) {
        preferencesAdapter.setProjectPreference("editor.lastOpenLineNumber",value);
    }

	public List getBuildConfigFiles() {
		return BrowserManager.getDefault().getConfigFiles();
	}
	
	public String getDefaultBuildConfigFile() {
		return null;	
	}

	public String getLastActiveBuildConfigFile() {
		return null;	
	}

    public String getProjectName() {
        return null;
    }

    public String getClassToExecute() {
        return preferencesAdapter.getProjectPreference("runtime.mainClass");
    }

    public void setClassToExecute(String mainClass) {
        preferencesAdapter.setProjectPreference("runtime.mainClass", mainClass);
    }

    public String getRootProjectDir() {
        return new File(Ajde.getDefault().getConfigurationManager().getActiveConfigFile()).getParent();
    }

	public String getExecutionArgs() {
		return null;	
	}

	public List getProjectSourceFiles() {
		return null;	
	}
    
    public String getVmArgs() {
    	return null;	
    }

    public String getProjectSourcePath() {
        return null;
    }

    public String getBootClasspath() {
        return System.getProperty("sun.boot.class.path");
    }

    public void setAjcOptions(String flags) {
        preferencesAdapter.setProjectPreference("build.flags", flags);
    }

    public String getAjcOptions() {
        return preferencesAdapter.getProjectPreference("build.flags");
    }

    public String getOutputPath() {
        String outputPath = preferencesAdapter.getProjectPreference("build.outputpath");
        if (outputPath == null) {
            return ".";
        } else {
            return outputPath;
        }
    }

    public void setOutputPath(String path) {
        preferencesAdapter.setProjectPreference("build.outputpath", path);
    }
    
    public OutputLocationManager getOutputLocationManager() {
    	return null;
    }

    public String getUserClasspath() {
        return preferencesAdapter.getProjectPreference("build.classpath");
    }

    public String getClasspath() {
        String systemPath = System.getProperty("java.class.path", ".");
        String userPath = preferencesAdapter.getProjectPreference("build.classpath");
        if (userPath != null && userPath.trim().length() != 0) {
        	return userPath;
        } else {
        	return systemPath;
        }
    }

    public void setClasspath(String path) {
        preferencesAdapter.setProjectPreference("build.classpath", path);
    }

    // 1.1 options
    public Set getInJars( ) {
    	return null;
    }
    
    public Set getInpath() {
    	return null;
    }
    
    public String getOutJar( ) { // XXX unimplemented
    	return null;
    }
    
	public Set getSourceRoots( ) { // XXX unimplemented
		return null;
	}    
	
	public Set getAspectPath( ) { // XXX unimplemented
		return null;
	}

	public Map getSourcePathResources() {
		return null;
	}
}

//public String getAjcWorkingDir() {
//String workingDir = preferencesAdapter.getProjectPreference("build.workingdir");
//if (workingDir == null || workingDir.equals(getOutputPath())) {
//  return getOutputPath() + "./ajworkingdir";
//} else {
//  return workingDir;
//}
//}

//public void setAjcWorkingDir(String path) {
//preferencesAdapter.setProjectPreference("build.workingdir", path);
//}

