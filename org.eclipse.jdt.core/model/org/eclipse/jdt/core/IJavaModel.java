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
package org.eclipse.jdt.core;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represent the root Java element corresponding to the workspace. 
 * Since there is only one such root element, it is commonly referred to as
 * <em>the</em> Java model element.
 * The Java model element needs to be opened before it can be navigated or manipulated.
 * The Java model element has no parent (it is the root of the Java element 
 * hierarchy). Its children are <code>IJavaProject</code>s.
 * <p>
 * This interface provides methods for performing copy, move, rename, and
 * delete operations on multiple Java elements.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients. An instance
 * of one of these handles can be created via
 * <code>JavaCore.create(workspace.getRoot())</code>.
 * </p>
 *
 * @see JavaCore#create(org.eclipse.core.resources.IWorkspaceRoot)
 */
public interface IJavaModel extends IJavaElement, IOpenable, IParent {
/**
 * Copies the given elements to the specified container(s).
 * If one container is specified, all elements are copied to that
 * container. If more than one container is specified, the number of
 * elements and containers must match, and each element is copied to
 * its associated container.
 * <p>
 * Optionally, each copy can positioned before a sibling
 * element. If <code>null</code> is specified for a given sibling, the copy
 * is inserted as the last child of its associated container.
 * </p>
 * <p>
 * Optionally, each copy can be renamed. If 
 * <code>null</code> is specified for the new name, the copy
 * is not renamed. 
 * </p>
 * <p>
 * Optionally, any existing child in the destination container with
 * the same name can be replaced by specifying <code>true</code> for
 * force. Otherwise an exception is thrown in the event that a name
 * collision occurs.
 * </p>
 *
 * @param elements the elements to copy
 * @param containers the container, or list of containers
 * @param siblings the list of siblings element any of which may be
 *   <code>null</code>; or <code>null</code>
 * @param renamings the list of new names any of which may be
 *   <code>null</code>; or <code>null</code>
 * @param replace <code>true</code> if any existing child in a target container
 *   with the target name should be replaced, and <code>false</code> to throw an
 *   exception in the event of a name collision
 * @param monitor a progress monitor
 * @exception JavaModelException if an element could not be copied. Reasons include:
 * <ul>
 * <li> A specified element, container, or sibling does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * <li> A <code>CoreException</code> occurred while updating an underlying resource
 * <li> A container is of an incompatible type (<code>INVALID_DESTINATION</code>)
 * <li> A sibling is not a child of it associated container (<code>INVALID_SIBLING</code>)
 * <li> A new name is invalid (<code>INVALID_NAME</code>)
 * <li> A child in its associated container already exists with the same
 * 		name and <code>replace</code> has been specified as <code>false</code> (<code>NAME_COLLISION</code>)
 * <li> A container or element is read-only (<code>READ_ONLY</code>) 
 * </ul>
 */
void copy(IJavaElement[] elements, IJavaElement[] containers, IJavaElement[] siblings, String[] renamings, boolean replace, IProgressMonitor monitor) throws JavaModelException;
/**
 * Deletes the given elements, forcing the operation if necessary and specified.
 *
 * @param elements the elements to delete
 * @param force a flag controlling whether underlying resources that are not
 *    in sync with the local file system will be tolerated
 * @param monitor a progress monitor
 * @exception JavaModelException if an element could not be deleted. Reasons include:
 * <ul>
 * <li> A specified element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * <li> A <code>CoreException</code> occurred while updating an underlying resource
 * <li> An element is read-only (<code>READ_ONLY</code>) 
 * </ul>
 */
void delete(IJavaElement[] elements, boolean force, IProgressMonitor monitor) throws JavaModelException;
/**
 * Returns the Java project with the given name. This is a handle-only method. 
 * The project may or may not exist.
 * 
 * @return the Java project with the given name
 */
IJavaProject getJavaProject(String name);
/**
 * Returns the Java projects in this Java model, or an empty array if there
 * are none.
 *
 * @return the Java projects in this Java model, or an empty array if there
 * are none
 * @exception JavaModelException if this request fails.
 */
IJavaProject[] getJavaProjects() throws JavaModelException;
/**
 * Returns the workspace associated with this Java model.
 * 
 * @return the workspace associated with this Java model
 */
IWorkspace getWorkspace();
/**
 * Moves the given elements to the specified container(s).
 * If one container is specified, all elements are moved to that
 * container. If more than one container is specified, the number of
 * elements and containers must match, and each element is moved to
 * its associated container.
 * <p>
 * Optionally, each element can positioned before a sibling
 * element. If <code>null</code> is specified for sibling, the element
 * is inserted as the last child of its associated container.
 * </p>
 * <p>
 * Optionally, each element can be renamed. If 
 * <code>null</code> is specified for the new name, the element
 * is not renamed. 
 * </p>
 * <p>
 * Optionally, any existing child in the destination container with
 * the same name can be replaced by specifying <code>true</code> for
 * force. Otherwise an exception is thrown in the event that a name
 * collision occurs.
 * </p>
 *
 * @param elements the elements to move
 * @param containers the container, or list of containers
 * @param siblings the list of siblings element any of which may be
 *   <code>null</code>; or <code>null</code>
 * @param renamings the list of new names any of which may be
 *   <code>null</code>; or <code>null</code>
 * @param replace <code>true</code> if any existing child in a target container
 *   with the target name should be replaced, and <code>false</code> to throw an
 *   exception in the event of a name collision
 * @param monitor a progress monitor
 * @exception JavaModelException if an element could not be moved. Reasons include:
 * <ul>
 * <li> A specified element, container, or sibling does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * <li> A <code>CoreException</code> occurred while updating an underlying resource
 * <li> A container is of an incompatible type (<code>INVALID_DESTINATION</code>)
 * <li> A sibling is not a child of it associated container (<code>INVALID_SIBLING</code>)
 * <li> A new name is invalid (<code>INVALID_NAME</code>)
 * <li> A child in its associated container already exists with the same
 * 		name and <code>replace</code> has been specified as <code>false</code> (<code>NAME_COLLISION</code>)
 * <li> A container or element is read-only (<code>READ_ONLY</code>) 
 * </ul>
 *
 * @exception IllegalArgumentException any element or container is <code>null</code>
 */
void move(IJavaElement[] elements, IJavaElement[] containers, IJavaElement[] siblings, String[] renamings, boolean replace, IProgressMonitor monitor) throws JavaModelException;

/**
 * Triggers an update of the JavaModel with respect to the referenced external archives.
 * This operation will issue a JavaModel delta describing the discovered changes, in term
 * of Java element package fragment roots added, removed or changed.
 * Note that a collection of elements can be passed so as to narrow the set of archives
 * to refresh (passing <code>null</code> along is equivalent to refreshing the entire mode). 
 * The elements can be:
 * <ul>
 * <li> package fragment roots corresponding to external archives
 * <li> Java projects, which referenced external archives will be refreshed
 * <li> Java model, all referenced external archives will be refreshed.
 * </ul>
 * <p> In case an archive is used by multiple projects, the delta issued will account for
 * all of them. This means that even if a project was not part of the elements scope, it
 * may still be notified of changes if it is referencing a library comprised in the scope.
 * <p>
 * @param elementsScope - a collection of elements defining the scope of the refresh
 * @param monitor - a progress monitor used to report progress
 * @exception JavaModelException in one of the corresponding situation:
 * <ul>
 *    <li> an exception occurs while accessing project resources </li>
 * </ul>
 * 
 * @see IJavaElementDelta
 * @since 2.0
 */
void refreshExternalArchives(IJavaElement[] elementsScope, IProgressMonitor monitor) throws JavaModelException;

/**
 * Renames the given elements as specified.
 * If one container is specified, all elements are renamed within that
 * container. If more than one container is specified, the number of
 * elements and containers must match, and each element is renamed within
 * its associated container.
 *
 * @param elements the elements to rename
 * @param destinations the container, or list of containers
 * @param names the list of new names
 * @param replace <code>true</code> if an existing child in a target container
 *   with the target name should be replaced, and <code>false</code> to throw an
 *   exception in the event of a name collision
 * @param monitor a progress monitor
 * @exception JavaModelException if an element could not be renamed. Reasons include:
 * <ul>
 * <li> A specified element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * <li> A <code>CoreException</code> occurred while updating an underlying resource
 * <li> A new name is invalid (<code>INVALID_NAME</code>)
 * <li> A child already exists with the same name and <code>replace</code> has been specified as <code>false</code> (<code>NAME_COLLISION</code>)
 * <li> An element is read-only (<code>READ_ONLY</code>) 
 * </ul>
 */
void rename(IJavaElement[] elements, IJavaElement[] destinations, String[] names, boolean replace, IProgressMonitor monitor) throws JavaModelException;

}
