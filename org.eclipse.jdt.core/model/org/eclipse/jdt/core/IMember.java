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

/**
 * Common protocol for Java elements that can be members of types.
 * This set consists of <code>IType</code>, <code>IMethod</code>, 
 * <code>IField</code>, and <code>IInitializer</code>.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IMember extends IJavaElement, ISourceReference, ISourceManipulation {
/**
 * Returns the class file in which this member is declared, or <code>null</code>
 * if this member is not declared in a class file (for example, a source type).
 * This is a handle-only method.
 * 
 * @return the class file in which this member is declared, or <code>null</code>
 * if this member is not declared in a class file (for example, a source type)
 */
IClassFile getClassFile();
/**
 * Returns the compilation unit in which this member is declared, or <code>null</code>
 * if this member is not declared in a compilation unit (for example, a binary type).
 * This is a handle-only method.
 * 
 * @return the compilation unit in which this member is declared, or <code>null</code>
 * if this member is not declared in a compilation unit (for example, a binary type)
 */
ICompilationUnit getCompilationUnit();
/**
 * Returns the type in which this member is declared, or <code>null</code>
 * if this member is not declared in a type (for example, a top-level type).
 * This is a handle-only method.
 * 
 * @return the type in which this member is declared, or <code>null</code>
 * if this member is not declared in a type (for example, a top-level type)
 */
IType getDeclaringType();
/**
 * Returns the modifier flags for this member. The flags can be examined using class
 * <code>Flags</code>.
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource.
 * @return the modifier flags for this member
 * @see Flags
 */
int getFlags() throws JavaModelException;
/**
 * Returns the source range of this member's simple name,
 * or <code>null</code> if this member does not have a name
 * (for example, an initializer), or if this member does not have
 * associated source code (for example, a binary type).
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource.
 * @return the source range of this member's simple name,
 * or <code>null</code> if this member does not have a name
 * (for example, an initializer), or if this member does not have
 * associated source code (for example, a binary type)
 */
ISourceRange getNameRange() throws JavaModelException;
/**
 * Returns whether this member is from a class file.
 * This is a handle-only method.
 *
 * @return <code>true</code> if from a class file, and <code>false</code> if
 *   from a compilation unit
 */
boolean isBinary();
}
