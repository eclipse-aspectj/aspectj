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
package org.eclipse.jdt.core;

/**
 * Specification for a generic source code formatter. Client plug-ins can contribute
 * an implementation for an ICodeFormatter, through the extension point "org.eclipse.jdt.core.codeFormatter".
 * In case none is found, a default formatter can be provided through the ToolFactory.
 * 
 * @see ToolFactory#createCodeFormatter()
 * @see ToolFactory#createDefaultCodeFormatter(Map options)
 * @since 2.0
 */
public interface ICodeFormatter {

	/** 
	 * Formats the String <code>sourceString</code>,
	 * and returns a string containing the formatted version.
	 * 
	 * @param string the string to format
	 * @param indentationLevel the initial indentation level, used 
	 *      to shift left/right the entire source fragment. An initial indentation
	 *      level of zero has no effect.
	 * @param positions an array of positions to map. These are
	 *      character-based source positions inside the original source,
	 *     for which corresponding positions in the formatted source will
	 *     be computed (so as to relocate elements associated with the original
	 *     source). It updates the positions array with updated positions.
	 *     If set to <code>null</code>, then no positions are mapped.
	 * @param lineSeparator the line separator to use in formatted source,
	 *     if set to <code>null</code>, then the platform default one will be used.
	 * @return the formatted output string.
	 */
	String format(String string, int indentationLevel, int[] positions, String lineSeparator);
}
