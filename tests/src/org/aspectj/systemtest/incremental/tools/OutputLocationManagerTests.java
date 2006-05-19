/* *******************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 * Adrian Colyer          initial implementation
* ******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.ajde.OutputLocationManager;

/**
 * Test the OutputLocationManager support used to enable multiple output folders.
 * These aren't true "multi-project incremental" tests, but that superclass has some
 * handy methods over and above AjdeInteractionTestCase that I want to use.
 */
public class OutputLocationManagerTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

	private static final String PROJECT_NAME = "MultipleOutputFolders";
	private MyOutputLocationManager outputLocationManager;
	
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject(PROJECT_NAME);
		this.outputLocationManager = new MyOutputLocationManager(new File(getFile(PROJECT_NAME, "")));
		configureOutputLocationManager(this.outputLocationManager);
	}
	
	public void testDefaultOutputLocationUsedWhenNoOutputLocationManager() {
		configureOutputLocationManager(null);
		build(PROJECT_NAME);
		assertFileExists(PROJECT_NAME,"bin/a/A.class");
		assertFileExists(PROJECT_NAME,"bin/b/B.class");
	}
	
	public void testTwoSourceRootsWithSeparateOutputLocations() {
		build(PROJECT_NAME);
		assertFileExists(PROJECT_NAME,"target/main/classes/a/A.class");
		assertFileExists(PROJECT_NAME,"target/test/classes/b/B.class");
	}
	
	public void testResourceCopying() {
		Map resourceMap = new HashMap();
		resourceMap.put("resourceOne.txt", new File(getFile(PROJECT_NAME,"srcRootOne/resourceOne.txt")));
		resourceMap.put("resourceTwo.txt", new File(getFile(PROJECT_NAME,"srcRootTwo/resourceTwo.txt")));
		configureResourceMap(resourceMap);
		build(PROJECT_NAME);
		assertFileExists(PROJECT_NAME,"target/main/classes/resourceOne.txt");
		assertFileExists(PROJECT_NAME,"target/test/classes/resourceTwo.txt");		
	}
	
	public void testGeneratedClassesPlacedInAppropriateOutputFolder() {
		configureNonStandardCompileOptions("-XnoInline");
		build(PROJECT_NAME);
		assertFileExists(PROJECT_NAME,"target/main/classes/a/A.class");
		assertFileExists(PROJECT_NAME,"target/main/classes/a/A$AjcClosure1.class");		
	}
	
	protected void assertFileExists(String project, String relativePath) {
		assertTrue("file " + relativePath + " should have been created as a result of building " + project,
				    new File(getFile(project, relativePath)).exists());
	}
	
	private static class MyOutputLocationManager implements OutputLocationManager {
		
		private File projectHome;
		
		public MyOutputLocationManager(File projectHome) {
			this.projectHome = projectHome;
		}

		public File getOutputLocationForClass(File compilationUnit) {
			String relativePath = "";
			String compilationUnitName = compilationUnit.getAbsolutePath();
			if (compilationUnitName.indexOf("srcRootOne") != -1) {
				relativePath = "target/main/classes";
			} else if (compilationUnitName.indexOf("srcRootTwo") != -1) {
				relativePath = "target/test/classes";
			}
			File ret =  new File(projectHome,relativePath);
			if (!ret.exists()) {
				ret.mkdirs();
			}
			return ret;
		}

		public File getOutputLocationForResource(File resource) {
			return getOutputLocationForClass(resource);
		}
		
	}
}
