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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.index.impl.JarFileEntryDocument;

/**
 * Creates java element handles.
 */
public class HandleFactory {

	/**
	 * Cache package fragment root information to optimize speed performance.
	 */
	private String lastPkgFragmentRootPath;
	private IPackageFragmentRoot lastPkgFragmentRoot;

	/**
	 * Cache package handles to optimize memory.
	 */
	private Map packageHandles;

	private IWorkspace workspace;
	private JavaModel javaModel;


	public HandleFactory(IWorkspace workspace) {
		this.workspace = workspace;
		this.javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
	}
	/**
	 * Creates an Openable handle from the given resource path.
	 * The resource path can be a path to a file in the workbench (eg. /Proj/com/ibm/jdt/core/HandleFactory.java)
	 * or a path to a file in a jar file - it then contains the path to the jar file and the path to the file in the jar
	 * (eg. c:/jdk1.2.2/jre/lib/rt.jar|java/lang/Object.class or /Proj/rt.jar|java/lang/Object.class)
	 * NOTE: This assumes that the resource path is the toString() of an IPath, 
	 *       i.e. it uses the IPath.SEPARATOR for file path
	 *            and it uses '/' for entries in a zip file.
	 */
	public Openable createOpenable(String resourcePath) {
		int separatorIndex;
		if ((separatorIndex= resourcePath.indexOf(JarFileEntryDocument.JAR_FILE_ENTRY_SEPARATOR)) > -1) {
			// path to a class file inside a jar
			String jarPath= resourcePath.substring(0, separatorIndex);
			// Optimization: cache package fragment root handle and package handles
			if (!jarPath.equals(this.lastPkgFragmentRootPath)) {
				IPackageFragmentRoot root= this.getJarPkgFragmentRoot(jarPath);
				if (root == null)
					return null; // match is outside classpath
				this.lastPkgFragmentRootPath= jarPath;
				this.lastPkgFragmentRoot= root;
				this.packageHandles= new HashMap(5);
			}
			// create handle
			String classFilePath= resourcePath.substring(separatorIndex + 1);
			int lastSlash= classFilePath.lastIndexOf('/');
			String packageName= lastSlash > -1 ? classFilePath.substring(0, lastSlash).replace('/', '.') : IPackageFragment.DEFAULT_PACKAGE_NAME;
			IPackageFragment pkgFragment= (IPackageFragment) this.packageHandles.get(packageName);
			if (pkgFragment == null) {
				pkgFragment= this.lastPkgFragmentRoot.getPackageFragment(packageName);
				this.packageHandles.put(packageName, pkgFragment);
			}
			IClassFile classFile= pkgFragment.getClassFile(classFilePath.substring(lastSlash + 1));
			return (Openable) classFile;
		} else {
			// path to a file in a directory
			// Optimization: cache package fragment root handle and package handles
			int length = -1;
			if (this.lastPkgFragmentRootPath == null 
				|| !(resourcePath.startsWith(this.lastPkgFragmentRootPath) 
					&& (length = this.lastPkgFragmentRootPath.length()) > 0
					&& resourcePath.charAt(length) == '/')) {
				IPackageFragmentRoot root= this.getPkgFragmentRoot(resourcePath);
				if (root == null)
					return null; // match is outside classpath
				this.lastPkgFragmentRoot= root;
				this.lastPkgFragmentRootPath= this.lastPkgFragmentRoot.getPath().toString();
				this.packageHandles= new HashMap(5);
			}
			// create handle
			int lastSlash= resourcePath.lastIndexOf(IPath.SEPARATOR);
			String packageName= lastSlash > (length= this.lastPkgFragmentRootPath.length()) ? resourcePath.substring(length + 1, lastSlash).replace(IPath.SEPARATOR, '.') : IPackageFragment.DEFAULT_PACKAGE_NAME;
			IPackageFragment pkgFragment= (IPackageFragment) this.packageHandles.get(packageName);
			if (pkgFragment == null) {
				pkgFragment= this.lastPkgFragmentRoot.getPackageFragment(packageName);
				this.packageHandles.put(packageName, pkgFragment);
			}
			String simpleName= resourcePath.substring(lastSlash + 1);
			if (Util.isJavaFileName(simpleName)) {
				ICompilationUnit unit= pkgFragment.getCompilationUnit(simpleName);
				return (Openable) unit;
			} else {
				IClassFile classFile= pkgFragment.getClassFile(simpleName);
				return (Openable) classFile;
			}
		}
	}
/**
	 * Returns the package fragment root that corresponds to the given jar path.
	 * See createOpenable(...) for the format of the jar path string.
	 */
	private IPackageFragmentRoot getJarPkgFragmentRoot(String jarPathString) {

		IPath jarPath= new Path(jarPathString);
		
		IResource jarFile= this.workspace.getRoot().findMember(jarPath);
		if (jarFile != null) {
			// internal jar: is it on the classpath of its project?
			//  e.g. org.eclipse.swt.win32/ws/win32/swt.jar 
			//        is NOT on the classpath of org.eclipse.swt.win32
			IJavaProject javaProject = this.javaModel.getJavaProject(jarFile);
			IClasspathEntry[] classpathEntries;
			try {
				classpathEntries = javaProject.getResolvedClasspath(true);
				for (int j= 0, entryCount= classpathEntries.length; j < entryCount; j++) {
					if (classpathEntries[j].getPath().equals(jarPath)) {
						return javaProject.getPackageFragmentRoot(jarFile);
					}
				}
			} catch (JavaModelException e) {
				// ignore and try to find another project
			}
		}
		
		// walk all projects and find the first one that has the given jar path in its classpath
		IJavaProject[] projects;
		try {
			projects = this.javaModel.getJavaProjects();
		} catch (JavaModelException e) {
			// java model is not accessible
			return null;
		}
		for (int i= 0, projectCount= projects.length; i < projectCount; i++) {
			try {
				JavaProject javaProject= (JavaProject)projects[i];
				IClasspathEntry[] classpathEntries= javaProject.getResolvedClasspath(true);
				for (int j= 0, entryCount= classpathEntries.length; j < entryCount; j++) {
					if (classpathEntries[j].getPath().equals(jarPath)) {
						if (jarFile != null) {
							// internal jar
							return javaProject.getPackageFragmentRoot(jarFile);
						} else {
							// external jar
							return javaProject.getPackageFragmentRoot0(jarPathString);
						}
					}
				}
			} catch (JavaModelException e) {
				// JavaModelException from getResolvedClasspath - a problem occured while accessing project: nothing we can do, ignore
			}
		}
		return null;
	}
/**
	 * Returns the package fragment root that contains the given resource path.
	 */
	private IPackageFragmentRoot getPkgFragmentRoot(String pathString) {

		IPath path= new Path(pathString);
		IProject[] projects= this.workspace.getRoot().getProjects();
		for (int i= 0, max= projects.length; i < max; i++) {
			try {
				IProject project = projects[i];
				if (!project.isAccessible() 
					|| !project.hasNature(JavaCore.NATURE_ID)) continue;
				IJavaProject javaProject= this.javaModel.getJavaProject(project);
				IPackageFragmentRoot[] roots= javaProject.getPackageFragmentRoots();
				for (int j= 0, rootCount= roots.length; j < rootCount; j++) {
					IPackageFragmentRoot root= roots[j];
					if (root.getPath().isPrefixOf(path)) {
						return root;
					}
				}
			} catch (CoreException e) {
				// CoreException from hasNature - should not happen since we check that the project is accessible
				// JavaModelException from getPackageFragmentRoots - a problem occured while accessing project: nothing we can do, ignore
			}
		}
		return null;
	}
}
