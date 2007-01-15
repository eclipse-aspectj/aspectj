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
package org.aspectj.tools.ajbrowser.core;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.core.ICompilerConfiguration;
import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.ajde.ui.UserPreferencesAdapter;
import org.aspectj.tools.ajbrowser.BrowserManager;

/**
 * AjBrowser implementation of ICompilerConfiguration which returns something
 * for getClasspath(), getJavaOptionsMap(), getNonStandardOptions() and 
 * getOutputLocationManager() and null for everything else. The reason it doesn't
 * return anything for getProjectSourceFiles() is that it uses .lst files to record
 * what is needed to build (via BuildConfigManager).
 */
public class BrowserCompilerConfiguration implements ICompilerConfiguration {

	private UserPreferencesAdapter preferencesAdapter;
	private IOutputLocationManager locationManager;

	public BrowserCompilerConfiguration(UserPreferencesAdapter preferencesAdapter) {
		this.preferencesAdapter = preferencesAdapter;
	}

	public String getClasspath() {
		StringBuffer classpath = new StringBuffer();
		String userPath = preferencesAdapter
			.getProjectPreference(PreferenceStoreConstants.BUILD_CLASSPATH);
		if (userPath != null && userPath.trim().length() != 0) {
			classpath.append(userPath);
		}
		List outputDirs = getOutputLocationManager().getAllOutputLocations();
		for (Iterator iterator = outputDirs.iterator(); iterator.hasNext();) {
			File dir = (File) iterator.next();
			classpath.append(File.pathSeparator + dir.getAbsolutePath() + File.pathSeparator);
		}
		classpath.append(System.getProperty("java.class.path", "."));
		//System.out.println("classpath: " + classpath.toString());
		return classpath.toString();
	}

	public Map getJavaOptionsMap() {
		return BrowserManager.getDefault().getJavaBuildOptions().getJavaBuildOptionsMap();
	}

	public String getNonStandardOptions() {
		return preferencesAdapter
				.getProjectPreference(PreferenceStoreConstants.NONSTANDARD_OPTIONS);
	}

	public IOutputLocationManager getOutputLocationManager() {
		if (locationManager == null) {
			locationManager = new BrowserOutputLocationManager(preferencesAdapter);
		}
		return locationManager;
	}

	public List getProjectSourceFiles() {
		// unimplemented in AjBrowser (uses BuildConfigManager instead)
		return null;
	}

	public Map getSourcePathResources() {
		// unimplemented in AjBrowser
		return null;
	}

	public Set getAspectPath() {
		// unimplemented in AjBrowser
		return null;
	}

	public Set getInpath() {
		// unimplemented in AjBrowser
		return null;
	}

	public String getOutJar() {
		// unimplemented in AjBrowser
		return null;
	}
}
