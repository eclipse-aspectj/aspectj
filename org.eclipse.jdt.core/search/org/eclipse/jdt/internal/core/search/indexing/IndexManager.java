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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.CRC32;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DeltaProcessor;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.impl.Index;
import org.eclipse.jdt.internal.core.search.IndexSelector;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.core.search.Util;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

public class IndexManager extends JobManager implements IIndexConstants {
	/* number of file contents in memory */
	public static int MAX_FILES_IN_MEMORY = 0;
	
	public IWorkspace workspace;

	/* indexes */
	Map indexes = new HashMap(5);

	/* read write monitors */
	private Map monitors = new HashMap(5);

	/* need to save ? */
	private boolean needToSave = false;
	private static final CRC32 checksumCalculator = new CRC32();
	private IPath javaPluginLocation = null;
	
	/* projects to check consistency for */
	IProject[] projectsToCheck = null;
	
/**
 * Before processing all jobs, need to ensure that the indexes are up to date.
 */
public void activateProcessing() {
	try {
		Thread.currentThread().sleep(10000); // wait 10 seconds so as not to interfere with plugin startup
	} catch (InterruptedException ie) {
	}	
	checkIndexConsistency();
	super.activateProcessing();
}
/**
 * Trigger addition of a resource to an index
 * Note: the actual operation is performed in background
 */
public void addSource(IFile resource, IPath indexedContainer){
	if (JavaCore.getPlugin() == null) return;	
	AddCompilationUnitToIndex job = new AddCompilationUnitToIndex(resource, this, indexedContainer);
	if (this.awaitingJobsCount() < MAX_FILES_IN_MEMORY) {
		job.initializeContents();
	}
	request(job);
}
/**
 * Trigger addition of a resource to an index
 * Note: the actual operation is performed in background
 */
public void addBinary(IFile resource, IPath indexedContainer){
	if (JavaCore.getPlugin() == null) return;	
	AddClassFileToIndex job = new AddClassFileToIndex(resource, this, indexedContainer);
	if (this.awaitingJobsCount() < MAX_FILES_IN_MEMORY) {
		job.initializeContents();
	}
	request(job);
}
/**
 * Index the content of the given source folder.
 */
public void indexSourceFolder(JavaProject javaProject, IPath sourceFolder) {
	IProject project = javaProject.getProject();
	final IPath container = project.getFullPath();
	IContainer folder;
	if (container.equals(sourceFolder)) {
		folder = project;
	} else {
		folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(sourceFolder);
	}
	try {
		folder.accept(new IResourceVisitor() {
			/*
			 * @see IResourceVisitor#visit(IResource)
			 */
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile) {
					if ("java".equalsIgnoreCase(resource.getFileExtension())) {  //$NON-NLS-1$
						addSource((IFile)resource, container);
					}
					return false;
				} else {
					return true;
				}
			}
		});
	} catch (CoreException e) {
		// Folder does not exist.
		// It will be indexed only when DeltaProcessor detects its addition
	}
}
/**
 * Ensures that indexes are up to date with workbench content. Typically
 * it is invoked in background when activate the job processing.
 */
