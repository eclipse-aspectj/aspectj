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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BindingIds;
import org.eclipse.jdt.internal.compiler.lookup.ProblemBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexedFile;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

public class TypeReferencePattern extends MultipleSearchPattern {

	private char[] qualification;
	private char[] simpleName;

	private char[] decodedQualification;
	private char[] decodedSimpleName;

	private static char[][] TAGS = { TYPE_REF, SUPER_REF, REF, CONSTRUCTOR_REF };
	private static char[][] REF_TAGS = { REF };

	/* Optimization: case where simpleName == null */
	private char[][] segments;
	private int currentSegment;
	private char[] decodedSegment;
	
public TypeReferencePattern(
	char[] qualification,
	char[] simpleName,
	int matchMode, 
	boolean isCaseSensitive) {

	super(matchMode, isCaseSensitive);

	this.qualification = isCaseSensitive ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);

	if (simpleName == null) {
		this.segments = qualification == null ? ONE_STAR_CHAR : CharOperation.splitOn('.', qualification);
	}
	
	this.needsResolve = qualification != null;
}
/**
 * Either decode ref/name, typeRef/name or superRef/superName/name
 */ 
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;
	int tagLength = currentTag.length;
	int nameLength = CharOperation.indexOf(SEPARATOR, word, tagLength);
	if (nameLength < 0) nameLength = size;
	if (this.simpleName == null) {
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		this.decodedSegment = CharOperation.subarray(word, tagLength, nameLength);
	} else {
		this.decodedSimpleName = CharOperation.subarray(word, tagLength, nameLength);
	}
}
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	if (currentTag == REF) {
		foundAmbiguousIndexMatches = true;
	}
	for (int i = 0, max = references.length; i < max; i++) {
		int reference = references[i];
		if (reference != -1) { // if the reference has not been eliminated
			IndexedFile file = input.getIndexedFile(reference);
			String path;
			if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
				requestor.acceptTypeReference(path, decodedSimpleName);
			}
		}
	}
}
protected char[][] getPossibleTags(){
	if (this.simpleName == null) {
		return REF_TAGS;
	} else {
		return TAGS;
	}
}
/**
 * @see AndPattern#hasNextQuery
 */
protected boolean hasNextQuery() {
	if (this.simpleName == null) {
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		if (this.segments.length > 2) {
			// if package has more than 2 segments, don't look at the first 2 since they are mostly
			// redundant (eg. in 'org.eclipse.jdt.core.*', 'org.eclipse is used all the time)
			return --this.currentSegment >= 2;
		} else {
			return --this.currentSegment >= 0;
		}
	} else {
		return false;
	}
}
/**
 * see SearchPattern.indexEntryPrefix()
 */
public char[] indexEntryPrefix(){

	if (this.simpleName == null) {
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		return AbstractIndexer.bestReferencePrefix(
			REF,
			this.segments[this.currentSegment],
			matchMode, 
			isCaseSensitive);
	} else {
		return AbstractIndexer.bestReferencePrefix(
			currentTag,
			simpleName,
			matchMode, 
			isCaseSensitive);
	}
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return COMPILATION_UNIT | CLASS | METHOD | FIELD;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {

	/* check type name matches */
	if (simpleName == null) {
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		switch(matchMode){
			case EXACT_MATCH :
				if (!CharOperation.equals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive)){
					return false;
				}
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive)){
					return false;
				}
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive)){
					return false;
				}
		}
	} else {
		switch(matchMode){
			case EXACT_MATCH :
				if (!CharOperation.equals(simpleName, decodedSimpleName, isCaseSensitive)){
					return false;
				}
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(simpleName, decodedSimpleName, isCaseSensitive)){
					return false;
				}
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(simpleName, decodedSimpleName, isCaseSensitive)){
					return false;
				}
		}
	}
	return true;
}
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (reference instanceof QualifiedNameReference) {
		this.matchReportReference((QualifiedNameReference)reference, element, accuracy, locator);
	} else if (reference instanceof QualifiedTypeReference) {
		this.matchReportReference((QualifiedTypeReference)reference, element, accuracy, locator);
	} else if (reference instanceof ArrayTypeReference) {
		this.matchReportReference((ArrayTypeReference)reference, element, accuracy, locator);
	} else {
		super.matchReportReference(reference, element, accuracy, locator);
	}
}
/**
 * Reports the match of the given qualified name reference.
 */
