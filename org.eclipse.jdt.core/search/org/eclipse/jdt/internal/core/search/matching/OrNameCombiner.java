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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;

public class OrNameCombiner implements IIndexSearchRequestor {

	IIndexSearchRequestor targetRequestor;
	HashtableOfObject acceptedAnswers = new HashtableOfObject(5);
		
public OrNameCombiner(IIndexSearchRequestor targetRequestor){
	this.targetRequestor = targetRequestor;
}
public void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName){

	if (!this.acceptedAnswers.containsKey(CharOperation.concat(packageName, simpleTypeName, '.'))){
		this.targetRequestor.acceptClassDeclaration(resourcePath, simpleTypeName, enclosingTypeNames, packageName);
	}
}
public void acceptConstructorDeclaration(String resourcePath, char[] typeName, int parameterCount) {}
public void acceptConstructorReference(String resourcePath, char[] typeName, int parameterCount) {}
public void acceptFieldDeclaration(String resourcePath, char[] fieldName) {}
public void acceptFieldReference(String resourcePath, char[] fieldName) {}
public void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {}
public void acceptMethodDeclaration(String resourcePath, char[] methodName, int parameterCount) {}
public void acceptMethodReference(String resourcePath, char[] methodName, int parameterCount) {}
public void acceptPackageReference(String resourcePath, char[] packageName) {}
public void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char[] enclosingTypeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers){
}
public void acceptTypeReference(String resourcePath, char[] typeName) {}
}