public void checkIndexConsistency() {

	if (VERBOSE) JobManager.verbose("STARTING ensuring consistency"); //$NON-NLS-1$

	boolean wasEnabled = isEnabled();	
	try {
		disable();

		IWorkspace workspace =  ResourcesPlugin.getWorkspace();
		if (workspace == null) return; // NB: workspace can be null if it has shut down (see http://dev.eclipse.org/bugs/show_bug.cgi?id=16175)
		if (this.projectsToCheck != null){
			IProject[] projects = this.projectsToCheck;
			this.projectsToCheck = null;
			for (int i = 0, max = projects.length; i < max; i++){
				IProject project = projects[i];
				// not only java project, given at startup nature may not have been set yet
				if (project.isOpen()) { 
					indexAll(project);
				}
			}
		}
	} finally {
		if (wasEnabled) enable();
		if (VERBOSE) JobManager.verbose("DONE ensuring consistency"); //$NON-NLS-1$
	}
}
private String computeIndexName(IPath path) {
	
	String pathString = path.toOSString();
	byte[] pathBytes = pathString.getBytes();
	checksumCalculator.reset();
	checksumCalculator.update(pathBytes);
	String fileName = Long.toString(checksumCalculator.getValue()) + ".index"; //$NON-NLS-1$
	if (VERBOSE) JobManager.verbose("-> index name for " + pathString + " is " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
	IPath indexPath = getJavaPluginWorkingLocation();
	String indexDirectory = indexPath.toOSString();
	if (indexDirectory.endsWith(File.separator)) {
		return indexDirectory + fileName;
	} else {
		return indexDirectory + File.separator + fileName;
	} 
}

/**
 * Returns the index for a given project, according to the following algorithm:
 * - if index is already in memory: answers this one back
 * - if (reuseExistingFile) then read it and return this index and record it in memory
 * - if (createIfMissing) then create a new empty index and record it in memory
 * 
 * Warning: Does not check whether index is consistent (not being used)
 */
public synchronized IIndex getIndex(IPath path, boolean reuseExistingFile, boolean createIfMissing) {
	// Path is already canonical per construction
	IIndex index = (IIndex) indexes.get(path);
	if (index == null) {
		try {
			String indexPath = null;
			
			// index isn't cached, consider reusing an existing index file
			if (reuseExistingFile){
				indexPath = computeIndexName(path);
				File indexFile = new File(indexPath);
				if (indexFile.exists()){ // check before creating index so as to avoid creating a new empty index if file is missing
					index = new Index(indexPath, "Index for " + path.toOSString(), true /*reuse index file*/); //$NON-NLS-1$
					if (index != null){
						indexes.put(path, index);
						monitors.put(index, new ReadWriteMonitor());
						return index;
					}
				}
			} 
			// index wasn't found on disk, consider creating an empty new one
			if (createIfMissing){
				if (indexPath == null) indexPath = computeIndexName(path);
				index = new Index(indexPath, "Index for " + path.toOSString(), false /*do not reuse index file*/); //$NON-NLS-1$
				if (index != null){
					indexes.put(path, index);
					monitors.put(index, new ReadWriteMonitor());
					return index;
				}
			}
		} catch (IOException e) {
			// The file could not be created. Possible reason: the project has been deleted.
			return null;
		}
	}
	//System.out.println(" index name: " + path.toOSString() + " <----> " + index.getIndexFile().getName());	
	return index;
}
private IPath getJavaPluginWorkingLocation() {
	if (javaPluginLocation == null) {
		javaPluginLocation = JavaCore.getPlugin().getStateLocation();
	}
	return javaPluginLocation;
}
/**
 * Index access is controlled through a read-write monitor so as
 * to ensure there is no concurrent read and write operations
 * (only concurrent reading is allowed).
 */
public ReadWriteMonitor getMonitorFor(IIndex index){
	
	return (ReadWriteMonitor)monitors.get(index);
}
/**
 * Trigger addition of the entire content of a project
 * Note: the actual operation is performed in background 
 */
public void indexAll(IProject project) {
	if (JavaCore.getPlugin() == null) return;

	// Also request indexing of binaries on the classpath
	// determine the new children
	try {
		IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
		IJavaProject javaProject = ((JavaModel) model).getJavaProject(project);	
		// only consider immediate libraries - each project will do the same
		// NOTE: force to resolve CP variables before calling indexer - 19303, so that initializers
		// will be run in the current thread.
		IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);	
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry= entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				this.indexLibrary(entry.getPath(), project);
			}
		}
	} catch(JavaModelException e){ // cannot retrieve classpath info
	}	
	this.request(new IndexAllProject(project, this));
}


/**
 * Advance to the next available job, once the current one has been completed.
 * Note: clients awaiting until the job count is zero are still waiting at this point.
 */
protected synchronized void moveToNextJob() {

	// remember that one job was executed, and we will need to save indexes at some point
	needToSave = true;
	super.moveToNextJob();
}
/**
 * No more job awaiting.
 */
protected void notifyIdle(long idlingTime){
	if (idlingTime > 1000 && needToSave) saveIndexes();
}
/**
 * Name of the background process
 */
public String processName(){
	return Util.bind("process.name"); //$NON-NLS-1$
}

/**
 * Recreates the index for a given path, keeping the same read-write monitor.
 * Returns the new empty index or null if it didn't exist before.
 * Warning: Does not check whether index is consistent (not being used)
 */
public synchronized IIndex recreateIndex(IPath path) {
	IIndex index = (IIndex) indexes.get(path);
	if (index != null) {
		try {
			// Path is already canonical
			String indexPath = computeIndexName(path);
			ReadWriteMonitor monitor = (ReadWriteMonitor)monitors.remove(index);
			index = new Index(indexPath, "Index for " + path.toOSString(), true /*reuse index file*/); //$NON-NLS-1$
			index.empty();
			indexes.put(path, index);
			monitors.put(index, monitor);
		} catch (IOException e) {
			// The file could not be created. Possible reason: the project has been deleted.
			return null;
		}
	}
	//System.out.println(" index name: " + path.toOSString() + " <----> " + index.getIndexFile().getName());	
	return index;
}
/**
 * Trigger removal of a resource to an index
 * Note: the actual operation is performed in background
 */
public void remove(String resourceName, IPath indexedContainer){
	request(new RemoveFromIndex(resourceName, indexedContainer, this));
}
/**
 * Removes the index for a given path. 
 * This is a no-op if the index did not exist.
 */
public synchronized void removeIndex(IPath path) {
	IIndex index = (IIndex)this.indexes.get(path);
	if (index == null) return;
	index.getIndexFile().delete();
	this.indexes.remove(path);
	this.monitors.remove(index);
}
/**
 * Removes all indexes whose paths start with (or are equal to) the given path. 
 */
