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
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class SuperInterfaceReferencePattern extends SuperTypeReferencePattern {
public SuperInterfaceReferencePattern(char[] superQualification, char[] superSimpleName, int matchMode, boolean isCaseSensitive) {
	super(superQualification, superSimpleName, matchMode, isCaseSensitive);
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	return
		this.decodedSuperClassOrInterface == IIndexConstants.INTERFACE_SUFFIX
		&& super.matchIndexEntry();
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("SuperInterfaceReferencePattern: <"); //$NON-NLS-1$
	if (superSimpleName != null) buffer.append(superSimpleName);
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
 * @see SearchPattern#matchesBinary
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!(binaryInfo instanceof IBinaryType)) return false;
	IBinaryType type = (IBinaryType)binaryInfo;

	char[][] superInterfaces = type.getInterfaceNames();
	if (superInterfaces != null) {
		for (int i = 0, max = superInterfaces.length; i < max; i++) {
			char[] superInterfaceName = (char[])superInterfaces[i].clone();
			CharOperation.replace(superInterfaceName, '/', '.');
			if (this.matchesType(this.superSimpleName, this.superQualification, superInterfaceName)){
				return true;
			}
		}
	}
	return false;
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof ReferenceBinding)) return IMPOSSIBLE_MATCH;

	// super interfaces
	int level = IMPOSSIBLE_MATCH;
	ReferenceBinding type = (ReferenceBinding) binding;
	ReferenceBinding[] superInterfaces = type.superInterfaces();
	for (int i = 0, max = superInterfaces.length; i < max; i++){
		int newLevel = this.matchLevelForType(this.superSimpleName, this.superQualification, superInterfaces[i]);
		switch (newLevel) {
			case IMPOSSIBLE_MATCH:
				break;
			case ACCURATE_MATCH:
				return ACCURATE_MATCH;
			default: // ie. INACCURATE_MATCH
				level = newLevel;
				break;
		}
	}
	return level;
}
}
