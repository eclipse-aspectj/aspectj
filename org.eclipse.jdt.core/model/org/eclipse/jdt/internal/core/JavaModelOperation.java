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

import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Defines behavior common to all Java Model operations
 */
public abstract class JavaModelOperation implements IWorkspaceRunnable, IProgressMonitor {
	/**
	 * The elements this operation operates on,
	 * or <code>null</code> if this operation
	 * does not operate on specific elements.
	 */
	protected IJavaElement[] fElementsToProcess;
	/**
	 * The parent elements this operation operates with
	 * or <code>null</code> if this operation
	 * does not operate with specific parent elements.
	 */
	protected IJavaElement[] fParentElements;
	/**
	 * An empty collection of <code>IJavaElement</code>s - the common
	 * empty result if no elements are created, or if this
	 * operation is not actually executed.
	 */
	protected static IJavaElement[] fgEmptyResult= new IJavaElement[] {};

	/**
	 * Collection of <code>IJavaElementDelta</code>s created by this operation.
	 * This collection starts out <code>null</code> and becomes an
	 * array of <code>IJavaElementDelta</code>s if the operation creates any
	 * deltas. This collection is registered with the Java Model notification
	 * manager if the operation completes successfully.
	 */
	protected IJavaElementDelta[] fDeltas= null;
	/**
	 * The elements created by this operation - empty
	 * until the operation actually creates elements.
	 */
	protected IJavaElement[] fResultElements= fgEmptyResult;

