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
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.util.Util;

public class DefaultProblem implements ProblemSeverities, IProblem {
		
	private char[] fileName;
	private int id;
	private int startPosition, endPosition, line;
	private int severity;
	private String[] arguments;
	private String message;
	
	public DefaultProblem(
		char[] originatingFileName,
		String message,
		int id,
		String[] stringArguments,
		int severity,
		int startPosition,
		int endPosition,
		int line) {

		this.fileName = originatingFileName;
		this.message = message;
		this.id = id;
		this.arguments = stringArguments;
		this.severity = severity;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.line = line;
	}

	public String errorReportSource(ICompilationUnit compilationUnit) {
		//extra from the source the innacurate     token
		//and "highlight" it using some underneath ^^^^^
		//put some context around too.

		//this code assumes that the font used in the console is fixed size

		//sanity .....
		if ((startPosition > endPosition)
			|| ((startPosition <= 0) && (endPosition <= 0)))
			return Util.bind("problem.noSourceInformation"); //$NON-NLS-1$

		final char SPACE = '\u0020';
		final char MARK = '^';
		final char TAB = '\t';
		char[] source = compilationUnit.getContents();
		//the next code tries to underline the token.....
		//it assumes (for a good display) that token source does not
		//contain any \r \n. This is false on statements ! 
		//(the code still works but the display is not optimal !)

		//compute the how-much-char we are displaying around the inaccurate token
		int begin = startPosition >= source.length ? source.length - 1 : startPosition;
		int relativeStart = 0;
		int end = endPosition >= source.length ? source.length - 1 : endPosition;
		int relativeEnd = 0;
		label : for (relativeStart = 0;; relativeStart++) {
			if (begin == 0)
				break label;
			if ((source[begin - 1] == '\n') || (source[begin - 1] == '\r'))
				break label;
			begin--;
		}
		label : for (relativeEnd = 0;; relativeEnd++) {
			if ((end + 1) >= source.length)
				break label;
			if ((source[end + 1] == '\r') || (source[end + 1] == '\n')) {
				break label;
			}
			end++;
		}
		//extract the message form the source
		char[] extract = new char[end - begin + 1];
		System.arraycopy(source, begin, extract, 0, extract.length);
		char c;
		//remove all SPACE and TAB that begin the error message...
		int trimLeftIndex = 0;
		while (((c = extract[trimLeftIndex++]) == TAB) || (c == SPACE)) {
		};
		System.arraycopy(
			extract,
			trimLeftIndex - 1,
			extract = new char[extract.length - trimLeftIndex + 1],
			0,
			extract.length);
		relativeStart -= trimLeftIndex;
		//buffer spaces and tabs in order to reach the error position
		int pos = 0;
		char[] underneath = new char[extract.length]; // can't be bigger
		for (int i = 0; i <= relativeStart; i++) {
			if (extract[i] == TAB) {
				underneath[pos++] = TAB;
			} else {
				underneath[pos++] = SPACE;
			}
		}
		//mark the error position
		for (int i = startPosition;
			i <= (endPosition >= source.length ? source.length - 1 : endPosition);
			i++)
			underneath[pos++] = MARK;
		//resize underneathto remove 'null' chars
		System.arraycopy(underneath, 0, underneath = new char[pos], 0, pos);

		return " " + Util.bind("problem.atLine", String.valueOf(line)) 	//$NON-NLS-2$ //$NON-NLS-1$
			+ "\n\t" + new String(extract) + "\n\t" + new String(underneath); //$NON-NLS-2$ //$NON-NLS-1$
	}

	/**
	 * Answer back the original arguments recorded into the problem.
	 * @return java.lang.String[]
	 */
	public String[] getArguments() {

		return arguments;
	}

	/**
	 * Answer the type of problem.
	 * @see org.eclipse.jdt.core.compiler.IProblem#getID()
	 * @return int
	 */
	public int getID() {

		return id;
	}

	/**
	 * Answer a localized, human-readable message string which describes the problem.
	 * @return java.lang.String
	 */
	public String getMessage() {

		return message;
	}

	/**
	 * Answer the file name in which the problem was found.
	 * @return char[]
	 */
	public char[] getOriginatingFileName() {

		return fileName;
	}

	/**
	 * Answer the end position of the problem (inclusive), or -1 if unknown.
	 * @return int
	 */
	public int getSourceEnd() {

		return endPosition;
	}

	/**
	 * Answer the line number in source where the problem begins.
	 * @return int
	 */
	public int getSourceLineNumber() {

		return line;
	}

	/**
	 * Answer the start position of the problem (inclusive), or -1 if unknown.
	 * @return int
	 */
	public int getSourceStart() {

		return startPosition;
	}

	/*
	 * Helper method: checks the severity to see if the Error bit is set.
	 * @return boolean
	 */
	public boolean isError() {

		return (severity & ProblemSeverities.Error) != 0;
	}

	/*
	 * Helper method: checks the severity to see if the Error bit is not set.
	 * @return boolean
	 */
	public boolean isWarning() {

		return (severity & ProblemSeverities.Error) == 0;
	}

	/**
	 * Set the end position of the problem (inclusive), or -1 if unknown.
	 *
	 * Used for shifting problem positions.
	 * @param sourceEnd the new value of the sourceEnd of the receiver
	 */
	public void setSourceEnd(int sourceEnd) {

		endPosition = sourceEnd;
	}

	/**
	 * Set the line number in source where the problem begins.
	 * @param lineNumber the new value of the line number of the receiver
	 */
	public void setSourceLineNumber(int lineNumber) {

		line = lineNumber;
	}

	/**
	 * Set the start position of the problem (inclusive), or -1 if unknown.
	 *
	 * Used for shifting problem positions.
	 * @param sourceStart the new value of the source start position of the receiver
	 */
	public void setSourceStart(int sourceStart) {

		startPosition = sourceStart;
	}

	public String toString() {

		String s = "Pb(" + (id & IgnoreCategoriesMask) + ") "; //$NON-NLS-1$ //$NON-NLS-2$
		if (message != null) {
			s += message;
		} else {
			if (arguments != null)
				for (int i = 0; i < arguments.length; i++)
					s += " " + arguments[i]; //$NON-NLS-1$
		}
		return s;
	}
}