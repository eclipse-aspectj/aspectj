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

import java.io.IOException;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexedFile;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

public class MethodDeclarationPattern extends MethodPattern {
public MethodDeclarationPattern(
	char[] selector, 
	int matchMode, 
	boolean isCaseSensitive,
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] returnQualification, 
	char[] returnSimpleName,
	char[][] parameterQualifications, 
	char[][] parameterSimpleNames) {

	super(matchMode, isCaseSensitive);

	this.selector = isCaseSensitive ? selector : CharOperation.toLowerCase(selector);
	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.returnQualification = isCaseSensitive ? returnQualification : CharOperation.toLowerCase(returnQualification);
	this.returnSimpleName = isCaseSensitive ? returnSimpleName : CharOperation.toLowerCase(returnSimpleName);

	if (parameterSimpleNames != null){
		this.parameterQualifications = new char[parameterSimpleNames.length][];
		this.parameterSimpleNames = new char[parameterSimpleNames.length][];
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++){
			this.parameterQualifications[i] = isCaseSensitive ? parameterQualifications[i] : CharOperation.toLowerCase(parameterQualifications[i]);
			this.parameterSimpleNames[i] = isCaseSensitive ? parameterSimpleNames[i] : CharOperation.toLowerCase(parameterSimpleNames[i]);
		}
	}	
	this.needsResolve = this.needsResolve();
}
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, word);	

	decodedParameterCount = Integer.parseInt(new String(word, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	decodedSelector = CharOperation.subarray(word, METHOD_DECL.length, lastSeparatorIndex);
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		String path;
		if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
			requestor.acceptMethodDeclaration(path, decodedSelector, decodedParameterCount);
		}
	}
}
public String getPatternName(){
	return "MethodDeclarationPattern: "; //$NON-NLS-1$
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
public char[] indexEntryPrefix() {

	return AbstractIndexer.bestMethodDeclarationPrefix(
			selector, 
			parameterSimpleNames == null ? -1 : parameterSimpleNames.length, 
			matchMode, 
			isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return CLASS;
}
/**
 * @see SearchPattern#matchesBinary(Object, Object)
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!(binaryInfo instanceof IBinaryMethod)) return false;

	IBinaryMethod method = (IBinaryMethod)binaryInfo;
	
	// selector
	if (!this.matchesName(this.selector, method.getSelector()))
		return false;

	// declaring type
	IBinaryType declaringType = (IBinaryType)enclosingBinaryInfo;
	if (declaringType != null) {
		char[] declaringTypeName = (char[])declaringType.getName().clone();
		CharOperation.replace(declaringTypeName, '/', '.');
		if (!this.matchesType(this.declaringSimpleName, this.declaringQualification, declaringTypeName)) {
			return false;
		}
	}

	String methodDescriptor = new String(method.getMethodDescriptor()).replace('/', '.');

	// look at return type only if declaring type is not specified
	if (this.declaringSimpleName == null) {
		String returnTypeSignature = Signature.toString(Signature.getReturnType(methodDescriptor));
		if (!this.matchesType(this.returnSimpleName, this.returnQualification, returnTypeSignature.toCharArray())) {
			return false;
		}
	}
		
	// parameter types
	int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (parameterCount > -1) {
		String[] arguments = Signature.getParameterTypes(methodDescriptor);
		int argumentCount = arguments.length;
		if (parameterCount != argumentCount)
			return false;
		for (int i = 0; i < parameterCount; i++) {
			char[] qualification = this.parameterQualifications[i];
			char[] type = this.parameterSimpleNames[i];
			if (!this.matchesType(type, qualification, Signature.toString(arguments[i]).toCharArray()))
				return false;
		}
	}

	return true;
}

/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (!(node instanceof MethodDeclaration)) return IMPOSSIBLE_MATCH;

	MethodDeclaration method = (MethodDeclaration)node;

	if (resolve) {
		return this.matchLevel(method.binding);
	} else {
		// selector
		if (!this.matchesName(this.selector, method.selector))
			return IMPOSSIBLE_MATCH;

		// return type
		TypeReference methodReturnType = method.returnType;
		if (methodReturnType != null) {
			char[][] methodReturnTypeName = methodReturnType.getTypeName();
			char[] sourceName = this.toArrayName(
				methodReturnTypeName[methodReturnTypeName.length-1], 
				methodReturnType.dimensions());
			if (!this.matchesName(this.returnSimpleName, sourceName))
				return IMPOSSIBLE_MATCH;
		}
			
		// parameter types
		int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
		if (parameterCount > -1) {
			int argumentCount = method.arguments == null ? 0 : method.arguments.length;
			if (parameterCount != argumentCount)
				return IMPOSSIBLE_MATCH;
		}

		return this.needsResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
	}
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof MethodBinding)) return IMPOSSIBLE_MATCH;
	int level;

	MethodBinding method = (MethodBinding)binding;
	
	// selector
	if (!this.matchesName(this.selector, method.selector))
		return IMPOSSIBLE_MATCH;

	// declaring type
	ReferenceBinding declaringType = method.declaringClass;
	if (!method.isStatic() && !method.isPrivate()) {
		level = this.matchLevelAsSubtype(declaringType, this.declaringSimpleName, this.declaringQualification);
	} else {
		level = this.matchLevelForType(this.declaringSimpleName, this.declaringQualification, declaringType);
	}
	if (level == IMPOSSIBLE_MATCH) {
		return IMPOSSIBLE_MATCH;
	}

	// look at return type only if declaring type is not specified
	if (this.declaringSimpleName == null) {
		int newLevel = this.matchLevelForType(this.returnSimpleName, this.returnQualification, method.returnType);
		switch (newLevel) {
			case IMPOSSIBLE_MATCH:
				return IMPOSSIBLE_MATCH;
			case ACCURATE_MATCH: // keep previous level
				break;
			default: // ie. INACCURATE_MATCH
				level = newLevel;
				break;
		}
	}
		
	// parameter types
	int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (parameterCount > -1) {
		int argumentCount = method.parameters == null ? 0 : method.parameters.length;
		if (parameterCount != argumentCount)
			return IMPOSSIBLE_MATCH;
		for (int i = 0; i < parameterCount; i++) {
			char[] qualification = this.parameterQualifications[i];
			char[] type = this.parameterSimpleNames[i];
			int newLevel = this.matchLevelForType(type, qualification, method.parameters[i]);
			switch (newLevel) {
				case IMPOSSIBLE_MATCH:
					return IMPOSSIBLE_MATCH;
				case ACCURATE_MATCH: // keep previous level
					break;
				default: // ie. INACCURATE_MATCH
					level = newLevel;
					break;
			}
		}
	}

	return level;
}
}
