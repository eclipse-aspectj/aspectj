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

public abstract class MethodPattern extends SearchPattern {

	// selector	
	protected char[] selector;
	
	// declaring type
	protected char[] declaringQualification;
	protected char[] declaringSimpleName;

	// return type
	protected char[] returnQualification;
	protected char[] returnSimpleName;

	// parameter types
	protected char[][] parameterQualifications;
	protected char[][] parameterSimpleNames;

	protected char[] decodedSelector;
	protected int decodedParameterCount;	
public MethodPattern(int matchMode, boolean isCaseSensitive) {
	super(matchMode, isCaseSensitive);
}
public abstract String getPatternName();
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {

	/* check selector matches */
	if (selector != null){
		switch(matchMode){
			case EXACT_MATCH :
				if (!CharOperation.equals(selector, decodedSelector, isCaseSensitive)){
					return false;
				}
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(selector, decodedSelector, isCaseSensitive)){
					return false;
				}
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(selector, decodedSelector, isCaseSensitive)){
					return false;
				}
		}
	}
	if (parameterSimpleNames != null){
		if (parameterSimpleNames.length != decodedParameterCount) return false;
	}
	return true;
}
/**
 * Returns whether a method declaration or message send will need to be resolved to 
 * find out if this method pattern matches it.
 */
protected boolean needsResolve() {

	// declaring type
	if (declaringSimpleName != null || declaringQualification != null) return true;

	// return type
	if (returnSimpleName != null || returnQualification != null) return true;

	// parameter types
	if (parameterSimpleNames != null){
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++){
			if (parameterQualifications[i] != null || parameterSimpleNames[i] != null) return true;
		}
	}
	return false;
}
public String toString(){

	StringBuffer buffer = new StringBuffer(20);
	buffer.append(this.getPatternName());
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName).append('.');
	else if (declaringQualification != null) buffer.append("*."); //$NON-NLS-1$
	if (selector != null) {
		buffer.append(selector);
	} else {
		buffer.append("*"); //$NON-NLS-1$
	}
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
	if (returnQualification != null) 
		buffer.append(" --> ").append(returnQualification).append('.'); //$NON-NLS-1$
	else if (returnSimpleName != null) buffer.append(" --> "); //$NON-NLS-1$
	if (returnSimpleName != null) 
		buffer.append(returnSimpleName);
	else if (returnQualification != null) buffer.append("*"); //$NON-NLS-1$
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
}
