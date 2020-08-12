/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.ajde.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test implementation of IOutputLocationManager. By default returns the same location for both resources and classes, however,
 * setter methods enable the user to specify different location for these. Note that the user is unable to specify different output
 * location for different class files.
 */
public class TestOutputLocationManager implements IOutputLocationManager {

	private String testProjectOutputPath;
	private File classOutputLoc;
	private File resourceOutputLoc;
	private List<File> allOutputLocations;
	private Map<File,String> inpathMap = Collections.emptyMap();

	public TestOutputLocationManager(String testProjectPath) {
		this.testProjectOutputPath = testProjectPath + File.separator + "bin";
	}

	public TestOutputLocationManager(String string, Map<File,String> inpathMap) {
		this(string);
		this.inpathMap = inpathMap;
	}

	@Override
	public File getOutputLocationForClass(File compilationUnit) {
		initLocations();
		return classOutputLoc;
	}

	@Override
	public File getOutputLocationForResource(File resource) {
		initLocations();
		return resourceOutputLoc;
	}

	@Override
	public Map<File,String> getInpathMap() {
		return inpathMap;
	}

	// -------------- setter methods useful for testing -------------
	public void setOutputLocForClass(File f) {
		classOutputLoc = f;
	}

	public void setOutputLocForResource(File f) {
		resourceOutputLoc = f;
	}

	@Override
	public List<File> getAllOutputLocations() {
		if (allOutputLocations == null) {
			allOutputLocations = new ArrayList<>();
			initLocations();
			allOutputLocations.add(classOutputLoc);
			if (!classOutputLoc.equals(resourceOutputLoc)) {
				allOutputLocations.add(resourceOutputLoc);
			}
		}
		return allOutputLocations;
	}

	@Override
	public File getDefaultOutputLocation() {
		initLocations();
		return classOutputLoc;
	}

	private void initLocations() {
		if (classOutputLoc == null) {
			classOutputLoc = new File(testProjectOutputPath);
		}
		if (resourceOutputLoc == null) {
			resourceOutputLoc = new File(testProjectOutputPath);
		}
	}

	@Override
	public String getSourceFolderForFile(File sourceFile) {
		return null;
	}

	@Override
	public void reportFileWrite(String outputfile, int filetype) {
	}

	@Override
	public void reportFileRemove(String outputfile, int filetype) {
	}

	@Override
	public int discoverChangesSince(File dir, long buildtime) {
		// TODO Auto-generated method stub
		return 0;
	}

}
