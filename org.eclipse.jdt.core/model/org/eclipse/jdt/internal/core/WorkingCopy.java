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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Implementation of a working copy compilation unit. A working
 * copy maintains the timestamp of the resource it was created
 * from.
 */

public class WorkingCopy extends CompilationUnit {

	/**
	 * If set, this is the factory that will be used to create the buffer.
	 */
	protected IBufferFactory bufferFactory;

	/**
	 * If set, this is the problem requestor which will be used to notify problems
	 * detected during reconciling.
	 */
	protected IProblemRequestor problemRequestor;
		
	/**
	 * A counter of the number of time clients have asked for this 
	 * working copy. It is set to 1, if the working
	 * copy is not managed. When destroyed, this counter is
	 * set to 0. Once destroyed, this working copy cannot be opened
	 * and non-handle info can not be accessed. This is
	 * never true if this compilation unit is not a working
	 * copy.
	 */
	protected int useCount = 1;
	
/**
 */
protected WorkingCopy(IPackageFragment parent, String name, IBufferFactory bufferFactory) {
	this(parent, name, bufferFactory, null);
}
/**
 */
protected WorkingCopy(IPackageFragment parent, String name, IBufferFactory bufferFactory, IProblemRequestor problemRequestor) {
	super(parent, name);
	this.bufferFactory = 
		bufferFactory == null ? 
			this.getBufferManager().getDefaultBufferFactory() :
			bufferFactory;
	this.problemRequestor = problemRequestor;
}
/**
 * @see IWorkingCopy
 */
public void commit(boolean force, IProgressMonitor monitor) throws JavaModelException {
	CommitWorkingCopyOperation op= new CommitWorkingCopyOperation(this, force);
	runOperation(op, monitor);
}
/**
 * Returns a new element info for this element.
 */
protected OpenableElementInfo createElementInfo() {
	return new WorkingCopyElementInfo();
}
/**
 * @see IWorkingCopy
 */
public void destroy() {
	if (--this.useCount > 0) {
		if (SHARED_WC_VERBOSE) {
			System.out.println("Decrementing use count of shared working copy " + this.toStringWithAncestors());//$NON-NLS-1$
		}
		return;
	}
	try {
		close();
		
		// if original element is not on classpath flush it from the cache 
		IJavaElement originalElement = this.getOriginalElement();
		if (!this.getParent().exists()) {
			((CompilationUnit)originalElement).close();
		}
		
		// remove working copy from the cache
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		
		// In order to be shared, working copies have to denote the same compilation unit 
		// AND use the same buffer factory.
		// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
		Map sharedWorkingCopies = manager.sharedWorkingCopies;
		
		Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(this.bufferFactory);
		if (perFactoryWorkingCopies != null){
			if (perFactoryWorkingCopies.remove(originalElement) != null) {
				if (SHARED_WC_VERBOSE) {
					System.out.println("Destroying shared working copy " + this.toStringWithAncestors());//$NON-NLS-1$
				}
	
				// report removed java delta
				JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
				delta.removed(this);
				manager.fire(delta, JavaModelManager.DEFAULT_CHANGE_EVENT);
			}
		}		
	} catch (JavaModelException e) {
		// do nothing
	}
}

public boolean exists() {
	if (this.useCount == 0) return false; // no longer exists once destroyed
	return super.exists();
}


/**
 * Answers custom buffer factory
 */
public IBufferFactory getBufferFactory(){

	return this.bufferFactory;
}

/**
 * Working copies must be identical to be equal.
 *
 * @see Object#equals
 */
public boolean equals(Object o) {
	return this == o; 
}


/**
 * @see IWorkingCopy
 */
public IJavaElement getOriginal(IJavaElement workingCopyElement) {
	//not a element contained in a compilation unit
	int javaElementType = workingCopyElement.getElementType();
	if (javaElementType < COMPILATION_UNIT || javaElementType == CLASS_FILE) {
		return null;
	}
	if (workingCopyElement instanceof BinaryMember) {
		return null;
	}
	IJavaElement parent = workingCopyElement.getParent();
	ArrayList hierarchy = new ArrayList(4);
	
	while (parent.getElementType() > COMPILATION_UNIT) {
		hierarchy.add(parent);
		parent = parent.getParent();
	}
	if (parent.getElementType() == COMPILATION_UNIT) {
		hierarchy.add(((ICompilationUnit)parent).getOriginalElement());
	}
	
	ICompilationUnit cu = (ICompilationUnit) getOriginalElement();
	if (javaElementType == COMPILATION_UNIT) {
		parent = workingCopyElement;
	}
	if (((ICompilationUnit) parent).isWorkingCopy() && !((ICompilationUnit) parent).getOriginalElement().equals(cu)) {
		return null;
	}
	switch (javaElementType) {
		case PACKAGE_DECLARATION :
			return cu.getPackageDeclaration(workingCopyElement.getElementName());
		case IMPORT_CONTAINER :
			return cu.getImportContainer();
		case IMPORT_DECLARATION :
			return cu.getImport(workingCopyElement.getElementName());
		case TYPE :
			if (hierarchy.size() == 1) {
				return cu.getType(workingCopyElement.getElementName());
			} else {
				//inner type
				return getOriginalType(hierarchy).getType(workingCopyElement.getElementName());
			}
		case METHOD :
			IType type;
			if (hierarchy.size() == 2) {
				String typeName = ((IJavaElement) hierarchy.get(0)).getElementName();
				type = cu.getType(typeName);
			} else {
				//inner type
				type = getOriginalType(hierarchy);
			}
			return type.getMethod(workingCopyElement.getElementName(), ((IMethod) workingCopyElement).getParameterTypes());
		case FIELD :
			if (hierarchy.size() == 2) {
				String typeName = ((IJavaElement) hierarchy.get(0)).getElementName();
				type = cu.getType(typeName);
			} else {
				//inner type
				type = getOriginalType(hierarchy);
			}
			return type.getField(workingCopyElement.getElementName());
		case INITIALIZER :
			if (hierarchy.size() == 2) {
				String typeName = ((IJavaElement) hierarchy.get(0)).getElementName();
				type = cu.getType(typeName);
			} else {
				//inner type
				type = getOriginalType(hierarchy);
			}
			return type.getInitializer(((Initializer) workingCopyElement).getOccurrenceCount());
		case COMPILATION_UNIT :
			return cu;
		default :
			return null;
	}
}
/**
 * @see IWorkingCopy
 */
public IJavaElement getOriginalElement() {
	return new CompilationUnit((IPackageFragment)getParent(), getElementName());
}
protected IType getOriginalType(ArrayList hierarchy) {
	int size = hierarchy.size() - 1;
	ICompilationUnit typeCU = (ICompilationUnit) hierarchy.get(size);
	String typeName = ((IJavaElement) hierarchy.get(size - 1)).getElementName();
	IType type = typeCU.getType(typeName);
	size= size - 2;
	while (size > -1) {
		typeName = ((IJavaElement) hierarchy.get(size)).getElementName();
		type = ((IType) type).getType(typeName);
		size--;
	}
	return type;
}

/*
 * Answer requestor to notify with problems
 */
public IProblemRequestor getProblemRequestor(){
	return this.problemRequestor;
}
/*
 * @see IJavaElement
 */
public IResource getResource() {
	return null;
}

/**
 * @see IWorkingCopy
 */
public IJavaElement getSharedWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {
	return this;
}
/**
 * Returns <code>null<code> - a working copy does not have an underlying resource.
 *
 * @see IJavaElement
 */
public IResource getUnderlyingResource() throws JavaModelException {
	return null;
}
/**
 * @see IWorkingCopy
 */
public IJavaElement getWorkingCopy() throws JavaModelException {
	return this;
}
/**
 * @see IWorkingCopy
 */
public IJavaElement getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {
	return this;
}
/**
 * @see IWorkingCopy
 */
public boolean isBasedOn(IResource resource) {
	if (resource.getType() != IResource.FILE) {
		return false;
	}
	if (this.useCount == 0) {
		return false;
	}
	try {
		// if resource got deleted, then #getModificationStamp() will answer IResource.NULL_STAMP, which is always different from the cached
		// timestamp
		return ((CompilationUnitElementInfo) getElementInfo()).fTimestamp == ((IFile) resource).getModificationStamp();
	} catch (JavaModelException e) {
		return false;
	}
}
/**
 * @see IWorkingCopy
 */
public boolean isWorkingCopy() {
	return true;
}
/**
 * @see IOpenable
 * @see IWorkingCopy
 *
 * @exception JavaModelException attempting to open a read only element for something other than navigation
 * 	or if this is a working copy being opened after it has been destroyed.
 */
public void open(IProgressMonitor pm) throws JavaModelException {
	if (this.useCount == 0) { // was destroyed
		throw newNotPresentException();
	} else {
		super.open(pm);
	}
}
/**
 * @see Openable
 */
protected IBuffer openBuffer(IProgressMonitor pm) throws JavaModelException {

	if (this.useCount == 0) throw newNotPresentException(); // was destroyed
	
	// create buffer - working copies may use custom buffer factory
	IBuffer buffer = getBufferFactory().createBuffer(this);
	if (buffer == null) return null;

	// set the buffer source if needed
	if (buffer.getCharacters() == null){
		ICompilationUnit original= (ICompilationUnit)this.getOriginalElement();
		IBuffer originalBuffer = original.getBuffer();
		if (originalBuffer != null) {
			char[] originalContents = originalBuffer.getCharacters();
			if (originalContents != null) {
				buffer.setContents((char[])originalContents.clone());
			}
		}
	}

	// add buffer to buffer cache
	this.getBufferManager().addBuffer(buffer);

	// listen to buffer changes
	buffer.addBufferChangedListener(this);

	return buffer;	
}

/**
 * @see IWorkingCopy
 */ 
public IMarker[] reconcile() throws JavaModelException {
	reconcile(false, null);
	return null;
}

/**
 * @see IWorkingCopy
 */ 
public void reconcile(boolean forceProblemDetection, IProgressMonitor monitor) throws JavaModelException {

	if (this.useCount == 0) throw newNotPresentException(); //was destroyed
	
	if (monitor != null){
		if (monitor.isCanceled()) return;
		monitor.beginTask(Util.bind("element.reconciling"), 10); //$NON-NLS-1$
	}

	boolean wasConsistent = isConsistent();
	JavaElementDeltaBuilder deltaBuilder = null;

	try {
		// create the delta builder (this remembers the current content of the cu)
		if (!wasConsistent){
			deltaBuilder = new JavaElementDeltaBuilder(this);
			
			// update the element infos with the content of the working copy
			this.makeConsistent(monitor);
			deltaBuilder.buildDeltas();
	
		}

		if (monitor != null) monitor.worked(2);
		
		// force problem detection? - if structure was consistent
		if (forceProblemDetection && wasConsistent){
			if (monitor != null && monitor.isCanceled()) return;
	
			IProblemRequestor problemRequestor = this.getProblemRequestor();
			if (problemRequestor != null && problemRequestor.isActive()){
				problemRequestor.beginReporting();
				CompilationUnitProblemFinder.resolve(this, problemRequestor, monitor);
				problemRequestor.endReporting();
			}
		}
		
		// fire the deltas
		if (deltaBuilder != null){
			if ((deltaBuilder.delta != null) && (deltaBuilder.delta.getAffectedChildren().length > 0)) {
				JavaModelManager.getJavaModelManager().
					fire(deltaBuilder.delta, ElementChangedEvent.POST_RECONCILE);
			}
		}
	} finally {
		if (monitor != null) monitor.done();
	}
}

/**
 * @see IWorkingCopy
 */
public void restore() throws JavaModelException {

	if (this.useCount == 0) throw newNotPresentException(); //was destroyed

	CompilationUnit original = (CompilationUnit) getOriginalElement();
	IBuffer buffer = this.getBuffer();
	if (buffer == null) return;
	buffer.setContents(original.getContents());
	updateTimeStamp(original);
	makeConsistent(null);
}
/*
 * @see JavaElement#rootedAt(IJavaProject)
 */
public IJavaElement rootedAt(IJavaProject project) {
	return
		new WorkingCopy(
			(IPackageFragment)((JavaElement)fParent).rootedAt(project), 
			fName,
			this.bufferFactory);

}
/**
 * @see IOpenable
 */
public void save(IProgressMonitor pm, boolean force) throws JavaModelException {
	if (isReadOnly()) {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}
	IBuffer buf = getBuffer();
	if (buf != null) { // some Openables (like a JavaProject) don't have a buffer
		buf.save(pm, force);
		this.reconcile();   // not simply makeConsistent, also computes fine-grain deltas
							// in case the working copy is being reconciled already (if not it would miss
							// one iteration of deltas).
	}
}

/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	buffer.append("[Working copy] "); //$NON-NLS-1$
	super.toStringInfo(0, buffer, info);
}
protected void updateTimeStamp(CompilationUnit original) throws JavaModelException {
	long timeStamp =
		((IFile) original.getUnderlyingResource()).getModificationStamp();
	if (timeStamp == IResource.NULL_STAMP) {
		throw new JavaModelException(
			new JavaModelStatus(IJavaModelStatusConstants.INVALID_RESOURCE));
	}
	((CompilationUnitElementInfo) getElementInfo()).fTimestamp = timeStamp;
}
}
