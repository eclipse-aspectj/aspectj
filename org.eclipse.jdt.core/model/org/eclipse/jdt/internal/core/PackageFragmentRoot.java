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

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @see IPackageFragmentRoot
 */
public class PackageFragmentRoot extends Openable implements IPackageFragmentRoot {

	/**
	 * The resource associated with this root.
	 * @see IResource
	 */
	protected IResource fResource;
/**
 * Constructs a package fragment root which is the root of the java package
 * directory hierarchy.
 */
protected PackageFragmentRoot(IResource resource, IJavaProject project) {
	this(resource, project, resource.getProjectRelativePath().toString());
	fResource = resource;
}
/**
 * Constructs a package fragment root which is the root of the java package
 * directory hierarchy.
 */
protected PackageFragmentRoot(IResource resource, IJavaProject project, String path) {
	super(PACKAGE_FRAGMENT_ROOT, project, path);
	fResource = resource;
}
/**
 * @see IPackageFragmentRoot
 */
public void attachSource(IPath zipPath, IPath rootPath, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, this));
}
/**
 * Compute the package fragment children of this package fragment root.
 * 
 * @exception JavaModelException  The resource associated with this package fragment root does not exist
 */
protected boolean computeChildren(OpenableElementInfo info) throws JavaModelException {
	try {
		// the underlying resource may be a folder or a project (in the case that the project folder
		// is actually the package fragment root)
		if (fResource.getType() == IResource.FOLDER || fResource.getType() == IResource.PROJECT) {
			ArrayList vChildren = new ArrayList(5);
			computeFolderChildren((IContainer) fResource, "", vChildren); //$NON-NLS-1$
			IJavaElement[] children = new IJavaElement[vChildren.size()];
			vChildren.toArray(children);
			info.setChildren(children);
		}
	} catch (JavaModelException e) {
		//problem resolving children; structure remains unknown
		info.setChildren(new IJavaElement[]{});
		throw e;
	}
	return true;
}
/**
 * Starting at this folder, create package fragments and add the fragments to the collection
 * of children.
 * 
 * @exception JavaModelException  The resource associated with this package fragment does not exist
 */
protected void computeFolderChildren(IContainer folder, String prefix, ArrayList vChildren) throws JavaModelException {
	IPackageFragment pkg = getPackageFragment(prefix);
	vChildren.add(pkg);
	try {
		IPath outputLocationPath = getJavaProject().getOutputLocation();
		IResource[] members = folder.members();
		for (int i = 0, max = members.length; i < max; i++) {
			IResource member = members[i];
			String memberName = member.getName();
			if (member.getType() == IResource.FOLDER 
				&& Util.isValidFolderNameForPackage(memberName)) {
					
				String newPrefix;
				if (prefix.length() == 0) {
					newPrefix = memberName;
				} else {
					newPrefix = prefix + "." + memberName; //$NON-NLS-1$
				}
				// eliminate binary output only if nested inside direct subfolders
				if (!member.getFullPath().equals(outputLocationPath)) {
					computeFolderChildren((IFolder) member, newPrefix, vChildren);
				}
			}
		}
	} catch(IllegalArgumentException e){
		throw new JavaModelException(e, IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST); // could be thrown by ElementTree when path is not found
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * Returns a new element info for this element.
 */
protected OpenableElementInfo createElementInfo() {
	return new PackageFragmentRootInfo();
}
/**
 * @see IPackageFragmentRoot
 */
public IPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreatePackageFragmentOperation op = new CreatePackageFragmentOperation(this, name, force);
	runOperation(op, monitor);
	return getPackageFragment(name);
}
/**
 * Returns the root's kind - K_SOURCE or K_BINARY, defaults
 * to K_SOURCE if it is not on the classpath.
 *
 * @exception NotPresentException if the project and root do
 * 		not exist.
 */
protected int determineKind(IResource underlyingResource) throws JavaModelException {
	IClasspathEntry[] entries= ((JavaProject)getJavaProject()).getExpandedClasspath(true);
	for (int i= 0; i < entries.length; i++) {
		IClasspathEntry entry= entries[i];
		if (entry.getPath().equals(underlyingResource.getFullPath())) {
			return entry.getContentKind();
		}
	}
	return IPackageFragmentRoot.K_SOURCE;
}
/**
 * Compares two objects for equality;
 * for <code>PackageFragmentRoot</code>s, equality is having the
 * same <code>JavaModel</code>, same resources, and occurrence count.
 *
 */
public boolean equals(Object o) {
	if (this == o)
		return true;
	if (!(o instanceof PackageFragmentRoot))
		return false;
	PackageFragmentRoot other = (PackageFragmentRoot) o;
	return getJavaModel().equals(other.getJavaModel()) && 
			fResource.equals(other.fResource) &&
			fOccurrenceCount == other.fOccurrenceCount;
}

/**
 * @see IJavaElement
 */
public boolean exists() {

	return super.exists() 
				&& isOnClasspath();
}
		
/**
 * @see Openable
 */
protected boolean generateInfos(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {
	
	((PackageFragmentRootInfo) info).setRootKind(determineKind(underlyingResource));
	return computeChildren(info);
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_PACKAGEFRAGMENTROOT;
}
/**
 * @see IPackageFragmentRoot
 */
public int getKind() throws JavaModelException {
	return ((PackageFragmentRootInfo)getElementInfo()).getRootKind();
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
public Object[] getNonJavaResources() throws JavaModelException {
	return ((PackageFragmentRootInfo) getElementInfo()).getNonJavaResources(getJavaProject(), getUnderlyingResource());
}
/**
 * @see IPackageFragmentRoot
 */
public IPackageFragment getPackageFragment(String packageName) {
	return new PackageFragment(this, packageName);
}
/**
 * Returns the package name for the given folder
 * (which is a decendent of this root).
 */
protected String getPackageName(IFolder folder) throws JavaModelException {
	IPath myPath= getPath();
	IPath pkgPath= folder.getFullPath();
	int mySegmentCount= myPath.segmentCount();
	int pkgSegmentCount= pkgPath.segmentCount();
	StringBuffer name = new StringBuffer(IPackageFragment.DEFAULT_PACKAGE_NAME);
	for (int i= mySegmentCount; i < pkgSegmentCount; i++) {
		if (i > mySegmentCount) {
			name.append('.');
		}
		name.append(pkgPath.segment(i));
	}
	return name.toString();
}
/**
 * @see IJavaElement
 */
public IPath getPath() {
	return fResource.getFullPath();
}
/*
 * @see IPackageFragmentRoot 
 */
public IClasspathEntry getRawClasspathEntry() throws JavaModelException {
	IPath path= this.getPath();
	IClasspathEntry[] entries= this.getJavaProject().getRawClasspath();
	for (int i= 0; i < entries.length; i++) {
		IClasspathEntry entry = entries[i];
	
		switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_PROJECT:
				// a root's project always refers directly to the root
				// no need to follow the project reference
				continue;
			case IClasspathEntry.CPE_CONTAINER:
				IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), this.getJavaProject());
				if (container != null){
					IClasspathEntry[] containerEntries = container.getClasspathEntries();
					for (int j = 0; j < containerEntries.length; j++){
						IClasspathEntry containerEntry = JavaCore.getResolvedClasspathEntry(containerEntries[j]);
						if (containerEntry != null && path.equals(containerEntry.getPath())) {
							return entry; // answer original entry
						}
					}
				}
				break;
			case IClasspathEntry.CPE_VARIABLE:
				entry = JavaCore.getResolvedClasspathEntry(entry);
				// don't break so as to run default
			default:
				if (entry != null && path.equals(entry.getPath())) {
					return entries[i];
				}
		}
	}
	return null;
}
/*
 * @see IJavaElement
 */
