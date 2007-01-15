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
package org.aspectj.ajde.ui.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajde.core.IOutputLocationManager;

/**
 * Test implementation of IOutputLocationManager. By default returns the
 * same location for both resources and classes, however, setter methods
 * enable the user to specify different location for these. Note that the
 * user is unable to specify different output location for different class
 * files.
 */
public class TestOutputLocationManager implements IOutputLocationManager {

	private String testProjectOutputPath;
	private File classOutputLoc;
	private File resourceOutputLoc;
	private List allOutputLocations;
	
	public TestOutputLocationManager(String testProjectPath) {
		this.testProjectOutputPath = testProjectPath + File.separator + "bin";
	}
	
	public String getUniqueIdentifier() {
		return testProjectOutputPath;
	}

	public File getOutputLocationForClass(File compilationUnit) {
		initLocations();
		return classOutputLoc;
	}

	public File getOutputLocationForResource(File resource) {
		initLocations();
		return resourceOutputLoc;
	}
	
	// -------------- setter methods useful for testing -------------
	public void setOutputLocForClass(File f) {
		classOutputLoc = f;
	}
	
	public void setOutputLocForResource(File f) {
		resourceOutputLoc = f;
	}

	public List getAllOutputLocations() {
		if(allOutputLocations == null) {
			allOutputLocations = new ArrayList();
			initLocations();
			allOutputLocations.add(classOutputLoc);
			if (!classOutputLoc.equals(resourceOutputLoc)) {
				allOutputLocations.add(resourceOutputLoc);
			}
		}
		return allOutputLocations;
	}

	public File getDefaultOutputLocation() {
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
	

}
