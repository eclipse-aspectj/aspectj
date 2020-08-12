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

import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajde.core.IBuildProgressMonitor;

/**
 * IBuildProgressMonitor that records how many files were compiled and woven as well as whether or not the build was a full build.
 * Will print progress information to the screen if VERBOSE is true.
 */
public class MultiProjTestBuildProgressMonitor implements IBuildProgressMonitor {

	public boolean VERBOSE = false;

	private List<String> compiledFiles = new ArrayList<>();
	private List<String> wovenClasses = new ArrayList<>();

	private long starttime = 0;
	private long totaltimetaken = 0;
	private boolean wasFullBuild = true;

	public void finish(boolean wasFullBuild) {
		log("IBuildProgressMonitor.finish(" + wasFullBuild + ")");
		this.wasFullBuild = wasFullBuild;
		totaltimetaken = (System.currentTimeMillis() - starttime);
	}

	public boolean isCancelRequested() {
		log("IBuildProgressMonitor.isCancelRequested()");
		return false;
	}

	public void setProgress(double percentDone) {
		log("IBuildProgressMonitor.setProgress(" + percentDone + ")");
	}

	public void setProgressText(String text) {
		log("BuildProgressMonitor.setProgressText(" + text + ")");
		if (text.startsWith("compiled: ")) {
			compiledFiles.add(text.substring(10));
		} else if (text.startsWith("woven class ")) {
			wovenClasses.add(text.substring(12));
		} else if (text.startsWith("woven aspect ")) {
			wovenClasses.add(text.substring(13));
		}
	}

	public void begin() {
		starttime = System.currentTimeMillis();
		log("IBuildProgressMonitor.start()");
	}

	public List<String> getCompiledFiles() {
		return compiledFiles;
	}

	public List<String> getWovenClasses() {
		return wovenClasses;
	}

	public void log(String s) {
		if (VERBOSE) {
			System.out.println(s);
		}
	}

	public long getTimeTaken() {
		return totaltimetaken;
	}

	public boolean wasFullBuild() {
		return wasFullBuild;
	}

	public void reset() {
		wasFullBuild = true;
		compiledFiles.clear();
		wovenClasses.clear();
	}
}
