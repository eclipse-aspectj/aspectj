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

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents an entire binary type (single <code>.class</code> file). 
 * A class file has a single child of type <code>IType</code>.
 * Class file elements need to be opened before they can be navigated.
 * If a class file cannot be parsed, its structure remains unknown. Use 
 * <code>IJavaElement.isStructureKnown</code> to determine whether this is the
 * case.
 * <p>
 * Note: <code>IClassFile</code> extends <code>ISourceReference</code>.
 * Source can be obtained for a class file if and only if source has been attached to this
 * class file. The source associated with a class file is the source code of
 * the compilation unit it was (nominally) generated from.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPackageFragmentRoot#attachSource
 */
 
public interface IClassFile extends IJavaElement, IParent, IOpenable, ISourceReference, ICodeAssist {
/**
 * Returns the smallest element within this class file that 
 * includes the given source position (a method, field, etc.), or
 * <code>null</code> if there is no element other than the class file
 * itself at the given position, or if the given position is not
 * within the source range of this class file.
 *
 * @param position a source position inside the class file
 * @return the innermost Java element enclosing a given source position or <code>null</code>
 *  if none (excluding the class file).
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
IJavaElement getElementAt(int position) throws JavaModelException;
/**
 * Returns the type contained in this class file.
 *
 * @return the type contained in this class file
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
IType getType() throws JavaModelException;
/**
 * Returns a working copy on the source associated with this class file using the given 
 * factory to create the buffer, or <code>null</code> if there is no source associated
 * with the class file.
 * <p>
 * The buffer will be automatically initialized with the source of the class file
 * upon creation.
 * <p>
 * The only valid operations on this working copy are <code>getBuffer()</code> or <code>getOriginalElement</code>.
 *
 * @param monitor a progress monitor used to report progress while opening this compilation unit
 *                 or <code>null</code> if no progress should be reported 
 * @param factory the factory that creates a buffer that is used to get the content of the working copy
 *                 or <code>null</code> if the internal factory should be used
 * @exception JavaModelException if the source of this class file can
 *   not be determined. Reasons include:
 * <ul>
 * <li> This class file does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * </ul>
 * @since 2.0
 */
IJavaElement getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory) throws JavaModelException;
/**
 * Returns whether this type represents a class. This is not guaranteed to be
 * instantaneous, as it may require parsing the underlying file.
 *
 * @return <code>true</code> if the class file represents a class.
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
boolean isClass() throws JavaModelException;
/**
 * Returns whether this type represents an interface. This is not guaranteed to
 * be instantaneous, as it may require parsing the underlying file. 
 *
 * @return <code>true</code> if the class file represents an interface.
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
boolean isInterface() throws JavaModelException;
}
