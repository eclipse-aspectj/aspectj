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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

/**
 * Common protocol for all elements provided by the Java model.
 * Java model elements are exposed to clients as handles to the actual underlying element.
 * The Java model may hand out any number of handles for each element. Handles
 * that refer to the same element are guaranteed to be equal, but not necessarily identical.
 * <p>
 * Methods annotated as "handle-only" do not require underlying elements to exist. 
 * Methods that require underlying elements to exist throw
 * a <code>JavaModelException</code> when an underlying element is missing.
 * <code>JavaModelException.isDoesNotExist</code> can be used to recognize
 * this common special case.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IJavaElement extends IAdaptable {

	/**
	 * Constant representing a Java model (workspace level object).
	 * A Java element with this type can be safely cast to <code>IJavaModel</code>.
	 */
	public static final int JAVA_MODEL = 1;

	/**
	 * Constant representing a Java project.
	 * A Java element with this type can be safely cast to <code>IJavaProject</code>.
	 */
	public static final int JAVA_PROJECT = 2;

	/**
	 * Constant representing a package fragment root.
	 * A Java element with this type can be safely cast to <code>IPackageFragmentRoot</code>.
	 */
	public static final int PACKAGE_FRAGMENT_ROOT = 3;

	/**
	 * Constant representing a package fragment.
	 * A Java element with this type can be safely cast to <code>IPackageFragment</code>.
	 */
	public static final int PACKAGE_FRAGMENT = 4;

	/**
	 * Constant representing a Java compilation unit.
	 * A Java element with this type can be safely cast to <code>ICompilationUnit</code>.
	 */
	public static final int COMPILATION_UNIT = 5;

	/**
	 * Constant representing a class file.
	 * A Java element with this type can be safely cast to <code>IClassFile</code>.
	 */
	public static final int CLASS_FILE = 6;

	/**
	 * Constant representing a type (a class or interface).
	 * A Java element with this type can be safely cast to <code>IType</code>.
	 */
	public static final int TYPE = 7;

	/**
	 * Constant representing a field.
	 * A Java element with this type can be safely cast to <code>IField</code>.
	 */
	public static final int FIELD = 8;

	/**
	 * Constant representing a method or constructor.
	 * A Java element with this type can be safely cast to <code>IMethod</code>.
	 */
	public static final int METHOD = 9;

	/**
	 * Constant representing a stand-alone instance or class initializer.
	 * A Java element with this type can be safely cast to <code>IInitializer</code>.
	 */
	public static final int INITIALIZER = 10;

	/**
	 * Constant representing a package declaration within a compilation unit.
	 * A Java element with this type can be safely cast to <code>IPackageDeclaration</code>.
	 */
	public static final int PACKAGE_DECLARATION = 11;

	/**
	 * Constant representing all import declarations within a compilation unit.
	 * A Java element with this type can be safely cast to <code>IImportContainer</code>.
	 */
	public static final int IMPORT_CONTAINER = 12;

	/**
	 * Constant representing an import declaration within a compilation unit.
	 * A Java element with this type can be safely cast to <code>IImportDeclaration</code>.
	 */
	public static final int IMPORT_DECLARATION = 13;

/**
 * Returns whether this Java element exists in the model.
 *
 * @return <code>true</code> if this element exists in the Java model
 */
boolean exists();
/**
 * Returns the first ancestor of this Java element that has the given type.
 * Returns <code>null</code> if no such an ancestor can be found.
 * This is a handle-only method.
 * 
 * @param ancestorType the given type
 * @return the first ancestor of this Java element that has the given type, null if no such an ancestor can be found
 * @since 2.0
 */
IJavaElement getAncestor(int ancestorType);
/**
 * Returns the resource that corresponds directly to this element,
 * or <code>null</code> if there is no resource that corresponds to
 * this element.
 * <p>
 * For example, the corresponding resource for an <code>ICompilationUnit</code>
 * is its underlying <code>IFile</code>. The corresponding resource for
 * an <code>IPackageFragment</code> that is not contained in an archive 
 * is its underlying <code>IFolder</code>. An <code>IPackageFragment</code>
 * contained in an archive has no corresponding resource. Similarly, there
 * are no corresponding resources for <code>IMethods</code>,
 * <code>IFields</code>, etc.
 * <p>
 *
 * @return the corresponding resource, or <code>null</code> if none
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource
 */
