/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

class RemoveFromIndex implements IJob {
	String resourceName;
	IPath indexedContainer;
	IndexManager manager;
	public RemoveFromIndex(
		String resourceName,
		IPath indexedContainer,
		IndexManager manager) {
		this.resourceName = resourceName;
		this.indexedContainer = indexedContainer;
		this.manager = manager;
	}
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(indexedContainer.segment(0));
	}
	public boolean execute(IProgressMonitor progressMonitor) {
		
		if (progressMonitor != null && progressMonitor.isCanceled()) return COMPLETE;
		
		try {
			IIndex index = manager.getIndex(this.indexedContainer, true /*reuse index file*/, true /*create if none*/);
			if (index == null)
				return COMPLETE;

			/* ensure no concurrent write access to index */
			ReadWriteMonitor monitor = manager.getMonitorFor(index);
			if (monitor == null)
				return COMPLETE; // index got deleted since acquired
			try {
				monitor.enterWrite(); // ask permission to write
				index.remove(resourceName);
			} finally {
				monitor.exitWrite(); // free write lock
			}
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to remove " + this.resourceName + " from index because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return FAILED;
		}
		return COMPLETE;
	}
	public String toString() {
		return "removing from index " + resourceName; //$NON-NLS-1$
	}
	/*
	 * @see IJob#cancel()
	 */
	public void cancel() {
	}

}