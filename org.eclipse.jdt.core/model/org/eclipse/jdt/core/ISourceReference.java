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
 * Common protocol for Java elements that have associated source code.
 * This set consists of <code>IClassFile</code>, <code>ICompilationUnit</code>,
 * <code>IPackageDeclaration</code>, <code>IImportDeclaration</code>,
 * <code>IImportContainer</code>, <code>IType</code>, <code>IField</code>,
 * <code>IMethod</code>, and <code>IInitializer</code>.
 * </ul>
 * <p>
 * Note: For <code>IClassFile</code>, <code>IType</code> and other members
 * derived from a binary type, the implementation returns source iff the
 * element has attached source code.
 * </p>
 * <p>
 * Source reference elements may be working copies if they were created from
 * a compilation unit that is a working copy.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPackageFragmentRoot#attachSource
 */
public interface ISourceReference {
/**
 * Returns whether this element exists in the model.
 *
 * @return <code>true</code> if this element exists in the Java model
 * @since 2.0
 */
boolean exists();
	
/**
 * Returns the source code associated with this element.
 * This extracts the substring from the source buffer containing this source
 * element. This corresponds to the source range that would be returned by
 * <code>getSourceRange</code>.
 * <p>
 * For class files, this returns the source of the entire compilation unit 
 * associated with the class file (if there is one).
 * </p>
 *
 * @return the source code, or <code>null</code> if this element has no 
 *   associated source code
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
String getSource() throws JavaModelException;
/**
 * Returns the source range associated with this element.
 * <p>
 * For class files, this returns the range of the entire compilation unit 
 * associated with the class file (if there is one).
 * </p>
 *
 * @return the source range, or <code>null</code> if this element has no 
 *   associated source code
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
ISourceRange getSourceRange() throws JavaModelException;
}
