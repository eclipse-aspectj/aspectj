/********************************************************************
 * Copyright (c) 2006 Contributors.All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 *   Helen Hawkins          converted to new interface (bug 148190)
 *   
 *******************************************************************/
package org.aspectj.ajde.core.internal;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.ajdt.internal.compiler.CompilationResultDestinationManager;

/**
 * Enables the output locations detailed by the IOutputLocationManager implementation to be related to the comipler/weaver.
 */
public class OutputLocationAdapter implements CompilationResultDestinationManager {

	private IOutputLocationManager locationManager;

	public OutputLocationAdapter(IOutputLocationManager mgr) {
		this.locationManager = mgr;
	}

	public File getOutputLocationForClass(File compilationUnit) {
		return this.locationManager.getOutputLocationForClass(compilationUnit);
	}

	public String getSourceFolderForFile(File sourceFile) {
		return this.locationManager.getSourceFolderForFile(sourceFile);
	}

	public File getOutputLocationForResource(File resource) {
		return this.locationManager.getOutputLocationForResource(resource);
	}

	public List<File> getAllOutputLocations() {
		return this.locationManager.getAllOutputLocations();
	}

	public File getDefaultOutputLocation() {
		return this.locationManager.getDefaultOutputLocation();
	}

	public void reportFileWrite(String outputfile, int filetype) {
		this.locationManager.reportFileWrite(outputfile, filetype);
	}

	public void reportFileRemove(String outputfile, int filetype) {
		this.locationManager.reportFileRemove(outputfile, filetype);
	}

	public int discoverChangesSince(File dir, long buildtime) {
		return this.locationManager.discoverChangesSince(dir,buildtime);
	}

	/**
	 * Return a map from fully qualified jar/dir entries to handle components.
	 * 
	 * @return a map from inpath entries (jars/dirs) to handle components.
	 */
	public Map<File,String> getInpathMap() {
		return this.locationManager.getInpathMap();
	}

}
