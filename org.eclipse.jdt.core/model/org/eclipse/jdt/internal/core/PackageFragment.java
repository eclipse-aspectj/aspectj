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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * @see IPackageFragment
 */
public class PackageFragment extends Openable implements IPackageFragment {
	/**
	 * Constant empty list of class files
	 */
	protected static IClassFile[] fgEmptyClassFileList= new IClassFile[] {};
	/**
	 * Constant empty list of compilation units
	 */
	protected static ICompilationUnit[] fgEmptyCompilationUnitList= new ICompilationUnit[] {};
/**
 * Constructs a handle for a package fragment
 *
 * @see IPackageFragment
 */
protected PackageFragment(IPackageFragmentRoot root, String name) {
	super(PACKAGE_FRAGMENT, root, name);
}
/**
 * Compute the children of this package fragment.
 *
 * <p>Package fragments which are folders recognize files based on the
 * type of the fragment
 * <p>Package fragments which are in a jar only recognize .class files (
 * @see JarPackageFragment).
 */
protected boolean computeChildren(OpenableElementInfo info, IResource resource) throws JavaModelException {
	ArrayList vChildren = new ArrayList();
	int kind = getKind();
	String extType;
	if (kind == IPackageFragmentRoot.K_SOURCE) {
		extType = "java"; //$NON-NLS-1$
	} else {
		extType = "class"; //$NON-NLS-1$
	}
	try {
		IResource[] members = ((IContainer) resource).members();
		for (int i = 0, max = members.length; i < max; i++) {
			IResource child = members[i];
			if (child.getType() != IResource.FOLDER) {
				String extension = child.getProjectRelativePath().getFileExtension();
				if (extension != null) {
					if (extension.equalsIgnoreCase(extType)) {
						IJavaElement childElement;
						if (kind == IPackageFragmentRoot.K_SOURCE && Util.isValidCompilationUnitName(child.getName())) {
							childElement = getCompilationUnit(child.getName());
							vChildren.add(childElement);
						} else if (Util.isValidClassFileName(child.getName())) {
							childElement = getClassFile(child.getName());
							vChildren.add(childElement);
						}
					}
				}
			}
		}
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
	IJavaElement[] children = new IJavaElement[vChildren.size()];
	vChildren.toArray(children);
	info.setChildren(children);
	return true;
}
/**
 * Returns true if this fragment contains at least one java resource.
 * Returns false otherwise.
 */
public boolean containsJavaResources() throws JavaModelException {
	return ((PackageFragmentInfo) getElementInfo()).containsJavaResources();
}
/**
 * @see ISourceManipulation
 */
public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Util.bind("operation.nullContainer")); //$NON-NLS-1$
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] containers= new IJavaElement[] {container};
	IJavaElement[] siblings= null;
	if (sibling != null) {
		siblings= new IJavaElement[] {sibling};
	}
	String[] renamings= null;
	if (rename != null) {
		renamings= new String[] {rename};
	}
	getJavaModel().copy(elements, containers, siblings, renamings, force, monitor);
}
/**
 * @see IPackageFragment
 */
public ICompilationUnit createCompilationUnit(String name, String contents, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateCompilationUnitOperation op= new CreateCompilationUnitOperation(this, name, contents, force);
	runOperation(op, monitor);
	return getCompilationUnit(name);
}
/**
 * @see JavaElement
 */
protected OpenableElementInfo createElementInfo() {
	return new PackageFragmentInfo();
}
/**
 * @see ISourceManipulation
 */
public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
	IJavaElement[] elements = new IJavaElement[] {this};
	getJavaModel().delete(elements, force, monitor);
}
/**
 * @see Openable
 */
