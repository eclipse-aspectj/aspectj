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
import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.ajde.ui.UserPreferencesAdapter;

/**
 * IOutputLocationManager which returns the same output location for
 * all files and resources.
 */
public class BrowserOutputLocationManager implements IOutputLocationManager {

	private UserPreferencesAdapter preferencesAdapter;

	public BrowserOutputLocationManager(
			UserPreferencesAdapter preferencesAdapter) {
		this.preferencesAdapter = preferencesAdapter;
	}

	public File getOutputLocationForClass(File compilationUnit) {
		return new File(getCommonOutputDir());
	}

	public File getOutputLocationForResource(File resource) {
		return new File(getCommonOutputDir());
	}

	private String getCommonOutputDir() {
		String outputPath = preferencesAdapter.getProjectPreference(
				PreferenceStoreConstants.BUILD_OUTPUTPATH);
		if (outputPath == null) {
			return ".";
		}
		return outputPath;
	}

	public List getAllOutputLocations() {
		List outputDirs = new ArrayList();
		outputDirs.add(new File(getCommonOutputDir()));
		return outputDirs;
	}

	public File getDefaultOutputLocation() {
		return new File(getCommonOutputDir());
	}

}
