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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.jdom.DOMFactory;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.core.jdom.IDOMPackage;
import org.eclipse.jdt.internal.core.jdom.DOMNode;

/**
 * <p>This operation adds/replaces a package declaration in an existing compilation unit.
 * If the compilation unit already includes the specified package declaration,
 * it is not generated (it does not generate duplicates).
 *
 * <p>Required Attributes:<ul>
 *  <li>Compilation unit element
 *  <li>Package name
 * </ul>
 */
public class CreatePackageDeclarationOperation extends CreateElementInCUOperation {
	/**
	 * The name of the package declaration being created
	 */
	protected String fName = null;
/**
 * When executed, this operation will add a package declaration to the given compilation unit.
 */
public CreatePackageDeclarationOperation(String name, ICompilationUnit parentElement) {
	super(parentElement);
	fName= name;
}
/**
 * @see CreateTypeMemberOperation#generateElementDOM
 */
protected IDOMNode generateElementDOM() throws JavaModelException {
	IJavaElement[] children = getCompilationUnit().getChildren();
	//look for an existing package declaration
	for (int i = 0; i < children.length; i++) {
		if (children[i].getElementType() ==  IJavaElement.PACKAGE_DECLARATION) {
			IPackageDeclaration pck = (IPackageDeclaration) children[i];
			IDOMPackage pack = (IDOMPackage) ((JavaElement)pck).findNode(fCUDOM);
			if (!pack.getName().equals(fName)) {
				 // get the insertion position before setting the name, as this makes it a detailed node
				 // thus the start position is always 0
				DOMNode node = (DOMNode)pack;
				fInsertionPosition = node.getStartPosition();
				fReplacementLength = node.getEndPosition() - fInsertionPosition + 1;
				pack.setName(fName);
				fCreatedElement = (org.eclipse.jdt.internal.core.jdom.DOMNode)pack;
			} else {
				//equivalent package declaration already exists
				fCreationOccurred= false;
			}
			
			return null;
		}
	}
	IDOMPackage pack = (new DOMFactory()).createPackage();
	pack.setName(fName);
	return pack;
}
/**
 * Creates and returns the handle for the element this operation created.
 */
protected IJavaElement generateResultHandle() {
	return getCompilationUnit().getPackageDeclaration(fName);
}
/**
 * @see CreateElementInCUOperation#getMainTaskName()
 */
public String getMainTaskName(){
	return Util.bind("operation.createPackageProgress"); //$NON-NLS-1$
}
/**
 * Sets the correct position for new package declaration:<ul>
 * <li> before the first import
 * <li> if no imports, before the first type
 * <li> if no type - first thing in the CU
 * <li> 
 */
protected void initializeDefaultPosition() {
	try {
		ICompilationUnit cu = getCompilationUnit();
		IImportDeclaration[] imports = cu.getImports();
		if (imports.length > 0) {
			createBefore(imports[0]);
			return;
		}
		IType[] types = cu.getTypes();
		if (types.length > 0) {
			createBefore(types[0]);
			return;
		}
	} catch (JavaModelException npe) {
	}
}
/**
 * Possible failures: <ul>
 *  <li>NO_ELEMENTS_TO_PROCESS - no compilation unit was supplied to the operation 
 *  <li>INVALID_NAME - a name supplied to the operation was not a valid
 * 		package declaration name.
 * </ul>
 * @see IJavaModelStatus
 * @see JavaConventions
 */
public IJavaModelStatus verify() {
	IJavaModelStatus status = super.verify();
	if (!status.isOK()) {
		return status;
	}
	if (JavaConventions.validatePackageName(fName).getSeverity() == IStatus.ERROR) {
		return new JavaModelStatus(IJavaModelStatusConstants.INVALID_NAME, fName);
	}
	return JavaModelStatus.VERIFIED_OK;
}
}
