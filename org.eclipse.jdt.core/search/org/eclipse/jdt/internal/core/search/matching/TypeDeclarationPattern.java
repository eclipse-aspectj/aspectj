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

import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexedFile;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

public class TypeDeclarationPattern extends SearchPattern {

	private char[] pkg;
	private char[][] enclosingTypeNames;
	protected char[] simpleName;

	// set to CLASS_SUFFIX for only matching classes 
	// set to INTERFACE_SUFFIX for only matching interfaces
	// set to TYPE_SUFFIX for matching both classes and interfaces
	protected char classOrInterface; 

	private char[] decodedPackage;
	private char[][] decodedEnclosingTypeNames;
	protected char[] decodedSimpleName;
	protected char decodedClassOrInterface;
	
public TypeDeclarationPattern(int matchMode, boolean isCaseSensitive) {
	super(matchMode, isCaseSensitive);
}
public TypeDeclarationPattern(
	char[] pkg,
	char[][] enclosingTypeNames,
	char[] simpleName,
	char classOrInterface,
	int matchMode, 
	boolean isCaseSensitive) {

	super(matchMode, isCaseSensitive);

	this.pkg = isCaseSensitive ? pkg : CharOperation.toLowerCase(pkg);
	if (isCaseSensitive || enclosingTypeNames == null) {
		this.enclosingTypeNames = enclosingTypeNames;
	} else {
		int length = enclosingTypeNames.length;
		this.enclosingTypeNames = new char[length][];
		for (int i = 0; i < length; i++){
			this.enclosingTypeNames[i] = CharOperation.toLowerCase(enclosingTypeNames[i]);
		}
	}
	this.simpleName = isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);
	this.classOrInterface = classOrInterface;
	
	this.needsResolve = pkg != null && enclosingTypeNames != null;
}
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;

	this.decodedClassOrInterface = word[TYPE_DECL_LENGTH];
	int oldSlash = TYPE_DECL_LENGTH+1;
	int slash = CharOperation.indexOf(SEPARATOR, word, oldSlash+1);
	if (slash == oldSlash+1){ 
		this.decodedPackage = NO_CHAR;
	} else {
		this.decodedPackage = CharOperation.subarray(word, oldSlash+1, slash);
	}
	this.decodedSimpleName = CharOperation.subarray(word, slash+1, slash = CharOperation.indexOf(SEPARATOR, word, slash+1));

	if (slash+1 < size){
		if (slash+3 == size && word[slash+1] == ONE_ZERO[0]) {
			this.decodedEnclosingTypeNames = ONE_ZERO_CHAR;
		} else {
			this.decodedEnclosingTypeNames = CharOperation.splitOn('/', CharOperation.subarray(word, slash+1, size-1));
		}
	} else {
		this.decodedEnclosingTypeNames = NO_CHAR_CHAR;
	}
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	boolean isClass = decodedClassOrInterface == CLASS_SUFFIX;
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		String path;
		if (file != null && scope.encloses(path =IndexedFile.convertPath(file.getPath()))) {
			if (isClass) {
				requestor.acceptClassDeclaration(path, decodedSimpleName, decodedEnclosingTypeNames, decodedPackage);
			} else {
				requestor.acceptInterfaceDeclaration(path, decodedSimpleName, decodedEnclosingTypeNames, decodedPackage);
			}
		}
	}
}
/**
 * see SearchPattern.indexEntryPrefix()
 */