protected void matchReportReference(QualifiedNameReference qNameRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = null;
	Binding binding = qNameRef.binding;
	TypeBinding typeBinding = null;
	char[][] nameTokens = qNameRef.tokens;
	int lastIndex = nameTokens.length-1;
	switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
		case BindingIds.FIELD : // reading a field
			typeBinding = qNameRef.actualReceiverType;
			int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
			lastIndex -= otherBindingsCount + 1;
			break;
		case BindingIds.TYPE : //=============only type ==============
			typeBinding = (TypeBinding)binding;
			break;
		case BindingIds.VARIABLE : //============unbound cases===========
		case BindingIds.TYPE | BindingIds.VARIABLE :						
			if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				typeBinding = pbBinding.searchType; // second chance with recorded type so far
				char[] partialQualifiedName = pbBinding.name;
				lastIndex = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
			}
			break;
	}
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0){
		if (this.matchesName(this.simpleName, nameTokens[lastIndex--])) {
			int level = this.matchLevelForType(this.simpleName, this.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) {
				tokens = new char[lastIndex+2][];
				System.arraycopy(nameTokens, 0, tokens, 0, lastIndex+2);
				break;
			}
		}
		if (typeBinding instanceof ReferenceBinding){
			typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
		} else {
			typeBinding = null;
		}
	} 
	if (tokens == null) {
		if (binding == null || binding instanceof ProblemBinding) {
			tokens = new char[][] {this.simpleName};
		} else {
			tokens = qNameRef.tokens;
		}
		if (!this.isCaseSensitive) {
			int length = tokens.length;
			char[][] lowerCaseTokens = new char[length][];
			for (int i = 0; i < length; i++) {
				char[] token = tokens[i];
				lowerCaseTokens[i] = CharOperation.toLowerCase(token);
			}
			tokens = lowerCaseTokens;
		}
	}
	locator.reportAccurateReference(qNameRef.sourceStart, qNameRef.sourceEnd, tokens, element, accuracy);
}
/**
 * Reports the match of the given qualified type reference.
 */
protected void matchReportReference(QualifiedTypeReference qTypeRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = null;
	TypeBinding typeBinding = qTypeRef.binding;
	if (typeBinding instanceof ArrayBinding) {
		typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
	}
	char[][] typeTokens = qTypeRef.tokens;
	int lastIndex = typeTokens.length-1;
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0){
		if (matchesName(this.simpleName, typeTokens[lastIndex--])) {
			int level = this.matchLevelForType(this.simpleName, this.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) {
				tokens = new char[lastIndex+2][];
				System.arraycopy(typeTokens, 0, tokens, 0, lastIndex+2);
				break;
			}
		}
		if (typeBinding instanceof ReferenceBinding){
			typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
		} else {
			typeBinding = null;
		}
	}
	if (tokens == null) {
		if (typeBinding == null || typeBinding instanceof ProblemReferenceBinding) {
			tokens = new char[][] {this.simpleName};
		} else {
			tokens = qTypeRef.tokens;
		}
		if (!this.isCaseSensitive) {
			int length = tokens.length;
			char[][] lowerCaseTokens = new char[length][];
			for (int i = 0; i < length; i++) {
				char[] token = tokens[i];
				lowerCaseTokens[i] = CharOperation.toLowerCase(token);
			}
			tokens = lowerCaseTokens;
		}
	}
	locator.reportAccurateReference(qTypeRef.sourceStart, qTypeRef.sourceEnd, tokens, element, accuracy);
}
/**
 * @see AndPattern#resetQuery
 */
protected void resetQuery() {
	if (this.simpleName == null) {
		/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
		this.currentSegment = this.segments.length - 1;
	}
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("TypeReferencePattern: pkg<"); //$NON-NLS-1$
	if (qualification != null) buffer.append(qualification);
	buffer.append(">, type<"); //$NON-NLS-1$
	if (simpleName != null) buffer.append(simpleName);
	buffer.append(">, "); //$NON-NLS-1$
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
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (node instanceof TypeReference) {
		return this.matchLevel((TypeReference)node, resolve);
	} else if (node instanceof NameReference) {
		return this.matchLevel((NameReference)node, resolve);
	} else if (node instanceof ImportReference) {
		return this.matchLevel((ImportReference)node, resolve);
	}
	return IMPOSSIBLE_MATCH;
}

