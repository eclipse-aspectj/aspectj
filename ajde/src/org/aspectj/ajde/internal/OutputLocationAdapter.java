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
package org.aspectj.ajde.internal;

import java.io.File;
import java.util.List;

import org.aspectj.ajde.OutputLocationManager;
import org.aspectj.ajdt.internal.compiler.CompilationResultDestinationManager;

public class OutputLocationAdapter implements CompilationResultDestinationManager {

	private OutputLocationManager locationManager;
	
	public OutputLocationAdapter(OutputLocationManager mgr) {
		this.locationManager = mgr;
	}
	
	public File getOutputLocationForClass(File compilationUnit) {
		return this.locationManager.getOutputLocationForClass(compilationUnit);
	}

	public File getOutputLocationForResource(File resource) {
		return this.locationManager.getOutputLocationForResource(resource);
	}

	public List getAllOutputLocations() {
		return this.locationManager.getAllOutputLocations();
	}

	public File getDefaultOutputLocation() {
		return this.locationManager.getDefaultOutputLocation();
	}

}
