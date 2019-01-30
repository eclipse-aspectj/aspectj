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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.core.IBuildProgressMonitor;

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
	private List messagesReceived = new ArrayList();
	private int currentVal;
	private boolean isCancelRequested = false;
		
	public void finish(boolean wasFullBuild) {
		System.out.println("build finished. Was full build: " + wasFullBuild);
	}

	public boolean isCancelRequested() {
		return isCancelRequested;
	}

	public void setProgress(double percentDone) {
		System.out.println("progress. Completed " + percentDone + " percent");
	}

	public void setProgressText(String text) {
		System.out.println("progress text: " + text);
		String newText = text+" [Percentage="+currentVal+"%]";
		messagesReceived.add(newText);
		if (text.startsWith("woven aspect ")) numWovenAspectMessages++;
		if (text.startsWith("woven class ")) numWovenClassMessages++;
		if (text.startsWith("compiled:")) numCompiledMessages++;
		if (programmableString != null
			&& text.indexOf(programmableString) != -1) {
			count--;
			if (count==0) {
				if (debugTests) System.out.println("Just got message '"+newText+"' - asking build to cancel");
				isCancelRequested = true;
				programmableString = null;
			}
		}
	}

	public void begin() {
		System.out.println("build started");
		currentVal = 0;
	}

	// ------------- methods to help with testing -------------
	public void cancelOn(String string,int count) {
		programmableString = string;
		this.count = count;
	}
	
	public boolean containsMessage(String prefix,String distinguishingMarks) {
		for (Iterator iter = messagesReceived.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			if (element.startsWith(prefix) &&
			    element.indexOf(distinguishingMarks)!=-1) return true;
		}
		return false;
	}
	
	public void dumpMessages() {
		System.out.println("ProgressMonitorMessages");
		for (Iterator iter = messagesReceived.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			System.out.println(element);
		}
	}

}
