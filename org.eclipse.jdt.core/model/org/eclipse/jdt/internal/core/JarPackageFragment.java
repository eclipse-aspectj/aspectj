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
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A package fragment that represents a package fragment found in a JAR.
 *
 * @see IPackageFragment
 */
class JarPackageFragment extends PackageFragment {
/**
 * Constructs a package fragment that is contained within a jar or a zip.
 */
protected JarPackageFragment(IPackageFragmentRoot root, String name) {
	super(root, name);
}
/**
 * Compute the children of this package fragment. Children of jar package fragments
 * can only be IClassFile (representing .class files).
 */
protected boolean computeChildren(OpenableElementInfo info) {
	JarPackageFragmentInfo jInfo= (JarPackageFragmentInfo)info;
	if (jInfo.fEntryNames != null){
		ArrayList vChildren = new ArrayList();
		for (Iterator iter = jInfo.fEntryNames.iterator(); iter.hasNext();) {
			String child = (String) iter.next();
			IClassFile classFile = getClassFile(child);
			vChildren.add(classFile);
		}
		IJavaElement[] children= new IJavaElement[vChildren.size()];
		vChildren.toArray(children);
		info.setChildren(children);
	} else {
		info.setChildren(JavaElementInfo.fgEmptyChildren);
	}
	return true;
}
/**
 * Compute all the non-java resources according to the entry name found in the jar file.
 */
/* package */ void computeNonJavaResources(String[] resNames, JarPackageFragmentInfo info, String zipName) {
	if (resNames == null) {
		info.setNonJavaResources(null);
		return;
	}
	int max = resNames.length;
	Object[] res = new Object[max];
	int index = 0;
	for (int i = 0; i < max; i++) {
		String resName = resNames[i];
		// consider that a .java file is not a non-java resource (see bug 12246 Packages view shows .class and .java files when JAR has source)
		if (!resName.toLowerCase().endsWith(".java")) { //$NON-NLS-1$
			if (!this.isDefaultPackage()) {
				resName = this.getElementName().replace('.', '/') + "/" + resName;//$NON-NLS-1$
			}
			res[index++] = new JarEntryFile(resName, zipName);
		}
	} 
	if (index != max) {
		System.arraycopy(res, 0, res = new Object[index], 0, index);
	}
	info.setNonJavaResources(res);
}
/**
 * Returns true if this fragment contains at least one java resource.
 * Returns false otherwise.
 */
public boolean containsJavaResources() throws JavaModelException {
	return ((JarPackageFragmentInfo) getElementInfo()).containsJavaResources();
}
/**
 * @see IPackageFragment
 */
public ICompilationUnit createCompilationUnit(String name, String contents, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see JavaElement
 */
protected OpenableElementInfo createElementInfo() {
	return new JarPackageFragmentInfo();
}
/**
 * @see IPackageFragment
 */
public IClassFile[] getClassFiles() throws JavaModelException {
	ArrayList list = getChildrenOfType(CLASS_FILE);
	IClassFile[] array= new IClassFile[list.size()];
	list.toArray(array);
	return array;
}
/**
 * A jar package fragment never contains compilation units.
 * @see IPackageFragment
 */
public ICompilationUnit[] getCompilationUnits() throws JavaModelException {
	return fgEmptyCompilationUnitList;
}
/**
 * A package fragment in a jar has no corresponding resource.
 *
 * @see IJavaElement
 */
public IResource getCorrespondingResource() throws JavaModelException {
	return null;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
public Object[] getNonJavaResources() throws JavaModelException {
	if (this.isDefaultPackage()) {
		// We don't want to show non java resources of the default package (see PR #1G58NB8)
		return JavaElementInfo.NO_NON_JAVA_RESOURCES;
	} else {
		return this.storedNonJavaResources();
	}
}
/**
 * Jars and jar entries are all read only
 */
public boolean isReadOnly() {
	return true;
}
/**
 * @see Openable#openWhenClosed()
 */
protected void openWhenClosed(IProgressMonitor pm) throws JavaModelException {
	// Open my jar
	getOpenableParent().open(pm);
}
/**
 * A package fragment in an archive cannot refresh its children.
 */
public void refreshChildren() {
	// do nothing
}
/*
 * @see JavaElement#rootedAt(IJavaProject)
 */
public IJavaElement rootedAt(IJavaProject project) {
	return
		new JarPackageFragment(
			(IPackageFragmentRoot)((JavaElement)fParent).rootedAt(project), 
			fName);
}
protected Object[] storedNonJavaResources() throws JavaModelException {
	return ((JarPackageFragmentInfo) getElementInfo()).getNonJavaResources();
}
}
