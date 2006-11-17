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
 *     AMC 01.20.2003 extended to support AspectJ 1.1 options,
 * 					  bugzilla #29769
 * ******************************************************************/

 

package org.aspectj.ajde.ui.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.ajde.BuildOptionsAdapter;
import org.aspectj.ajde.ui.UserPreferencesAdapter;

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
	// 1.1 constants added by AMC
    private static final String INCREMENTAL_MODE = AJC + ".incrementalMode";
    private static final String COMPLIANCE_LEVEL = AJC + ".complianceLevel";
	private static final String SOURCE_LEVEL = AJC + ".sourceLevel";
	private static final String WARNINGS = AJC + ".warnings";
	private static final String DEBUG_OPTIONS = AJC + ".debugOptions";
	private static final String NO_IMPORT_ERROR = AJC + ".noImportError";
	private static final String PRESERVE_LOCALS = AJC + ".preserveLocals";
	private static final String DEFAULT = "default";
	
	public AjcBuildOptions(UserPreferencesAdapter userPreferencesAdapter) {
		this.preferencesAdapter = userPreferencesAdapter;
		
		
	}

	/** @deprecated */	
	public boolean getUseJavacMode() {
		return getBooleanOptionVal(USE_JAVAC_MODE);
	}
	
	/** @deprecated */
	public void setUseJavacMode(boolean value) {
		setBooleanOptionVal(USE_JAVAC_MODE, value);
	}
	
	/** @deprecated */
	public String getWorkingOutputPath() {
		return preferencesAdapter.getProjectPreference(WORKING_DIR);
	}
	
	/** @deprecated */
	public void setWorkingDir(String path) {
		preferencesAdapter.setProjectPreference(WORKING_DIR, path);	
	}
	
	/** @deprecated */
	public boolean getPreprocessMode() {
		return getBooleanOptionVal(PREPROCESS_MODE);
	}
	
	/** @deprecated */
	public void setPreprocessMode(boolean value) {
		setBooleanOptionVal(PREPROCESS_MODE, value);	
	}
	
	public String getCharacterEncoding() {
		return preferencesAdapter.getProjectPreference(CHARACTER_ENCODING);	
	}
	
	public void setCharacterEncoding(String value) {
		preferencesAdapter.setProjectPreference(CHARACTER_ENCODING, value);	
	}
	
	/** @deprecated */
	public boolean getSourceOnePointFourMode() {
		return getBooleanOptionVal(SOURCE_ONE_POINT_FOUR_MODE);
	}
	
	/** @deprecated */
	public void setSourceOnePointFourMode(boolean value) {
		setBooleanOptionVal(SOURCE_ONE_POINT_FOUR_MODE, value);	
	}
	
    public boolean getIncrementalMode() {
        return getBooleanOptionVal(INCREMENTAL_MODE);
    }

    public void setIncrementalMode(boolean value) {
        setBooleanOptionVal(INCREMENTAL_MODE, value);
    }

	/** @deprecated */
	public boolean getLenientSpecMode() {
		return getBooleanOptionVal(LENIENT_MODE);
	}
	
	/** @deprecated */
	public void setLenientSpecMode(boolean value) {
		setBooleanOptionVal(LENIENT_MODE, value);	
	}
	
	/** @deprecated */
	public boolean getStrictSpecMode() {
		return getBooleanOptionVal(STRICT_MODE);	
	}
	
	/** @deprecated */
	public void setStrictSpecMode(boolean value) {
		setBooleanOptionVal(STRICT_MODE, value);
	}
	
	/** @deprecated */
	public boolean getPortingMode() {
		return getBooleanOptionVal(PORTING_MODE);
	}
	
	/** @deprecated */
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

	// -------------
	// new 1.1 compiler options start here...
	
	/**
	 * JDK Compliance level to be used by the compiler, either 
	 * VERSION_13, VERSION_14 or VERSION_15.
	 * From -1.3 / -1.4 / -1.5
	 */
	public String getComplianceLevel() {
		return preferencesAdapter.getProjectPreference(COMPLIANCE_LEVEL);		
	}
	
	public void setComplianceLevel( String value ) {
		preferencesAdapter.setProjectPreference(COMPLIANCE_LEVEL,value);	
	}
	
	/**
	 * Source compatibility level, either VERSION_13, VERSION_14 or VERSION_15.
	 * From -source (eclipse option)
	 */
	public String getSourceCompatibilityLevel() {
		return preferencesAdapter.getProjectPreference(SOURCE_LEVEL);		
	}

	public void setSourceCompatibilityLevel(String value) {
		preferencesAdapter.setProjectPreference(SOURCE_LEVEL, value);		
	}
	
	/**
	 * Optional warnings, empty List is equivalent to -warn:none,
	 * returning null uses eclipse compiler default settings
	 * From -warn:xxx,yyy
	 */
	public Set getWarnings() {
		String warnings = preferencesAdapter.getProjectPreference(WARNINGS);
		return toWarningSet( warnings );
	}
	
	public void setWarnings( Set warningSet ) {
		String warnings = fromWarningSet( warningSet );
		preferencesAdapter.setProjectPreference(WARNINGS, warnings); 
	}	
	
	/**
	 * Debug level. DEBUG_ALL == {SOURCE, LINES, VARS}.
	 * Empty list is equivalent to -g:none, returning
	 * non uses eclipse compiler default settings
	 * From -g:xxx
	 */
	public Set getDebugLevel() {
		String debug = preferencesAdapter.getProjectPreference(DEBUG_OPTIONS);
		return toDebugSet( debug );
	}

	public void setDebugLevel( Set debugSet ) {
		String debug = fromDebugSet( debugSet );
		preferencesAdapter.setProjectPreference(DEBUG_OPTIONS, debug); 
	}	
	
	/**
	 * No errors generated for unresolved imports
	 * From -noImportError
	 */
	public boolean getNoImportError() {
		return getBooleanOptionVal(NO_IMPORT_ERROR);
	}
	
	public void setNoImportError(boolean value) {
		setBooleanOptionVal(NO_IMPORT_ERROR, value);	
	}
	
	/**
	 * Preserve all unused local variables (for debug)
	 * From -preserveAllLocals
	 */
	public boolean getPreserveAllLocals() {
		return getBooleanOptionVal(PRESERVE_LOCALS);
	}
	
	public void setPreserveAllLocals(boolean value) {
		setBooleanOptionVal(PRESERVE_LOCALS, value);	
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
	
	private Set toWarningSet( String warnings ) {
		if ( null == warnings ) return null;
		if ( warnings.equals(DEFAULT) ) return null;
		
		Set warningSet = new HashSet();	
		StringTokenizer tok = new StringTokenizer( warnings, "," );
		while ( tok.hasMoreTokens() ) {
			String warning = tok.nextToken();
			warningSet.add( warning );	
		}
		return warningSet;
	}
	
	private String fromWarningSet( Set warningSet ) {
		if ( warningSet == null ) return DEFAULT;
		
		StringBuffer warnings = new StringBuffer();
		Iterator it = warningSet.iterator();
		while ( it.hasNext() ) {
			String w = (String) it.next();	
			if (warnings.length() > 0 ) warnings.append(',');
			warnings.append( w );
		}
		return warnings.toString();
	}

	private Set toDebugSet( String debugOptions ) {
		if ( null == debugOptions ) return null;
		if ( debugOptions.equals(DEFAULT) ) return null;
		
		Set debugSet = new HashSet();	
		StringTokenizer tok = new StringTokenizer( debugOptions, "," );
		while ( tok.hasMoreTokens() ) {
			String debug = tok.nextToken();
			debugSet.add( debug );	
		}
		return debugSet;	}
	
	private String fromDebugSet( Set debugSet ) {
		if ( debugSet == null ) return DEFAULT;
		
		StringBuffer debugOptions = new StringBuffer();
		Iterator it = debugSet.iterator();
		while ( it.hasNext() ) {
			String d = (String) it.next();	
			if (debugOptions.length() > 0 ) debugOptions.append(',');
			debugOptions.append( d );
		}
		return debugOptions.toString();
	}


	public Map getJavaOptionsMap() {
		return null;
	}

}
