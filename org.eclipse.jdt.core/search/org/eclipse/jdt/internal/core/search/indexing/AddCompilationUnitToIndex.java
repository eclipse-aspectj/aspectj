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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.impl.IFileDocument;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

class AddCompilationUnitToIndex extends IndexRequest {
	IFile resource;
	IndexManager manager;
	IPath indexedContainer;
	char[] contents;
	public AddCompilationUnitToIndex(
		IFile resource,
		IndexManager manager,
		IPath indexedContainer) {
		this.resource = resource;
		this.manager = manager;
		this.indexedContainer = indexedContainer;
	}
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(this.indexedContainer.segment(0));
	}
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled()) return COMPLETE;
		try {
			IIndex index = manager.getIndex(this.indexedContainer, true /*reuse index file*/, true /*create if none*/);

			/* ensure no concurrent write access to index */
			if (index == null)
				return COMPLETE;
			ReadWriteMonitor monitor = manager.getMonitorFor(index);
			if (monitor == null)
				return COMPLETE; // index got deleted since acquired
			try {
				monitor.enterWrite(); // ask permission to write
				char[] contents = this.getContents();
				if (contents == null)
					return FAILED;
				index.add(new IFileDocument(resource, contents), new SourceIndexer());
			} finally {
				monitor.exitWrite(); // free write lock
			}
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.resource + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return FAILED;
		}
		return COMPLETE;
	}
	private char[] getContents() {
		if (this.contents == null)
			this.initializeContents();
		return contents;
	}
	public void initializeContents() {

		try {
			IPath location = resource.getLocation();
			if (location != null) {
				this.contents =
					org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(
						location.toFile(), null);
			}
		} catch (IOException e) {
		}
	}
	public String toString() {
		return "indexing " + resource.getFullPath(); //$NON-NLS-1$
	}
}