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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;

/**
 * This class is used to perform operations on multiple <code>IJavaElement</code>.
 * It is responible for running each operation in turn, collecting
 * the errors and merging the corresponding <code>JavaElementDelta</code>s.
 * <p>
 * If several errors occured, they are collected in a multi-status
 * <code>JavaModelStatus</code>. Otherwise, a simple <code>JavaModelStatus</code>
 * is thrown.
 */
public abstract class MultiOperation extends JavaModelOperation {
	/**
	 * The list of renamings supplied to the operation
	 */
	protected String[] fRenamingsList= null;
	/**
	 * Table specifying the new parent for elements being 
	 * copied/moved/renamed.
	 * Keyed by elements being processed, and
	 * values are the corresponding destination parent.
	 */
	protected Map fParentElements;
	/**
	 * Table specifying insertion positions for elements being 
	 * copied/moved/renamed. Keyed by elements being processed, and
	 * values are the corresponding insertion point.
	 * @see processElements(IProgressMonitor)
	 */
	protected Map fInsertBeforeElements= new HashMap(1);
	/**
	 * This table presents the data in <code>fRenamingList</code> in a more
	 * convenient way.
	 */
	protected Map fRenamings;
/**
 * Creates a new <code>MultiOperation</code>.
 */
protected MultiOperation(IJavaElement[] elementsToProcess, IJavaElement[] parentElements, boolean force) {
	super(elementsToProcess, parentElements, force);
	fParentElements = new HashMap(elementsToProcess.length);
	if (elementsToProcess.length == parentElements.length) {
		for (int i = 0; i < elementsToProcess.length; i++) {
			fParentElements.put(elementsToProcess[i], parentElements[i]);
		}
	} else { //same destination for all elements to be moved/copied/renamed
		for (int i = 0; i < elementsToProcess.length; i++) {
			fParentElements.put(elementsToProcess[i], parentElements[0]);
		}
	}

}
/**
 * Creates a new <code>MultiOperation</code> on <code>elementsToProcess</code>.
 */
protected MultiOperation(IJavaElement[] elementsToProcess, boolean force) {
	super(elementsToProcess, force);
}
/**
 * Convenience method to create a <code>JavaModelException</code>
 * embending a <code>JavaModelStatus</code>.
 */
protected void error(int code, IJavaElement element) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(code, element));
}
/**
 * Executes the operation.
 *
 * @exception JavaModelException if one or several errors occured during the operation.
 * If multiple errors occured, the corresponding <code>JavaModelStatus</code> is a
 * multi-status. Otherwise, it is a simple one.
 */
protected void executeOperation() throws JavaModelException {
	try {
		processElements();
	} catch (JavaModelException jme) {
		throw jme;
	} finally {
		mergeDeltas();
	}
}
/**
 * Returns the parent of the element being copied/moved/renamed.
 */
protected IJavaElement getDestinationParent(IJavaElement child) {
	return (IJavaElement)fParentElements.get(child);
}
/**
 * Returns the name to be used by the progress monitor.
 */
protected abstract String getMainTaskName();
/**
 * Returns the new name for <code>element</code>, or <code>null</code>
 * if there are no renamings specified.
 */
protected String getNewNameFor(IJavaElement element) {
	if (fRenamings != null)
		return (String) fRenamings.get(element);
	else
		return null;
}
/**
 * Sets up the renamings hashtable - keys are the elements and
 * values are the new name.
 */
private void initializeRenamings() {
	if (fRenamingsList != null && fRenamingsList.length == fElementsToProcess.length) {
		fRenamings = new HashMap(fRenamingsList.length);
		for (int i = 0; i < fRenamingsList.length; i++) {
			if (fRenamingsList[i] != null) {
				fRenamings.put(fElementsToProcess[i], fRenamingsList[i]);
			}
		}
	}
}
/**
 * Returns <code>true</code> if this operation represents a move or rename, <code>false</code>
 * if this operation represents a copy.<br>
 * Note: a rename is just a move within the same parent with a name change.
 */
protected boolean isMove() {
	return false;
}
/**
 * Returns <code>true</code> if this operation represents a rename, <code>false</code>
 * if this operation represents a copy or move.
 */
protected boolean isRename() {
	return false;
}
/**
 * Process all of the changed deltas generated by these operations.
 */
protected void mergeDeltas() {
	if (fDeltas != null) {
		JavaElementDelta rootDelta = newJavaElementDelta();
		boolean insertedTree = false;
		for (int i = 0; i < fDeltas.length; i++) {
			IJavaElementDelta delta = fDeltas[i];
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (int j = 0; j < children.length; j++) {
				JavaElementDelta projectDelta = (JavaElementDelta) children[j];
				rootDelta.insertDeltaTree(projectDelta.getElement(), projectDelta);
				insertedTree = true;
			}
		}
		if (insertedTree)
			fDeltas = new IJavaElementDelta[] {rootDelta};
		else
			fDeltas = null;
	}
}
/**
 * Subclasses must implement this method to process a given <code>IJavaElement</code>.
 */
protected abstract void processElement(IJavaElement element) throws JavaModelException;
/**
 * Processes all the <code>IJavaElement</code>s in turn, collecting errors
 * and updating the progress monitor.
 *
 * @exception JavaModelException if one or several operation(s) was unable to
 * be completed.
 */