IResource getCorrespondingResource() throws JavaModelException;
/**
 * Returns the name of this element. This is a handle-only method.
 *
 * @return the element name
 */
String getElementName();
/**
 * Returns this element's kind encoded as an integer.
 * This is a handle-only method.
 *
 * @return the kind of element; one of the constants declared in
 *   <code>IJavaElement</code>
 * @see IJavaElement
 */
public int getElementType();
/**
 * Returns a string representation of this element handle. The format of
 * the string is not specified; however, the identifier is stable across
 * workspace sessions, and can be used to recreate this handle via the 
 * <code>JavaCore.create(String)</code> method.
 *
 * @return the string handle identifier
 * @see JavaCore#create(java.lang.String)
 */
String getHandleIdentifier();
/**
 * Returns the Java model.
 * This is a handle-only method.
 *
 * @return the Java model
 */
IJavaModel getJavaModel();
/**
 * Returns the Java project this element is contained in,
 * or <code>null</code> if this element is not contained in any Java project
 * (for instance, the <code>IJavaModel</code> is not contained in any Java 
 * project).
 * This is a handle-only method.
 *
 * @return the containing Java project, or <code>null</code> if this element is
 *   not contained in a Java project
 */
IJavaProject getJavaProject();
/**
 * Returns the first openable parent. If this element is openable, the element
 * itself is returned. Returns <code>null</code> if this element doesn't have
 * an openable parent.
 * This is a handle-only method.
 * 
 * @return the first openable parent or <code>null</code> if this element doesn't have
 * an openable parent.
 * @since 2.0
 */
IOpenable getOpenable();
/**
 * Returns the element directly containing this element,
 * or <code>null</code> if this element has no parent.
 * This is a handle-only method.
 *
 * @return the parent element, or <code>null</code> if this element has no parent
 */
IJavaElement getParent();
/**
 * Returns the path to the innermost resource enclosing this element. 
 * If this element is not included in an external archive, 
 * the path returned is the full, absolute path to the underlying resource, 
 * relative to the workbench. 
 * If this element is included in an external archive, 
 * the path returned is the absolute path to the archive in the file system.
 * This is a handle-only method.
 * 
 * @return the path to the innermost resource enclosing this element
 * @since 2.0
 */
IPath getPath();
/**
 * Returns the innermost resource enclosing this element. 
 * If this element is included in an archive and this archive is not external, 
 * this is the underlying resource corresponding to the archive. 
 * If this element is included in an external archive, <code>null</code>
 * is returned.
 * If this element is a working copy, <code>null</code> is returned.
 * This is a handle-only method.
 * 
 * @return the innermost resource enclosing this element, <code>null</code> if this 
 * element is a working copy or is included in an external archive
 * @since 2.0
 */
IResource getResource();
/**
 * Returns the smallest underlying resource that contains
 * this element, or <code>null</code> if this element is not contained
 * in a resource.
 *
 * @return the underlying resource, or <code>null</code> if none
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its underlying resource
 */
IResource getUnderlyingResource() throws JavaModelException;
/**
 * Returns whether this Java element is read-only. An element is read-only
 * if its structure cannot be modified by the java model. 
 * <p>
 * Note this is different from IResource.isReadOnly(). For example, .jar
 * files are read-only as the java model doesn't know how to add/remove 
 * elements in this file, but the underlying IFile can be writable.
 * <p>
 * This is a handle-only method.
 *
 * @return <code>true</code> if this element is read-only
 */
boolean isReadOnly();
/**
 * Returns whether the structure of this element is known. For example, for a
 * compilation unit that could not be parsed, <code>false</code> is returned.
 * If the structure of an element is unknown, navigations will return reasonable
 * defaults. For example, <code>getChildren</code> will return an empty collection.
 * <p>
 * Note: This does not imply anything about consistency with the
 * underlying resource/buffer contents.
 * </p>
 *
 * @return <code>true</code> if the structure of this element is known
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource
 */
boolean isStructureKnown() throws JavaModelException;
}
