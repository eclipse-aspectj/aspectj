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

public interface IIndexSearchRequestor {
/**
 * Accepts the declaration of a class in the compilation unit with the given resource path.
 * The class is declared in the given package and with the given type name. 
 * <p>
 * Note that the resource path can be null if the search query doesn't require it (eg. get all class names).
 */
void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName);
/**
 * Accepts the declaration of a constructor in the compilation unit with the given resource path.
 * The constructor is declared with a given name and number of arguments.
 */
void acceptConstructorDeclaration(String resourcePath, char[] typeName, int parameterCount);
/**
 * Accepts the reference to a constructor in the compilation unit with the given resource path.
 * The constructor is referenced using the given name and a number of arguments.
 *
 * Note that the resource path can be null if the search query doesn't require it.
 */
void acceptConstructorReference(String resourcePath, char[] typeName, int parameterCount);
/**
 * Accepts the declaration of a field in the compilation unit with the given resource path.
 * <p>
 * Note that the resource path can be null if the search query doesn't require it (eg. get all class names).
 * Likewise, the declaring package name and the declaring type names if the query doesn't require them.
 */
void acceptFieldDeclaration(String resourcePath, char[] fieldName);
/**
 * Accepts the reference to a field in the compilation unit with the given resource path.
 * The field is referenced using the given name 
 */
void acceptFieldReference(String resourcePath, char[] fieldName);
/**
 * Accepts the declaration of an interface in the compilation unit with the given resource path.
 * The interface is declared in the given package and with the given type name. 
 * <p>
 * Note that the resource path can be null if the search query doesn't require it (eg. get all interface names).
 */
void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName);
/**
 * Accepts the declaration of a method in the compilation unit with the given resource path.
 * The method is declared with a given method name and number of arguments.
 */
void acceptMethodDeclaration(String resourcePath, char[] methodName, int parameterCount);
/**
 * Accepts the reference to a method in the compilation unit with the given resource path.
 * The method is referenced using the given selector and a number of arguments.
 *
 * Note that the resource path can be null if the search query doesn't require it.
 */
void acceptMethodReference(String resourcePath, char[] methodName, int parameterCount);
/**
 * Accepts the reference to a package in the compilation unit with the given resource path.
 * The package is referenced using the given package name.
 *
 * Note that the resource path can be null if the search query doesn't require it.
 */
void acceptPackageReference(String resourcePath, char[] packageName);
/**
 * Accepts the reference to a supertype in the compilation unit with the given resource path.
 * Note that the resource path and/or the package name can be null.
 */
void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char[] enclosingTypeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers);
/**
 * Accepts the reference to a class in the compilation unit with the given resource path.
 * The class is referenced using the given type name.
 * <p>
 * Note that the resource path can be null if the search query doesn't require it.
 */
void acceptTypeReference(String resourcePath, char[] typeName);
}
