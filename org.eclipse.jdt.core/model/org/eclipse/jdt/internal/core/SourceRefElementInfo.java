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

import org.eclipse.jdt.core.ISourceRange;

/** 
 * Element info for ISourceReference elements. 
 */
/* package */ class SourceRefElementInfo extends JavaElementInfo {
	protected int fSourceRangeStart, fSourceRangeEnd;
protected SourceRefElementInfo() {
	setIsStructureKnown(true);
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ISourceType#getDeclarationSourceEnd()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceMethod#getDeclarationSourceEnd()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceField#getDeclarationSourceEnd()
 */
public int getDeclarationSourceEnd() {
	return fSourceRangeEnd;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ISourceType#getDeclarationSourceStart()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceMethod#getDeclarationSourceStart()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceField#getDeclarationSourceStart()
 */
public int getDeclarationSourceStart() {
	return fSourceRangeStart;
}
protected ISourceRange getSourceRange() {
	return new SourceRange(fSourceRangeStart, fSourceRangeEnd - fSourceRangeStart + 1);
}
protected void setSourceRangeEnd(int end) {
	fSourceRangeEnd = end;
}
protected void setSourceRangeStart(int start) {
	fSourceRangeStart = start;
}
}
