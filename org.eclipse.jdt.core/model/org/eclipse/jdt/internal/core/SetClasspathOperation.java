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
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * This operation sets an <code>IJavaProject</code>'s classpath.
 *
 * @see IJavaProject
 */
public class SetClasspathOperation extends JavaModelOperation {

	IClasspathEntry[] oldResolvedPath;
	IClasspathEntry[] newRawPath;
	boolean canChangeResource;
	boolean needCycleCheck;
	boolean needValidation;
	
	IPath newOutputLocation;
	public static final IClasspathEntry[] ReuseClasspath = new IClasspathEntry[0];
	public static final IPath ReuseOutputLocation = new Path("Reuse Existing Output Location");  //$NON-NLS-1$
	
	/**
	 * When executed, this operation sets the classpath of the given project.
	 */
	public SetClasspathOperation(
		IJavaProject project,
		IClasspathEntry[] oldResolvedPath,
		IClasspathEntry[] newRawPath,
		IPath newOutputLocation,
		boolean canChangeResource,
		boolean forceSave,
		boolean needCycleCheck,
		boolean needValidation) {

		super(new IJavaElement[] { project });
		this.oldResolvedPath = oldResolvedPath;
		this.newRawPath = newRawPath;
		this.newOutputLocation = newOutputLocation;
		this.canChangeResource = canChangeResource;
		this.needCycleCheck = needCycleCheck;
		this.needValidation = needValidation;
	}

	/**
	 * Adds deltas for the given roots, with the specified change flag,
	 * and closes the root. Helper method for #setClasspath
	 */
	protected void addClasspathDeltas(
		IPackageFragmentRoot[] roots,
		int flag,
		JavaElementDelta delta) {

		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			delta.changed(root, flag);
			if ((flag & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0 
					|| (flag & IJavaElementDelta.F_SOURCEATTACHED) != 0
					|| (flag & IJavaElementDelta.F_SOURCEDETACHED) != 0){
				try {
					root.close();
				} catch (JavaModelException e) {
				}
				// force detach source on jar package fragment roots (source will be lazily computed when needed)
				if (root instanceof JarPackageFragmentRoot) {
					((JarPackageFragmentRoot) root).setSourceAttachmentProperty(null);// loose info - will be recomputed
				}
			}
		}
	}



	/**
	 * Returns the index of the item in the list if the given list contains the specified entry. If the list does
	 * not contain the entry, -1 is returned.
	 * A helper method for #setClasspath
	 */
	protected int classpathContains(
		IClasspathEntry[] list,
		IClasspathEntry entry) {

		for (int i = 0; i < list.length; i++) {
			IClasspathEntry other = list[i];
			if (other.getContentKind() == entry.getContentKind()
				&& other.getEntryKind() == entry.getEntryKind()
				&& other.isExported() == entry.isExported()
				&& other.getPath().equals(entry.getPath())) {
					return i;
			}
		}
		return -1;
	}

