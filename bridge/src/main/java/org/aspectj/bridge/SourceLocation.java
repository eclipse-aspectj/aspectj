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

import org.aspectj.util.LangUtil;

/**
 * Immutable source location. This guarantees that the source file is not null and that the numeric values are positive and line &le;
 * endLine.
 * 
 * @see org.aspectj.lang.reflect.SourceLocation
 */
public class SourceLocation implements ISourceLocation {

	private static final long serialVersionUID = -5434765814401009794L;

	private transient int cachedHashcode = -1;

	/** used when SourceLocation is not available */
	public static final ISourceLocation UNKNOWN = new SourceLocation(ISourceLocation.NO_FILE, 0, 0, 0);

	private final File sourceFile;
	private final int startLine;
	private final int column;
	private final int endLine;
	private int offset;
	private final String context;
	private boolean noColumn;
	private String sourceFileName;

	/** @throws IllegalArgumentException if the input would not be a valid line */
	public static final void validLine(int line) {
		if (line < 0) {
			throw new IllegalArgumentException("negative line: " + line);
		} else if (line > ISourceLocation.MAX_LINE) {
			throw new IllegalArgumentException("line too large: " + line);
		}
	}

	/** @throws IllegalArgumentException if the input would not be a valid column */
	public static final void validColumn(int column) {
		if (column < 0) {
			throw new IllegalArgumentException("negative column: " + column);
		} else if (column > ISourceLocation.MAX_COLUMN) {
			throw new IllegalArgumentException("column too large: " + column);
		}
	}

	/**
	 * Same as SourceLocation(file, line, line, 0), except that column is not rendered during toString()
	 */
	public SourceLocation(File file, int line) {
		this(file, line, line, NO_COLUMN);
	}

	/** same as SourceLocation(file, line, endLine, ISourceLocation.NO_COLUMN) */
	public SourceLocation(File file, int line, int endLine) {
		this(file, line, endLine, ISourceLocation.NO_COLUMN);
	}

	/**
	 * @param file File of the source; if null, use ISourceLocation.NO_FILE, not null
	 * @param line int starting line of the location - positive number
	 * @param endLine int ending line of the location - &le; starting line
	 * @param column int character position of starting location - positive number
	 */
	public SourceLocation(File file, int line, int endLine, int column) {
		this(file, line, endLine, column, (String) null);
	}

	public SourceLocation(File file, int line, int endLine, int column, String context) {
		if (column == NO_COLUMN) {
			column = 0;
			noColumn = true;
		}
		if (null == file) {
			file = ISourceLocation.NO_FILE;
		}
		validLine(line);
		validLine(endLine);
		LangUtil.throwIaxIfFalse(line <= endLine, line + " > " + endLine);
		LangUtil.throwIaxIfFalse(column >= 0, "negative column: " + column);
		this.sourceFile = file;
		this.startLine = line;
		this.column = column;
		this.endLine = endLine;
		this.context = context;
	}

	public SourceLocation(File file, int line, int endLine, int column, String context, String sourceFileName) {
		this(file, line, endLine, column, context);
		this.sourceFileName = sourceFileName;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public int getLine() {
		return startLine;
	}

	/**
	 * @return int actual column or 0 if not available per constructor treatment of ISourceLocation.NO_COLUMN
	 */
	public int getColumn() {
		return column;
	}

	public int getEndLine() {
		return endLine;
	}

	/** @return null String or application-specific context */
	public String getContext() {
		return context;
	}

	/** @return String {context\n}{file:}line{:column} */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (null != context) {
			sb.append(context);
			sb.append(LangUtil.EOL);
		}
		if (sourceFile != ISourceLocation.NO_FILE) {
			sb.append(sourceFile.getPath());
		}
		if (startLine > 0) {
			sb.append(":");
			sb.append(startLine); // "" + startLine + "-" + endLine);
		}
		if (!noColumn) {
			sb.append(":" + column);
		}
		if (offset >= 0) {
			sb.append("::" + offset);
		}
		return sb.toString();
	}

	// XXX Ctors for this type should know about an offset, rather than
	// it being set through these methods - but there are just too many
	// ctors at the moment! It needs sorting out.
	public int getOffset() {
		return offset;
	}

	public void setOffset(int i) {
		cachedHashcode = -1;
		offset = i;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof SourceLocation)) {
			return false;
		}
		SourceLocation o = (SourceLocation) obj;
		return startLine == o.startLine && column == o.column && endLine == o.endLine && offset == o.offset
				&& (sourceFile == null ? o.sourceFile == null : sourceFile.equals(o.sourceFile))
				&& (context == null ? o.context == null : context.equals(o.context)) && noColumn == o.noColumn
				&& (sourceFileName == null ? o.sourceFileName == null : sourceFileName.equals(o.sourceFileName));
	}

	public int hashCode() {
		if (cachedHashcode == -1) {
			cachedHashcode = (sourceFile == null ? 0 : sourceFile.hashCode());
			cachedHashcode = cachedHashcode * 37 + startLine;
			cachedHashcode = cachedHashcode * 37 + column;
			cachedHashcode = cachedHashcode * 37 + endLine;
			cachedHashcode = cachedHashcode * 37 + offset;
			cachedHashcode = cachedHashcode * 37 + (context == null ? 0 : context.hashCode());
			cachedHashcode = cachedHashcode * 37 + (noColumn ? 0 : 1);
			cachedHashcode = cachedHashcode * 37 + (sourceFileName == null ? 0 : sourceFileName.hashCode());
		}
		return cachedHashcode;
	}

}
