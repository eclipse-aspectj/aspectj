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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexedFile;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

public class MethodReferencePattern extends MethodPattern {
	IType declaringType;
	public char[][][] allSuperDeclaringTypeNames;

public MethodReferencePattern(
	char[] selector, 
	int matchMode, 
	boolean isCaseSensitive,
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] returnQualification, 
	char[] returnSimpleName,
	char[][] parameterQualifications, 
	char[][] parameterSimpleNames,
	IType declaringType) {

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
	this.declaringType = declaringType;
	this.needsResolve = this.needsResolve();
}
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, word);	

	decodedParameterCount = Integer.parseInt(new String(word, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	decodedSelector = CharOperation.subarray(word, METHOD_REF.length, lastSeparatorIndex);
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		String path;
		if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
			requestor.acceptMethodReference(path, decodedSelector, decodedParameterCount);
		}
	}
}
public String getPatternName(){
	return "MethodReferencePattern: "; //$NON-NLS-1$
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
public char[] indexEntryPrefix() {

	return AbstractIndexer.bestMethodReferencePrefix(
			selector, 
			parameterSimpleNames == null ? -1 : parameterSimpleNames.length, 
			matchMode, 
			isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return METHOD | FIELD;
}

public void initializePolymorphicSearch(MatchLocator locator, IProgressMonitor progressMonitor) {
	try {
		this.allSuperDeclaringTypeNames = 
			new SuperTypeNamesCollector(
				this, 
				locator,
				this.declaringType, 
				progressMonitor).collect();
	} catch (JavaModelException e) {
		// inaccurate matches will be found
	}
}

/**
 * Returns whether the code gen will use an invoke virtual for 
 * this message send or not.
 */
private boolean isVirtualInvoke(MethodBinding method, MessageSend messageSend) {
	return !method.isStatic() && !messageSend.isSuperAccess() && !method.isPrivate();
}

/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (!(node instanceof MessageSend)) return IMPOSSIBLE_MATCH;
	MessageSend messageSend = (MessageSend)node;

	if (resolve) {
		return this.matchLevel(messageSend.binding, messageSend);
	} else {
		// selector
		if (this.selector != null && !this.matchesName(this.selector, messageSend.selector))
			return IMPOSSIBLE_MATCH;
			
		// argument types
		int argumentCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
		if (argumentCount > -1) {
			int parameterCount = messageSend.arguments == null ? 0 : messageSend.arguments.length;
			if (parameterCount != argumentCount)
				return IMPOSSIBLE_MATCH;
		}

		return this.needsResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
	}
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding, MessageSend messageSend) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof MethodBinding)) return IMPOSSIBLE_MATCH;
	int level;

	MethodBinding method = (MethodBinding)binding;

	// selector
	if (this.selector != null && !this.matchesName(this.selector, method.selector))
		return IMPOSSIBLE_MATCH;

	// receiver type
	ReferenceBinding receiverType = 
		binding == null ? 
			null : 
			method.declaringClass;
	if (this.isVirtualInvoke(method, messageSend) && (!(messageSend.receiverType instanceof ArrayBinding))) {
		level = this.matchLevelAsSubtype(receiverType, this.declaringSimpleName, this.declaringQualification);
		if (level == IMPOSSIBLE_MATCH) {
			level = this.matchLevelForType(this.allSuperDeclaringTypeNames, receiverType);
			if (level == IMPOSSIBLE_MATCH) {
				return IMPOSSIBLE_MATCH;
			}
		}
	} else {
		level = this.matchLevelForType(this.declaringSimpleName, this.declaringQualification, receiverType);
		if (level == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
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
		
	// argument types
	int argumentCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (argumentCount > -1) {
		if (method.parameters == null) {
			level = INACCURATE_MATCH;
		} else {
			int parameterCount = method.parameters.length;
			if (parameterCount != argumentCount) return IMPOSSIBLE_MATCH;
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
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (reference instanceof MessageSend) {
		// message ref are starting at the selector start
		locator.report(
			(int) (((MessageSend) reference).nameSourcePosition >> 32),
			reference.sourceEnd,
			element,
			accuracy);
	} else {
		super.matchReportReference(reference, element, accuracy, locator);
	}
	
}
}
