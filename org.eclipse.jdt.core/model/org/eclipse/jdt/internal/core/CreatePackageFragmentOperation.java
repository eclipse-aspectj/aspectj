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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * This operation creates a new package fragment under a given package fragment root. 
 * The following must be specified: <ul>
 * <li>the package fragment root
 * <li>the package name
 * </ul>
 * <p>Any needed folders/package fragments are created.
 * If the package fragment already exists, this operation has no effect.
 * The result elements include the <code>IPackageFragment</code> created and any side effect
 * package fragments that were created.
 *
 * <p>NOTE: A default package fragment exists by default for a given root.
 *
 * <p>Possible exception conditions: <ul>
 *  <li>Package fragment root is read-only
 *  <li>Package fragment's name is taken by a simple (non-folder) resource
 * </ul>
 */
public class CreatePackageFragmentOperation extends JavaModelOperation {
	/**
	 * The fully qualified, dot-delimited, package name.
	 */
	protected String fName;
/**
 * When executed, this operation will create a package fragment with the given name
 * under the given package fragment root. The dot-separated name is broken into
 * segments. Intermediate folders are created as required for each segment.
 * If the folders already exist, this operation has no effect.
 */
public CreatePackageFragmentOperation(IPackageFragmentRoot parentElement, String packageName, boolean force) {
	super(null, new IJavaElement[]{parentElement}, force);
	fName = packageName;
}
/**
 * Execute the operation - creates the new package fragment and any
 * side effect package fragments.
 *
 * @exception JavaModelException if the operation is unable to complete
 */
protected void executeOperation() throws JavaModelException {
	JavaElementDelta delta = newJavaElementDelta();
	IPackageFragmentRoot root = (IPackageFragmentRoot) getParentElement();
	String[] names = Signature.getSimpleNames(fName);
	beginTask(Util.bind("operation.createPackageFragmentProgress"), names.length); //$NON-NLS-1$
	IContainer parentFolder = (IContainer) root.getUnderlyingResource();
	String sideEffectPackageName = ""; //$NON-NLS-1$
	ArrayList resultElements = new ArrayList(names.length);
	int i;
	for (i = 0; i < names.length; i++) {
		String subFolderName = names[i];
		sideEffectPackageName += subFolderName;
		IResource subFolder = parentFolder.findMember(subFolderName);
		if (subFolder == null) {
			createFolder(parentFolder, subFolderName, fForce);
			parentFolder = parentFolder.getFolder(new Path(subFolderName));
			IPackageFragment addedFrag = root.getPackageFragment(sideEffectPackageName);
			delta.added(addedFrag);
			resultElements.add(addedFrag);
		} else {
			parentFolder = (IContainer) subFolder;
		}
		sideEffectPackageName += '.';
		worked(1);
	}
	if (resultElements.size() > 0) {
		fResultElements = new IJavaElement[resultElements.size()];
		resultElements.toArray(fResultElements);
		addDelta(delta);
	}
	done();
}
/**
 * Possible failures: <ul>
 *  <li>NO_ELEMENTS_TO_PROCESS - the root supplied to the operation is
 * 		<code>null</code>.
 *	<li>INVALID_NAME - the name provided to the operation 
 * 		is <code>null</code> or is not a valid package fragment name.
 *	<li>READ_ONLY - the root provided to this operation is read only.
 *	<li>NAME_COLLISION - there is a pre-existing resource (file)
 * 		with the same name as a folder in the package fragment's hierarchy.
 *	<li>ELEMENT_NOT_PRESENT - the underlying resource for the root is missing
 * </ul>
 * @see IJavaModelStatus
 * @see JavaConventions
 */
public IJavaModelStatus verify() {
	if (getParentElement() == null) {
		return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
	}
	
	if (fName == null || (fName.length() > 0 && JavaConventions.validatePackageName(fName).getSeverity() == IStatus.ERROR)) {
		return new JavaModelStatus(IJavaModelStatusConstants.INVALID_NAME, fName);
	}
	IPackageFragmentRoot root = (IPackageFragmentRoot) getParentElement();
	if (root.isReadOnly()) {
		return new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, root);
	}
	String[] names = Signature.getSimpleNames(fName);
	try {
		IContainer parentFolder = (IContainer) root.getUnderlyingResource();
		int i;
		for (i = 0; i < names.length; i++) {
			IResource subFolder = parentFolder.findMember(names[i]);
			if (subFolder != null) {
				if (subFolder.getType() != IResource.FOLDER) {
					return new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION);
				}
				parentFolder = (IContainer) subFolder;
			}
		}
	} catch (JavaModelException npe) {
		return npe.getJavaModelStatus();
	}
	return JavaModelStatus.VERIFIED_OK;
}
}
