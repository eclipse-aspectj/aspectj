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
package org.eclipse.jdt.core.search;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.index.impl.JarFileEntryDocument;

/**
 * An <code>IJavaSearchScope</code> defines where search result should be found by a
 * <code>SearchEngine</code>. Clients must pass an instance of this interface
 * to the <code>search(...)</code> methods. Such an instance can be created using the
 * following factory methods on <code>SearchEngine</code>: <code>createHierarchyScope(IType)</code>,
 * <code>createJavaSearchScope(IResource[])</code>, <code>createWorkspaceScope()</code>, or
 * clients may choose to implement this interface.
 */
public interface IJavaSearchScope {
	/**
	 * This constant defines the separator of the resourcePath string of the <code>encloses(String)</code>
	 * method. If present in the string, it separates the path to the jar file from the path
	 * to the .class file in the jar.
	 */
	String JAR_FILE_ENTRY_SEPARATOR = JarFileEntryDocument.JAR_FILE_ENTRY_SEPARATOR;
/**
 * Checks whether the resource at the given path is enclosed by this scope.
 *
 * @param resourcePath if the resource is contained in
 * a JAR file, the path is composed of 2 paths separated
 * by <code>JAR_FILE_ENTRY_SEPARATOR</code>: the first path is the full OS path 
 * to the JAR (if it is an external JAR), or the workspace relative <code>IPath</code>
 * to the JAR (if it is an internal JAR), 
 * the second path is the path to the resource inside the JAR.
 * @return whether the resource is enclosed by this scope
 */
public boolean encloses(String resourcePath);
/**
 * Checks whether this scope encloses the given element.
 *
 * @param element the element
 * @return <code>true</code> if the element is in this scope
 */
public boolean encloses(IJavaElement element);
/**
 * Returns the paths to the enclosing projects and JARs for this search scope.
 * 
 * @return an array of paths to the enclosing projects and JARS. A project path is
 *			the full path to the project. A JAR path is the full OS path to the JAR file.			
 */
IPath[] enclosingProjectsAndJars();
/**
 * Returns whether this scope contains any <code>.class</code> files (either
 * in folders or within JARs).
 * 
 * @return whether this scope contains any <code>.class</code> files
 * @deprecated
 */
boolean includesBinaries();
/**
 * Returns whether this scope includes classpaths defined by 
 * the projects of the resources of this search scope.
 * 
 * @return whether this scope includes classpaths
 * @deprecated 
 */
boolean includesClasspaths();
/**
 * Sets whether this scope contains any <code>.class</code> files (either
 * in folders or within JARs).
 * 
 * @param includesBinaries whether this scope contains any <code>.class</code> files
 * @deprecated Use SearchEngine.createJavaSearchScope(IJavaElement[]) with the package fragment
 * 				roots that correspond to the binaries instead
 */
public void setIncludesBinaries(boolean includesBinaries);
/**
 * Sets whether this scope includes the classpaths defined by 
 * the projects of the resources of this search scope.
 * 
 * @return includesClasspaths whether this scope includes classpaths
 * @deprecated Use SearchEngine.createJavaSearchScope(IJavaElement[]) 
 * 				with a java project instead
 */
public void setIncludesClasspaths(boolean includesClasspaths);
}
