/* *******************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
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
		//AjdeInteractionTestbed.VERBOSE = true;
		//MultiProjectIncrementalTests.VERBOSE = true;
		AjdeInteractionTestbedLauncher.testdataSrcDir = args[0];
		AjdeInteractionTestbedLauncher launcher = new AjdeInteractionTestbedLauncher();
		launcher.setUp();
		launcher.buildProject(args[1]);
		launcher.printBuildReport();
		launcher.tearDown();
	}
	
	public AjdeInteractionTestbedLauncher() {
		String classPath = System.getProperty("java.class.path");
		((MyProjectPropertiesAdapter)MyProjectPropertiesAdapter.getInstance()).setClasspath(classPath);
	}
	
	private void buildProject(String projectName) {
		configureBuildStructureModel(true);
		initialiseProject(projectName);
		build(projectName);
	}
	
	
}
