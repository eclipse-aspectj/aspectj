/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseSourceLocation;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.aspectj.org.eclipse.jdt.core.compiler.IProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.Compiler;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.aspectj.weaver.LintMessage;

/**
 * @author colyer
 * 
 *         To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class WeaverMessageHandler implements IMessageHandler {
	private IMessageHandler sink;
	private CompilationResult currentlyWeaving;
	private Compiler compiler;

	public WeaverMessageHandler(IMessageHandler handler, Compiler compiler) {
		this.sink = handler;
		this.compiler = compiler;
	}

	public void resetCompiler(Compiler c) {
		this.compiler = c;
		currentlyWeaving = null;
	}

	public void setCurrentResult(CompilationResult result) {
		currentlyWeaving = result;
	}

	public boolean handleMessage(IMessage message) throws AbortException {
		if (!(message.isError() || message.isWarning()))
			return sink.handleMessage(message);
		// we only care about warnings and errors here...
		ISourceLocation sLoc = message.getSourceLocation();

		// See bug 62073. We should assert that the caller pass the correct primary source location.
		// But for AJ1.2 final we will simply do less processing of the locations if that is not the
		// case (By calling sink.handleMessage()) - this ensures we don't put out bogus source context info.
		if (sLoc instanceof EclipseSourceLocation) {
			EclipseSourceLocation esLoc = (EclipseSourceLocation) sLoc;
			if (currentlyWeaving != null && esLoc.getCompilationResult() != null) {
				if (!currentlyWeaving.equals(((EclipseSourceLocation) sLoc).getCompilationResult()))
					return sink.handleMessage(message);
				// throw new RuntimeException("Primary source location must match the file we are currently processing!");
			}
		}
		// bug 128618 - want to do a similar thing as in bug 62073 above, however
		// we're not an EclipseSourceLocation we're a SourceLocation.
		if (sLoc instanceof SourceLocation) {
			SourceLocation sl = (SourceLocation) sLoc;
			if (currentlyWeaving != null && sl.getSourceFile() != null) {
				if (!String.valueOf(currentlyWeaving.getFileName()).equals(sl.getSourceFile().getAbsolutePath())) {
					return sink.handleMessage(message);
					// throw new RuntimeException("Primary source location must match the file we are currently processing!");
				}
			}
		}

		CompilationResult problemSource = currentlyWeaving;
		if (problemSource == null) {
			// must be a problem found during completeTypeBindings phase of begin to compile
			if (sLoc instanceof EclipseSourceLocation) {
				problemSource = ((EclipseSourceLocation) sLoc).getCompilationResult();
			}
			if (problemSource == null) {
				// XXX this is ok for ajc, will have to do better for AJDT in time...
				return sink.handleMessage(message);
			}
		}
		int startPos = getStartPos(sLoc, problemSource);
		int endPos = getEndPos(sLoc, problemSource);
		int severity = message.isError() ? ProblemSeverities.Error : ProblemSeverities.Warning;
		char[] filename = problemSource.fileName;
		boolean usedBinarySourceFileName = false;
		if (problemSource.isFromBinarySource()) {
			if (sLoc != null) {
				filename = sLoc.getSourceFile().getPath().toCharArray();
				usedBinarySourceFileName = true;
			}
		}
		ReferenceContext referenceContext = findReferenceContextFor(problemSource);
		CategorizedProblem problem = compiler.problemReporter.createProblem(filename, IProblem.Unclassified, new String[0],
				new String[] { message.getMessage() }, severity, startPos, endPos, sLoc != null ? sLoc.getLine() : 0,
				sLoc != null ? sLoc.getColumn() : 0);
		IProblem[] seeAlso = buildSeeAlsoProblems(problem, message.getExtraSourceLocations(), problemSource,
				usedBinarySourceFileName);
		problem.setSeeAlsoProblems(seeAlso);

		StringBuffer details = new StringBuffer();
		// Stick more info in supplementary message info
		if (message.getDetails() != null) {
			details.append(message.getDetails());
		}
		// Remember if this message was due to a deow
		if (message.getDeclared()) {
			details.append("[deow=true]");
		}
		if (message instanceof LintMessage) {
			String lintMessageName = ((LintMessage) message).getLintKind();
			details.append("[Xlint:").append(lintMessageName).append("]");
		}
		if (details.length() != 0) {
			problem.setSupplementaryMessageInfo(details.toString());
		}
		compiler.problemReporter.record(problem, problemSource, referenceContext, message.isError());
		return true;
	}

	public boolean isIgnoring(Kind kind) {
		return sink.isIgnoring(kind);
	}

	/**
	 * No-op
	 * 
	 * @see org.aspectj.bridge.IMessageHandler#isIgnoring(org.aspectj.bridge.IMessage.Kind)
	 * @param kind
	 */
	public void dontIgnore(IMessage.Kind kind) {

	}

	/**
	 * No-op
	 * 
	 * @see org.aspectj.bridge.IMessageHandler#ignore(org.aspectj.bridge.IMessage.Kind)
	 * @param kind
	 */
	public void ignore(Kind kind) {
	}

	private int getStartPos(ISourceLocation sLoc, CompilationResult result) {
		int pos = 0;
		if (sLoc == null)
			return 0;
		int line = sLoc.getLine();
		if (sLoc instanceof EclipseSourceLocation) {
			pos = ((EclipseSourceLocation) sLoc).getStartPos();
		} else {
			if (line <= 1)
				return 0;
			if (result != null) {
				if ((result.lineSeparatorPositions != null) && (result.lineSeparatorPositions.length >= (line - 1))) {
					pos = result.lineSeparatorPositions[line - 2] + 1;
				}
			}
		}
		return pos;
	}

	private int getEndPos(ISourceLocation sLoc, CompilationResult result) {
		int pos = 0;
		if (sLoc == null)
			return 0;
		int line = sLoc.getLine();
		if (line <= 0)
			line = 1;
		if (sLoc instanceof EclipseSourceLocation) {
			pos = ((EclipseSourceLocation) sLoc).getEndPos();
		} else {
			if (result != null) {
				if ((result.lineSeparatorPositions != null) && (result.lineSeparatorPositions.length >= line)) {
					pos = result.lineSeparatorPositions[line - 1] - 1;
				}
			}
		}
		return pos;
	}

	private ReferenceContext findReferenceContextFor(CompilationResult result) {
		ReferenceContext context = null;
		if (compiler.unitsToProcess == null)
			return null;
		for (int i = 0; i < compiler.unitsToProcess.length; i++) {
			if ((compiler.unitsToProcess[i] != null) && (compiler.unitsToProcess[i].compilationResult == result)) {
				context = compiler.unitsToProcess[i];
				break;
			}
		}
		return context;
	}

	private IProblem[] buildSeeAlsoProblems(IProblem originalProblem, List sourceLocations, CompilationResult problemSource,
			boolean usedBinarySourceFileName) {
		List<IProblem> ret = new ArrayList<>();

		for (Object sourceLocation : sourceLocations) {
			ISourceLocation loc = (ISourceLocation) sourceLocation;
			if (loc != null) {
				DefaultProblem dp = new DefaultProblem(loc.getSourceFile().getPath().toCharArray(), "see also", 0, new String[]{},
						ProblemSeverities.Ignore, getStartPos(loc, null), getEndPos(loc, null), loc.getLine(), loc.getColumn());
				ret.add(dp);
			} else {
				System.err.println("About to abort due to null location, dumping state:");
				System.err.println("> Original Problem=" + problemSource.toString());
				throw new RuntimeException(
						"Internal Compiler Error: Unexpected null source location passed as 'see also' location.");
			}
		}
		if (usedBinarySourceFileName) {
			DefaultProblem dp = new DefaultProblem(problemSource.fileName, "see also", 0, new String[] {},
					ProblemSeverities.Ignore, 0, 0, 0, 0);
			ret.add(dp);
		}
		IProblem[] retValue = (IProblem[]) ret.toArray(new IProblem[] {});
		return retValue;
	}

}
