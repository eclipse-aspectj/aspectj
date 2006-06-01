/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde;


public class TestBuildListener implements BuildListener {
	
	public boolean buildFinished = false;
	public boolean buildSucceeded = false;

	public void reset() { 
		buildFinished = false;
	}
	
	public void compileStarted(String buildConfigFile) { }
	
    public void compileFinished(String buildConfigFile, int buildTime, boolean succeeded, boolean warnings) {
//        int timeInSeconds = buildTime/1000;
        buildSucceeded = succeeded;
        buildFinished = true;
    } 
    
    public void compileAborted(String buildConfigFile, String message) { }
    
    public boolean getBuildFinished() {
    	return buildFinished;
    }
    
    public boolean getBuildSucceeded() { 
    	return buildSucceeded;	
    }
}


