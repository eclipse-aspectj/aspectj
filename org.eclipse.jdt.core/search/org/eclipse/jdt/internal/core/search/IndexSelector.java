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
package org.eclipse.jdt.internal.core.search;

import java.util.ArrayList;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * Selects the indexes that correspond to projects in a given search scope
 * and that are dependent on a given focus element.
 */
public class IndexSelector {
	IJavaSearchScope searchScope;
	IJavaElement focus;
	IndexManager indexManager;
	IIndex[] indexes;
public IndexSelector(
	IJavaSearchScope searchScope,
	IJavaElement focus,
	IndexManager indexManager) {
	this.searchScope = searchScope;
	this.focus = focus;
	this.indexManager = indexManager;
}
/**
 * Returns whether elements of the given project or jar can see the focus element
 * either because the focus is part of the project or the jar, or because it is 
 * accessible throught the project's classpath
 */
private boolean canSeeFocus(IPath projectOrJarPath) {
	// if it is a workspace scope, focus is visible from everywhere
	if (this.searchScope instanceof JavaWorkspaceScope) return true;
	
	try {
		while (!(this.focus instanceof IJavaProject) && !(this.focus instanceof JarPackageFragmentRoot)) {
			this.focus = this.focus.getParent();
		}
		IJavaModel model = this.focus.getJavaModel();
		IJavaProject project = this.getJavaProject(projectOrJarPath, model);
		if (this.focus instanceof JarPackageFragmentRoot) {
			// focus is part of a jar
			JarPackageFragmentRoot jar = (JarPackageFragmentRoot)this.focus;
			IPath jarPath = jar.getPath();
			if (project == null) {
				// consider that a jar can see another jar only they are both referenced by the same project
				return this.haveSameParent(projectOrJarPath, jarPath, model); 
			} else {
				IClasspathEntry[] entries = ((JavaProject)project).getExpandedClasspath(true);
				for (int i = 0, length = entries.length; i < length; i++) {
					IClasspathEntry entry = entries[i];
					if ((entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) 
						&& entry.getPath().equals(jarPath)) {
							return true;
					}
				}
				return false;
			}
		} else {
			// focus is part of a project
			IJavaProject focusProject = (IJavaProject)this.focus;
			if (project == null) {
				// consider that a jar can see a project only if it is referenced by this project
				IClasspathEntry[] entries = ((JavaProject)focusProject).getExpandedClasspath(true);
				for (int i = 0, length = entries.length; i < length; i++) {
					IClasspathEntry entry = entries[i];
					if ((entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) 
						&& entry.getPath().equals(projectOrJarPath)) {
							return true;
					}
				}
				return false;
			} else {
				if (focusProject.equals(project)) {
					return true;
				} else {
					IPath focusPath = focusProject.getProject().getFullPath();
					IClasspathEntry[] entries = ((JavaProject)project).getExpandedClasspath(true);
					for (int i = 0, length = entries.length; i < length; i++) {
						IClasspathEntry entry = entries[i];
						if ((entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) 
							&& entry.getPath().equals(focusPath)) {
								return true;
						}
					}
					return false;
				}
			}
		}
	} catch (JavaModelException e) {
		return false;
	}
}
private void computeIndexes() {
	ArrayList indexesInScope = new ArrayList();
	IPath[] projectsAndJars = this.searchScope.enclosingProjectsAndJars();
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace()	.getRoot();
	for (int i = 0; i < projectsAndJars.length; i++) {
		IPath location;
		IPath path = projectsAndJars[i];
		if ((!root.getProject(path.lastSegment()).exists()) // if project does not exist
			&& path.segmentCount() > 1
			&& ((location = root.getFile(path).getLocation()) == null
				|| !new java.io.File(location.toOSString()).exists()) // and internal jar file does not exist
			&& !new java.io.File(path.toOSString()).exists()) { // and external jar file does not exist
				continue;
		}
		if (this.focus == null || this.canSeeFocus(path)) {
			IIndex index = this.indexManager.getIndex(path, true /*reuse index file*/, false /*do not create if none*/);
			if (index != null && indexesInScope.indexOf(index) == -1) {
				indexesInScope.add(index);
			}
		}
	}
	this.indexes = new IIndex[indexesInScope.size()];
	indexesInScope.toArray(this.indexes);
}
public IIndex[] getIndexes() {
	if (this.indexes == null) {
		this.computeIndexes();
	}
	return this.indexes;
}
/**
 * Returns the java project that corresponds to the given path.
 * Returns null if the path doesn't correspond to a project.
 */
private IJavaProject getJavaProject(IPath path, IJavaModel model) {
	IJavaProject project = model.getJavaProject(path.lastSegment());
	if (project.exists()) {
		return project;
	} else {
		return null;
	}
}
/**
 * Returns whether the given jars are referenced in the classpath of the same project.
 */
private boolean haveSameParent(IPath jarPath1, IPath jarPath2, IJavaModel model) {
	if (jarPath1.equals(jarPath2)) {
		return true;
	}
	try {
		IJavaProject[] projects = model.getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject project = projects[i];
			IClasspathEntry[] entries = ((JavaProject)project).getExpandedClasspath(true);
			boolean referencesJar1 = false;
			boolean referencesJar2 = false;
			for (int j = 0, length2 = entries.length; j < length2; j++) {
				IClasspathEntry entry = entries[j];
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					IPath entryPath = entry.getPath();
					if (entryPath.equals(jarPath1)) {
						referencesJar1 = true;
					} else if (entryPath.equals(jarPath2)) {
						referencesJar2 = true;
					}
				}
			}
			if (referencesJar1 && referencesJar2) {
				return true;
			}
		
		}
	} catch (JavaModelException e) {
		e.printStackTrace();
	}	
	return false;
}

}
