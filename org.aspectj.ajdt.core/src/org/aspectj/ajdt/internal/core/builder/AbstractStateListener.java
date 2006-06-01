/**
 * Copyright (c) 2005 IBM and other contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.util.List;

/**
 * Subtypes can override whatever they want...
 * 
 * @author AndyClement
 *
 */
public abstract class AbstractStateListener implements IStateListener {

	public void detectedClassChangeInThisDir(File f) {	}

	public void aboutToCompareClasspaths(List oldClasspath, List newClasspath) {	}

	public void pathChangeDetected() {	}

	public void detectedAspectDeleted(File f) {	}

	public void buildSuccessful(boolean wasFullBuild) {	}
	
	public void recordDecision(String decision) {}
	
	public void recordInformation(String info) {}

}
