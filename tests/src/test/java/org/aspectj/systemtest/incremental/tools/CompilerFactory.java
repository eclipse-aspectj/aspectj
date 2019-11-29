/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import org.aspectj.ajde.core.AjCompiler;

/**
 * Manages the different compilers for the different projects within one test run
 */
public class CompilerFactory {

	private static Map<String,AjCompiler> compilerMap = new Hashtable<>();
	
	/**
	 * If an AjCompiler exists for the given projectDir then returns
	 * that, otherwise creates a new one.
	 */
	public static AjCompiler getCompilerForProjectWithDir(String projectDir) {
		if (compilerMap.containsKey(projectDir)) {
			return (AjCompiler) compilerMap.get(projectDir);
		}
		
		AjCompiler compiler = new AjCompiler(
				projectDir,
				new MultiProjTestCompilerConfiguration(projectDir),
				new MultiProjTestBuildProgressMonitor(),
				new MultiProjTestMessageHandler());
		compilerMap.put(projectDir,compiler);
		return compiler;
	}
	
	/**
	 * Clears the current map - before doing so clears the state of 
	 * each compiler (this ensures everything is cleaned up in the
	 * IncrementalStateManager)
	 */
	public static void clearCompilerMap() {
		Collection<AjCompiler> compilers = compilerMap.values();
		for (AjCompiler compiler: compilers) {
			compiler.clearLastState();
		}
		compilerMap.clear();
	}
	
}
