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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class CompletionRequestorWrapper implements ICompletionRequestor {
	static final char[] ARG = "arg".toCharArray();  //$NON-NLS-1$
	
	ICompletionRequestor clientRequestor;
	NameLookup nameLookup;
	
public CompletionRequestorWrapper(ICompletionRequestor clientRequestor, NameLookup nameLookup){
	this.clientRequestor = clientRequestor;
	this.nameLookup = nameLookup;
}
public void acceptAnonymousType(char[] superTypePackageName,char[] superTypeName,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance){
	if(parameterNames == null)
		parameterNames = findMethodParameterNames(superTypePackageName, superTypeName, superTypeName, parameterPackageNames, parameterTypeNames);

	if(CompletionEngine.DEBUG) {
		printDebug("acceptAnonymousType",  new String[]{ //$NON-NLS-1$
			String.valueOf(superTypePackageName),
			String.valueOf(superTypeName),
			String.valueOf(parameterPackageNames),
			String.valueOf(parameterTypeNames),
			String.valueOf(parameterNames),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptAnonymousType(superTypePackageName, superTypeName, parameterPackageNames, parameterTypeNames, parameterNames, completionName, modifiers, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptClass(char[] packageName, char[] className, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptClass",  new String[]{ //$NON-NLS-1$
			String.valueOf(packageName),
			String.valueOf(className),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptClass(packageName, className, completionName, modifiers, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptError(IProblem error) {
	
	if(CompletionEngine.DEBUG) {
		System.out.print("COMPLETION - acceptError("); //$NON-NLS-1$
		System.out.print(error);
		System.out.println(")"); //$NON-NLS-1$
	}
	this.clientRequestor.acceptError(error);
}
/**
 * See ICompletionRequestor
 */
public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name, char[] typePackageName, char[] typeName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptField",  new String[]{ //$NON-NLS-1$
			String.valueOf(declaringTypePackageName),
			String.valueOf(declaringTypeName),
			String.valueOf(name),
			String.valueOf(typePackageName),
			String.valueOf(typeName),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptField(declaringTypePackageName, declaringTypeName, name, typePackageName, typeName, completionName, modifiers, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptInterface(char[] packageName, char[] interfaceName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptInterface",  new String[]{ //$NON-NLS-1$
			String.valueOf(packageName),
			String.valueOf(interfaceName),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptInterface(packageName, interfaceName, completionName, modifiers, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptKeyword(char[] keywordName, int completionStart, int completionEnd, int relevance) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptKeyword",  new String[]{ //$NON-NLS-1$
			String.valueOf(keywordName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptKeyword(keywordName, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptLabel(char[] labelName, int completionStart, int completionEnd, int relevance) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptLabel",  new String[]{ //$NON-NLS-1$
			String.valueOf(labelName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptLabel(labelName, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptLocalVariable(char[] name, char[] typePackageName, char[] typeName, int modifiers, int completionStart, int completionEnd, int relevance) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptLocalVariable",  new String[]{ //$NON-NLS-1$
			String.valueOf(name),
			String.valueOf(typePackageName),
			String.valueOf(typeName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptLocalVariable(name, typePackageName, typeName, modifiers, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
	if(parameterNames == null)
		parameterNames = findMethodParameterNames(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames);

	if(CompletionEngine.DEBUG) {
		printDebug("acceptMethod",  new String[]{ //$NON-NLS-1$
			String.valueOf(declaringTypePackageName),
			String.valueOf(declaringTypeName),
			String.valueOf(selector),
			String.valueOf(parameterPackageNames),
			String.valueOf(parameterTypeNames),
			String.valueOf(parameterNames),
			String.valueOf(returnTypePackageName),
			String.valueOf(returnTypeName),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, parameterNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptMethodDeclaration(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
	if(parameterNames == null) {
		int length = parameterTypeNames.length;
		
		parameterNames = findMethodParameterNames(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames);
		
		StringBuffer completion = new StringBuffer(completionName.length);
			
		int start = 0;
		int end = CharOperation.indexOf('%', completionName);

		completion.append(CharOperation.subarray(completionName, start, end));
		
		for(int i = 0 ; i < length ; i++){
			completion.append(parameterNames[i]);
			start = end + 1;
			end = CharOperation.indexOf('%', completionName, start);
			if(end > -1){
				completion.append(CharOperation.subarray(completionName, start, end));
			} else {
				completion.append(CharOperation.subarray(completionName, start, completionName.length));
			}
		}
		
		completionName = completion.toString().toCharArray();
	}	
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptMethodDeclaration",  new String[]{ //$NON-NLS-1$
			String.valueOf(declaringTypePackageName),
			String.valueOf(declaringTypeName),
			String.valueOf(selector),
			String.valueOf(parameterPackageNames),
			String.valueOf(parameterTypeNames),
			String.valueOf(parameterNames),
			String.valueOf(returnTypePackageName),
			String.valueOf(returnTypeName),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptMethodDeclaration(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, parameterNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptModifier(char[] modifierName, int completionStart, int completionEnd, int relevance) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptModifier",  new String[]{ //$NON-NLS-1$
			String.valueOf(modifierName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptModifier(modifierName, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptPackage(char[] packageName, char[] completionName, int completionStart, int completionEnd, int relevance) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptPackage",  new String[]{ //$NON-NLS-1$
			String.valueOf(packageName),
			String.valueOf(completionName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptPackage(packageName, completionName, completionStart, completionEnd, relevance);
}
/**
 * See ICompletionRequestor
 */
public void acceptType(char[] packageName, char[] typeName, char[] completionName, int completionStart, int completionEnd, int relevance) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptType",  new String[]{ //$NON-NLS-1$
			String.valueOf(packageName),
			String.valueOf(typeName),
			String.valueOf(completionName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptType(packageName, typeName, completionName, completionStart, completionEnd, relevance);
}
public void acceptVariableName(char[] typePackageName, char[] typeName, char[] name, char[] completionName, int completionStart, int completionEnd, int relevance){
	
	if(CompletionEngine.DEBUG) {
		System.out.println("COMPLETION - acceptVariableName"); //$NON-NLS-1$
		printDebug("acceptVariableName",  new String[]{ //$NON-NLS-1$
			String.valueOf(typePackageName),
			String.valueOf(typeName),
			String.valueOf(name),
			String.valueOf(completionName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd),
			String.valueOf(relevance)
		});
	}
	this.clientRequestor.acceptVariableName(typePackageName, typeName, name, completionName, completionStart, completionEnd, relevance);
}
private char[][] findMethodParameterNames(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames){
	char[][] parameterNames = null;
	int length = parameterTypeNames.length;
	
	char[] typeName = CharOperation.concat(declaringTypePackageName,declaringTypeName,'.');
	IType type = nameLookup.findType(new String(typeName), false, NameLookup.ACCEPT_CLASSES & NameLookup.ACCEPT_INTERFACES);
	if(type instanceof BinaryType){
		String[] args = new String[length];
		for(int i = 0;	i< length ; i++){
			char[] parameterType = CharOperation.concat(parameterPackageNames[i],parameterTypeNames[i],'.');
			args[i] = Signature.createTypeSignature(parameterType,true);
		}
		IMethod method = type.getMethod(new String(selector),args);
		try{
			parameterNames = new char[length][];
			String[] params = method.getParameterNames();
			for(int i = 0;	i< length ; i++){
				parameterNames[i] = params[i].toCharArray();
			}
		} catch(JavaModelException e){
			parameterNames = null;
		}
			
	}
	// default parameters name
	if(parameterNames == null) {
		parameterNames = new char[length][];
		for (int i = 0; i < length; i++) {
			parameterNames[i] = CharOperation.concat(ARG, String.valueOf(i).toCharArray());
		}
	}
	return parameterNames;
}

private void printDebug(String header, String[] param){
	StringBuffer buffer = new StringBuffer();
	buffer.append("COMPLETION - "); //$NON-NLS-1$
	buffer.append(header);
	buffer.append("(");//$NON-NLS-1$
	
	for (int i = 0; i < param.length; i++) {
		if(i != 0)
			buffer.append(", ");//$NON-NLS-1$
		buffer.append(param[i]);
	}

	buffer.append(")");//$NON-NLS-1$
	System.out.println(buffer.toString());
}
}
