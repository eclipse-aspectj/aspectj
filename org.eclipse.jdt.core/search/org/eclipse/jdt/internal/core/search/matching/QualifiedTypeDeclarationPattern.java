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

import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.index.IEntryResult;

public class QualifiedTypeDeclarationPattern extends TypeDeclarationPattern {
	
	private char[] qualification;
	private char[] decodedQualification;
	
public QualifiedTypeDeclarationPattern(
	char[] qualification,
	char[] simpleName,
	char classOrInterface,
	int matchMode, 
	boolean isCaseSensitive) {
		
	super(matchMode, isCaseSensitive);

	this.qualification = isCaseSensitive ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);
	this.classOrInterface = classOrInterface;
	
	this.needsResolve = qualification != null;
}

public void decodeIndexEntry(IEntryResult entryResult){
	
	char[] word = entryResult.getWord();
	int size = word.length;

	this.decodedClassOrInterface = word[TYPE_DECL_LENGTH];
	int oldSlash = TYPE_DECL_LENGTH+1;
	int slash = CharOperation.indexOf(SEPARATOR, word, oldSlash+1);
	char[] pkgName;
	if (slash == oldSlash+1){ 
		pkgName = NO_CHAR;
	} else {
		pkgName = CharOperation.subarray(word, oldSlash+1, slash);
	}
	this.decodedSimpleName = CharOperation.subarray(word, slash+1, slash = CharOperation.indexOf(SEPARATOR, word, slash+1));

	char[][] enclosingTypeNames;
	if (slash+1 < size){
		if (slash+3 == size && word[slash+1] == ONE_ZERO[0]) {
			enclosingTypeNames = ONE_ZERO_CHAR;
		} else {
			enclosingTypeNames = CharOperation.splitOn('/', CharOperation.subarray(word, slash+1, size-1));
		}
	} else {
		enclosingTypeNames = NO_CHAR_CHAR;
	}
	this.decodedQualification = CharOperation.concatWith(pkgName, enclosingTypeNames, '.');
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
	if (!this.matchesType(this.simpleName, this.qualification, typeName)) {
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
	/* check qualification */
	if (qualification != null) {
		switch(matchMode){
			case EXACT_MATCH :
				if (!CharOperation.equals(qualification, decodedQualification, isCaseSensitive)){
					return false;
				}
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(qualification, decodedQualification, isCaseSensitive)){
					return false;
				}
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(qualification, decodedQualification, isCaseSensitive)){
					return false;
				}
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
	return this.matchLevelForType(this.simpleName, this.qualification, type);
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	switch (classOrInterface){
		case CLASS_SUFFIX :
			buffer.append("ClassDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		case INTERFACE_SUFFIX :
			buffer.append("InterfaceDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		default :
			buffer.append("TypeDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
	}
	if (this.qualification != null) buffer.append(this.qualification);
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
}
