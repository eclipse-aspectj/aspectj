/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.bridge;

import java.io.File;

/**
 * Represent source location as a starting line/column and ending line in a source file. Implementations should be immutable. XXX
 * why?
 * 
 * @see org.aspectj.lang.reflect.SourceLocation
 * @see org.aspectj.compiler.base.parser.SourceInfo
 * @see org.aspectj.tools.ide.SourceLine
 * @see org.aspectj.testing.harness.ErrorLine
 */
public interface ISourceLocation extends java.io.Serializable {
	static final int MAX_LINE = Integer.MAX_VALUE / 2;
	static final int MAX_COLUMN = MAX_LINE;

	/** non-null but empty (nonexisting) File constant */
	static final File NO_FILE = new File("ISourceLocation.NO_FILE");

	/** signal that column is not known */
	static final int NO_COLUMN = Integer.MIN_VALUE + 1;

	/** non-null but empty constant source location */
	static final ISourceLocation EMPTY = new SourceLocation(NO_FILE, 0, 0, 0);

	/**
	 * @return File source or NO_FILE if the implementation requires a non-null result or null otherwise
	 */
	File getSourceFile();

	/** @return 0..MAX_LINE */
	int getLine();

	/**
	 * @return int 0..MAX_COLUMN actual column or 0 if column input was ISourceLocation.NO_COLUMN
	 */
	int getColumn();

	/**
	 * @return offset into file
	 */
	int getOffset();

	/** @return getLine()..MAX_LINE */
	int getEndLine();

	/** @return String application-specific context for source */
	String getContext();

	/**
	 * In the cases where getSourceFile().getName() returns a class file (for example when we have a binary aspect) this should
	 * return the name of the source file (for example BinaryAspect.aj)
	 * 
	 * @return the name of the source file
	 */
	String getSourceFileName();

}
