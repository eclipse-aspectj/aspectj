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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;

/**
 * Scope limited to the subtype and supertype hierarchy of a given type.
 */
public class HierarchyScope extends AbstractSearchScope {

	private ITypeHierarchy fHierarchy;
	private IType[] fTypes;
	private HashSet resourcePaths;
	private IPath[] enclosingProjectsAndJars;

	protected IResource[] elements;
	protected int elementCount;
	
	protected boolean needsRefresh;

	/* (non-Javadoc)
	 * Adds the given resource to this search scope.
	 */
	public void add(IResource element) {
		if (this.elementCount == this.elements.length) {
			System.arraycopy(
				this.elements,
				0,
				this.elements = new IResource[this.elementCount * 2],
				0,
				this.elementCount);
		}
		elements[elementCount++] = element;
	}
	
	/* (non-Javadoc)
	 * Creates a new hiearchy scope for the given type.
	 */
	public HierarchyScope(IType type) throws JavaModelException {
		this.initialize();
		fHierarchy = type.newTypeHierarchy(null);
		buildResourceVector();
			
		//disabled for now as this could be expensive
		//JavaModelManager.getJavaModelManager().rememberScope(this);
	}
	private void buildResourceVector() throws JavaModelException {
		HashMap resources = new HashMap();
		HashMap paths = new HashMap();
		fTypes = fHierarchy.getAllTypes();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (int i = 0; i < fTypes.length; i++) {
			IType type = fTypes[i];
			IResource resource = type.getUnderlyingResource();
			if (resource != null && resources.get(resource) == null) {
				resources.put(resource, resource);
				add(resource);
			}
			IPackageFragmentRoot root =
				(IPackageFragmentRoot) type.getPackageFragment().getParent();
			if (root instanceof JarPackageFragmentRoot) {
				// type in a jar
				JarPackageFragmentRoot jar = (JarPackageFragmentRoot) root;
				IPath jarPath = jar.getPath();
				Object target = JavaModel.getTarget(workspaceRoot, jarPath, true);
				String zipFileName;
				if (target instanceof IFile) {
					// internal jar
					zipFileName = jarPath.toString();
				} else if (target instanceof File) {
					// external jar
					zipFileName = ((File)target).getPath();
				} else {
					continue; // unknown target
				}
				String resourcePath =
					zipFileName
						+ JAR_FILE_ENTRY_SEPARATOR
						+ type.getFullyQualifiedName().replace('.', '/')
						+ ".class";//$NON-NLS-1$
				
				this.resourcePaths.add(resourcePath);
				paths.put(jarPath, type);
			} else {
				// type is a project
				paths.put(type.getJavaProject().getProject().getFullPath(), type);
			}
		}
		this.enclosingProjectsAndJars = new IPath[paths.size()];
		int i = 0;
		for (Iterator iter = paths.keySet().iterator(); iter.hasNext();) {
			this.enclosingProjectsAndJars[i++] = (IPath) iter.next();
		}
	}
	/* (non-Javadoc)
	 * @see IJavaSearchScope#encloses(String)
	 */
	public boolean encloses(String resourcePath) {
		if (this.needsRefresh) {
			try {
				this.refresh();
			} catch(JavaModelException e) {
				return false;
			}
		}
		int separatorIndex = resourcePath.indexOf(JAR_FILE_ENTRY_SEPARATOR);
		if (separatorIndex != -1) {
			return this.resourcePaths.contains(resourcePath);
		} else {
			for (int i = 0; i < this.elementCount; i++) {
				if (resourcePath.startsWith(this.elements[i].getFullPath().toString())) {
					return true;
				}
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see IJavaSearchScope#encloses(IJavaElement)
	 */
	public boolean encloses(IJavaElement element) {
		if (this.needsRefresh) {
			try {
				this.refresh();
			} catch(JavaModelException e) {
				return false;
			}
		}
		IType type = null;
		if (element instanceof IType) {
			type = (IType) element;
		} else if (element instanceof IMember) {
			type = ((IMember) element).getDeclaringType();
		}
		if (type != null) {
			if (fHierarchy.contains(type)) {
				return true;
			} else {
				// be flexible: look at original element (see bug 14106 Declarations in Hierarchy does not find declarations in hierarchy)
				IType original;
				if (!type.isBinary() 
						&& (original = (IType)type.getCompilationUnit().getOriginal(type)) != null) {
					return fHierarchy.contains(original);
				}
			}
		} 
		return false;
	}
	/* (non-Javadoc)
	 * Returns whether this search scope encloses the given resource.
	 */
	protected boolean encloses(IResource element) {
		IPath elementPath = element.getFullPath();
		for (int i = 0; i < elementCount; i++) {
			if (this.elements[i].getFullPath().isPrefixOf(elementPath)) {
				return true;
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see IJavaSearchScope#enclosingProjectsAndJars()
	 * @deprecated
	 */
	public IPath[] enclosingProjectsAndJars() {
		if (this.needsRefresh) {
			try {
				this.refresh();
			} catch(JavaModelException e) {
				return new IPath[0];
			}
		}
		return this.enclosingProjectsAndJars;
	}
	protected void initialize() {
		this.resourcePaths = new HashSet();
		this.elements = new IResource[5];
		this.elementCount = 0;
		this.needsRefresh = false;		
	}
	/*
	 * @see AbstractSearchScope#processDelta(IJavaElementDelta)
	 */
	public void processDelta(IJavaElementDelta delta) {
		if (this.needsRefresh) return;
		this.needsRefresh = ((TypeHierarchy)fHierarchy).isAffected(delta);
	}
	protected void refresh() throws JavaModelException {
		this.initialize();
		fHierarchy.refresh(null);
		this.buildResourceVector();
	}
	public String toString() {
		return "HierarchyScope on " + ((JavaElement)fHierarchy.getType()).toStringWithAncestors(); //$NON-NLS-1$
	}

}