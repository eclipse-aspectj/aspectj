/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import org.aspectj.bridge.IProgressListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.aspectj.org.eclipse.jdt.internal.core.builder.BuildNotifier;

/**
 * @author colyer
 *
 * Build progress notification inside Eclipse
 */
public class AjBuildNotifier extends BuildNotifier implements IProgressListener {
	
	/**
	 * @param monitor
	 * @param project
	 */
	public AjBuildNotifier(IProgressMonitor monitor, IProject project) {
		super(monitor, project);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.bridge.IProgressListener#setText(java.lang.String)
	 */
	public void setText(String text) {
		subTask(text);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.bridge.IProgressListener#setProgress(double)
	 */
	public void setProgress(double percentDone) {
		updateProgress((float)(percentDone/100.0f));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.bridge.IProgressListener#setCancelledRequested(boolean)
	 */
	public void setCancelledRequested(boolean cancelRequested) {
		// no-op
	}

	/* (non-Javadoc)
	 * @see org.aspectj.bridge.IProgressListener#isCancelledRequested()
	 */
	public boolean isCancelledRequested() {
		// can't delegate to super methods as they throw exception, which is not what we want inside weaver
		boolean cancelRequested = cancelling;
		if (monitor != null) {
			cancelRequested = cancelRequested || monitor.isCanceled();
		}
		return cancelRequested;
	}
}
