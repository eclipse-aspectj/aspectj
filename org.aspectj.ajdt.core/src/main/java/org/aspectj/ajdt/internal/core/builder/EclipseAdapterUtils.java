/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.io.File;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IProgressListener;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.org.eclipse.jdt.core.compiler.IProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.aspectj.weaver.LintMessage;
import org.aspectj.weaver.World;

public class EclipseAdapterUtils {

	// XXX some cut-and-paste from eclipse sources
	public static String makeLocationContext(ICompilationUnit compilationUnit, IProblem problem) {
		// Extract the erroneous token plus some context around it from the beginning of the first to the end of the last
		// problematic line. Usually, it will be a single line, but not necessarily. After extraction, highlight the
		// erroneous token, "underlining" it withthe appropriate number of carets ('^').
		//
		// This code assumes, that the console font is fixed size.

		// Sanity checks
		int startPosition = problem.getSourceStart();
		int endPosition = problem.getSourceEnd();

		if (startPosition > endPosition || startPosition <= 0 && endPosition <= 0 || compilationUnit == null)
			//return Util.bind("problem.noSourceInformation"); //$NON-NLS-1$
			return "(no source information available)";

		final char SPACE = '\u0020';
		final char MARK = '^';
		final char TAB = '\t';
		char[] source = compilationUnit.getContents();
		// the next code tries to underline the token.....
		// it assumes (for a good display) that token source does not
		// contain any \r \n. This is false on statements !
		// (the code still works but the display is not optimal !)

		// compute the how-much-char we are displaying around the inaccurate token
		int contextStart = startPosition >= source.length ? source.length - 1 : startPosition;
		if (contextStart == -1)
			return "(no source information available)"; // Don't like this - why does it occur? pr152835
		int contextEnd = endPosition >= source.length ? source.length - 1 : endPosition;
		int problemLength = contextEnd - contextStart + 1;
		int trimLeftIndex = 0;
		char c;
		while (
			trimLeftIndex <= problemLength &&
			((c = source[contextStart + trimLeftIndex]) == TAB || c == SPACE)
		) {
			trimLeftIndex++;
		}
		contextStart += trimLeftIndex;
		problemLength -= trimLeftIndex;

		// Find the beginning of the first line containing the problem (contextStart)
		// as well as the relative problem start offset (problemStartOffset) from there
		int problemStartOffset;
		for (problemStartOffset = 0; ; problemStartOffset++) {
			if (contextStart == 0)
				break;
			if ((c = source[contextStart - 1]) == '\n' || c == '\r')
				break;
			contextStart--;
		}

		// Find the end of the last line containing the problem (contextEnd)
		// as well as the relative problem end offset (problemEndOffset) from there
		int problemEndOffset;
		for (problemEndOffset = 0; ; problemEndOffset--) {
			if (contextEnd + 1 >= source.length)
				break;
			if ((c = source[contextEnd + 1]) == '\r' || c == '\n')
				break;
			contextEnd++;
		}

		// Extract the problematic lines of code from the source
		char[] extract = new char[contextEnd - contextStart + 1];
		System.arraycopy(source, contextStart, extract, 0, extract.length);

		// Dedent (left-trim) the first line, i.e. remove leading spaces and tabs
		trimLeftIndex = 0;
		while (
			trimLeftIndex < extract.length &&
			((c = extract[trimLeftIndex]) == TAB || c == SPACE)
		) {
			trimLeftIndex++;
		}
		if (trimLeftIndex >= extract.length)
			return new String(extract) + "\n"; // TODO: Shouldn't it return "" or "\n"?
		System.arraycopy(extract, trimLeftIndex, extract = new char[extract.length - trimLeftIndex], 0, extract.length);
		problemStartOffset -= trimLeftIndex;

		// Insert spaces to reach the error position
		int pos = 0;
		char[] underline = new char[extract.length]; // can't be bigger
		for (int i = 0; i < problemStartOffset; i++)
			underline[pos++] = SPACE;
		// Underline the error position with a '^^^^^' character sequence
		for (int i = 0; i < problemLength; i++)
			underline[pos++] = MARK;

		// Resize to remove trailing NUL characters
		System.arraycopy(underline, 0, underline = new char[problemStartOffset + problemLength], 0, pos);

		return new String(extract) + "\n" + new String(underline); //$NON-NLS-2$ //$NON-NLS-1$
	}