	/**
	 * Recursively adds all subfolders of <code>folder</code> to the given collection.
	 */
	protected void collectAllSubfolders(IFolder folder, ArrayList collection) throws JavaModelException {
		try {
			IResource[] members= folder.members();
			for (int i = 0, max = members.length; i < max; i++) {
				IResource r= members[i];
				if (r.getType() == IResource.FOLDER) {
					collection.add(r);
					collectAllSubfolders((IFolder)r, collection);
				}
			}	
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	/**
	 * Returns a collection of package fragments that have been added/removed
	 * as the result of changing the output location to/from the given
	 * location. The collection is empty if no package fragments are
	 * affected.
	 */
	protected ArrayList determineAffectedPackageFragments(IPath location) throws JavaModelException {
		ArrayList fragments = new ArrayList();
		JavaProject project =getProject();
	
		// see if this will cause any package fragments to be affected
		IWorkspace workspace = getWorkspace();
		IResource resource = null;
		if (location != null) {
			resource = workspace.getRoot().findMember(location);
		}
		if (resource != null && resource.getType() == IResource.FOLDER) {
			IFolder folder = (IFolder) resource;
			// only changes if it actually existed
			IClasspathEntry[] classpath = project.getExpandedClasspath(true);
			for (int i = 0; i < classpath.length; i++) {
				IClasspathEntry entry = classpath[i];
				IPath path = classpath[i].getPath();
				if (entry.getEntryKind() != IClasspathEntry.CPE_PROJECT && path.isPrefixOf(location) && !path.equals(location)) {
					IPackageFragmentRoot[] roots = project.getPackageFragmentRoots(classpath[i]);
					IPackageFragmentRoot root = roots[0];
					// now the output location becomes a package fragment - along with any subfolders
					ArrayList folders = new ArrayList();
					folders.add(folder);
					collectAllSubfolders(folder, folders);
					Iterator elements = folders.iterator();
					int segments = path.segmentCount();
					while (elements.hasNext()) {
						IFolder f = (IFolder) elements.next();
						IPath relativePath = f.getFullPath().removeFirstSegments(segments);
						String name = relativePath.toOSString();
						name = name.replace(File.pathSeparatorChar, '.');
						if (name.endsWith(".")) { //$NON-NLS-1$
							name = name.substring(0, name.length() - 1);
						}
						IPackageFragment pkg = root.getPackageFragment(name);
						fragments.add(pkg);
					}
				}
			}
		}
		return fragments;
	}

	/**
	 * Sets the classpath of the pre-specified project.
	 */
	protected void executeOperation() throws JavaModelException {

		// project reference updated - may throw an exception if unable to write .project file
		updateProjectReferencesIfNecessary();

		// classpath file updated - may throw an exception if unable to write .classpath file
		saveClasspathIfNecessary();
		
		// perform classpath and output location updates, if exception occurs in classpath update,
		// make sure the output location is updated before surfacing the exception (in case the output
		// location update also throws an exception, give priority to the classpath update one).
		JavaModelException originalException = null;

		try {
			if (this.newRawPath != ReuseClasspath) updateClasspath();

		} catch(JavaModelException e){
			originalException = e;
			throw e;

		} finally { // if traversed by an exception we still need to update the output location when necessary

			try {
				if (this.newOutputLocation != ReuseOutputLocation) updateOutputLocation();

			} catch(JavaModelException e){
				if (originalException != null) throw originalException; 
				throw e;
			}
		}
		done();
	}

	/**
	 * Generates the delta of removed/added/reordered roots.
	 * Use three deltas in case the same root is removed/added/reordered (i.e. changed from
	 * K_SOURCE to K_BINARY or visa versa)
	 */
	protected void generateClasspathChangeDeltas(
		IClasspathEntry[] oldResolvedPath,
		IClasspathEntry[] newResolvedPath,
		JavaModelManager manager,
		JavaProject project) {

		boolean needToUpdateDependents = false;
		
		JavaElementDelta delta = new JavaElementDelta(getJavaModel());
		boolean hasDelta = false;
		int oldLength = oldResolvedPath.length;
		int newLength = newResolvedPath.length;
		boolean oldResolvedPathLongest = oldLength >= newLength;
			
		IndexManager indexManager = manager.getIndexManager();
		for (int i = 0; i < oldResolvedPath.length; i++) {
			
			int index = classpathContains(newResolvedPath, oldResolvedPath[i]);
			if (index == -1) {
				// do not notify remote project changes
				if (oldResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_PROJECT){
					needToUpdateDependents = true;
					continue; 
				}

				IPackageFragmentRoot[] pkgFragmentRoots =
					project.getPackageFragmentRoots(oldResolvedPath[i]);
				addClasspathDeltas(pkgFragmentRoots, IJavaElementDelta.F_REMOVED_FROM_CLASSPATH, delta);

				int changeKind = oldResolvedPath[i].getEntryKind();
				needToUpdateDependents |= (changeKind == IClasspathEntry.CPE_SOURCE) || oldResolvedPath[i].isExported();

				// Remove the .java files from the index.
				// Note that .class files belong to binary folders which can be shared, 
				// so leave the index for .class files.
				if (indexManager != null && changeKind == IClasspathEntry.CPE_SOURCE) {
					indexManager.removeSourceFolderFromIndex(project, oldResolvedPath[i].getPath());
				}
				hasDelta = true;

			} else {
				// do not notify remote project changes
				if (oldResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_PROJECT){
					continue; 
				}				
				needToUpdateDependents |= (oldResolvedPath[i].isExported() != newResolvedPath[index].isExported());
				if (oldResolvedPathLongest && index != i) { //reordering of the classpath
						addClasspathDeltas(
							project.getPackageFragmentRoots(oldResolvedPath[i]),
							IJavaElementDelta.F_CLASSPATH_REORDER,
							delta);
						int changeKind = oldResolvedPath[i].getEntryKind();
						needToUpdateDependents |= (changeKind == IClasspathEntry.CPE_SOURCE);
		
						hasDelta = true;
				}
				
				// check source attachment
				int sourceAttachmentFlags = 
					this.getSourceAttachmentDeltaFlag(
						oldResolvedPath[i].getSourceAttachmentPath(),
						newResolvedPath[index].getSourceAttachmentPath());
				int sourceAttachmentRootFlags = 
					this.getSourceAttachmentDeltaFlag(
						oldResolvedPath[i].getSourceAttachmentRootPath(),
						newResolvedPath[index].getSourceAttachmentRootPath());
				int flags = sourceAttachmentFlags | sourceAttachmentRootFlags;
				if (flags != 0) {
					addClasspathDeltas(
						project.getPackageFragmentRoots(oldResolvedPath[i]),
						flags,
						delta);
					hasDelta = true;
				}
			}
		}

		for (int i = 0; i < newResolvedPath.length; i++) {

			int index = classpathContains(oldResolvedPath, newResolvedPath[i]);
			if (index == -1) {
				// do not notify remote project changes
				if (newResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_PROJECT){
					needToUpdateDependents = true;
					continue; 
				}
				addClasspathDeltas(
					project.getPackageFragmentRoots(newResolvedPath[i]),
					IJavaElementDelta.F_ADDED_TO_CLASSPATH,
					delta);
				int changeKind = newResolvedPath[i].getEntryKind();
				
				// Request indexing
				if (indexManager != null) {
					switch (changeKind) {
						case IClasspathEntry.CPE_LIBRARY:
							boolean pathHasChanged = true;
							IPath newPath = newResolvedPath[i].getPath();
							for (int j = 0; j < oldResolvedPath.length; j++) {
								IClasspathEntry oldEntry = oldResolvedPath[j];
								if (oldEntry.getPath().equals(newPath)) {
									pathHasChanged = false;
									break;
								}
							}
							if (pathHasChanged) {
								indexManager.indexLibrary(newPath, project.getProject());
							}
							break;
						case IClasspathEntry.CPE_SOURCE:
							indexManager.indexSourceFolder(project, newResolvedPath[i].getPath());
							break;
					}
				}
				
				needToUpdateDependents |= (changeKind == IClasspathEntry.CPE_SOURCE) || newResolvedPath[i].isExported();
				hasDelta = true;

			} // classpath reordering has already been generated in previous loop
		}
		
		if (hasDelta) {
			this.addDelta(delta);
		}
		if (needToUpdateDependents){
			updateAffectedProjects(project.getProject().getFullPath());
		}
	}

	protected JavaProject getProject() {
		return ((JavaProject) getElementsToProcess()[0]);
	}

	/*
	 * Returns the source attachment flag for the delta between the 2 give source paths.
	 * Returns either F_SOURCEATTACHED, F_SOURCEDETACHED, F_SOURCEATTACHED | F_SOURCEDETACHED
	 * or 0 if there is no difference.
	 */
	private int getSourceAttachmentDeltaFlag(IPath oldPath, IPath newPath) {
		if (oldPath == null) {
			if (newPath != null) {
				return IJavaElementDelta.F_SOURCEATTACHED;
			} else {
				return 0;
			}
		} else if (newPath == null) {
			return IJavaElementDelta.F_SOURCEDETACHED;
		} else if (!oldPath.equals(newPath)) {
			return IJavaElementDelta.F_SOURCEATTACHED | IJavaElementDelta.F_SOURCEDETACHED;
		} else {
			return 0;
		}
	}

	/**
	 * Returns <code>true</code> if this operation performs no resource modifications,
	 * otherwise <code>false</code>. Subclasses must override.
	 */
	public boolean isReadOnly() {
		return !this.canChangeResource;
	}

	protected void saveClasspathIfNecessary() throws JavaModelException {
		
		if (!this.canChangeResource) return;
				
		IClasspathEntry[] classpathForSave;
		JavaProject project = getProject();
		if (this.newRawPath == ReuseClasspath){
			classpathForSave = project.getRawClasspath();
		} else {
			classpathForSave = this.newRawPath;
		}
		IPath outputLocationForSave;
		if (this.newOutputLocation == ReuseOutputLocation){
			outputLocationForSave = project.getOutputLocation();
		} else {
			outputLocationForSave = this.newOutputLocation;
		}
		// if read-only .classpath, then the classpath setting will never been performed completely
		this.hasModifiedResource = project.saveClasspath(classpathForSave, outputLocationForSave);
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer(20);
		buffer.append("SetClasspathOperation\n"); //$NON-NLS-1$
		buffer.append(" - classpath : "); //$NON-NLS-1$
		if (this.newRawPath == ReuseClasspath){
			buffer.append("<Reuse Existing Classpath>"); //$NON-NLS-1$
		} else {
			buffer.append("{"); //$NON-NLS-1$
			for (int i = 0; i < this.newRawPath.length; i++) {
				if (i > 0) buffer.append(","); //$NON-NLS-1$
				IClasspathEntry element = this.newRawPath[i];
				buffer.append(" ").append(element.toString()); //$NON-NLS-1$
			}
		}
		buffer.append("\n - output location : ");  //$NON-NLS-1$
		if (this.newOutputLocation == ReuseOutputLocation){
			buffer.append("<Reuse Existing Output Location>"); //$NON-NLS-1$
		} else {
			buffer.append(this.newOutputLocation.toString()); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	private void updateClasspath() throws JavaModelException {

		JavaProject project = ((JavaProject) getElementsToProcess()[0]);

		beginTask(Util.bind("classpath.settingProgress", project.getElementName()), 2); //$NON-NLS-1$

		// SIDE-EFFECT: from thereon, the classpath got modified
		project.setRawClasspath0(this.newRawPath);

		// resolve new path (asking for marker creation if problems)
		IClasspathEntry[] newResolvedPath = 
			project.getResolvedClasspath(
				true, // ignoreUnresolvedEntry
				this.canChangeResource);// also update cp markers

		if (this.oldResolvedPath != null) {
			generateClasspathChangeDeltas(
				this.oldResolvedPath,
				newResolvedPath,
				project.getJavaModelManager(),
				project);
		} else {
			updateAffectedProjects(project.getProject().getFullPath());
		}
		
		if (this.needCycleCheck){
			updateCycleMarkers(newResolvedPath);
		}
	}

	/**
	 * Update projects which are affected by this classpath change:
	 * those which refers to the current project as source
	 */
	protected void updateAffectedProjects(IPath prerequisiteProjectPath) {

		try {
			IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
			IJavaProject originatingProject = getProject();
			IJavaProject[] projects = model.getJavaProjects();
			for (int i = 0, projectCount = projects.length; i < projectCount; i++) {
				try {
					JavaProject project = (JavaProject) projects[i];
					if (project.equals(originatingProject)) continue; // skip itself
					
					// consider ALL dependents (even indirect ones), since they may need to
					// flush their respective namelookup caches (all pkg fragment roots).

					IClasspathEntry[] classpath = project.getExpandedClasspath(true);
					for (int j = 0, entryCount = classpath.length; j < entryCount; j++) {
						IClasspathEntry entry = classpath[j];
						if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT
							&& entry.getPath().equals(prerequisiteProjectPath)) {
							project.setRawClasspath(
								project.getRawClasspath(), 
								SetClasspathOperation.ReuseOutputLocation, 
								this.fMonitor, 
								this.canChangeResource, 
								false, 
								project.getResolvedClasspath(true), 
								false, // no further cycle check
								false); // updating only - no validation
							break;
						}
					}
				} catch (JavaModelException e) {
				}
			}
		} catch (JavaModelException e) {
		}
		
	}

	/**
	 * Update cycle markers
	 */
	protected void updateCycleMarkers(IClasspathEntry[] newResolvedPath) {
		
		if (!this.canChangeResource) return;
		 
		try {
			JavaProject project = getProject();
			if (!project.hasClasspathCycle(project.getResolvedClasspath(true))
					&& !project.hasCycleMarker()){
				return;
			}
		
			JavaProject.updateAllCycleMarkers();
		} catch(JavaModelException e){
		}
	}

	/**
	 * Sets the output location of the pre-specified project.
	 *
	 * <p>This can cause changes in package fragments - i.e. if the
	 * old and new output location folder could be considered as
	 * a package fragment.
	 */
	protected void updateOutputLocation() throws JavaModelException {
		
		JavaProject project= ((JavaProject) getElementsToProcess()[0]);

		beginTask(Util.bind("classpath.settingOutputLocationProgress", project.getElementName()), 2); //$NON-NLS-1$
		
		IPath oldLocation= project.getOutputLocation();
	
		// see if this will cause any package fragments to be added
		boolean deltaToFire= false;
		JavaElementDelta delta = newJavaElementDelta();
		ArrayList added= determineAffectedPackageFragments(oldLocation);
		Iterator iter = added.iterator();
		while (iter.hasNext()){
			IPackageFragment frag= (IPackageFragment)iter.next();
			((IPackageFragmentRoot)frag.getParent()).close();
			delta.added(frag);
			deltaToFire = true;
		}
	
		// see if this will cause any package fragments to be removed
		ArrayList removed= determineAffectedPackageFragments(this.newOutputLocation);
		iter = removed.iterator();
		while (iter.hasNext()){
			IPackageFragment frag= (IPackageFragment)iter.next();
			((IPackageFragmentRoot)frag.getParent()).close();
			delta.removed(frag);
			deltaToFire = true;
		}
		
		project.getJavaProjectElementInfo().setOutputLocation(this.newOutputLocation);
		if (deltaToFire) {
			addDelta(delta);	
		}
		worked(1);
	}
	
	/**
	 * Update projects references so that the build order is consistent with the classpath
	 */
	protected void updateProjectReferencesIfNecessary() throws JavaModelException {
		
		if (!this.canChangeResource) return;
		if (this.newRawPath == ReuseClasspath || !this.needCycleCheck) return;

		JavaProject jproject = getProject();
		String[] oldRequired = jproject.getRequiredProjectNames();
		String[] newRequired =jproject.projectPrerequisites(jproject.getResolvedClasspath(this.newRawPath, true, false));

		try {		
			IProject project = jproject.getProject();
			IProjectDescription description = project.getDescription();
			 
			IProject[] projectReferences = description.getReferencedProjects();
			
			HashSet oldReferences = new HashSet(projectReferences.length);
			for (int i = 0; i < projectReferences.length; i++){
				String projectName = projectReferences[i].getName();
				oldReferences.add(projectName);
			}
			HashSet newReferences = (HashSet)oldReferences.clone();

			for (int i = 0; i < oldRequired.length; i++){
				String projectName = oldRequired[i];
				newReferences.remove(projectName);
			}
			for (int i = 0; i < newRequired.length; i++){
				String projectName = newRequired[i];
				newReferences.add(projectName);
			}

			Iterator iter;
			int newSize = newReferences.size();
			
			checkIdentity: {
				if (oldReferences.size() == newSize){
					iter = newReferences.iterator();
					while (iter.hasNext()){
						if (!oldReferences.contains(iter.next())){
							break checkIdentity;
						}
					}
					return;
				}
			}
			String[] requiredProjectNames = new String[newSize];
			int index = 0;
			iter = newReferences.iterator();
			while (iter.hasNext()){
				requiredProjectNames[index++] = (String)iter.next();
			}
			Util.sort(requiredProjectNames); // ensure that if changed, the order is consistent
			
			IProject[] requiredProjectArray = new IProject[newSize];
			IWorkspaceRoot wksRoot = project.getWorkspace().getRoot();
			for (int i = 0; i < newSize; i++){
				requiredProjectArray[i] = wksRoot.getProject(requiredProjectNames[i]);
			}

			description.setReferencedProjects(requiredProjectArray);
			project.setDescription(description, this.fMonitor);

		} catch(CoreException e){
			throw new JavaModelException(e);
		}
	}

	public IJavaModelStatus verify() {

		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}

		if (needValidation) {
			IJavaProject project = (IJavaProject) getElementToProcess();
			// retrieve classpath 
			IClasspathEntry[] entries = this.newRawPath;
			if (entries == ReuseClasspath){
				try {
					entries = project.getRawClasspath();			
				} catch (JavaModelException e) {
					return e.getJavaModelStatus();
				}
			}		
			// retrieve output location
			IPath outputLocation = this.newOutputLocation;
			if (outputLocation == ReuseOutputLocation){
				try {
					outputLocation = project.getOutputLocation();
				} catch (JavaModelException e) {
					return e.getJavaModelStatus();
				}
			}
					
			// perform validation
			return JavaConventions.validateClasspath(
				project,
				entries,
				outputLocation);
		}
		
		return JavaModelStatus.VERIFIED_OK;
	}
}