public synchronized void removeIndexFamily(IPath path) {
	ArrayList toRemove = null;
	Iterator iterator = this.indexes.keySet().iterator();
	while (iterator.hasNext()) {
		IPath indexPath = (IPath)iterator.next();
		if (path.isPrefixOf(indexPath)) {
			if (toRemove == null) {
				toRemove = new ArrayList();
			}
			toRemove.add(indexPath);
		}
	}
	if (toRemove != null) {
		for (int i = 0, length = toRemove.size(); i < length; i++) {
			this.removeIndex((IPath)toRemove.get(i));
		}
	}
}
/**
 * Remove the content of the given source folder from the index.
 */
public void removeSourceFolderFromIndex(JavaProject javaProject, IPath sourceFolder) {
	this.request(new RemoveFolderFromIndex(sourceFolder.toString(), javaProject.getProject().getFullPath(), this));
}

/**
 * Flush current state
 */
public void reset(){

	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	if (workspace != null){
		// force to resolve classpaths for projects to check, so as to avoid running CP variable initializers in the background thread
		this.projectsToCheck = workspace.getRoot().getProjects();
		for (int i = 0, max = this.projectsToCheck == null ? 0 : this.projectsToCheck.length; i < max; i++){
			IJavaProject project = JavaCore.create(this.projectsToCheck[i]);
			try {
				// force to resolve CP variables before calling indexer - 19303 (indirectly through consistency check)
				project.getResolvedClasspath(true);
			} catch (JavaModelException e) {
			}
		} 
	}
	super.reset();
	if (indexes != null){
		indexes = new HashMap(5);
		monitors = new HashMap(5);
	}
	javaPluginLocation = null;
}
/**
 * Commit all index memory changes to disk
 */
public void saveIndexes(){

	ArrayList indexList = new ArrayList();
	synchronized(this){
		for (Iterator iter = indexes.values().iterator(); iter.hasNext();){
			indexList.add(iter.next());
		}
	}

	for (Iterator iter = indexList.iterator(); iter.hasNext();){		
		IIndex index = (IIndex)iter.next();
		if (index == null) continue; // index got deleted since acquired
		ReadWriteMonitor monitor = getMonitorFor(index);
		if (monitor == null) continue; // index got deleted since acquired
		try {
			monitor.enterWrite();
			if (IndexManager.VERBOSE){
				if (index.hasChanged()){ 
					JobManager.verbose("-> merging index " + index.getIndexFile()); //$NON-NLS-1$
				}
			}
			try {
				index.save();
			} catch(IOException e){
				if (IndexManager.VERBOSE) {
					JobManager.verbose("-> got the following exception while merging:"); //$NON-NLS-1$
					e.printStackTrace();
				}
				//org.eclipse.jdt.internal.core.Util.log(e);
			}
		} finally {
			monitor.exitWrite();
		}
	}
	needToSave = false;
}

public void shutdown() {
	
	HashMap keepingIndexesPaths = new HashMap();
	IndexSelector indexSelector = new IndexSelector(new JavaWorkspaceScope(), null, this);
	IIndex[] selectedIndexes = indexSelector.getIndexes();
	for (int i = 0, max = selectedIndexes.length; i < max; i++) {
		String path = selectedIndexes[i].getIndexFile().getAbsolutePath();
		keepingIndexesPaths.put(path, path);
	}
	File indexesDirectory = new File(this.getJavaPluginWorkingLocation().toOSString());
	if (indexesDirectory.isDirectory()) {
		File[] indexesFiles = indexesDirectory.listFiles();
		for (int i = 0, indexesFilesLength = indexesFiles.length; i < indexesFilesLength; i++) {
			String fileName = indexesFiles[i].getAbsolutePath();
			if (fileName.toLowerCase().endsWith(".index") && keepingIndexesPaths.get(fileName) == null) { //$NON-NLS-1$
				if (VERBOSE) {
					JobManager.verbose("Deleting index file " + indexesFiles[i]); //$NON-NLS-1$
				}
				indexesFiles[i].delete();
			}
		}
		
	}
	super.shutdown();		
}

/**
 * Trigger addition of a library to an index
 * Note: the actual operation is performed in background
 */
public void indexLibrary(IPath path, IProject referingProject) {
	if (JavaCore.getPlugin() == null) return;

	Object target = JavaModel.getTarget(ResourcesPlugin.getWorkspace().getRoot(), path, true);
	
	IndexRequest request = null;
	if (target instanceof IFile) {
		request = new AddJarFileToIndex((IFile)target, this, referingProject.getName());
	} else if (target instanceof java.io.File) {
		// remember the timestamp of this library
		long timestamp = DeltaProcessor.getTimeStamp((java.io.File)target);
		JavaModelManager.getJavaModelManager().deltaProcessor.externalTimeStamps.put(path, new Long(timestamp));
		request = new AddJarFileToIndex(path, this, referingProject.getName());
	} else if (target instanceof IFolder) {
		request = new IndexBinaryFolder((IFolder)target, this, referingProject);
	} else {
		return;
	}
	
	// check if the same request is not already in the queue
	for (int i = this.jobEnd; i >= this.jobStart; i--) {
		IJob awaiting = this.awaitingJobs[i];
		if (awaiting != null && request.equals(awaiting)) {
			return;
		}
	}

	this.request(request);
}
}

