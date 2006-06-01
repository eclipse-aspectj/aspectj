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
 * Implementations of this interface get told interesting information about
 * decisions made in AjState objects.  Should help us improve incremental
 * compilation, and ease the testing of incremental compilation!
 *
 * Not yet complete, will expand as we determine what extra useful information
 * should be recorded.
 * 
 * @author AndyClement
 */
public interface IStateListener {

	public void detectedClassChangeInThisDir(File f);

	public void aboutToCompareClasspaths(List oldClasspath, List newClasspath);

	public void pathChangeDetected();
	
	/**
	 * Called if state processing detects a file was deleted that contained an aspect declaration.
	 * Incremental compilation will not be attempted if this occurs.
	 */
	public void detectedAspectDeleted(File f);

	public void buildSuccessful(boolean wasFullBuild);
	
	/**
	 * When a decision is made during compilation (such as needing to recompile some new file, or drop back to batch) this
	 * method is called with the decision.
	 */
	public void recordDecision(String decision);
	
	/**
	 * Provides feedback during compilation on what stage we are at
	 */
	public void recordInformation(String info);

}
