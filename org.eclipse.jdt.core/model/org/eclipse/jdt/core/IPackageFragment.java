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
 * A package fragment is a portion of the workspace corresponding to an entire package,
 * or to a portion thereof. The distinction between a package fragment and a package
 * is that a package with some name is the union of all package fragments in the class path
 * which have the same name.
 * <p>
 * Package fragments elements need to be opened before they can be navigated or manipulated.
 * The children are of type <code>ICompilationUnit</code> (representing a source file) or 
 * <code>IClassFile</code> (representing a binary class file).
 * The children are listed in no particular order.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IPackageFragment extends IParent, IJavaElement, IOpenable, ISourceManipulation {

	/**	
	 * <p>
	 * The name of package fragment for the default package (value: the empty 
	 * string, <code>""</code>).
	 * </p>
 	*/
	public static final String DEFAULT_PACKAGE_NAME = ""; //$NON-NLS-1$
	/**
	 * Returns whether this fragment contains at least one Java resource.
	 * @return true if this fragment contains at least one Java resource, false otherwise
	 */
	boolean containsJavaResources() throws JavaModelException;
	/**
	 * Creates and returns a compilation unit in this package fragment 
	 * with the specified name and contents. No verification is performed
	 * on the contents.
	 *
	 * <p>It is possible that a compilation unit with the same name already exists in this 
	 * package fragment.
	 * The value of the <code>force</code> parameter effects the resolution of
	 * such a conflict:<ul>
	 * <li> <code>true</code> - in this case the compilation is created with the new contents</li>
	 * <li> <code>false</code> - in this case a <code>JavaModelException</code> is thrown</li>
	 * </ul>
	 *
	 * @param contents the given contents
	 * @param force specify how to handle conflict is the same name already exists
	 * @param monitor the given progress monitor
	 * @param name the given name
	 * @exception JavaModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while creating an underlying resource
	 * <li> The name is not a valid compilation unit name (INVALID_NAME)
	 * <li> The contents are <code>null</code> (INVALID_CONTENTS)
	 * </ul>
	 * @return a compilation unit in this package fragment 
	 * with the specified name and contents
	 */
	ICompilationUnit createCompilationUnit(String name, String contents, boolean force, IProgressMonitor monitor) throws JavaModelException;
	/**
	 * Returns the class file with the specified name
	 * in this package (for example, <code>"Object.class"</code>).
	 * The ".class" suffix is required.
	 * This is a handle-only method.  The class file may or may not be present.
	 * @param name the given name
	 * @return the class file with the specified name in this package
	 */
	IClassFile getClassFile(String name);
	/**
	 * Returns all of the class files in this package fragment.
	 *
	 * <p>Note: it is possible that a package fragment contains only
	 * compilation units (i.e. its kind is <code>K_SOURCE</code>), in
	 * which case this method returns an empty collection.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return all of the class files in this package fragment
	 */
	IClassFile[] getClassFiles() throws JavaModelException;
	/**
	 * Returns the compilation unit with the specified name
	 * in this package (for example, <code>"Object.java"</code>).
	 * The name has to be a valid compilation unit name.
	 * This is a handle-only method.  The compilation unit may or may not be present.
	 * @see JavaConventions#validateCompilationUnitName
	 * @param name the given name
	 * @return the compilation unit with the specified name in this package
	 */
	ICompilationUnit getCompilationUnit(String name);
	/**
	 * Returns all of the compilation units in this package fragment.
	 *
	 * <p>Note: it is possible that a package fragment contains only
	 * class files (i.e. its kind is <code>K_BINARY</code>), in which
	 * case this method returns an empty collection.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return all of the compilation units in this package fragment
	 */
	ICompilationUnit[] getCompilationUnits() throws JavaModelException;
	/**
	 * Returns the dot-separated package name of this fragment, for example
	 * <code>"java.lang"</code>, or <code>""</code> (the empty string),
	 * for the default package.
	 * 
	 * @return the dot-separated package name of this fragment
	 */
	String getElementName();
	/**
	 * Returns this package fragment's root kind encoded as an integer.
	 * A package fragment can contain <code>.java</code> source files,
	 * or <code>.class</code> files. This is a convenience method.
	 *
	 * @see IPackageFragmentRoot#K_SOURCE
	 * @see IPackageFragmentRoot#K_BINARY
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return this package fragment's root kind encoded as an integer
	 */
	int getKind() throws JavaModelException;
	/**
	 * Returns an array of non-Java resources contained in this package fragment.
	 * 
	 * @return an array of non-Java resources contained in this package fragment
	 */
	Object[] getNonJavaResources() throws JavaModelException;
	/**
	 * Returns whether this package fragment's name is
	 * a prefix of other package fragments in this package fragment's
	 * root.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return true if this package fragment's name is a prefix of other package fragments in this package fragment's root, false otherwise
	 */
	boolean hasSubpackages() throws JavaModelException;
	/**
	 * Returns whether this package fragment is a default package.
	 * This is a handle-only method.
	 * 
	 * @return true if this package fragment is a default package
	 */
	boolean isDefaultPackage();
}