	/**
	 * Extract source location file, start and end lines, and context. Column is not extracted correctly.
	 * @param progressListener
	 *
	 * @return ISourceLocation with correct file and lines but not column.
	 */
	public static ISourceLocation makeSourceLocation(ICompilationUnit unit, IProblem problem, IProgressListener progressListener) {
		int line = problem.getSourceLineNumber();
		File file = new File(new String(problem.getOriginatingFileName()));
		// cheat here...269912 - don't build the context if under IDE control
		if (progressListener!=null) {
			return new SourceLocation(file, line, line, 0, null);
		} else {
			String context = makeLocationContext(unit, problem);
			// XXX 0 column is wrong but recoverable from makeLocationContext
			return new SourceLocation(file, line, line, 0, context);
		}
	}

	/**
	 * Extract message text and source location, including context.
	 *
	 * @param world
	 * @param progressListener
	 */
	public static IMessage makeMessage(ICompilationUnit unit, IProblem problem, World world, IProgressListener progressListener) {
		ISourceLocation sourceLocation = makeSourceLocation(unit, problem, progressListener);
		IProblem[] seeAlso = problem.seeAlso();
		// If the user has turned off classfile line number gen, then we may not be able to tell them
		// about all secondary locations (pr209372)
		int validPlaces = 0;
		for (IProblem value : seeAlso) {
			if (value.getSourceLineNumber() >= 0)
				validPlaces++;
		}
		ISourceLocation[] seeAlsoLocations = new ISourceLocation[validPlaces];
		int pos = 0;
		for (IProblem iProblem : seeAlso) {
			if (iProblem.getSourceLineNumber() >= 0) {
				seeAlsoLocations[pos++] = new SourceLocation(new File(new String(iProblem.getOriginatingFileName())), iProblem
						.getSourceLineNumber());
			}
		}
		// We transform messages from AJ types to eclipse IProblems
		// and back to AJ types. During their time as eclipse problems,
		// we remember whether the message originated from a declare
		// in the extraDetails.
		String extraDetails = problem.getSupplementaryMessageInfo();
		boolean declared = false;
		boolean isLintMessage = false;
		String lintkey = null;
		if (extraDetails != null && extraDetails.endsWith("[deow=true]")) {
			declared = true;
			extraDetails = extraDetails.substring(0, extraDetails.length() - "[deow=true]".length());
		}
		if (extraDetails != null && extraDetails.contains("[Xlint:")) {
			isLintMessage = true;
			lintkey = extraDetails.substring(extraDetails.indexOf("[Xlint:"));
			lintkey = lintkey.substring("[Xlint:".length());
			lintkey = lintkey.substring(0, lintkey.indexOf("]"));
		}

		// If the 'problem' represents a TO DO kind of thing then use the message kind that
		// represents this so AJDT sees it correctly.
		IMessage.Kind kind;
		if (problem.getID() == IProblem.Task) {
			kind = IMessage.TASKTAG;
		} else {
			if (problem.isError()) {
				kind = IMessage.ERROR;
			} else {
				kind = IMessage.WARNING;
			}
		}
		IMessage msg = null;
		if (isLintMessage) {
			msg = new LintMessage(problem.getMessage(), extraDetails, world.getLint().fromKey(lintkey), kind, sourceLocation, null,
					seeAlsoLocations, declared, problem.getID(), problem.getSourceStart(), problem.getSourceEnd());
		} else {
			msg = new Message(problem.getMessage(), extraDetails, kind, sourceLocation, null, seeAlsoLocations, declared, problem
					.getID(), problem.getSourceStart(), problem.getSourceEnd());
		}
		return msg;
	}

	public static IMessage makeErrorMessage(ICompilationUnit unit, String text, Exception ex) {
		ISourceLocation loc = new SourceLocation(new File(new String(unit.getFileName())), 0, 0, 0, "");
		IMessage msg = new Message(text, IMessage.ERROR, ex, loc);
		return msg;
	}

	public static IMessage makeErrorMessage(String srcFile, String text, Exception ex) {
		ISourceLocation loc = new SourceLocation(new File(srcFile), 0, 0, 0, "");
		IMessage msg = new Message(text, IMessage.ERROR, ex, loc);
		return msg;
	}

	private EclipseAdapterUtils() {
	}

}
