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
import org.eclipse.jdt.internal.core.index.IQueryResult;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

class RemoveFolderFromIndex implements IJob {
	String folderPath;
	IPath indexedContainer;
	IndexManager manager;
	public RemoveFolderFromIndex(
		String folderPath,
		IPath indexedContainer,
		IndexManager manager) {
		this.folderPath = folderPath;
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
				monitor.enterRead(); // ask permission to read
				IQueryResult[] results = index.queryInDocumentNames(this.folderPath); // all file names beonlonging to the folder or its subfolders
				for (int i = 0, max = results == null ? 0 : results.length; i < max; i++) {
					String fileName = results[i].getPath();
					manager.remove(fileName, this.indexedContainer); // write lock will be acquired by the remove operation
				}
			} finally {
				monitor.exitRead(); // free read lock
			}
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to remove " + this.folderPath + " from index because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return FAILED;
		}
		return COMPLETE;
	}
	public String toString() {
		return "removing from index " + this.folderPath; //$NON-NLS-1$
	}
	/*
	 * @see IJob#cancel()
	 */
	public void cancel() {
	}

}