	/**
	 * The progress monitor passed into this operation
	 */
	protected IProgressMonitor fMonitor= null;
	/**
	 * A flag indicating whether this operation is nested.
	 */
	protected boolean fNested = false;
	/**
	 * Conflict resolution policy - by default do not force (fail on a conflict).
	 */
	protected boolean fForce= false;
	/*
	 * Whether the operation has modified resources, and thus whether resource
	 * delta notifcation will happen.
	 */
	protected boolean hasModifiedResource = false;
/**
 * A common constructor for all Java Model operations.
 */
protected JavaModelOperation(IJavaElement[] elements) {
	fElementsToProcess = elements;
}
/**
 * Common constructor for all Java Model operations.
 */
protected JavaModelOperation(IJavaElement[] elementsToProcess, IJavaElement[] parentElements) {
	fElementsToProcess = elementsToProcess;
	fParentElements= parentElements;
}
/**
 * A common constructor for all Java Model operations.
 */
protected JavaModelOperation(IJavaElement[] elementsToProcess, IJavaElement[] parentElements, boolean force) {
	fElementsToProcess = elementsToProcess;
	fParentElements= parentElements;
	fForce= force;
}
/**
 * A common constructor for all Java Model operations.
 */
protected JavaModelOperation(IJavaElement[] elements, boolean force) {
	fElementsToProcess = elements;
	fForce= force;
}
/**
 * Common constructor for all Java Model operations.
 */
protected JavaModelOperation(IJavaElement element) {
	fElementsToProcess = new IJavaElement[]{element};
}
/**
 * A common constructor for all Java Model operations.
 */
protected JavaModelOperation(IJavaElement element, boolean force) {
	fElementsToProcess = new IJavaElement[]{element};
	fForce= force;
}
/**
 * Adds the given delta to the collection of deltas
 * that this operation has created. These deltas are
 * automatically registered with the Java Model Manager
 * when the operation completes.
 */
protected void addDelta(IJavaElementDelta delta) {
	if (fDeltas == null) {
		fDeltas= new IJavaElementDelta[] {delta};
	} else {
		IJavaElementDelta[] copy= new IJavaElementDelta[fDeltas.length + 1];
		System.arraycopy(fDeltas, 0, copy, 0, fDeltas.length);
		copy[fDeltas.length]= delta;
		fDeltas= copy;
	}
}
/**
 * @see IProgressMonitor
 */
public void beginTask(String name, int totalWork) {
	if (fMonitor != null) {
		fMonitor.beginTask(name, totalWork);
	}
}
/**
 * Checks with the progress monitor to see whether this operation
 * should be canceled. An operation should regularly call this method
 * during its operation so that the user can cancel it.
 *
 * @exception OperationCanceledException if cancelling the operation has been requested
 * @see IProgressMonitor#isCanceled
 */
protected void checkCanceled() {
	if (isCanceled()) {
		throw new OperationCanceledException(Util.bind("operation.cancelled")); //$NON-NLS-1$
	}
}
/**
 * Common code used to verify the elements this operation is processing.
 * @see JavaModelOperation#verify()
 */
protected IJavaModelStatus commonVerify() {
	if (fElementsToProcess == null || fElementsToProcess.length == 0) {
		return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
	}
	for (int i = 0; i < fElementsToProcess.length; i++) {
		if (fElementsToProcess[i] == null) {
			return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
		}
	}
	return JavaModelStatus.VERIFIED_OK;
}
/**
 * Convenience method to copy resources
 */
protected void copyResources(IResource[] resources, IPath destinationPath) throws JavaModelException {
	IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
	IWorkspace workspace = resources[0].getWorkspace();
	try {
		workspace.copy(resources, destinationPath, false, subProgressMonitor);
		this.hasModifiedResource = true;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * Convenience method to create a file
 */
protected void createFile(IContainer folder, String name, InputStream contents, boolean force) throws JavaModelException {
	IFile file= folder.getFile(new Path(name));
	try {
		file.create(
			contents, 
			force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
			getSubProgressMonitor(1));
		this.hasModifiedResource = true;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * Convenience method to create a folder
 */
protected void createFolder(IContainer parentFolder, String name, boolean force) throws JavaModelException {
	IFolder folder= parentFolder.getFolder(new Path(name));
	try {
		// we should use true to create the file locally. Only VCM should use tru/false
		folder.create(
			force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
			true, // local
			getSubProgressMonitor(1));
		this.hasModifiedResource = true;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * Convenience method to delete an empty package fragment
 */
protected void deleteEmptyPackageFragment(
	IPackageFragment fragment,
	boolean force)
	throws JavaModelException {

	IContainer resource = (IContainer) fragment.getCorrespondingResource();
	IResource rootResource = fragment.getParent().getUnderlyingResource();

	try {
		resource.delete(
			force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
			getSubProgressMonitor(1));
		while (resource instanceof IFolder) {
			// deleting a package: delete the parent if it is empty (eg. deleting x.y where folder x doesn't have resources but y)
			// without deleting the package fragment root
			resource = resource.getParent();
			if (!resource.equals(rootResource) && resource.members().length == 0) {
				resource.delete(
					force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
					getSubProgressMonitor(1));
				this.hasModifiedResource = true;
			}
		}
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * Convenience method to delete a resource
 */
protected void deleteResource(IResource resource,int flags) throws JavaModelException {
	try {
		resource.delete(flags, getSubProgressMonitor(1));
		this.hasModifiedResource = true;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * Convenience method to delete resources
 */
protected void deleteResources(IResource[] resources, boolean force) throws JavaModelException {
	if (resources == null || resources.length == 0) return;
	IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
	IWorkspace workspace = resources[0].getWorkspace();
	try {
		workspace.delete(
			resources,
			force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
			subProgressMonitor);
		this.hasModifiedResource = true;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * @see IProgressMonitor
 */
public void done() {
	if (fMonitor != null) {
		fMonitor.done();
	}
}
/**
 * Verifies the operation can proceed and executes the operation.
 * Subclasses should override <code>#verify</code> and
 * <code>executeOperation</code> to implement the specific operation behavior.
 *
 * @exception JavaModelException The operation has failed.
 */
protected void execute() throws JavaModelException {
	IJavaModelStatus status= verify();
	if (status.isOK()) {
		executeOperation();
	} else {
		throw new JavaModelException(status);
	}
}
/**
 * Convenience method to run an operation within this operation
 */
public void executeNestedOperation(JavaModelOperation operation, int subWorkAmount) throws JavaModelException {
	IProgressMonitor subProgressMonitor = getSubProgressMonitor(subWorkAmount);
	// fix for 1FW7IKC, part (1)
	try {
		operation.setNested(true);
		operation.run(subProgressMonitor);
		if (operation.hasModifiedResource()) {
			this.hasModifiedResource = true;
		}
		//accumulate the nested operation deltas
		if (operation.fDeltas != null) {
			for (int i = 0; i < operation.fDeltas.length; i++) {
				addDelta(operation.fDeltas[i]);
			}
		}
	} catch (CoreException ce) {
		if (ce instanceof JavaModelException) {
			throw (JavaModelException)ce;
		} else {
			// translate the core exception to a java model exception
			if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
				Throwable e = ce.getStatus().getException();
				if (e instanceof JavaModelException) {
					throw (JavaModelException) e;
				}
			}
			throw new JavaModelException(ce);
		}
	}
}
/**
 * Performs the operation specific behavior. Subclasses must override.
 */
protected abstract void executeOperation() throws JavaModelException;
/**
 * Returns the compilation unit the given element is contained in,
 * or the element itself (if it is a compilation unit),
 * otherwise <code>null</code>.
 */
protected ICompilationUnit getCompilationUnitFor(IJavaElement element) {

	return ((JavaElement)element).getCompilationUnit();
}
/**
 * Returns the elements to which this operation applies,
 * or <code>null</code> if not applicable.
 */
protected IJavaElement[] getElementsToProcess() {
	return fElementsToProcess;
}
/**
 * Returns the element to which this operation applies,
 * or <code>null</code> if not applicable.
 */
protected IJavaElement getElementToProcess() {
	if (fElementsToProcess == null || fElementsToProcess.length == 0) {
		return null;
	}
	return fElementsToProcess[0];
}
/**
 * Returns the Java Model this operation is operating in.
 */
public IJavaModel getJavaModel() {
	if (fElementsToProcess == null || fElementsToProcess.length == 0) {
		return getParentElement().getJavaModel();
	} else {
		return fElementsToProcess[0].getJavaModel();
	}
}
/**
 * Returns the parent element to which this operation applies,
 * or <code>null</code> if not applicable.
 */
protected IJavaElement getParentElement() {
	if (fParentElements == null || fParentElements.length == 0) {
		return null;
	}
	return fParentElements[0];
}
/**
 * Returns the parent elements to which this operation applies,
 * or <code>null</code> if not applicable.
 */
protected IJavaElement[] getParentElements() {
	return fParentElements;
}
/**
 * Returns the elements created by this operation.
 */
public IJavaElement[] getResultElements() {
	return fResultElements;
}
/**
 * Creates and returns a subprogress monitor if appropriate.
 */
protected IProgressMonitor getSubProgressMonitor(int workAmount) {
	IProgressMonitor sub = null;
	if (fMonitor != null) {
		sub = new SubProgressMonitor(fMonitor, workAmount, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
	}
	return sub;
}
/**
 * Returns the <code>IWorkspace</code> this operation is working in, or
 * <code>null</code> if this operation has no elements to process.
 */
protected IWorkspace getWorkspace() {
	if (fElementsToProcess != null && fElementsToProcess.length > 0) {
		IJavaProject project = fElementsToProcess[0].getJavaProject();
		if (project != null) {
			return project.getJavaModel().getWorkspace();
		}
	}
	return null;
}
/**
 * Returns whether this operation has performed any resource modifications.
 * Returns false if this operation has not been executed yet.
 */
public boolean hasModifiedResource() {
	return !this.isReadOnly() && this.hasModifiedResource;
}
public void internalWorked(double work) {
	if (fMonitor != null) {
		fMonitor.internalWorked(work);
	}
}
/**
 * @see IProgressMonitor
 */
public boolean isCanceled() {
	if (fMonitor != null) {
		return fMonitor.isCanceled();
	}
	return false;
}
/**
 * Returns <code>true</code> if this operation performs no resource modifications,
 * otherwise <code>false</code>. Subclasses must override.
 */
public boolean isReadOnly() {
	return false;
}
/**
 * Traverses the deltas for an working copies and makes them
 * consistent.
 */
protected void makeWorkingCopiesConsistent(IJavaElementDelta[] deltas) {
	for (int i= 0; i < deltas.length; i++) {
		walkDeltaMakingWorkingCopiesConsistent(deltas[i]);
	}
}
/**
 * Convenience method to move resources
 */
protected void moveResources(IResource[] resources, IPath destinationPath) throws JavaModelException {
	IProgressMonitor subProgressMonitor = null;
	if (fMonitor != null) {
		subProgressMonitor = new SubProgressMonitor(fMonitor, resources.length, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
	}
	IWorkspace workspace = resources[0].getWorkspace();
	try {
		workspace.move(resources, destinationPath, false, subProgressMonitor);
		this.hasModifiedResource = true;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * Creates and returns a new <code>IJavaElementDelta</code>
 * on the Java Model.
 */
public JavaElementDelta newJavaElementDelta() {
	return new JavaElementDelta(getJavaModel());
}
/**
 * Registers any deltas this operation created, with the
 * Java Model manager.
 */
protected void registerDeltas() {
	if (fDeltas != null && !fNested) {
		// hook to ensure working copies remain consistent
		makeWorkingCopiesConsistent(fDeltas);
		JavaModelManager manager= (JavaModelManager)JavaModelManager.getJavaModelManager();
		for (int i= 0; i < fDeltas.length; i++) {
			manager.registerJavaModelDelta(fDeltas[i]);
		}
	}
}
/**
 * Main entry point for Java Model operations.  Executes this operation
 * and registers any deltas created.
 *
 * @see IWorkspaceRunnable
 * @exception CoreException if the operation fails
 */
public void run(IProgressMonitor monitor) throws CoreException {
	try {
		fMonitor = monitor;
		execute();
	} finally {
		registerDeltas();
	}
}
/**
 * @see IProgressMonitor
 */
public void setCanceled(boolean b) {
	if (fMonitor != null) {
		fMonitor.setCanceled(b);
	}
}
/**
 * Sets whether this operation is nested or not.
 * @see CreateElementInCUOperation#checkCanceled
 */
protected void setNested(boolean nested) {
	fNested = nested;
}
/**
 * @see IProgressMonitor
 */
public void setTaskName(String name) {
	if (fMonitor != null) {
		fMonitor.setTaskName(name);
	}
}
/**
 * @see IProgressMonitor
 */
public void subTask(String name) {
	if (fMonitor != null) {
		fMonitor.subTask(name);
	}
}
/**
 * Returns a status indicating if there is any known reason
 * this operation will fail.  Operations are verified before they
 * are run.
 *
 * Subclasses must override if they have any conditions to verify
 * before this operation executes.
 *
 * @see IJavaModelStatus
 */
protected IJavaModelStatus verify() {
	return commonVerify();
}
/**
 * Traverses the delta making any working copies consistent
 */
protected void walkDeltaMakingWorkingCopiesConsistent(IJavaElementDelta delta) {
	if (delta.getElement().getElementType() == IJavaElement.COMPILATION_UNIT) {
		ICompilationUnit unit = (ICompilationUnit) delta.getElement();
		if (unit.isWorkingCopy()) {
			try {
				unit.makeConsistent(null);
			} catch (JavaModelException e) {
			}
		}
	} else {
		IJavaElementDelta[] deltas = delta.getAffectedChildren();
		for (int i = 0; i < deltas.length; i++) {
			walkDeltaMakingWorkingCopiesConsistent(deltas[i]);
		}
	}
}
/**
 * @see IProgressMonitor
 */
public void worked(int work) {
	if (fMonitor != null) {
		fMonitor.worked(work);
		checkCanceled();
	}
}
}
