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
package org.eclipse.jdt.internal.core.jdom;

/**
 * The <coe>ILineSeparatorFinder</code> finds previous and next line separators
 * in source.
 */
public interface ILineStartFinder {
/**
 * Returns the position of the start of the line at or before the given source position.
 *
 * <p>This defaults to zero if the position corresponds to a position on the first line
 * of the source.
 */
public int getLineStart(int position);
}
