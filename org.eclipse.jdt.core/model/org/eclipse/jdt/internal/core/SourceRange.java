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
 * @see ISourceRange
 */
/* package */ class SourceRange implements ISourceRange {

protected int offset, length;

protected SourceRange(int offset, int length) {
	this.offset = offset;
	this.length = length;
}
/**
 * @see ISourceRange
 */
public int getLength() {
	return this.length;
}
/**
 * @see ISourceRange
 */
public int getOffset() {
	return this.offset;
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[offset="); //$NON-NLS-1$
	buffer.append(this.offset);
	buffer.append(", length="); //$NON-NLS-1$
	buffer.append(this.length);
	buffer.append("]"); //$NON-NLS-1$
	return buffer.toString();
}
}
