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
package org.eclipse.jdt.internal.core.search;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Collects the resource paths reported by a client to this search requestor.
 */
public class PathCollector implements IIndexSearchRequestor {
	
	/* a set of resource paths */
	public HashSet paths = new HashSet(5);
/**
 * @see IIndexSearchRequestor
 */
public void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
	this.paths.add( resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptConstructorDeclaration(String resourcePath, char[] typeName, int parameterCount) {
		
	this.paths.add(resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptConstructorReference(String resourcePath, char[] typeName, int parameterCount) {
		
	this.paths.add(resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptFieldDeclaration(String resourcePath, char[] fieldName) {
	this.paths.add(resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptFieldReference(String resourcePath, char[] fieldName) {
		
	this.paths.add(resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
		
	this.paths.add(resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptMethodDeclaration(String resourcePath, char[] methodName, int parameterCount) {
		
	this.paths.add(resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptMethodReference(String resourcePath, char[] methodName, int parameterCount) {
		
	this.paths.add(resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptPackageReference(String resourcePath, char[] packageName) {
		
	this.paths.add(resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char[] enclosingTypeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers) {
	this.paths.add(resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers) {
	this.paths.add(resourcePath);
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptTypeReference(String resourcePath, char[] typeName) {
	this.paths.add(resourcePath);
}
/**
 * Returns the files that correspond to the paths that have been collected.
 */
public IFile[] getFiles(IWorkspace workspace) {
	IFile[] result = new IFile[this.paths.size()];
	int i = 0;
	for (Iterator iter = this.paths.iterator(); iter.hasNext();) {
		String resourcePath = (String)iter.next();
		IPath path = new Path(resourcePath);
		result[i++] = workspace.getRoot().getFile(path);
	}
	return result;
}
/**
 * Returns the paths that have been collected.
 */
public String[] getPaths() {
	String[] result = new String[this.paths.size()];
	int i = 0;
	for (Iterator iter = this.paths.iterator(); iter.hasNext();) {
		result[i++] = (String)iter.next();
	}
	return result;
}
}
