/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *    IBM Corporation - initial API
 *    IBM Corporation, 2002/03/01- added notion of shared working copy
 *    IBM Corporation, 2002/26/01- added notion of IProblemRequestor
 ******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Common protocol for Java elements that support working copies.
 * <p>
 * A working copy of a Java element acts just like a regular element (handle),
 * except it is not attached to an underlying resource. A working copy is not
 * visible to the rest of the Java model. Changes in a working copy's
 * buffer are not realized in a resource. To bring the Java model up-to-date with a working
 * copy's contents, an explicit commit must be performed on the working copy. 
 * Other operations performed on a working copy update the
 * contents of the working copy's buffer but do not commit the contents
 * of the working copy.
 * </p>
 * <p>
 * Note: The contents of a working copy is determined when a working
 * copy is created, based on the current content of the element the working
 * copy is created from. If a working copy is an <code>IOpenable</code> and is explicitly
 * closed, the working copy's buffer will be thrown away. However, clients should not
 * explicitly open and close working copies.
 * </p>
 * <p>
 * The client that creates a working copy is responsible for
 * destroying the working copy. The Java model will never automatically
 * destroy or close a working copy. (Note that destroying a working copy
 * does not commit it to the model, it only frees up the memory occupied by
 * the element). After a working copy is destroyed, the working copy cannot
 * be accessed again. Non-handle methods will throw a 
 * <code>JavaModelException</code> indicating the Java element does not exist.
 * </p>
 * <p>
 * A working copy cannot be created from another working copy.
 * Calling <code>getWorkingCopy</code> on a working copy returns the receiver.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IWorkingCopy {
	
	/**
	 * Commits the contents of this working copy to its original element
	 * and underlying resource, bringing the Java model up-to-date with
	 * the current contents of the working copy.
	 *
	 * <p>It is possible that the contents of the original resource have changed
	 * since this working copy was created, in which case there is an update conflict.
	 * The value of the <code>force</code> parameter effects the resolution of
	 * such a conflict:<ul>
	 * <li> <code>true</code> - in this case the contents of this working copy are applied to
	 * 	the underlying resource even though this working copy was created before
	 *	a subsequent change in the resource</li>
	 * <li> <code>false</code> - in this case a <code>JavaModelException</code> is thrown</li>
	 * </ul>
	 *
	 * @param force a flag to handle the cases when the contents of the original resource have changed
	 * since this working copy was created
	 * @param monitor the given progress monitor
	 * @exception JavaModelException if this working copy could not commit. Reasons include:
	 * <ul>
	 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> This element is not a working copy (INVALID_ELEMENT_TYPES)
	 * <li> A update conflict (described above) (UPDATE_CONFLICT)
	 * </ul>
	 */
	void commit(boolean force, IProgressMonitor monitor) throws JavaModelException;
	
	/**
	 * Destroys this working copy, closing its buffer and discarding
	 * its structure. Subsequent attempts to access non-handle information
	 * for this working copy will result in <code>IJavaModelException</code>s. Has
	 * no effect if this element is not a working copy.
	 * <p>
	 * If this working copy is shared, it is destroyed only when the number of calls to
	 * <code>destroy()</code> is the same as the number of calls to <code>
	 * getSharedWorkingCopy(IProgressMonitor, IBufferFactory)</code>. 
	 * A REMOVED IJavaElementDelta is then reported on this working copy.
	 */
	void destroy();
	
	/**
	 * Finds the shared working copy for this element, given a <code>IBuffer</code> factory. 
	 * If no working copy has been created for this element associated with this
	 * buffer factory, returns <code>null</code>.
	 * <p>
	 * Users of this method must not destroy the resulting working copy. 
	 * 
	 * @param bufferFactory the given <code>IBuffer</code> factory
	 * @return the found shared working copy for this element, <code>null</code> if none
	 * @see IBufferFactory
	 * @since 2.0
	 */
	IJavaElement findSharedWorkingCopy(IBufferFactory bufferFactory);

	/**
	 * Returns the original element the specified working copy element was created from,
	 * or <code>null</code> if this is not a working copy element.  This is a handle
	 * only method, the returned element may or may not exist.
	 * 
	 * @return the original element the specified working copy element was created from,
	 * or <code>null</code> if this is not a working copy element
	 */
	IJavaElement getOriginal(IJavaElement workingCopyElement);
	
	/**
	 * Returns the original element this working copy was created from,
	 * or <code>null</code> if this is not a working copy.
	 * 
	 * @return the original element this working copy was created from,
	 * or <code>null</code> if this is not a working copy
	 */
	IJavaElement getOriginalElement();
	
	/** 
	 * Finds the elements in this compilation unit that correspond to
	 * the given element.
	 * An element A corresponds to an element B if:
	 * <ul>
	 * <li>A has the same element name as B.
	 * <li>If A is a method, A must have the same number of arguments as
	 *     B and the simple names of the argument types must be equals.
	 * <li>The parent of A corresponds to the parent of B recursively up to
	 *     their respective compilation units.
	 * <li>A exists.
	 * </ul>
	 * Returns <code>null</code> if no such java elements can be found
	 * or if the given element is not included in a compilation unit.
	 * 
	 * @param element the given element
	 * @return the found elements in this compilation unit that correspond to the given element
	 * @since 2.0 
	 */
	IJavaElement[] findElements(IJavaElement element);
	
	/**
	 * Finds the primary type of this compilation unit (i.e.&nbsp;the type with the same name as the
	 * compilation unit), or <code>null</code> if no such a type exists.
	 * 
	 * @return the found primary type of this compilation unit, or <code>null</code> if no such a type exists
	 * @since 2.0
	 */
	IType findPrimaryType();
	
	/**
	 * Returns a shared working copy on this element using the given factory to create
	 * the buffer, or this element if this element is already a working copy.
	 * This API can only answer an already existing working copy if it is based on the same
	 * original compilation unit AND was using the same buffer factory (i.e. as defined by <code>Object#equals</code>).	 
	 * <p>
	 * The life time of a shared working copy is as follows:
	 * <ul>
	 * <li>The first call to <code>getSharedWorkingCopy(...)</code> creates a new working copy for this
	 *     element</li>
	 * <li>Subsequent calls increment an internal counter.</li>
	 * <li>A call to <code>destroy()</code> decrements the internal counter.</li>
	 * <li>When this counter is 0, the working copy is destroyed.
	 * </ul>
	 * So users of this method must destroy exactly once the working copy.
	 * <p>
	 * Note that the buffer factory will be used for the life time of this working copy, i.e. if the 
	 * working copy is closed then reopened, this factory will be used.
	 * The buffer will be automatically initialized with the original's compilation unit content
	 * upon creation.
	 * <p>
	 * When the shared working copy instance is created, an ADDED IJavaElementDelta is reported on this
	 * working copy.
	 *
	 * @param monitor a progress monitor used to report progress while opening this compilation unit
	 *                 or <code>null</code> if no progress should be reported 
	 * @param factory the factory that creates a buffer that is used to get the content of the working copy
	 *                 or <code>null</code> if the internal factory should be used
	 * @param problemRequestor a requestor which will get notified of problems detected during
	 * 	reconciling as they are discovered. The requestor can be set to <code>null</code> indicating
	 * 	that the client is not interested in problems.
	 * @exception JavaModelException if the contents of this element can
	 *   not be determined. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * </ul>
	 * @return a shared working copy on this element using the given factory to create
	 * the buffer, or this element if this element is already a working copy
	 * @see IBufferFactory
	 * @see IProblemRequestor
	 * @since 2.0
	 */
	IJavaElement getSharedWorkingCopy(
		IProgressMonitor monitor,
		IBufferFactory factory,
		IProblemRequestor problemRequestor)
		throws JavaModelException;
		
	/**
	 * Returns a new working copy of this element if this element is not
	 * a working copy, or this element if this element is already a working copy.
	 *
	 * Note: if intending to share a working copy amongst several clients, then 
	 * <code>#getSharedWorkingCopy</code> should be used instead.
	 * 
	 * @exception JavaModelException if the contents of this element can
	 *   not be determined. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * </ul>
	 * @return a new working copy of this element if this element is not
	 * a working copy, or this element if this element is already a working copy
	 */
	IJavaElement getWorkingCopy() throws JavaModelException;
	
	/**
	 * Returns a new working copy of this element using the given factory to create
	 * the buffer, or this element if this element is already a working copy.
	 * Note that this factory will be used for the life time of this working copy, i.e. if the 
	 * working copy is closed then reopened, this factory will be reused.
	 * The buffer will be automatically initialized with the original's compilation unit content
	 * upon creation.
	 *
	 * Note: if intending to share a working copy amongst several clients, then 
	 * <code>#getSharedWorkingCopy</code> should be used instead.
	 *
	 * @param monitor a progress monitor used to report progress while opening this compilation unit
	 *                 or <code>null</code> if no progress should be reported 
	 * @param factory the factory that creates a buffer that is used to get the content of the working copy
	 *                 or <code>null</code> if the internal factory should be used
	 * @param problemRequestor a requestor which will get notified of problems detected during
	 * 	reconciling as they are discovered. The requestor can be set to <code>null</code> indicating
	 * 	that the client is not interested in problems.
	 * @exception JavaModelException if the contents of this element can
	 *   not be determined. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * </ul>
	 * @return a new working copy of this element using the given factory to create
	 * the buffer, or this element if this element is already a working copy
	 * @since 2.0
	 */
	IJavaElement getWorkingCopy(
		IProgressMonitor monitor,
		IBufferFactory factory,
		IProblemRequestor problemRequestor)
		throws JavaModelException;
		
	/**
	 * Returns whether this working copy's original element's content
	 * has not changed since the inception of this working copy.
	 * 
	 * @return true if this working copy's original element's content
	 * has not changed since the inception of this working copy, false otherwise
	 */
	boolean isBasedOn(IResource resource);
	
	/**
	 * Returns whether this element is a working copy.
	 * 
	 * @return true if this element is a working copy, false otherwise
	 */
	boolean isWorkingCopy();
	
	/**
	 * Reconciles the contents of this working copy.
	 * It performs the reconciliation by locally caching the contents of 
	 * the working copy, updating the contents, then creating a delta 
	 * over the cached contents and the new contents, and finally firing
	 * this delta.
	 * <p>
	 * If the working copy hasn't changed, then no problem will be detected,
	 * this is equivalent to <code>IWorkingCopy#reconcile(false, null)</code>.
	 * <p>
	 * Compilation problems found in the new contents are notified through the
	 * <code>IProblemRequestor</code> interface which was passed at
	 * creation, and no longer as transient markers. Therefore this API will
	 * return <code>null</code>.
	 * <p>
	 * Note: It has been assumed that added inner types should
	 * not generate change deltas.  The implementation has been
	 * modified to reflect this assumption.
	 *
	 * @exception JavaModelException if the contents of the original element
	 *		cannot be accessed. Reasons include:
	 * <ul>
	 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * </ul>
	 * @return <code>null</code>
	 */
	IMarker[] reconcile() throws JavaModelException;
	
	/**
	 * Reconciles the contents of this working copy.
	 * It performs the reconciliation by locally caching the contents of 
	 * the working copy, updating the contents, then creating a delta 
	 * over the cached contents and the new contents, and finally firing
	 * this delta.
	 * <p>
	 * The boolean argument allows to force problem detection even if the
	 * working copy is already consistent.
	 * <p>
	 * Compilation problems found in the new contents are notified through the
	 * <code>IProblemRequestor</code> interface which was passed at
	 * creation, and no longer as transient markers. Therefore this API answers
	 * nothing.
	 * <p>
	 * Note: It has been assumed that added inner types should
	 * not generate change deltas.  The implementation has been
	 * modified to reflect this assumption.
	 *
	 * @param forceProblemDetection boolean indicating whether problem should be recomputed
	 *   even if the source hasn't changed.
	 * @param monitor a progress monitor
	 * @exception JavaModelException if the contents of the original element
	 *		cannot be accessed. Reasons include:
	 * <ul>
	 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * </ul>
	 * @since 2.0
	 */
	void reconcile(boolean forceProblemDetection, IProgressMonitor monitor) throws JavaModelException;

	/**
	 * Restores the contents of this working copy to the current contents of
	 * this working copy's original element. Has no effect if this element
	 * is not a working copy.
	 *
	 * <p>Note: This is the inverse of committing the content of the
	 * working copy to the original element with <code>commit(boolean, IProgressMonitor)</code>.
	 *
	 * @exception JavaModelException if the contents of the original element
	 *		cannot be accessed.  Reasons include:
	 * <ul>
	 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * </ul>
	 */
	void restore() throws JavaModelException;
}