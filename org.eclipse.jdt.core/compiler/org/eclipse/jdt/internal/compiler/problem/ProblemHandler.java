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
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;

/*
 * Compiler error handler, responsible to determine whether
 * a problem is actually a warning or an error; also will
 * decide whether the compilation task can be processed further or not.
 *
 * Behavior : will request its current policy if need to stop on
 *	first error, and if should proceed (persist) with problems.
 */

public class ProblemHandler implements ProblemSeverities {

	final public IErrorHandlingPolicy policy;
	public final IProblemFactory problemFactory;
	public final CompilerOptions options;
/*
 * Problem handler can be supplied with a policy to specify
 * its behavior in error handling. Also see static methods for
 * built-in policies.
 *
 */
public ProblemHandler(IErrorHandlingPolicy policy, CompilerOptions options, IProblemFactory problemFactory) {
	this.policy = policy;
	this.problemFactory = problemFactory;
	this.options = options;
}
/*
 * Given the current configuration, answers which category the problem
 * falls into:
 *		Error | Warning | Ignore
 */
public int computeSeverity(int problemId){
	
	return Error; // by default all problems are errors
}
public IProblem createProblem(
	char[] fileName, 
	int problemId, 
	String[] problemArguments, 
	int severity, 
	int problemStartPosition, 
	int problemEndPosition, 
	int lineNumber,
	ReferenceContext referenceContext,
	CompilationResult unitResult) {

	return problemFactory.createProblem(
		fileName, 
		problemId, 
		problemArguments, 
		severity, 
		problemStartPosition, 
		problemEndPosition, 
		lineNumber); 
}
public void handle(
	int problemId, 
	String[] problemArguments, 
	int severity, 
	int problemStartPosition, 
	int problemEndPosition, 
	ReferenceContext referenceContext, 
	CompilationResult unitResult) {

	if (severity == Ignore)
		return;

	// if no reference context, we need to abort from the current compilation process
	if (referenceContext == null) {
		if ((severity & Error) != 0) { // non reportable error is fatal
			throw new AbortCompilation(problemId, problemArguments);
		} else {
			return; // ignore non reportable warning
		}
	}

	IProblem problem = 
		this.createProblem(
			unitResult.getFileName(), 
			problemId, 
			problemArguments, 
			severity, 
			problemStartPosition, 
			problemEndPosition, 
			problemStartPosition >= 0
				? searchLineNumber(unitResult.lineSeparatorPositions, problemStartPosition)
				: 0,
			referenceContext,
			unitResult); 
	if (problem == null) return; // problem couldn't be created, ignore
	
	switch (severity & Error) {
		case Error :
			this.record(problem, unitResult, referenceContext);
			referenceContext.tagAsHavingErrors();

			// should abort ?
			int abortLevel;
			if ((abortLevel = 
				(policy.stopOnFirstError() ? AbortCompilation : severity & Abort)) != 0) {

				referenceContext.abort(abortLevel);
			}
			break;
		case Warning :
			this.record(problem, unitResult, referenceContext);
			break;
	}
}
/**
 * Standard problem handling API, the actual severity (warning/error/ignore) is deducted
 * from the problem ID and the current compiler options.
 */
public void handle(
	int problemId, 
	String[] problemArguments, 
	int problemStartPosition, 
	int problemEndPosition, 
	ReferenceContext referenceContext, 
	CompilationResult unitResult) {

	this.handle(
		problemId,
		problemArguments,
		this.computeSeverity(problemId), // severity inferred using the ID
		problemStartPosition,
		problemEndPosition,
		referenceContext,
		unitResult);
}
public void record(IProblem problem, CompilationResult unitResult, ReferenceContext referenceContext) {
	unitResult.record(problem, referenceContext);
}
/**
 * Search the line number corresponding to a specific position
 *
 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
 */
public static final int searchLineNumber(int[] startLineIndexes, int position) {
	if (startLineIndexes == null)
		return 1;
	int length = startLineIndexes.length;
	if (length == 0)
		return 1;
	int g = 0, d = length - 1;
	int m = 0;
	while (g <= d) {
		m = (g + d) /2;
		if (position < startLineIndexes[m]) {
			d = m-1;
		} else if (position > startLineIndexes[m]) {
			g = m+1;
		} else {
			return m + 1;
		}
	}
	if (position < startLineIndexes[m]) {
		return m+1;
	}
	return m+2;
}
}
