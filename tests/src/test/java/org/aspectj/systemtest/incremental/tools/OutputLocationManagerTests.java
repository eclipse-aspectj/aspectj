/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *    Adrian Colyer      initial implementation
 *    Helen Hawkins      Converted to new interface (bug 148190)
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.ajde.core.IOutputLocationManager;

/**
 * Test the OutputLocationManager support used to enable multiple output folders. These aren't true "multi-project incremental"
 * tests, but that superclass has some handy methods over and above AjdeInteractionTestCase that I want to use.
 */
public class OutputLocationManagerTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

	private static final String PROJECT_NAME = "MultipleOutputFolders";
	private MyOutputLocationManager outputLocationManager;

	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject(PROJECT_NAME);
		this.outputLocationManager = new MyOutputLocationManager(new File(getFile(PROJECT_NAME, "")));
		configureOutputLocationManager(PROJECT_NAME, this.outputLocationManager);
	}

	public void testDefaultOutputLocationUsedWhenNoOutputLocationManager() {
		configureOutputLocationManager(PROJECT_NAME, null);
		build(PROJECT_NAME);
		assertFileExists(PROJECT_NAME, "bin/a/A.class");
		assertFileExists(PROJECT_NAME, "bin/b/B.class");
	}

	public void testTwoSourceRootsWithSeparateOutputLocations() {
		build(PROJECT_NAME);
		assertFileExists(PROJECT_NAME, "target/main/classes/a/A.class");
		assertFileExists(PROJECT_NAME, "target/test/classes/b/B.class");
	}

	public void testResourceCopying() {
		Map<String,File> resourceMap = new HashMap<>();
		resourceMap.put("resourceOne.txt", new File(getFile(PROJECT_NAME, "srcRootOne/resourceOne.txt")));
		resourceMap.put("resourceTwo.txt", new File(getFile(PROJECT_NAME, "srcRootTwo/resourceTwo.txt")));
		configureResourceMap(PROJECT_NAME, resourceMap);
		build(PROJECT_NAME);
		assertFileExists(PROJECT_NAME, "target/main/classes/resourceOne.txt");
		assertFileExists(PROJECT_NAME, "target/test/classes/resourceTwo.txt");
	}

	public void testGeneratedClassesPlacedInAppropriateOutputFolder() {
		configureNonStandardCompileOptions(PROJECT_NAME, "-XnoInline");
		build(PROJECT_NAME);
		assertFileExists(PROJECT_NAME, "target/main/classes/a/A.class");
		assertFileExists(PROJECT_NAME, "target/main/classes/a/A$AjcClosure1.class");
	}

	/**
	 * Tests the case when we have two aspects, each of which are sent to a different output location. There should be an aop.xml
	 * file in each of the two output directories.
	 */
	public void testOutXmlForAspectsWithDifferentOutputDirs() {
		configureNonStandardCompileOptions(PROJECT_NAME, "-outxml");
		build(PROJECT_NAME);
		assertFileExists(PROJECT_NAME, "target/main/classes/META-INF/aop-ajc.xml");
		assertFileExists(PROJECT_NAME, "target/test/classes/META-INF/aop-ajc.xml");
		// aop.xml file should exist even if there aren't any aspects (mirrors
		// what happens when there's one output dir)
		checkXMLAspectCount(PROJECT_NAME, "", 0, getFile(PROJECT_NAME, "target/anotherTest/classes"));
		// add aspects to the srcRootThree src dir and they should appear in the
		// corresponding aop.xml file
		alter(PROJECT_NAME, "inc1");
		build(PROJECT_NAME);
		checkXMLAspectCount(PROJECT_NAME, "c.C$AnAspect", 1, getFile(PROJECT_NAME, "target/anotherTest/classes"));
	}

	protected void assertFileExists(String project, String relativePath) {
		assertTrue("file " + relativePath + " should have been created as a result of building " + project, new File(getFile(
				project, relativePath)).exists());
	}

	private static class MyOutputLocationManager implements IOutputLocationManager {

		private File projectHome;
		private List<File> allOutputDirs;

		public MyOutputLocationManager(File projectHome) {
			this.projectHome = projectHome;

		}

		public void reportFileWrite(String outputfile, int filetype) {
		}

		public void reportFileRemove(String outputfile, int filetype) {
		}
		
		public Map<File,String> getInpathMap() {
			return Collections.emptyMap();
		}


		public File getOutputLocationForClass(File compilationUnit) {
			String relativePath = "";
			String compilationUnitName = compilationUnit.getAbsolutePath();
			if (compilationUnitName.contains("srcRootOne")) {
				relativePath = "target/main/classes";
			} else if (compilationUnitName.contains("srcRootTwo")) {
				relativePath = "target/test/classes";
			} else if (compilationUnitName.contains("srcRootThree")) {
				relativePath = "target/anotherTest/classes";
			}
			File ret = new File(projectHome, relativePath);
			if (!ret.exists()) {
				ret.mkdirs();
			}
			return ret;
		}

		public File getOutputLocationForResource(File resource) {
			return getOutputLocationForClass(resource);
		}

		public List<File> getAllOutputLocations() {
			if (allOutputDirs == null) {
				allOutputDirs = new ArrayList<>();
				allOutputDirs.add(new File(projectHome, "target/main/classes"));
				allOutputDirs.add(new File(projectHome, "target/test/classes"));
				allOutputDirs.add(new File(projectHome, "target/anotherTest/classes"));
			}
			return allOutputDirs;
		}

		public File getDefaultOutputLocation() {
			return new File(projectHome, "target/main/classes");
		}

		public String getSourceFolderForFile(File sourceFile) {
			return null;
		}

		public int discoverChangesSince(File dir, long buildtime) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	public void reportFileWrite(String outputfile, int filetype) {
	}

	public void reportFileRemove(String outputfile, int filetype) {
	}

}
