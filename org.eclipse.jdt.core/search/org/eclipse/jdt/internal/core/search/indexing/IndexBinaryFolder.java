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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.IQueryResult;
import org.eclipse.jdt.internal.core.index.impl.IFileDocument;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

public class IndexBinaryFolder extends IndexRequest {
	IFolder folder;
	IndexManager manager;
	IProject project;
	public IndexBinaryFolder(
		IFolder folder,
		IndexManager manager,
		IProject project) {
		this.folder = folder;
		this.manager = manager;
		this.project = project;
	}
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(this.project.getName());
	}
public boolean equals(Object o) {
	if (!(o instanceof IndexBinaryFolder)) return false;
	return this.folder.equals(((IndexBinaryFolder)o).folder);
}
public int hashCode() {
	return this.folder.hashCode();
}
	/**
	 * Ensure consistency of a folder index. Need to walk all nested resources,
	 * and discover resources which have either been changed, added or deleted
	 * since the index was produced.
	 */
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled()) return COMPLETE;

		if (!this.folder.isAccessible())
			return COMPLETE; // nothing to do

		IIndex index = manager.getIndex(this.folder.getFullPath(), true /*reuse index file*/, true /*create if none*/);
		if (index == null)
			return COMPLETE;
		ReadWriteMonitor monitor = manager.getMonitorFor(index);
		if (monitor == null)
			return COMPLETE; // index got deleted since acquired
		try {
			monitor.enterRead(); // ask permission to read

			/* if index has changed, commit these before querying */
			if (index.hasChanged()) {
				try {
					monitor.exitRead(); // free read lock
					monitor.enterWrite(); // ask permission to write
					if (IndexManager.VERBOSE)
						JobManager.verbose("-> merging index " + index.getIndexFile()); //$NON-NLS-1$
					index.save();
				} catch (IOException e) {
					return FAILED;
				} finally {
					monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
				}
			}
			final String OK = "OK"; //$NON-NLS-1$
			final String DELETED = "DELETED"; //$NON-NLS-1$
			final long indexLastModified = index.getIndexFile().lastModified();

			final Hashtable indexedFileNames = new Hashtable(100);
			IQueryResult[] results = index.queryInDocumentNames("");// all file names //$NON-NLS-1$
			for (int i = 0, max = results == null ? 0 : results.length; i < max; i++) {
				String fileName = results[i].getPath();
				indexedFileNames.put(fileName, DELETED);
			}
			this.folder.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					if (isCancelled) return false;
					if (resource.getType() == IResource.FILE) {
						String extension = resource.getFileExtension();
						if ((extension != null)
							&& extension.equalsIgnoreCase("class")) { //$NON-NLS-1$
							IPath path = resource.getLocation();
							if (path != null) {
								File resourceFile = path.toFile();
								String name = new IFileDocument((IFile) resource).getName();
								if (indexedFileNames.get(name) == null) {
									indexedFileNames.put(name, resource);
								} else {
									indexedFileNames.put(
										name,
										resourceFile.lastModified() > indexLastModified
											? (Object) resource
											: (Object) OK);
								}
							}
						}
						return false;
					}
					return true;
				}
			});
			Enumeration names = indexedFileNames.keys();
			while (names.hasMoreElements()) {
				if (this.isCancelled) return FAILED;
				
				String name = (String) names.nextElement();
				Object value = indexedFileNames.get(name);
				if (value instanceof IFile) {
					manager.addBinary((IFile) value, this.folder.getFullPath());
				} else if (value == DELETED) {
					manager.remove(name, this.project.getFullPath());
				}
			}
		} catch (CoreException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.folder + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			manager.removeIndex(this.folder.getFullPath());
			return FAILED;
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.folder + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			manager.removeIndex(this.folder.getFullPath());
			return FAILED;
		} finally {
			monitor.exitRead(); // free read lock
		}
		return COMPLETE;
	}
	public String toString() {
		return "indexing binary folder " + this.folder.getFullPath(); //$NON-NLS-1$
	}
}