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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.IQueryResult;
import org.eclipse.jdt.internal.core.index.impl.JarFileEntryDocument;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

class AddJarFileToIndex extends IndexRequest {

	IndexManager manager;
	String projectName;
	IFile resource;
	private String toString;
	IPath path;

	public AddJarFileToIndex(
		IFile resource,
		IndexManager manager,
		String projectName) {
		this.resource = resource;
		this.path = resource.getFullPath();
		this.manager = manager;
		this.projectName = projectName;
	}
	// can be found either by project name or JAR path name
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(projectName) || this.path.toString().equals(jobFamily);
	}
public boolean equals(Object o) {
	if (!(o instanceof AddJarFileToIndex)) return false;
	if (this.resource != null) {
		return this.resource.equals(((AddJarFileToIndex)o).resource);
	}
	if (this.path != null) {
		return this.path.equals(((AddJarFileToIndex)o).path);
	}
	return false;
}
public int hashCode() {
	if (this.resource != null) {
		return this.resource.hashCode();
	}
	if (this.path != null) {
		return this.path.hashCode();
	}
	return -1;
}	
	public boolean execute(IProgressMonitor progressMonitor) {
		
		if (progressMonitor != null && progressMonitor.isCanceled()) return COMPLETE;
		try {
			IPath indexedPath = this.path;
			// if index already cached, then do not perform any check
			IIndex index = (IIndex) manager.getIndex(indexedPath, false /*do not reuse index file*/, false /*do not create if none*/);
			if (index != null) {
				if (JobManager.VERBOSE) 
					JobManager.verbose("-> no indexing required (index already exists) for " + this.path); //$NON-NLS-1$
				return COMPLETE;
			}

			index = manager.getIndex(indexedPath, true /*reuse index file*/, true /*create if none*/);
			if (index == null) {
				if (JobManager.VERBOSE) 
					JobManager.verbose("-> index could not be created for " + this.path); //$NON-NLS-1$
				return COMPLETE;
			}
			ReadWriteMonitor monitor = manager.getMonitorFor(index);
			if (monitor == null) {
				if (JobManager.VERBOSE) 
					JobManager.verbose("-> index for " + this.path + " just got deleted"); //$NON-NLS-1$//$NON-NLS-2$
				return COMPLETE; // index got deleted since acquired
			}
			ZipFile zip = null;
			try {
				// this path will be a relative path to the workspace in case the zipfile in the workspace otherwise it will be a path in the
				// local file system
				Path zipFilePath = null;

				monitor.enterWrite(); // ask permission to write
				if (resource != null) {
					IPath location = this.resource.getLocation();
					if (location == null)
						return FAILED;
					zip = new ZipFile(location.toFile());
					zipFilePath = (Path) this.resource.getFullPath().makeRelative();
					// absolute path relative to the workspace
				} else {
					zip = new ZipFile(this.path.toFile());
					zipFilePath = (Path) this.path;
					// path is already canonical since coming from a library classpath entry
				}

				if (JobManager.VERBOSE)
					JobManager.verbose("-> indexing " + zip.getName()); //$NON-NLS-1$
				long initialTime = System.currentTimeMillis();

				final HashSet indexedFileNames = new HashSet(100);
				IQueryResult[] results = index.queryInDocumentNames(""); // all file names //$NON-NLS-1$
				int resultLength = results == null ? 0 : results.length;
				if (resultLength != 0) {
					/* check integrity of the existing index file
					 * if the length is equal to 0, we want to index the whole jar again
					 * If not, then we want to check that there is no missing entry, if
					 * one entry is missing then we 
					 */
					for (int i = 0; i < resultLength; i++) {
						String fileName = results[i].getPath();
						indexedFileNames.add(fileName);
					}
					boolean needToReindex = false;
					for (Enumeration e = zip.entries(); e.hasMoreElements();) {
						// iterate each entry to index it
						ZipEntry ze = (ZipEntry) e.nextElement();
						if (Util.isClassFileName(ze.getName())) {
							JarFileEntryDocument entryDocument =
								new JarFileEntryDocument(ze, null, zipFilePath);
							if (!indexedFileNames.remove(entryDocument.getName())) {
								needToReindex = true;
								break;
							}
						}
					}
					if (!needToReindex && indexedFileNames.size() == 0) {
						if (JobManager.VERBOSE)
							JobManager.verbose(
								"-> no indexing required (index is consistent with library) for " //$NON-NLS-1$
								+ zip.getName() + " (" //$NON-NLS-1$
								+ (System.currentTimeMillis() - initialTime) + "ms)"); //$NON-NLS-1$
						return COMPLETE;
					}
				}

				/*
				 * Index the jar for the first time or reindex the jar in case the previous index file has been corrupted
				 */
				if (index != null) {
					// index already existed: recreate it so that we forget about previous entries
					index = manager.recreateIndex(indexedPath);
				}
				for (Enumeration e = zip.entries(); e.hasMoreElements();) {
					if (this.isCancelled) {
						if (JobManager.VERBOSE) {
							JobManager.verbose(
								"-> indexing of " //$NON-NLS-1$
								+ zip.getName() 
								+ " has been cancelled"); //$NON-NLS-1$
						}
						return FAILED;
					}
					
					// iterate each entry to index it
					ZipEntry ze = (ZipEntry) e.nextElement();
					if (Util.isClassFileName(ze.getName())) {
						byte[] classFileBytes =
							org.eclipse.jdt.internal.compiler.util.Util.getZipEntryByteContent(ze, zip);
						// Add the name of the file to the index
						index.add(
							new JarFileEntryDocument(ze, classFileBytes, zipFilePath),
							new BinaryIndexer(true));
					}
				}
				if (JobManager.VERBOSE)
					JobManager.verbose(
						"-> done indexing of " //$NON-NLS-1$
						+ zip.getName() + " (" //$NON-NLS-1$
						+ (System.currentTimeMillis() - initialTime) + "ms)"); //$NON-NLS-1$
			} finally {
				if (zip != null)
					zip.close();
				monitor.exitWrite(); // free write lock
			}
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.path + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			manager.removeIndex(this.path);
			return FAILED;
		}
		return COMPLETE;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (10/10/00 1:27:18 PM)
	 * @return java.lang.String
	 */
	public String toString() {
		if (toString == null) {
			toString = "indexing " + this.path.toString(); //$NON-NLS-1$
		}
		return toString;
	}

	public AddJarFileToIndex(
		IPath path,
		IndexManager manager,
		String projectName) {
		// external JAR scenario - no resource
		this.path = path;
		this.manager = manager;
		this.projectName = projectName;
	}
}