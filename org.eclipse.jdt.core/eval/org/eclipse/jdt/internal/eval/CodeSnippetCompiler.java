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
package org.eclipse.jdt.internal.eval;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;

/**
 * A compiler that compiles code snippets. 
 */
public class CodeSnippetCompiler extends Compiler {
/**
 * Creates a new code snippet compiler initialized with a code snippet parser.
 */
public CodeSnippetCompiler(
		INameEnvironment environment, 
		IErrorHandlingPolicy policy, 
		Map settings, 
		ICompilerRequestor requestor, 
		IProblemFactory problemFactory,
		EvaluationContext evaluationContext,
		int codeSnippetStart,
		int codeSnippetEnd) {
	super(environment, policy, settings, requestor, problemFactory);
	this.parser = 
		new CodeSnippetParser(problemReporter, evaluationContext, this.options.parseLiteralExpressionsAsConstants, this.options.assertMode, codeSnippetStart, codeSnippetEnd);
	this.parseThreshold = 1; // fully parse only the code snippet compilation unit
}
}
