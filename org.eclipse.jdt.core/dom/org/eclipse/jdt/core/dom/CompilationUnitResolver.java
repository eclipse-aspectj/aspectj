/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

import java.util.*;

class CompilationUnitResolver extends Compiler {
	
	/**
	 * Answer a new CompilationUnitVisitor using the given name environment and compiler options.
	 * The environment and options will be in effect for the lifetime of the compiler.
	 * When the compiler is run, compilation results are sent to the given requestor.
	 *
	 *  @param environment org.eclipse.jdt.internal.compiler.api.env.INameEnvironment
	 *      Environment used by the compiler in order to resolve type and package
	 *      names. The name environment implements the actual connection of the compiler
	 *      to the outside world (e.g. in batch mode the name environment is performing
	 *      pure file accesses, reuse previous build state or connection to repositories).
	 *      Note: the name environment is responsible for implementing the actual classpath
	 *            rules.
	 *
	 *  @param policy org.eclipse.jdt.internal.compiler.api.problem.IErrorHandlingPolicy
	 *      Configurable part for problem handling, allowing the compiler client to
	 *      specify the rules for handling problems (stop on first error or accumulate
	 *      them all) and at the same time perform some actions such as opening a dialog
	 *      in UI when compiling interactively.
	 *      @see org.eclipse.jdt.internal.compiler.api.problem.DefaultErrorHandlingPolicies
	 * 
	 *	@param settings The settings to use for the resolution.
	 *      
	 *  @param requestor org.eclipse.jdt.internal.compiler.api.ICompilerRequestor
	 *      Component which will receive and persist all compilation results and is intended
	 *      to consume them as they are produced. Typically, in a batch compiler, it is 
	 *      responsible for writing out the actual .class files to the file system.
	 *      @see org.eclipse.jdt.internal.compiler.api.CompilationResult
	 *
	 *  @param problemFactory org.eclipse.jdt.internal.compiler.api.problem.IProblemFactory
	 *      Factory used inside the compiler to create problem descriptors. It allows the
	 *      compiler client to supply its own representation of compilation problems in
	 *      order to avoid object conversions. Note that the factory is not supposed
	 *      to accumulate the created problems, the compiler will gather them all and hand
	 *      them back as part of the compilation unit result.
	 */
	public CompilationUnitResolver(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory) {

		super(environment, policy, settings, requestor, problemFactory, false);
	}

	/**
	 * Add additional source types
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding) {
		CompilationResult result =
			new CompilationResult(sourceTypes[0].getFileName(), 1, 1, this.options.maxProblemsPerUnit);
		// need to hold onto this
		CompilationUnitDeclaration unit =
			SourceTypeConverter.buildCompilationUnit(
				sourceTypes,
				true,
				true,
				lookupEnvironment.problemReporter,
				result);

		if (unit != null) {
			this.lookupEnvironment.buildTypeBindings(unit);
			this.lookupEnvironment.completeTypeBindings(unit, true);
		}
	}

	/*
	 *  Low-level API performing the actual compilation
	 */
	protected static IErrorHandlingPolicy getHandlingPolicy() {

		// passes the initial set of files to the batch oracle (to avoid finding more than once the same units when case insensitive match)	
		return new IErrorHandlingPolicy() {
			public boolean stopOnFirstError() {
				return false;
			}
			public boolean proceedOnErrors() {
				return false; // stop if there are some errors 
			}
		};
	}

	protected static INameEnvironment getNameEnvironment(ICompilationUnit sourceUnit)
		throws JavaModelException {
		return (SearchableEnvironment) ((JavaProject) sourceUnit.getJavaProject())
			.getSearchableNameEnvironment();
	}

	protected static INameEnvironment getNameEnvironment(IJavaProject javaProject)
		throws JavaModelException {
		return (SearchableEnvironment) ((JavaProject) javaProject)
			.getSearchableNameEnvironment();
	}
	
