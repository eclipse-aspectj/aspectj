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

 

package org.aspectj.ajde.ui.internal;

import org.aspectj.ajde.*;
import org.aspectj.ajde.ui.*;

public class AjcBuildOptions implements BuildOptionsAdapter {
	
	private UserPreferencesAdapter preferencesAdapter = null;
	
	private static final String AJC = "ajc";
	private static final String USE_JAVAC_MODE = AJC + ".useJavacMode";
	private static final String WORKING_DIR = AJC + ".workingDir";
	private static final String PREPROCESS_MODE = AJC + ".preprocessMode";
	private static final String CHARACTER_ENCODING = AJC + ".characterEncoding";
	private static final String SOURCE_ONE_POINT_FOUR_MODE = AJC + ".sourceOnePointFourMode";
	private static final String LENIENT_MODE = AJC + ".lenientSpecMode";
	private static final String STRICT_MODE = AJC + ".strictSpecMode";
	private static final String PORTING_MODE = AJC + ".portingMode";
	private static final String VERBOSE_MODE = AJC + ".verboseMode";
	private static final String NONSTANDARD_OPTIONS = AJC + ".nonStandardOptions";
	
	public AjcBuildOptions(UserPreferencesAdapter userPreferencesAdapter) {
		this.preferencesAdapter = userPreferencesAdapter;
	}
	
	public boolean getUseJavacMode() {
		return getBooleanOptionVal(USE_JAVAC_MODE);
	}
	
	public void setUseJavacMode(boolean value) {
		setBooleanOptionVal(USE_JAVAC_MODE, value);
	}
	
	public String getWorkingOutputPath() {
		return preferencesAdapter.getProjectPreference(WORKING_DIR);
	}
	
	public void setWorkingDir(String path) {
		preferencesAdapter.setProjectPreference(WORKING_DIR, path);	
	}
	
	public boolean getPreprocessMode() {
		return getBooleanOptionVal(PREPROCESS_MODE);
	}
	
	public void setPreprocessMode(boolean value) {
		setBooleanOptionVal(PREPROCESS_MODE, value);	
	}
	
	public String getCharacterEncoding() {
		return preferencesAdapter.getProjectPreference(CHARACTER_ENCODING);	
	}
	
	public void setCharacterEncoding(String value) {
		preferencesAdapter.setProjectPreference(CHARACTER_ENCODING, value);	
	}
	
	public boolean getSourceOnePointFourMode() {
		return getBooleanOptionVal(SOURCE_ONE_POINT_FOUR_MODE);
	}
	
	public void setSourceOnePointFourMode(boolean value) {
		setBooleanOptionVal(SOURCE_ONE_POINT_FOUR_MODE, value);	
	}
	
	public boolean getLenientSpecMode() {
		return getBooleanOptionVal(LENIENT_MODE);
	}
	
	public void setLenientSpecMode(boolean value) {
		setBooleanOptionVal(LENIENT_MODE, value);	
	}
	
	public boolean getStrictSpecMode() {
		return getBooleanOptionVal(STRICT_MODE);	
	}
	
	public void setStrictSpecMode(boolean value) {
		setBooleanOptionVal(STRICT_MODE, value);
	}
	
	public boolean getPortingMode() {
		return getBooleanOptionVal(PORTING_MODE);
	}
	
	public void setPortingMode(boolean value) {
		setBooleanOptionVal(PORTING_MODE, value);
	}
	
	public boolean getVerboseMode() {
		return getBooleanOptionVal(VERBOSE_MODE);
	}
	
	public void setVerboseMode(boolean value) {
		setBooleanOptionVal(VERBOSE_MODE, value);
	}
	
	public String getNonStandardOptions() {
		return preferencesAdapter.getProjectPreference(NONSTANDARD_OPTIONS);		
	}
	
	public void setNonStandardOptions(String value) {
		preferencesAdapter.setProjectPreference(NONSTANDARD_OPTIONS, value);	
	}
	
	private boolean getBooleanOptionVal(String name) {
		if (preferencesAdapter.getProjectPreference(name) != null) {
			return preferencesAdapter.getProjectPreference(name).equals("true");
		} else {
			return false;
		}
	}
	
	private void setBooleanOptionVal(String name, boolean value) {
		if (value) {
			preferencesAdapter.setProjectPreference(name, "true");
		} else {
			preferencesAdapter.setProjectPreference(name, "false");
		}
	}
}