protected void processElements() throws JavaModelException {
	beginTask(getMainTaskName(), fElementsToProcess.length);
	IJavaModelStatus[] errors = new IJavaModelStatus[3];
	int errorsCounter = 0;
	for (int i = 0; i < fElementsToProcess.length; i++) {
		try {
			verify(fElementsToProcess[i]);
			processElement(fElementsToProcess[i]);
		} catch (JavaModelException jme) {
			if (errorsCounter == errors.length) {
				// resize
				System.arraycopy(errors, 0, (errors = new IJavaModelStatus[errorsCounter*2]), 0, errorsCounter);
			}
			errors[errorsCounter++] = jme.getJavaModelStatus();
		} finally {
			worked(1);
		}
	}
	done();
	if (errorsCounter == 1) {
		throw new JavaModelException(errors[0]);
	} else if (errorsCounter > 1) {
		if (errorsCounter != errors.length) {
			// resize
			System.arraycopy(errors, 0, (errors = new IJavaModelStatus[errorsCounter]), 0, errorsCounter);
		}
		throw new JavaModelException(JavaModelStatus.newMultiStatus(errors));
	}
}
/**
 * Sets the insertion position in the new container for the modified element. The element
 * being modified will be inserted before the specified new sibling. The given sibling
 * must be a child of the destination container specified for the modified element.
 * The default is <code>null</code>, which indicates that the element is to be
 * inserted at the end of the container.
 */
public void setInsertBefore(IJavaElement modifiedElement, IJavaElement newSibling) {
	fInsertBeforeElements.put(modifiedElement, newSibling);
}
/**
 * Sets the new names to use for each element being copied. The renamings
 * correspond to the elements being processed, and the number of
 * renamings must match the number of elements being processed.
 * A <code>null</code> entry in the list indicates that an element
 * is not to be renamed.
 *
 * <p>Note that some renamings may not be used.  If both a parent
 * and a child have been selected for copy/move, only the parent
 * is changed.  Therefore, if a new name is specified for the child,
 * the child's name will not be changed.
 */
public void setRenamings(String[] renamings) {
	fRenamingsList = renamings;
	initializeRenamings();
}
/**
 * This method is called for each <code>IJavaElement</code> before
 * <code>processElement</code>. It should check that this <code>element</code>
 * can be processed.
 */
protected abstract void verify(IJavaElement element) throws JavaModelException;
/**
 * Verifies that the <code>destination</code> specified for the <code>element</code> is valid for the types of the
 * <code>element</code> and <code>destination</code>.
 */
protected void verifyDestination(IJavaElement element, IJavaElement destination) throws JavaModelException {
	if (destination == null || !destination.exists())
		error(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, destination);
	
	int destType = destination.getElementType();
	switch (element.getElementType()) {
		case IJavaElement.PACKAGE_DECLARATION :
		case IJavaElement.IMPORT_DECLARATION :
			if (destType != IJavaElement.COMPILATION_UNIT)
				error(IJavaModelStatusConstants.INVALID_DESTINATION, element);
			break;
		case IJavaElement.TYPE :
			if (destType != IJavaElement.COMPILATION_UNIT && destType != IJavaElement.TYPE)
				error(IJavaModelStatusConstants.INVALID_DESTINATION, element);
			break;
		case IJavaElement.METHOD :
		case IJavaElement.FIELD :
		case IJavaElement.INITIALIZER :
			if (destType != IJavaElement.TYPE || destination instanceof BinaryType)
				error(IJavaModelStatusConstants.INVALID_DESTINATION, element);
			break;
		case IJavaElement.COMPILATION_UNIT :
			if (destType != IJavaElement.PACKAGE_FRAGMENT)
				error(IJavaModelStatusConstants.INVALID_DESTINATION, element);
			else if (isMove() && ((ICompilationUnit) element).isWorkingCopy())
				error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT :
			IPackageFragment fragment = (IPackageFragment) element;
			IJavaElement parent = fragment.getParent();
			if (parent.isReadOnly())
				error(IJavaModelStatusConstants.READ_ONLY, element);
			else if (destType != IJavaElement.PACKAGE_FRAGMENT_ROOT)
				error(IJavaModelStatusConstants.INVALID_DESTINATION, element);
			break;
		default :
			error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element);
	}
}
/**
 * Verify that the new name specified for <code>element</code> is
 * valid for that type of Java element.
 */
protected void verifyRenaming(IJavaElement element) throws JavaModelException {
	String newName = getNewNameFor(element);
	boolean isValid = true;

	switch (element.getElementType()) {
		case IJavaElement.PACKAGE_FRAGMENT :
			if (element.getElementName().equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
				// don't allow renaming of default package (see PR #1G47GUM)
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION, element));
			}
			isValid = JavaConventions.validatePackageName(newName).getSeverity() != IStatus.ERROR;
			break;
		case IJavaElement.COMPILATION_UNIT :
			isValid = JavaConventions.validateCompilationUnitName(newName).getSeverity() != IStatus.ERROR;
			break;
		case IJavaElement.INITIALIZER :
			isValid = false; //cannot rename initializers
			break;
		default :
			isValid = JavaConventions.validateIdentifier(newName).getSeverity() != IStatus.ERROR;
			break;
	}

	if (!isValid) {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_NAME, element, newName));
	}
}
/**
 * Verifies that the positioning sibling specified for the <code>element</code> is exists and
 * its parent is the destination container of this <code>element</code>.
 */
protected void verifySibling(IJavaElement element, IJavaElement destination) throws JavaModelException {
	IJavaElement insertBeforeElement = (IJavaElement) fInsertBeforeElements.get(element);
	if (insertBeforeElement != null) {
		if (!insertBeforeElement.exists() || !insertBeforeElement.getParent().equals(destination)) {
			error(IJavaModelStatusConstants.INVALID_SIBLING, insertBeforeElement);
		}
	}
}
}