public char[] indexEntryPrefix(){

	return AbstractIndexer.bestTypeDeclarationPrefix(
			pkg,
			simpleName,
			classOrInterface,
			matchMode, 
			isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return COMPILATION_UNIT | CLASS | METHOD | FIELD;
}
/**
 * @see SearchPattern#matchesBinary(Object, Object)
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!(binaryInfo instanceof IBinaryType)) return false;

	IBinaryType type = (IBinaryType)binaryInfo;

	// fully qualified name
	char[] typeName = (char[])type.getName().clone();
	CharOperation.replace(typeName, '/', '.');
	char[] enclosingTypeName = this.enclosingTypeNames == null ? null : CharOperation.concatWith(this.enclosingTypeNames, '.');
	if (!this.matchesType(this.simpleName, this.pkg, enclosingTypeName, typeName)) {
		return false;
	}

	// class or interface
	switch (this.classOrInterface) {
		case CLASS_SUFFIX:
			if (type.isInterface())
				return false;
			break;
		case INTERFACE_SUFFIX:
			if (!type.isInterface())
				return false;
			break;
	}
	
	return true;
}
/**
 * Returns whether the given type binding matches the given simple name pattern 
 * package pattern and enclosing name pattern.
 */
protected boolean matchesType(char[] simpleNamePattern, char[] pkgPattern, char[] enclosingNamePattern, char[] fullyQualifiedTypeName) {
	if (enclosingNamePattern == null) {
		return this.matchesType(simpleNamePattern, pkgPattern, fullyQualifiedTypeName);
	} else {
		char[] pattern;
		if (pkgPattern == null) {
			pattern = enclosingNamePattern;
		} else {
			pattern = CharOperation.concat(pkgPattern, enclosingNamePattern, '.');
		}
		return this.matchesType(simpleNamePattern, pattern, fullyQualifiedTypeName);
	}
}

/**
 * see SearchPattern.matchIndexEntry
 */
protected boolean matchIndexEntry(){

	/* check class/interface nature */
	switch(classOrInterface){
		case CLASS_SUFFIX :
		case INTERFACE_SUFFIX :
			if (classOrInterface != decodedClassOrInterface) return false;
		default :
	}
	/* check qualification - exact match only */
	if (pkg != null && !CharOperation.equals(pkg, decodedPackage, isCaseSensitive))
		return false;
	/* check enclosingTypeName - exact match only */
	if (enclosingTypeNames != null){
		// empty char[][] means no enclosing type, i.e. the decoded one is the empty char array
		if (enclosingTypeNames.length == 0){
			if (decodedEnclosingTypeNames != NO_CHAR_CHAR) return false;
		} else {
			if (!CharOperation.equals(enclosingTypeNames, decodedEnclosingTypeNames, isCaseSensitive)) return false;
		}
	}
	/* check simple name matches */
	if (simpleName != null){
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
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	switch (classOrInterface){
		case CLASS_SUFFIX :
			buffer.append("ClassDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
		case INTERFACE_SUFFIX :
			buffer.append("InterfaceDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
		default :
			buffer.append("TypeDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
	}
	if (pkg != null) buffer.append(pkg);
	buffer.append(">, enclosing<"); //$NON-NLS-1$
	if (enclosingTypeNames != null) {
		for (int i = 0; i < enclosingTypeNames.length; i++){
			buffer.append(enclosingTypeNames[i]);
			if (i < enclosingTypeNames.length - 1)
				buffer.append('.');
		}
	}
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
	if (!(node instanceof TypeDeclaration)) return IMPOSSIBLE_MATCH;

	TypeDeclaration type = (TypeDeclaration)node;

	if (resolve) {
		return this.matchLevel(type.binding);
	} else {
		// type name
		if (this.simpleName != null && !this.matchesName(this.simpleName, type.name))
			return IMPOSSIBLE_MATCH;
		else
			return this.needsResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
	}
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof TypeBinding)) return IMPOSSIBLE_MATCH;

	TypeBinding type = (TypeBinding)binding;

	// class or interface
	switch (this.classOrInterface) {
		case CLASS_SUFFIX:
			if (type.isInterface())
				return IMPOSSIBLE_MATCH;
			break;
		case INTERFACE_SUFFIX:
			if (!type.isInterface())
				return IMPOSSIBLE_MATCH;
			break;
	}

	// fully qualified name
	char[] enclosingTypeName = this.enclosingTypeNames == null ? null : CharOperation.concatWith(this.enclosingTypeNames, '.');
	return this.matchLevelForType(this.simpleName, this.pkg, enclosingTypeName, type);
}

/**
 * Returns whether the given type binding matches the given simple name pattern 
 * qualification pattern and enclosing type name pattern.
 */
protected int matchLevelForType(char[] simpleNamePattern, char[] qualificationPattern, char[] enclosingNamePattern, TypeBinding type) {
	if (enclosingNamePattern == null) {
		return this.matchLevelForType(simpleNamePattern, qualificationPattern, type);
	} else {
		if (qualificationPattern == null) {
			return matchLevelForType(simpleNamePattern, enclosingNamePattern, type);
		} else {
			// pattern was created from a Java element: qualification is the package name.
			char[] fullQualificationPattern = CharOperation.concat(qualificationPattern, enclosingNamePattern, '.');
			if ( CharOperation.equals(pkg, CharOperation.concatWith(type.getPackage().compoundName, '.'))) {
				return this.matchLevelForType(simpleNamePattern, fullQualificationPattern, type);
			} else {
				return IMPOSSIBLE_MATCH;
			}
		}
	}
}
}