public IResource getResource() {
	return fResource;
}
/**
 * Cannot attach source to a folder.
 *
 * @see IPackageFragmentRoot
 */
public IPath getSourceAttachmentPath() throws JavaModelException {
	return null;
}
/**
 * Cannot attach source to a folder.
 *
 * @see IPackageFragmentRoot
 */
public IPath getSourceAttachmentRootPath() throws JavaModelException {
	return null;
}
/**
 * @see IJavaElement
 */
public IResource getUnderlyingResource() throws JavaModelException {
	if (fResource.exists()) {
		return fResource;
	} else {
		throw newNotPresentException();
	}
	
}
public int hashCode() {
	return fResource.hashCode();
}
/**
 * @see IPackageFragmentRoot
 */
public boolean isArchive() {
	return false;
}
/**
 * @see IPackageFragmentRoot
 */
public boolean isExternal() {
	return false;
}

/*
 * Returns whether this package fragment root is on the classpath of its project.
 */
protected boolean isOnClasspath() {

	IJavaProject project = this.getJavaProject();

	if (this.getElementType() == IJavaElement.JAVA_PROJECT){
		return true;
	}
	
	IPath path = this.getPath();

	try {
		// check package fragment root on classpath of its project
		IClasspathEntry[] classpath = project.getResolvedClasspath(true);	
		for (int i = 0, length = classpath.length; i < length; i++) {
			IClasspathEntry entry = classpath[i];
			if (entry.getPath().equals(path)) {
				return true;
			}
		}
	} catch(JavaModelException e){
		// could not read classpath, then assume it is outside
	}
	return false;

}

protected void openWhenClosed(IProgressMonitor pm) throws JavaModelException {
	if (!this.resourceExists() || !this.isOnClasspath()) throw newNotPresentException();
	super.openWhenClosed(pm);
}

/**
 * Recomputes the children of this element, based on the current state
 * of the workbench.
 */
public void refreshChildren() {
	try {
		OpenableElementInfo info= (OpenableElementInfo)getElementInfo();
		computeChildren(info);
	} catch (JavaModelException e) {
		// do nothing.
	}
}
/*
 * @see JavaElement#rootedAt(IJavaProject)
 */
public IJavaElement rootedAt(IJavaProject project) {
	return
		new PackageFragmentRoot(
			fResource,
			project, 
			fName);
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	if (getElementName().length() == 0) {
		buffer.append("[project root]"); //$NON-NLS-1$
	} else {
		buffer.append(getElementName());
	}
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	}
}
}