/**
 * Returns whether this type pattern matches the given import reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(ImportReference importRef, boolean resolve) {

	if (importRef.onDemand) return IMPOSSIBLE_MATCH;

	char[][] tokens = importRef.tokens;
	int importLength = tokens.length;
	
	if (this.qualification != null) {
		char[] pattern;
		if (this.simpleName == null) {
			pattern = this.qualification;
		} else {
			pattern = CharOperation.concat(this.qualification, this.simpleName, '.');
		}
		char[] qualifiedTypeName = CharOperation.concatWith(importRef.tokens, '.');
		switch (this.matchMode) {
			case EXACT_MATCH :
			case PREFIX_MATCH :
				if (CharOperation.prefixEquals(pattern, qualifiedTypeName, this.isCaseSensitive)) {
					return POSSIBLE_MATCH;
				} 
				break;
			case PATTERN_MATCH:
				if (CharOperation.match(pattern, qualifiedTypeName, this.isCaseSensitive)) {
					return POSSIBLE_MATCH;
				}
				break;
		}
		return IMPOSSIBLE_MATCH;
	} else {
		if (this.simpleName == null) {
			return this.needsResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
		} else {
			for (int i = 0; i < importLength; i++){
				if (this.matchesName(this.simpleName, tokens[i])){
					return this.needsResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
				}
			}
			return IMPOSSIBLE_MATCH;
		}
	}
}

/**
 * Returns whether this type pattern matches the given name reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(NameReference nameRef, boolean resolve) {
	if (!resolve) {
		if (this.simpleName == null) {
			return this.needsResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
		} else {
			if (nameRef instanceof SingleNameReference) {
				if (this.matchesName(this.simpleName, ((SingleNameReference)nameRef).token)) {
					// can only be a possible match since resolution is needed 
					// to find out if it is a type ref
					return POSSIBLE_MATCH;
				} else {
					return IMPOSSIBLE_MATCH;
				}
			} else { // QualifiedNameReference
				char[][] tokens = ((QualifiedNameReference)nameRef).tokens;
				for (int i = 0, max = tokens.length; i < max; i++){
					if (this.matchesName(this.simpleName, tokens[i])) {
						// can only be a possible match since resolution is needed 
						// to find out if it is a type ref
						return POSSIBLE_MATCH;
					}
				}
				return IMPOSSIBLE_MATCH;
			}				
		}
	} else {
		Binding binding = nameRef.binding;

		if (nameRef instanceof SingleNameReference) {
			if (binding == null || binding instanceof ProblemBinding){
				return INACCURATE_MATCH;
			} else if (binding instanceof TypeBinding) {
				return this.matchLevelForType(this.simpleName, this.qualification, (TypeBinding) binding);
			} else {
				return IMPOSSIBLE_MATCH; // must be a type binding
			}
		} else { // QualifiedNameReference
			TypeBinding typeBinding = null;
			QualifiedNameReference qNameRef = (QualifiedNameReference)nameRef;
			char[][] tokens = qNameRef.tokens;
			int lastIndex = tokens.length-1;
			switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
				case BindingIds.FIELD : // reading a field
					typeBinding = nameRef.actualReceiverType;
					// no valid match amongst fields
					int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
					lastIndex -= otherBindingsCount + 1;
					if (lastIndex < 0) return IMPOSSIBLE_MATCH;
					break;
				case BindingIds.LOCAL : // reading a local variable
					return IMPOSSIBLE_MATCH; // no type match in it
				case BindingIds.TYPE : //=============only type ==============
					typeBinding = (TypeBinding)binding;
					break;
				/*
				 * Handling of unbound qualified name references. The match may reside in the resolved fragment,
				 * which is recorded inside the problem binding, along with the portion of the name until it became a problem.
				 */
				case BindingIds.VARIABLE : //============unbound cases===========
				case BindingIds.TYPE | BindingIds.VARIABLE :						
					if (binding instanceof ProblemBinding) {
						ProblemBinding pbBinding = (ProblemBinding) binding;
						typeBinding = pbBinding.searchType; // second chance with recorded type so far
						char[] partialQualifiedName = pbBinding.name;
						lastIndex = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
						if (typeBinding == null || lastIndex < 0) return INACCURATE_MATCH;
					}
					break;
			}
			// try to match all enclosing types for which the token matches as well.
			while (typeBinding != null && lastIndex >= 0){
				if (this.matchesName(this.simpleName, tokens[lastIndex--])) {
					int level = this.matchLevelForType(this.simpleName, this.qualification, typeBinding);
					if (level != IMPOSSIBLE_MATCH) {
						return level;
					}
				}
				if (typeBinding instanceof ReferenceBinding){
					typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
				} else {
					typeBinding = null;
				}
			}
			return IMPOSSIBLE_MATCH;
		}
	}
}

/**
 * Reports the match of the given array type reference.
 */
