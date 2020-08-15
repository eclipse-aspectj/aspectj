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

import java.util.ArrayList;
import java.util.List;

/**
 * Test implementation of IBuildProgressMonitor which prints out
 * progress to the console and enables users to cancel the build process
 * after a specified string has been printed.
 */
public class TestBuildProgressMonitor implements IBuildProgressMonitor {

	private static boolean debugTests = false;

	public int numWovenClassMessages = 0;
	public int numWovenAspectMessages = 0;
	public int numCompiledMessages = 0;

	private String programmableString;
	private int count;
	private List<String> messagesReceived = new ArrayList<>();
	private int currentVal;
	private boolean isCancelRequested = false;

	public void finish(boolean wasFullBuild) {
		info("build finished. Was full build: " + wasFullBuild);
	}

	public boolean isCancelRequested() {
		return isCancelRequested;
	}

	private void info(String message) {
		if (AjdeCoreModuleTests.verbose) {
			System.out.println(message);
		}
	}

	public void setProgress(double percentDone) {
		info("progress. Completed " + percentDone + " percent");
	}

	public void setProgressText(String text) {
		info("progress text: " + text);
		String newText = text+" [Percentage="+currentVal+"%]";
		messagesReceived.add(newText);
		if (text.startsWith("woven aspect ")) {
			numWovenAspectMessages++;
		}
		if (text.startsWith("woven class ")) {
			numWovenClassMessages++;
		}
		if (text.startsWith("compiled:")) {
			numCompiledMessages++;
		}
		if (programmableString != null
				&& text.contains(programmableString)) {
			count--;
			if (count==0) {
				if (debugTests) {
					System.out.println("Just got message '"+newText+"' - asking build to cancel");
				}
				isCancelRequested = true;
				programmableString = null;
			}
		}
	}

	public void begin() {
		info("build started");
		currentVal = 0;
	}

	// ------------- methods to help with testing -------------
	public void cancelOn(String string,int count) {
		programmableString = string;
		this.count = count;
	}

	public boolean containsMessage(String prefix,String distinguishingMarks) {
		for (String element: messagesReceived) {
			if (element.startsWith(prefix) &&
					element.contains(distinguishingMarks)) {
				return true;
			}
		}
		return false;
	}

	public void dumpMessages() {
		System.out.println("ProgressMonitorMessages");
		for (String element: messagesReceived) {
			System.out.println(element);
		}
	}

}
