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

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexedFile;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

/**
 * The selector is unused, the constructor name is specified by the type simple name.
 */ 
public class ConstructorReferencePattern extends MethodReferencePattern {

	private char[] decodedTypeName;
	
public ConstructorReferencePattern(
	char[] declaringSimpleName, 
	int matchMode, 
	boolean isCaseSensitive, 
	char[] declaringQualification, 
	char[][] parameterQualifications, 
	char[][] parameterSimpleNames,
	IType declaringType) {
		
	super(
		null, 
		matchMode, 
		isCaseSensitive, 
		declaringQualification, 
		declaringSimpleName, 
		null, 
		null, 
		parameterQualifications, 
		parameterSimpleNames,
		declaringType);
}
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, word);	

	decodedParameterCount = Integer.parseInt(new String(word, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	decodedTypeName = CharOperation.subarray(word, CONSTRUCTOR_REF.length, lastSeparatorIndex);
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		String path;
		if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
			requestor.acceptConstructorReference(path, decodedTypeName, decodedParameterCount);
		}
	}
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
public char[] indexEntryPrefix() {

	return AbstractIndexer.bestConstructorReferencePrefix(
			declaringSimpleName, 
			parameterSimpleNames == null ? -1 : parameterSimpleNames.length, 
			matchMode, 
			isCaseSensitive);
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {

	/* check selector matches */
	if (declaringSimpleName != null){
		switch(matchMode){
			case EXACT_MATCH :
				if (!CharOperation.equals(declaringSimpleName, decodedTypeName, isCaseSensitive)){
					return false;
				}
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(declaringSimpleName, decodedTypeName, isCaseSensitive)){
					return false;
				}
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(declaringSimpleName, decodedTypeName, isCaseSensitive)){
					return false;
				}
		}
	}
	if (parameterSimpleNames != null){
		if (parameterSimpleNames.length != decodedParameterCount) return false;
	}
	return true;
}
public String toString(){

	StringBuffer buffer = new StringBuffer(20);
	buffer.append("ConstructorReferencePattern: "); //$NON-NLS-1$
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName);
	else if (declaringQualification != null) buffer.append("*"); //$NON-NLS-1$
	buffer.append('(');
	if (parameterSimpleNames == null) {
		buffer.append("..."); //$NON-NLS-1$
	} else {
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++){
			if (i > 0) buffer.append(", "); //$NON-NLS-1$
			if (parameterQualifications[i] != null) buffer.append(parameterQualifications[i]).append('.');
			if (parameterSimpleNames[i] == null) buffer.append('*'); else buffer.append(parameterSimpleNames[i]);
		}
	}
	buffer.append(')');
	buffer.append(", "); //$NON-NLS-1$
	switch(matchMode){
		case EXACT_MATCH : 
			buffer.append("exact match, "); //$NON-NLS-1$
			break;
		case PREFIX_MATCH :
			buffer.append("prefix match, "); //$NON-NLS-1$
			break;
		case PATTERN_MATCH :
			buffer.append("pattern match, "); //$NON-NLS-1$
			break;
	}
	if (isCaseSensitive)
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}

/**
 * Returns whether this constructor pattern  matches the given allocation expression.
 * Look at resolved information only if specified.
 */
private int matchLevel(AllocationExpression allocation, boolean resolve) {

	// constructor name is simple type name
	char[][] typeName = allocation.type.getTypeName();
	if (this.declaringSimpleName != null 
			&& !this.matchesName(this.declaringSimpleName, typeName[typeName.length-1]))
		return IMPOSSIBLE_MATCH;

	if (resolve) {
		return this.matchLevel(allocation.binding);
	} else {
		// argument types
		int argumentCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
		if (argumentCount > -1) {
			int parameterCount = allocation.arguments == null ? 0 : allocation.arguments.length;
			if (parameterCount != argumentCount)
				return IMPOSSIBLE_MATCH;
		}
		return this.needsResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
	}
}

/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (node instanceof AllocationExpression) {
		return this.matchLevel((AllocationExpression)node, resolve);
	} else if (node instanceof ExplicitConstructorCall) {
		return this.matchLevel((ExplicitConstructorCall)node, resolve);
	}
	return IMPOSSIBLE_MATCH;
}

/**
 * Returns whether this constructor pattern  matches the given explicit constructor call.
 * Look at resolved information only if specified.
 */
private int matchLevel(ExplicitConstructorCall call, boolean resolve) {
	// TBD: constructor name is super simple type name

	if (resolve) {
		return this.matchLevel(call.binding);
	} else {
		// argument types
		int argumentCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
		if (argumentCount > -1) {
			int parameterCount = call.arguments == null ? 0 : call.arguments.length;
			if (parameterCount != argumentCount)
				return IMPOSSIBLE_MATCH;
		}
		return this.needsResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
	}
}

/**
 * @see SearchPattern#matchLevel(Binding binding).
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof MethodBinding)) return IMPOSSIBLE_MATCH;
	int level;

	// declaring type
	MethodBinding method = (MethodBinding)binding;
	ReferenceBinding declaringBinding = method.declaringClass;
	level = this.matchLevelForType(this.declaringSimpleName, this.declaringQualification, declaringBinding);
	if (level == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
		
	// argument types
	int argumentCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (argumentCount > -1) {
		if (method.parameters == null) {
			level = INACCURATE_MATCH;
		} else {
			int parameterCount = method.parameters.length;
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
	}

	return level;
}
}
