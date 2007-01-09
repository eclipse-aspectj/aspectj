/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 * Andy Clement          initial implementation
 * ******************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.ajdt.internal.compiler.CompilationResultDestinationManager;
import org.aspectj.asm.AsmManager;


/**
 * Central point for all things incremental...
 * - keeps track of the state recorded for each different config file
 * - allows limited interaction with these states
 *
 * - records dependency/change info for particular classpaths
 *   > this will become what JDT keeps in its 'State' object when its finished
 */
public class IncrementalStateManager {

	// FIXME asc needs an API through Ajde for trashing its contents
	// FIXME asc needs some memory mgmt (softrefs?) to recover memory
	// SECRETAPI will consume more memory, so turn on at your own risk ;)  Set to 'true' when memory usage is understood
	public  static boolean recordIncrementalStates = false;
	public  static boolean debugIncrementalStates = false;
	private static Hashtable incrementalStates = new Hashtable();
	
	public static void recordSuccessfulBuild(String buildConfig, AjState state) {
		if (!recordIncrementalStates) return;
		incrementalStates.put(buildConfig,state);
	}
	
	public static boolean removeIncrementalStateInformationFor(String buildConfig) {
		return incrementalStates.remove(buildConfig)!=null;
	}
	
	public static void clearIncrementalStates() {
		for (Iterator iter = incrementalStates.values().iterator(); iter.hasNext();) {
			AjState element = (AjState) iter.next();
			element.wipeAllKnowledge();
		}
		incrementalStates.clear();
		AsmManager.getDefault().createNewASM(); // forget what you know...
	}
	
	public static Set getConfigFilesKnown() {
		return incrementalStates.keySet();
	}

	public static AjState retrieveStateFor(String configFile) {
		return (AjState)incrementalStates.get(configFile);
	}
	
	// now, managing changes to entries on a classpath
	
	public static AjState findStateManagingOutputLocation(File location) {
		Collection allStates = incrementalStates.values();
		if (debugIncrementalStates) System.err.println("> findStateManagingOutputLocation("+location+") has "+allStates.size()+" states to look through");
		for (Iterator iter = allStates.iterator(); iter.hasNext();) {
			AjState element = (AjState) iter.next();
			AjBuildConfig ajbc = element.getBuildConfig();
			if (ajbc==null) {
				// FIXME asc why can it ever be null?
				if (debugIncrementalStates) System.err.println("  No build configuration for state "+element);
				continue;
			}
			File outputDir = ajbc.getOutputDir();
			if (outputDir != null && outputDir.equals(location)) {
				if (debugIncrementalStates) System.err.println("< findStateManagingOutputLocation("+location+") returning "+element);
				return element;				
			} 
			CompilationResultDestinationManager outputManager = ajbc.getCompilationResultDestinationManager();
			if (outputManager != null) {
				List outputDirs = outputManager.getAllOutputLocations();
				for (Iterator iterator = outputDirs.iterator(); iterator
						.hasNext();) {
					File dir = (File) iterator.next();
					if (dir.equals(location)) {
						if (debugIncrementalStates) System.err.println("< findStateManagingOutputLocation("+location+") returning "+element);
						return element;				
					}
				}
			}
			if (outputDir == null && outputManager == null) {
				// FIXME why can it ever be null? due to using outjar?
				if (debugIncrementalStates) System.err.println("  output directory and output location manager for "+ajbc+" are null");
				continue;
			}

		}
		if (debugIncrementalStates) System.err.println("< findStateManagingOutputLocation("+location+") returning null");
		return null;
	}
	
	// FIXME asc needs a persistence mechanism for storing/loading all state info
	// FIXME asc needs to understand two config files might point at the same output dir... what to do about this?
}