/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde.internal;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.BuildProgressMonitor;
import org.aspectj.bridge.IProgressListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.core.builder.BuildNotifier;

public class BuildNotifierAdapter implements IProgressListener {

    private BuildProgressMonitor progressMonitor;
    private int numCompilationUnitPasses = 1;
    private int completedPasses = 0;
	private boolean cancelled = false;

	// ??? get rid of project coupling
	public BuildNotifierAdapter(BuildProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}
  
	public void begin() {
		progressMonitor.start(Ajde.getDefault().getConfigurationManager().getActiveConfigFile());
		progressMonitor.setProgressText("starting build...");
	}

	public void cancelBuild() {
		progressMonitor.setProgressText("cancelling build...");  
		cancelled = true;
	}

	public void setProgress(double percentDone) {
		progressMonitor.setProgressBarVal((int)(percentDone * progressMonitor.getProgressBarMax()));
	}

	public void setText(String text) {
		progressMonitor.setProgressText(text);
	}

}