	/*
	 * Answer the component to which will be handed back compilation results from the compiler
	 */
	protected static ICompilerRequestor getRequestor() {
		return new ICompilerRequestor() {
			public void acceptResult(CompilationResult compilationResult) {
			}
		};
	}

	public static CompilationUnitDeclaration resolve(
		ICompilationUnit unitElement,
		IAbstractSyntaxTreeVisitor visitor)
		throws JavaModelException {

		char[] fileName = unitElement.getElementName().toCharArray();
		CompilationUnitResolver compilationUnitVisitor =
			new CompilationUnitResolver(
				getNameEnvironment(unitElement),
				getHandlingPolicy(),
				JavaCore.getOptions(),
				getRequestor(),
				getProblemFactory(fileName, visitor));

		CompilationUnitDeclaration unit = null;
		try {
			String encoding = JavaCore.getOption(JavaCore.CORE_ENCODING);

			IPackageFragment packageFragment = (IPackageFragment)unitElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
			char[][] expectedPackageName = null;
			if (packageFragment != null){
				expectedPackageName = CharOperation.splitOn('.', packageFragment.getElementName().toCharArray());
			}
			
			unit =
				compilationUnitVisitor.resolve(
					new BasicCompilationUnit(
						unitElement.getSource().toCharArray(),
						expectedPackageName,
						new String(fileName),
						encoding));
			return unit;
		} finally {
			if (unit != null) {
				unit.cleanUp();
			}
		}
	}
	
	public static CompilationUnitDeclaration parse(char[] source) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilerOptions compilerOptions = new CompilerOptions(JavaCore.getOptions());
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory(Locale.getDefault())),
			false,
			compilerOptions.assertMode);
		org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit = 
			new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);
		CompilationUnitDeclaration compilationUnitDeclaration = parser.dietParse(sourceUnit, new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit));
		
		if (compilationUnitDeclaration.ignoreMethodBodies) {
			compilationUnitDeclaration.ignoreFurtherInvestigation = true;
			// if initial diet parse did not work, no need to dig into method bodies.
			return compilationUnitDeclaration; 
		}
		
		//fill the methods bodies in order for the code to be generated
		//real parse of the method....
		parser.scanner.setSource(source);
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = compilationUnitDeclaration.types;
		if (types != null) {
			for (int i = types.length; --i >= 0;)
				types[i].parseMethod(parser, compilationUnitDeclaration);
		}
		return compilationUnitDeclaration;
	}

	protected static IProblemFactory getProblemFactory(final char[] fileName, final IAbstractSyntaxTreeVisitor visitor) {

		return new DefaultProblemFactory(Locale.getDefault()) {
			public IProblem createProblem(
				char[] originatingFileName,
				int problemId,
				String[] arguments,
				int severity,
				int startPosition,
				int endPosition,
				int lineNumber) {

				IProblem problem =
					super.createProblem(
						originatingFileName,
						problemId,
						arguments,
						severity,
						startPosition,
						endPosition,
						lineNumber);
				// only consider problems associated with resolved file
				if (CharOperation.equals(originatingFileName, fileName)){
					visitor.acceptProblem(problem);
				}
				return problem;
			}
		};
	}

	public static CompilationUnitDeclaration resolve(
		char[] source,
		String unitName,
		IJavaProject javaProject,
		IAbstractSyntaxTreeVisitor visitor)
		throws JavaModelException {
	
		CompilationUnitResolver compilationUnitVisitor =
			new CompilationUnitResolver(
				getNameEnvironment(javaProject),
				getHandlingPolicy(),
				JavaCore.getOptions(),
				getRequestor(),
				getProblemFactory(unitName.toCharArray(), visitor));
	
		CompilationUnitDeclaration unit = null;
		try {
			String encoding = JavaCore.getOption(JavaCore.CORE_ENCODING);
			char[][] expectedPackageName = null;
	
			unit =
				compilationUnitVisitor.resolve(
					new BasicCompilationUnit(source, expectedPackageName, unitName, encoding));
			return unit;
		} finally {
			if (unit != null) {
				unit.cleanUp();
			}
		}
	}	
}