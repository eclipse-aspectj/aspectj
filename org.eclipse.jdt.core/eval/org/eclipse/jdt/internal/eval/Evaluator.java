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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * A evaluator builds a compilation unit and compiles it into class files.
 * If the compilation unit has problems, reports the problems using the
 * requestor.
 */
public abstract class Evaluator {
	EvaluationContext context;
	INameEnvironment environment;
	Map options;
	IRequestor requestor;
	IProblemFactory problemFactory;
/**
 * Creates a new evaluator.
 */
Evaluator(EvaluationContext context, INameEnvironment environment, Map options, IRequestor requestor, IProblemFactory problemFactory) {
	this.context = context;
	this.environment = environment;
	this.options = options;
	this.requestor = requestor;
	this.problemFactory = problemFactory;
}
/**
 * Adds the given problem to the corresponding evaluation result in the given table. If the evaluation
 * result doesn't exist yet, adds it in the table. Its evaluation id and evaluation type
 * are computed so that they correspond to the given problem. If it is found to be an internal problem,
 * then the evaluation id of the result is the given compilation unit source.
 */
abstract protected void addEvaluationResultForCompilationProblem(Map resultsByIDs,IProblem problem, char[] cuSource);
/**
 * Returns the evaluation results that converts the given compilation result that has problems.
 * If the compilation result has more than one problem, then the problems are broken down so that
 * each evaluation result has the same evaluation id. 
 */
protected EvaluationResult[] evaluationResultsForCompilationProblems(CompilationResult result, char[] cuSource) {
	// Break down the problems and group them by ids in evaluation results
	IProblem[] problems = result.getProblems();
	HashMap resultsByIDs = new HashMap(5);
	for (int i = 0; i < problems.length; i++) {
		addEvaluationResultForCompilationProblem(resultsByIDs, problems[i], cuSource);
	}

	// Copy results
	int size = resultsByIDs.size();
	EvaluationResult[] evalResults = new EvaluationResult[size];
	Iterator results = resultsByIDs.values().iterator();
	for (int i = 0; i < size; i++) {
		evalResults[i] = (EvaluationResult)results.next();
	}

	return evalResults;
}
/**
 * Compiles and returns the class definitions for the current compilation unit.
 * Returns null if there are any errors.
 */
ClassFile[] getClasses() {
	final char[] source = getSource();
	final ArrayList classDefinitions = new ArrayList();

	// The requestor collects the class definitions and problems
	class CompilerRequestor implements ICompilerRequestor {
		boolean hasErrors = false;
		public void acceptResult(CompilationResult result) {
			if (result.hasProblems()) {
				EvaluationResult[] evalResults = evaluationResultsForCompilationProblems(result, source);
				for (int i = 0; i < evalResults.length; i++) {
					EvaluationResult evalResult = evalResults[i];
					IProblem[] problems = evalResult.getProblems();
					for (int j = 0; j < problems.length; j++) {
						Evaluator.this.requestor.acceptProblem(problems[j], evalResult.getEvaluationID(), evalResult.getEvaluationType());
					}
				}
			}
			if (result.hasErrors()) {
				hasErrors = true;
			} else {
				ClassFile[] classFiles = result.getClassFiles();
				for (int i = 0; i < classFiles.length; i++) {
					ClassFile classFile = classFiles[i];
/* 
			
					char[] filename = classFile.fileName();
					int length = filename.length;
					char[] relativeName = new char[length + 6];
					System.arraycopy(filename, 0, relativeName, 0, length);
					System.arraycopy(".class".toCharArray(), 0, relativeName, length, 6);
					CharOperation.replace(relativeName, '/', java.io.File.separatorChar);
					ClassFile.writeToDisk("d:/test/snippet", new String(relativeName), classFile.getBytes());
					String str = "d:/test/snippet" + "/" + new String(relativeName);
					System.out.println(org.eclipse.jdt.core.tools.classfmt.disassembler.ClassFileDisassembler.disassemble(str));				
 */	
					classDefinitions.add(classFile);
				}
			}
		}
	}

	// Compile compilation unit
	CompilerRequestor compilerRequestor = new CompilerRequestor();
	Compiler compiler = getCompiler(compilerRequestor);
	compiler.compile(new ICompilationUnit[] {new ICompilationUnit() {
		public char[] getFileName() {
			 // Name of class is name of CU
			return CharOperation.concat(Evaluator.this.getClassName(), ".java".toCharArray()); //$NON-NLS-1$
		}
		public char[] getContents() {
			return source;
		}
		public char[] getMainTypeName() {
			return Evaluator.this.getClassName();
		}
		public char[][] getPackageName() {
			return null;
		}
	}});
	if (compilerRequestor.hasErrors) {
		return null;
	} else {
		ClassFile[] result = new ClassFile[classDefinitions.size()];
		classDefinitions.toArray(result);
		return result;
	}
}
/**
 * Returns the name of the current class. This is the simple name of the class.
 * This doesn't include the extension ".java" nor the name of the package.
 */
abstract protected char[] getClassName();
/**
 * Creates and returns a compiler for this evaluator.
 */
Compiler getCompiler(ICompilerRequestor requestor) {
	return new Compiler(
		this.environment, 
		DefaultErrorHandlingPolicies.exitAfterAllProblems(), 
		this.options, 
		requestor, 
		this.problemFactory);
}
/**
 * Builds and returns the source for the current compilation unit.
 */
abstract protected char[] getSource();
}
