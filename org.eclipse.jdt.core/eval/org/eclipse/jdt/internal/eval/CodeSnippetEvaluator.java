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

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * A code snippet evaluator compiles and returns class file for a code snippet.
 * Or it reports problems against the code snippet. 
 */
public class CodeSnippetEvaluator extends Evaluator implements EvaluationConstants {
	/**
	 * Whether the code snippet support classes should be found in the provided name environment
	 * or on disk.
	 */
	final static boolean DEVELOPMENT_MODE = false;

	/**
	 * The code snippet to evaluate.
	 */
	char[] codeSnippet;

	/**
	 * The code snippet to generated compilation unit mapper
	 */
	CodeSnippetToCuMapper mapper;
/**
 * Creates a new code snippet evaluator.
 */
CodeSnippetEvaluator(char[] codeSnippet, EvaluationContext context, INameEnvironment environment, Map options, IRequestor requestor, IProblemFactory problemFactory) {
	super(context, environment, options, requestor, problemFactory);
	this.codeSnippet = codeSnippet;
}
/**
 * @see org.eclipse.jdt.internal.eval.Evaluator
 */
protected void addEvaluationResultForCompilationProblem(Map resultsByIDs, IProblem problem, char[] cuSource) {
	CodeSnippetToCuMapper mapper = getMapper();
	int pbLineNumber = problem.getSourceLineNumber();
	int evaluationType = mapper.getEvaluationType(pbLineNumber);

	char[] evaluationID = null;
	switch(evaluationType) {
		case EvaluationResult.T_PACKAGE:
			evaluationID = this.context.packageName;
			
			// shift line number, source start and source end
			problem.setSourceLineNumber(1);
			problem.setSourceStart(0);
			problem.setSourceEnd(evaluationID.length - 1);
			break;
			
		case EvaluationResult.T_IMPORT:
			evaluationID = mapper.getImport(pbLineNumber);

			// shift line number, source start and source end
			problem.setSourceLineNumber(1);
			problem.setSourceStart(0);
			problem.setSourceEnd(evaluationID.length - 1);
			break;

		case EvaluationResult.T_CODE_SNIPPET:
			evaluationID = this.codeSnippet;
		
			// shift line number, source start and source end
			problem.setSourceLineNumber(pbLineNumber - this.mapper.lineNumberOffset);
			problem.setSourceStart(problem.getSourceStart() - this.mapper.startPosOffset);
			problem.setSourceEnd(problem.getSourceEnd() - this.mapper.startPosOffset);
			break;
			
		case EvaluationResult.T_INTERNAL:
			evaluationID = cuSource;
			break;
	}

	EvaluationResult result = (EvaluationResult)resultsByIDs.get(evaluationID);
	if (result == null) {
		resultsByIDs.put(evaluationID, new EvaluationResult(evaluationID, evaluationType, new IProblem[] {problem}));
	} else {
		result.addProblem(problem);
	}
}
/**
 * @see org.eclipse.jdt.internal.eval.Evaluator
 */
protected char[] getClassName() {
	return CharOperation.concat(CODE_SNIPPET_CLASS_NAME_PREFIX, Integer.toString(this.context.CODE_SNIPPET_COUNTER + 1).toCharArray());
}
/**
 * @see Evaluator.
 */
Compiler getCompiler(ICompilerRequestor requestor) {
	Compiler compiler = null;
	if (!DEVELOPMENT_MODE) {
		// If we are not developping the code snippet support classes,
		// use a regular compiler and feed its lookup environment with 
		// the code snippet support classes

		compiler = 
			new CodeSnippetCompiler(
				this.environment, 
				DefaultErrorHandlingPolicies.exitAfterAllProblems(), 
				this.options, 
				requestor, 
				this.problemFactory,
				this.context,
				getMapper().startPosOffset,
				getMapper().startPosOffset + codeSnippet.length - 1);
		// Initialize the compiler's lookup environment with the already compiled super classes
		IBinaryType binary = this.context.getRootCodeSnippetBinary();
		if (binary != null) {
			compiler.lookupEnvironment.cacheBinaryType(binary);
		}
		VariablesInfo installedVars = this.context.installedVars;
		if (installedVars != null) {
			ClassFile[] globalClassFiles = installedVars.classFiles;
			for (int i = 0; i < globalClassFiles.length; i++) {
				ClassFileReader binaryType = null;
				try {
					binaryType = new ClassFileReader(globalClassFiles[i].getBytes(), null);
				} catch (ClassFormatException e) {
					e.printStackTrace(); // Should never happen since we compiled this type
				}
				compiler.lookupEnvironment.cacheBinaryType(binaryType);
			}
		}
	} else {
		// If we are developping the code snippet support classes,
		// use a wrapped environment so that if the code snippet classes are not found
		// then a default implementation is provided.

		compiler = new Compiler(
			getWrapperEnvironment(), 
			DefaultErrorHandlingPolicies.exitAfterAllProblems(), 
			this.options, 
			requestor, 
			this.problemFactory);
	}
	return compiler;
}
private CodeSnippetToCuMapper getMapper() {
	if (this.mapper == null) {
		char[] varClassName = null;
		VariablesInfo installedVars = this.context.installedVars;
		if (installedVars != null) {
			char[] superPackageName = installedVars.packageName;
			if (superPackageName != null && superPackageName.length != 0) {
				varClassName = CharOperation.concat(superPackageName, installedVars.className, '.');
			} else {
				varClassName = installedVars.className;
			}
			
		}
		this.mapper = new CodeSnippetToCuMapper(
			this.codeSnippet, 
			this.context.packageName,
			this.context.imports,
			getClassName(),
			varClassName,
			this.context.localVariableNames, 
			this.context.localVariableTypeNames, 
			this.context.localVariableModifiers, 
			this.context.declaringTypeName			
		);

	}
	return this.mapper;
}
/**
 * @see org.eclipse.jdt.internal.eval.Evaluator
 */
protected char[] getSource() {
	return getMapper().cuSource;
}
/**
 * Returns an environment that wraps the client's name environment.
 * This wrapper always considers the wrapped environment then if the name is
 * not found, it search in the code snippet support. This includes the superclass
 * org.eclipse.jdt.internal.eval.target.CodeSnippet as well as the global variable classes.
 */
private INameEnvironment getWrapperEnvironment() {
	return new CodeSnippetEnvironment(this.environment, this.context);
}
}
