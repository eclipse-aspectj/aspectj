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

import java.util.HashSet;

import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;

public class OrPathCombiner implements IIndexSearchRequestor {

	IIndexSearchRequestor targetRequestor;
	HashSet acceptedAnswers = new HashSet(5);
public OrPathCombiner(IIndexSearchRequestor targetRequestor){
	this.targetRequestor = targetRequestor;
}
public void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName){

	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptClassDeclaration(resourcePath, simpleTypeName, enclosingTypeNames, packageName);
	}
}
public void acceptConstructorDeclaration(String resourcePath, char[] typeName, int parameterCount) {
	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptConstructorDeclaration(resourcePath, typeName, parameterCount);
	}		
}
public void acceptConstructorReference(String resourcePath, char[] typeName, int parameterCount) {
	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptConstructorReference(resourcePath, typeName, parameterCount);
	}			
}
public void acceptFieldDeclaration(String resourcePath, char[] fieldName) {
	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptFieldDeclaration(resourcePath, fieldName);
	}	
}
public void acceptFieldReference(String resourcePath, char[] fieldName) {
	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptFieldReference(resourcePath, fieldName);
	}		
}
public void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptInterfaceDeclaration(resourcePath, simpleTypeName, enclosingTypeNames, packageName);
	}		
}
public void acceptMethodDeclaration(String resourcePath, char[] methodName, int parameterCount) {
	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptMethodDeclaration(resourcePath, methodName, parameterCount);
	}		
}
public void acceptMethodReference(String resourcePath, char[] methodName, int parameterCount) {
	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptMethodReference(resourcePath, methodName, parameterCount);
	}			
}
public void acceptPackageReference(String resourcePath, char[] packageName) {
	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptPackageReference(resourcePath, packageName);
	}	
}
public void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char[] enclosingTypeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers){
	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptSuperTypeReference(resourcePath, qualification, typeName, enclosingTypeName, classOrInterface, superQualification, superTypeName, superClassOrInterface, modifiers);
	}
}
public void acceptTypeReference(String resourcePath, char[] typeName) {
	if (this.acceptedAnswers.add(resourcePath)){
		this.targetRequestor.acceptTypeReference(resourcePath, typeName);
	}
}
}