protected void matchReportReference(ArrayTypeReference arrayRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = this.simpleName == null ? NO_CHAR_CHAR : new char[][] {this.simpleName};
	locator.reportAccurateReference(arrayRef.sourceStart, arrayRef.sourceEnd, tokens, element, accuracy);
}

/**
 * Returns whether this type pattern matches the given type reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(TypeReference typeRef, boolean resolve) {
	if (!resolve) {
		if (this.simpleName == null) {
			return this.needsResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
		} else {
			if (typeRef instanceof SingleTypeReference) {
				if (this.matchesName(this.simpleName, ((SingleTypeReference)typeRef).token)) {
					return this.needsResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
				} else {
					return IMPOSSIBLE_MATCH;
				}
			} else { // QualifiedTypeReference
				char[][] tokens = ((QualifiedTypeReference)typeRef).tokens;
				for (int i = 0, max = tokens.length; i < max; i++){
					if (this.matchesName(this.simpleName, tokens[i])) {
						// can only be a possible match since resolution is needed 
						// to find out if it is a type ref
						return POSSIBLE_MATCH;
					}
				}
				return IMPOSSIBLE_MATCH;
			}				
		} 
	} else {
		TypeBinding typeBinding = typeRef.binding;
		if (typeBinding == null) {
			return INACCURATE_MATCH;
		} else {
			if (typeBinding instanceof ArrayBinding) typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
			if (typeBinding instanceof ProblemReferenceBinding) return INACCURATE_MATCH;
			if (typeRef instanceof SingleTypeReference){
				return this.matchLevelForType(this.simpleName, this.qualification, typeBinding);
			} else { // QualifiedTypeReference
				QualifiedTypeReference qTypeRef = (QualifiedTypeReference)typeRef;
				char[][] tokens = qTypeRef.tokens;
				int lastIndex = tokens.length-1;
				// try to match all enclosing types for which the token matches as well.
				while (typeBinding != null && lastIndex >= 0){
					if (matchesName(this.simpleName, tokens[lastIndex--])) {
						int level = this.matchLevelForType(this.simpleName, this.qualification, typeBinding);
						if (level != IMPOSSIBLE_MATCH) {
							return level;
						}
					}
					if (typeBinding instanceof ReferenceBinding){
						typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
					} else {
						typeBinding = null;
					}
				}
				return IMPOSSIBLE_MATCH;
			} 
		}
			
	}
}
/**
 * @see SearchPattern#matchReportImportRef(ImportReference, Binding, IJavaElement, int, MatchLocator)
 */
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	ReferenceBinding typeBinding = null;
	char[][] tokens = null;
	if (binding instanceof ReferenceBinding) {
		typeBinding = (ReferenceBinding)binding;
	}
	char[][] typeTokens = importRef.tokens;
	int lastIndex = typeTokens.length-1;
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0){
		if (matchesName(this.simpleName, typeTokens[lastIndex--])) {
			int level = this.matchLevelForType(this.simpleName, this.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) {
				tokens = new char[lastIndex+2][];
				System.arraycopy(typeTokens, 0, tokens, 0, lastIndex+2);
				break;
			}
		}
		if (typeBinding instanceof ReferenceBinding){
			typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
		} else {
			typeBinding = null;
		}
	}
	if (tokens == null) {
		if (typeBinding == null || typeBinding instanceof ProblemReferenceBinding) {
			tokens = new char[][] {this.simpleName};
		} else {
			tokens = importRef.tokens;
		}
		if (!this.isCaseSensitive) {
			int length = tokens.length;
			char[][] lowerCaseTokens = new char[length][];
			for (int i = 0; i < length; i++) {
				char[] token = tokens[i];
				lowerCaseTokens[i] = CharOperation.toLowerCase(token);
			}
			tokens = lowerCaseTokens;
		}
	}
	locator.reportAccurateReference(importRef.sourceStart, importRef.sourceEnd, tokens, element, accuracy);
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof TypeBinding)) return IMPOSSIBLE_MATCH;
	TypeBinding typeBinding = (TypeBinding)binding;
	if (typeBinding instanceof ArrayBinding) typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
	if (typeBinding instanceof ProblemReferenceBinding) return INACCURATE_MATCH;

	while (typeBinding != null ) {
		int level = this.matchLevelForType(this.simpleName, this.qualification, typeBinding);
		if (level != IMPOSSIBLE_MATCH) {
			return level;
		}
		if (typeBinding instanceof ReferenceBinding){
			typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
		} else {
			typeBinding = null;
		}
	}
	return IMPOSSIBLE_MATCH;
}
}
