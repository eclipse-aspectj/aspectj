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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.core.builder.BuildNotifier;

public class BuildNotifierAdapter extends BuildNotifier {

    private BuildProgressMonitor progressMonitor;
    private int numCompilationUnitPasses = 1;
    private int completedPasses = 0;
	private boolean cancelled = false;

	// ??? get rid of project coupling
	public BuildNotifierAdapter(IProject project, BuildProgressMonitor progressMonitor, int numFiles) {
		super(null, project);
		this.progressMonitor = progressMonitor;
		this.numCompilationUnitPasses = numFiles*2;
	}
  
	public void begin() {
		progressMonitor.start(Ajde.getDefault().getConfigurationManager().getActiveConfigFile());
		progressMonitor.setProgressText("starting build...");
	}

	public void cancelBuild() {
		progressMonitor.setProgressText("cancelling build...");  
		cancelled = true;
	}

	public void compiled(ICompilationUnit unit) {
		completedPasses++;
		float val = (float)completedPasses/numCompilationUnitPasses;
		int intVal = (int)((float)val*100);
		progressMonitor.setProgressBarVal(intVal);
		progressMonitor.setProgressText("compiled: " + new String(unit.getFileName()));
	}

	public void generatedBytecode(String message) {
		completedPasses++;
		float val = (float)completedPasses/numCompilationUnitPasses;
		int intVal = (int)((float)val*100);
		progressMonitor.setProgressBarVal(intVal);
		progressMonitor.setProgressText(message);			
	}
  
	public void checkCancel() {
		if (cancelled) throw new OperationCanceledException();
	}

}