protected boolean generateInfos(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {
	
	return computeChildren(info, underlyingResource);
}
/**
 * @see IPackageFragment#getClassFile(String)
 */
public IClassFile getClassFile(String name) {
	return new ClassFile(this, name);
}
/**
 * Returns a the collection of class files in this - a folder package fragment which has a root
 * that has its kind set to <code>IPackageFragmentRoot.K_Source</code> does not
 * recognize class files.
 *
 * @see IPackageFragment#getClassFiles()
 */
public IClassFile[] getClassFiles() throws JavaModelException {
	if (getKind() == IPackageFragmentRoot.K_SOURCE) {
		return fgEmptyClassFileList;
	}
	
	ArrayList list = getChildrenOfType(CLASS_FILE);
	IClassFile[] array= new IClassFile[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IPackageFragment#getCompilationUnit(String)
 */
public ICompilationUnit getCompilationUnit(String name) {
	return new CompilationUnit(this, name);
}
/**
 * @see IPackageFragment#getCompilationUnits()
 */
public ICompilationUnit[] getCompilationUnits() throws JavaModelException {
	if (getKind() == IPackageFragmentRoot.K_BINARY) {
		return fgEmptyCompilationUnitList;
	}
	
	ArrayList list = getChildrenOfType(COMPILATION_UNIT);
	ICompilationUnit[] array= new ICompilationUnit[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see JavaElement#getHandleMementoDelimiter()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_PACKAGEFRAGMENT;
}
/**
 * @see IPackageFragment#getKind()
 */
public int getKind() throws JavaModelException {
	return ((IPackageFragmentRoot)getParent()).getKind();
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
public Object[] getNonJavaResources() throws JavaModelException {
	if (this.isDefaultPackage()) {
		// We don't want to show non java resources of the default package (see PR #1G58NB8)
		return JavaElementInfo.NO_NON_JAVA_RESOURCES;
	} else {
		return ((PackageFragmentInfo) getElementInfo()).getNonJavaResources(getUnderlyingResource());
	}
}
/**
 * @see IJavaElement#getPath()
 */
public IPath getPath() {
	PackageFragmentRoot root = this.getPackageFragmentRoot();
	if (root.isArchive()) {
		return root.getPath();
	} else {
		return root.getPath().append(this.getElementName().replace('.', '/'));
	}
}
/**
 * @see IJavaElement#getResource()
 */
public IResource getResource() {
	PackageFragmentRoot root = this.getPackageFragmentRoot();
	if (root.isArchive()) {
		return root.getResource();
	} else {
		String elementName = this.getElementName();
		if (elementName.length() == 0) {
			return root.getResource();
		} else {
			return ((IContainer)root.getResource()).getFolder(new Path(this.getElementName().replace('.', '/')));
		}
	}
}
/**
 * @see IJavaElement#getUnderlyingResource()
 */
public IResource getUnderlyingResource() throws JavaModelException {
	IResource rootResource = fParent.getUnderlyingResource();
	if (rootResource == null) {
		//jar package fragment root that has no associated resource
		return null;
	}
	// the underlying resource may be a folder or a project (in the case that the project folder
	// is atually the package fragment root)
	if (rootResource.getType() == IResource.FOLDER || rootResource.getType() == IResource.PROJECT) {
		IContainer folder = (IContainer) rootResource;
		String[] segs = Signature.getSimpleNames(fName);
		for (int i = 0; i < segs.length; ++i) {
			IResource child = folder.findMember(segs[i]);
			if (child == null || child.getType() != IResource.FOLDER) {
				throw newNotPresentException();
			}
			folder = (IFolder) child;
		}
		return folder;
	} else {
		return rootResource;
	}
}
/**
 * @see IPackageFragment#hasSubpackages()
 */
public boolean hasSubpackages() throws JavaModelException {
	IJavaElement[] packages= ((IPackageFragmentRoot)getParent()).getChildren();
	String name = getElementName();
	int nameLength = name.length();
	String packageName = isDefaultPackage() ? name : name+"."; //$NON-NLS-1$
	for (int i= 0; i < packages.length; i++) {
		String otherName = packages[i].getElementName();
		if (otherName.length() > nameLength && otherName.startsWith(packageName)) {
			return true;
		}
	}
	return false;
}
/**
 * @see IPackageFragment#isDefaultPackage()
 */
public boolean isDefaultPackage() {
	return this.getElementName().length() == 0;
}
/**
 * @see ISourceManipulation#move(IJavaElement, IJavaElement, String, boolean, IProgressMonitor)
 */
public void move(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Util.bind("operation.nullContainer")); //$NON-NLS-1$
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] containers= new IJavaElement[] {container};
	IJavaElement[] siblings= null;
	if (sibling != null) {
		siblings= new IJavaElement[] {sibling};
	}
	String[] renamings= null;
	if (rename != null) {
		renamings= new String[] {rename};
	}
	getJavaModel().move(elements, containers, siblings, renamings, force, monitor);
}
/**
 * Recomputes the children of this element, based on the current state
 * of the workbench.
 */
public void refreshChildren() {
	try {
		OpenableElementInfo info= (OpenableElementInfo)getElementInfo();
		computeChildren(info, getUnderlyingResource());
	} catch (JavaModelException e) {
		// do nothing.
	}
}
/**
 * @see ISourceManipulation#rename(String, boolean, IProgressMonitor)
 */
public void rename(String name, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (name == null) {
		throw new IllegalArgumentException(Util.bind("element.nullName")); //$NON-NLS-1$
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] dests= new IJavaElement[] {this.getParent()};
	String[] renamings= new String[] {name};
	getJavaModel().rename(elements, dests, renamings, force, monitor);
}
/*
 * @see JavaElement#rootedAt(IJavaProject)
 */
public IJavaElement rootedAt(IJavaProject project) {
	return
		new PackageFragment(
			(IPackageFragmentRoot)((JavaElement)fParent).rootedAt(project), 
			fName);
}
/**
 * Debugging purposes
 */
protected void toStringChildren(int tab, StringBuffer buffer, Object info) {
	if (tab == 0) {
		super.toStringChildren(tab, buffer, info);
	}
}
/**
 * Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	if (getElementName().length() == 0) {
		buffer.append("[default]"); //$NON-NLS-1$
	} else {
		buffer.append(getElementName());
	}
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else {
		if (tab > 0) {
			buffer.append(" (...)"); //$NON-NLS-1$
		}
	}
}
}
