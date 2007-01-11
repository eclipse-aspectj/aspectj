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
 *     Helen Hawkins  converted to new interface (pr148190) 
 * ******************************************************************/


package org.aspectj.ajde.core.internal;

import org.aspectj.ajde.core.IBuildProgressMonitor;
import org.aspectj.bridge.IProgressListener;

/**
 * Enables the compiler/weaver progres to be related to the user via the 
 * IBuildProgressMonitor as well as relating whether or not the user has 
 * cancelled the build progress back to the compiler/weaver.
 */
public class AjdeCoreBuildNotifierAdapter implements IProgressListener {

    private IBuildProgressMonitor progressMonitor;

	public AjdeCoreBuildNotifierAdapter(IBuildProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	public void setProgress(double percentDone) {
		progressMonitor.setProgress(percentDone);
	}

	public void setText(String text) {
		progressMonitor.setProgressText(text);
	}

	public boolean isCancelledRequested() {
		return progressMonitor.isCancelRequested();
	}

	public void setCancelledRequested(boolean cancelRequested) {
		// do nothing - since ask the progressMonitor whether
		// cancel has been requested
	}

}
