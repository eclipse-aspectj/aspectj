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

public class IndexSearchAdapter implements IIndexSearchRequestor {
/**
 * @see IIndexSearchRequestor
 */
public void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptConstructorDeclaration(String resourcePath, char[] typeName, int parameterCount) {
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptConstructorReference(String resourcePath, char[] typeName, int parameterCount) {
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptFieldDeclaration(String resourcePath, char[] fieldName) {
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptFieldReference(String resourcePath, char[] fieldName) {
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptMethodDeclaration(String resourcePath, char[] methodName, int parameterCount) {
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptMethodReference(String resourcePath, char[] methodName, int parameterCount) {
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptPackageReference(String resourcePath, char[] packageName) {
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char[] enclosingTypeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers){
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptTypeReference(String resourcePath, char[] typeName) {
}
}
