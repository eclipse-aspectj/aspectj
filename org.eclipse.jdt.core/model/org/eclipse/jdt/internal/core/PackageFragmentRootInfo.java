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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * The element info for <code>PackageFragmentRoot</code>s.
 */
class PackageFragmentRootInfo extends OpenableElementInfo {

	/**
	 * The kind of the root associated with this info.
	 * Valid kinds are: <ul>
	 * <li><code>IPackageFragmentRoot.K_SOURCE</code>
	 * <li><code>IPackageFragmentRoot.K_BINARY</code></ul>
	 */
	protected int fRootKind= IPackageFragmentRoot.K_SOURCE;

	/**
	 * A array with all the non-java resources contained by this PackageFragment
	 */
	protected Object[] fNonJavaResources;
/**
 * Create and initialize a new instance of the receiver
 */
public PackageFragmentRootInfo() {
	fNonJavaResources = null;
}
/**
 * Starting at this folder, create non-java resources for this package fragment root 
 * and add them to the non-java resources collection.
 * 
 * @exception JavaModelException  The resource associated with this package fragment does not exist
 */
private Object[] computeFolderNonJavaResources(JavaProject project, IContainer folder) throws JavaModelException {
	Object[] nonJavaResources = new IResource[5];
	int nonJavaResourcesCounter = 0;
	try {
		IResource[] members = folder.members();
		for (int i = 0, max = members.length; i < max; i++) {
			IResource member = members[i];
			if (member.getType() == IResource.FILE) {
				String fileName = member.getName();
				if (!Util.isValidCompilationUnitName(fileName) && !Util.isValidClassFileName(fileName)) {
					// check case of a .zip or .jar file on classpath
					if (project.findPackageFragmentRoot0(member.getFullPath()) == null) {
						if (nonJavaResources.length == nonJavaResourcesCounter) {
							// resize
							System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter * 2]), 0, nonJavaResourcesCounter);
						}
						nonJavaResources[nonJavaResourcesCounter++] = member;
					}
				}
			} else if (member.getType() == IResource.FOLDER) {
				if (!Util.isValidFolderNameForPackage(member.getName())) {
					if (nonJavaResources.length == nonJavaResourcesCounter) {
						// resize
						System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter * 2]), 0, nonJavaResourcesCounter);
					}
					nonJavaResources[nonJavaResourcesCounter++] = member;
				}
			}
		}
		if (nonJavaResources.length != nonJavaResourcesCounter) {
			System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter]), 0, nonJavaResourcesCounter);
		}
		return nonJavaResources;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * Compute the non-package resources of this package fragment root.
 * 
 * @exception JavaModelException  The resource associated with this package fragment root does not exist
 */
private Object[] computeNonJavaResources(IJavaProject project, IResource underlyingResource) {
	Object[] nonJavaResources = NO_NON_JAVA_RESOURCES;
	try {
		// the underlying resource may be a folder or a project (in the case that the project folder
		// is actually the package fragment root)
		if (underlyingResource.getType() == IResource.FOLDER || underlyingResource.getType() == IResource.PROJECT) {
			nonJavaResources = computeFolderNonJavaResources((JavaProject)project, (IContainer) underlyingResource);
		}
	} catch (JavaModelException e) {
	}
	return nonJavaResources;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
synchronized Object[] getNonJavaResources(IJavaProject project, IResource underlyingResource) {
	Object[] nonJavaResources = fNonJavaResources;
	if (nonJavaResources == null) {
		nonJavaResources = this.computeNonJavaResources(project, underlyingResource);
		fNonJavaResources = nonJavaResources;
	}
	return nonJavaResources;
}
/**
 * Returns the kind of this root.
 */
public int getRootKind() {
	return fRootKind;
}
/**
 * Set the fNonJavaResources to res value
 */
synchronized void setNonJavaResources(Object[] resources) {
	fNonJavaResources = resources;
}
/**
 * Sets the kind of this root.
 */
protected void setRootKind(int newRootKind) {
	fRootKind = newRootKind;
}
}
