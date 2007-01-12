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
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajde.core.IOutputLocationManager;

/**
 * An IOutputLocationManager which by default sends all output to the 
 * testProjectPath\bin directory. However, there are getter methods which
 * enable sending resources and classes to different output dirs. Doesn't
 * enable sending different classes to different output locations.
 */
public class MultiProjTestOutputLocationManager implements IOutputLocationManager {

	private String testProjectOutputPath;
	private File classOutputLoc;
	private File resourceOutputLoc;
	private List allOutputLocations;
	
	public MultiProjTestOutputLocationManager(String testProjectPath) {
		this.testProjectOutputPath = testProjectPath + File.separator + "bin";
	}

	public File getOutputLocationForClass(File compilationUnit) {
		initLocations();
		return classOutputLoc;
	}

	public File getOutputLocationForResource(File resource) {
		initLocations();
		return resourceOutputLoc;
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
	
	// -------------- setter methods useful for testing -------------
	public void setOutputLocForClass(File f) {
		classOutputLoc = f;
	}
	
	public void setOutputLocForResource(File f) {
		resourceOutputLoc = f;
	}

}
