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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

/**
 * This operation deletes a collection of resources and all of their children.
 * It does not delete resources which do not belong to the Java Model
 * (eg GIF files).
 */
public class DeleteResourceElementsOperation extends MultiOperation {
/**
 * When executed, this operation will delete the given elements. The elements
 * to delete cannot be <code>null</code> or empty, and must have a corresponding
 * resource.
 */
protected DeleteResourceElementsOperation(IJavaElement[] elementsToProcess, boolean force) {
	super(elementsToProcess, force);
}
/**
 * Deletes the direct children of <code>frag</code> corresponding to its kind
 * (K_SOURCE or K_BINARY), and deletes the corresponding folder if it is then
 * empty.
 */
private void deletePackageFragment(IPackageFragment frag)
	throws JavaModelException {
	IResource res = frag.getCorrespondingResource();
	if (res != null && res.getType() == IResource.FOLDER) {
		// collect the children to remove
		IJavaElement[] childrenOfInterest = frag.getChildren();
		if (childrenOfInterest.length > 0) {
			IResource[] resources = new IResource[childrenOfInterest.length];
			// remove the children
			for (int i = 0; i < childrenOfInterest.length; i++) {
				resources[i] = childrenOfInterest[i].getCorrespondingResource();
			}
			deleteResources(resources, fForce);
		}

		// Discard non-java resources
		Object[] nonJavaResources = frag.getNonJavaResources();
		int actualResourceCount = 0;
		for (int i = 0, max = nonJavaResources.length; i < max; i++){
			if (nonJavaResources[i] instanceof IResource) actualResourceCount++;
		}
		IResource[] actualNonJavaResources = new IResource[actualResourceCount];
		for (int i = 0, max = nonJavaResources.length, index = 0; i < max; i++){
			if (nonJavaResources[i] instanceof IResource) actualNonJavaResources[index++] = (IResource)nonJavaResources[i];
		}
		deleteResources(actualNonJavaResources, fForce);
		
		// delete remaining files in this package (.class file in the case where Proj=src=bin)
		IResource[] remainingFiles;
		try {
			remainingFiles = ((IFolder) res).members();
		} catch (CoreException ce) {
			throw new JavaModelException(ce);
		}
		boolean isEmpty = true;
		for (int i = 0, length = remainingFiles.length; i < length; i++) {
			IResource file = remainingFiles[i];
			if (file instanceof IFile) {
				this.deleteResource(file, IResource.FORCE | IResource.KEEP_HISTORY);
			} else {
				isEmpty = false;
			}
		}
		if (isEmpty) {
			// delete recursively empty folders
			deleteEmptyPackageFragment(frag, false);
		}
	}
}
/**
 * @see MultiOperation
 */
protected String getMainTaskName() {
	return Util.bind("operation.deleteResourceProgress"); //$NON-NLS-1$
}
/**
 * @see MultiOperation. This method delegate to <code>deleteResource</code> or
 * <code>deletePackageFragment</code> depending on the type of <code>element</code>.
 */
protected void processElement(IJavaElement element) throws JavaModelException {
	switch (element.getElementType()) {
		case IJavaElement.CLASS_FILE :
		case IJavaElement.COMPILATION_UNIT :
			deleteResource(element.getCorrespondingResource(), fForce ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY);
			break;
		case IJavaElement.PACKAGE_FRAGMENT :
			deletePackageFragment((IPackageFragment) element);
			break;
		default :
			throw new JavaModelException(new JavaModelStatus(JavaModelStatus.INVALID_ELEMENT_TYPES, element));
	}
	// ensure the element is closed
	if (element instanceof IOpenable) {
		((IOpenable)element).close();
	}
}
/**
 * @see MultiOperation
 */
protected void verify(IJavaElement element) throws JavaModelException {
	if (element == null || !element.exists())
		error(JavaModelStatus.ELEMENT_DOES_NOT_EXIST, element);

	int type = element.getElementType();
	if (type <= IJavaElement.PACKAGE_FRAGMENT_ROOT || type > IJavaElement.COMPILATION_UNIT)
		error(JavaModelStatus.INVALID_ELEMENT_TYPES, element);
	else if (type == IJavaElement.PACKAGE_FRAGMENT && element instanceof JarPackageFragment)
		error(JavaModelStatus.INVALID_ELEMENT_TYPES, element);
}
}
