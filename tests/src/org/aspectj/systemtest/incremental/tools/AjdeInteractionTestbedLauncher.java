/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 *   Helen Hawkins          Converted to new interface (bug 148190)
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;


/**
 * command-line launcher for Ajde-like aspectj runs for use with
 * profiling tools.
 */
public class AjdeInteractionTestbedLauncher extends
		MultiProjectIncrementalTests {
	
	/**
	 * usage: AjdeInteractionTestbedLauncher srcDir projectName 
	 * @param args workspace_root_dir project_name
	 */
	public static void main(String[] args) throws Exception {
		AjdeInteractionTestbedLauncher.testdataSrcDir = args[0];
		AjdeInteractionTestbedLauncher launcher = new AjdeInteractionTestbedLauncher(args[1]);
		launcher.setUp();
		launcher.buildProject(args[1]);
		//launcher.printBuildReport();
		launcher.tearDown();
	}
	
	public AjdeInteractionTestbedLauncher(String projectName) {
		String classPath = System.getProperty("java.class.path");
		((MultiProjTestCompilerConfiguration)getCompilerForProjectWithName(projectName)
				.getCompilerConfiguration()).setClasspath(classPath);
	}
	
	private void buildProject(String projectName) {
		initialiseProject(projectName);
		build(projectName);
	}
	
	
}
