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


package org.aspectj.ajde.internal;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.BuildProgressMonitor;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.bridge.IProgressListener;

public class BuildNotifierAdapter implements IProgressListener {

    private BuildProgressMonitor progressMonitor;
//    private AjBuildManager buildManager;
//    private int numCompilationUnitPasses = 1;
//    private int completedPasses = 0;
	private boolean cancelRequested = false;

	public BuildNotifierAdapter(BuildProgressMonitor progressMonitor, AjBuildManager buildManager) {
		this.progressMonitor = progressMonitor;
//		this.buildManager = buildManager;
	}
  
	public void begin() {
		progressMonitor.start(Ajde.getDefault().getConfigurationManager().getActiveConfigFile());
		progressMonitor.setProgressText("starting build...");
	}

	public void cancelBuild() {
		progressMonitor.setProgressText("cancelling build...");  
		cancelRequested = true;
	}

	public void setProgress(double percentDone) {
		progressMonitor.setProgressBarVal((int)(percentDone * progressMonitor.getProgressBarMax()));
	}

	public void setText(String text) {
		progressMonitor.setProgressText(text);
	}

	public void setCancelledRequested(boolean cancelRequested) {
		this.cancelRequested = cancelRequested;
	}

	public boolean isCancelledRequested() {
		return cancelRequested;
	}

}
