/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Palo Alto Research Center, Incorporated - AspectJ adaptation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.parser.*;

/**
 * AspectJ - added several extension points for subclasses
 */
public abstract class AbstractMethodDeclaration
	extends AstNode
	implements ProblemSeverities, ReferenceContext {
		
	public MethodScope scope;
	//it is not relevent for constructor but it helps to have the name of the constructor here 
	//which is always the name of the class.....parsing do extra work to fill it up while it do not have to....
	public char[] selector;
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiers;
	public int modifiersSourceStart;
	public Argument[] arguments;
	public TypeReference[] thrownExceptions;
	public Statement[] statements;
	public int explicitDeclarations;
	public MethodBinding binding;
	public boolean ignoreFurtherInvestigation = false;
	public boolean needFreeReturn = false;

	public int bodyStart;
	public int bodyEnd = -1;
	public CompilationResult compilationResult;
	
	AbstractMethodDeclaration(CompilationResult compilationResult){
		this.compilationResult = compilationResult;
	}
	
	/*
	 *	We cause the compilation task to abort to a given extent.
	 */
	public void abort(int abortLevel) {

		if (scope == null) {
			throw new AbortCompilation(); // cannot do better
		}

		CompilationResult compilationResult =
			scope.referenceCompilationUnit().compilationResult;

		switch (abortLevel) {
			case AbortCompilation :
				throw new AbortCompilation(compilationResult);
			case AbortCompilationUnit :
				throw new AbortCompilationUnit(compilationResult);
			case AbortType :
				throw new AbortType(compilationResult);
			default :
				throw new AbortMethod(compilationResult);
		}
	}

	public void analyseCode(
		ClassScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// starting of the code analysis for methods
		if (ignoreFurtherInvestigation)
			return;
		try {
			if (binding == null)
				return;
			// may be in a non necessary <clinit> for innerclass with static final constant fields
			if (binding.isAbstract() || binding.isNative())
				return;

			ExceptionHandlingFlowContext methodContext =
				new ExceptionHandlingFlowContext(
					flowContext,
					this,
					binding.thrownExceptions,
					scope,
					FlowInfo.DeadEnd);

			// propagate to statements
			if (statements != null) {
				for (int i = 0, count = statements.length; i < count; i++) {
					Statement stat;
					if (!flowInfo.complainIfUnreachable((stat = statements[i]), scope)) {
						flowInfo = stat.analyseCode(scope, methodContext, flowInfo);
					}
				}
			}
			// check for missing returning path
			TypeBinding returnType = binding.returnType;
			if ((returnType == VoidBinding) || isAbstract()) {
				needFreeReturn =
					!((flowInfo == FlowInfo.DeadEnd) || flowInfo.isFakeReachable());
			} else {
				if (flowInfo != FlowInfo.DeadEnd) {
					// special test for empty methods that should return something
					if ((statements == null) && (returnType != VoidBinding)) {
						scope.problemReporter().shouldReturn(returnType, this);
					} else {
						scope.problemReporter().shouldReturn(
							returnType,
							statements[statements.length - 1]);
					}
				}
			}
		} catch (AbortMethod e) {
			this.ignoreFurtherInvestigation = true;
		}
	}

	/**
	 * Bind and add argument's binding into the scope of the method
	 */
	public void bindArguments() {

		if (arguments != null) {
			// by default arguments in abstract/native methods are considered to be used (no complaint is expected)
			boolean used = binding == null || binding.isAbstract() || binding.isNative();

			int length = arguments.length;
			for (int i = 0; i < length; i++) {
				TypeBinding argType = binding == null ? null : binding.parameters[i];
				arguments[i].bind(scope, argType, used);
			}
		}
	}

	/**
	 * Record the thrown exception type bindings in the corresponding type references.
	 */
	public void bindThrownExceptions() {

		if (this.thrownExceptions != null
			&& this.binding != null
			&& this.binding.thrownExceptions != null) {
			int length = this.binding.thrownExceptions.length;
			for (int i = 0; i < length; i++) {
				this.thrownExceptions[i].binding = this.binding.thrownExceptions[i];
			}
		}
	}

	public CompilationResult compilationResult() {
		
		return this.compilationResult;
	}
	
	/**
	 * Bytecode generation for a method
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		
		int problemResetPC = 0;
		classFile.codeStream.wideMode = false; // reset wideMode to false
		if (ignoreFurtherInvestigation) {
			// method is known to have errors, dump a problem method
			if (this.binding == null)
				return; // handle methods with invalid signature or duplicates
			int problemsLength;
			IProblem[] problems =
				scope.referenceCompilationUnit().compilationResult.getProblems();
			IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
			System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
			classFile.addProblemMethod(this, binding, problemsCopy);
			return;
		}
		// regular code generation
		try {
			problemResetPC = classFile.contentsOffset;
			this.generateCode(classFile);
		} catch (AbortMethod e) {
			// a fatal error was detected during code generation, need to restart code gen if possible
			if (e.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
				// a branch target required a goto_w, restart code gen in wide mode.
				try {
					this.traverse(new ResetStateForCodeGenerationVisitor(), classScope);
					classFile.contentsOffset = problemResetPC;
					classFile.methodCount--;
					classFile.codeStream.wideMode = true; // request wide mode 
					this.generateCode(classFile); // restart method generation
				} catch (AbortMethod e2) {
					int problemsLength;
					IProblem[] problems =
						scope.referenceCompilationUnit().compilationResult.getProblems();
					IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
					System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
					classFile.addProblemMethod(this, binding, problemsCopy, problemResetPC);
				}
			} else {
				// produce a problem method accounting for this fatal error
				int problemsLength;
				IProblem[] problems =
					scope.referenceCompilationUnit().compilationResult.getProblems();
				IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
				System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
				classFile.addProblemMethod(this, binding, problemsCopy, problemResetPC);
			}
		}
	}

	private void generateCode(ClassFile classFile) {

		classFile.generateMethodInfoHeader(binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = generateInfoAttributes(classFile);
		if ((!binding.isNative()) && (!binding.isAbstract())) {
			int codeAttributeOffset = classFile.contentsOffset;
			classFile.generateCodeAttributeHeader();
			CodeStream codeStream = classFile.codeStream;
			codeStream.reset(this, classFile);
			// initialize local positions
			scope.computeLocalVariablePositions(binding.isStatic() ? 0 : 1, codeStream);

			// arguments initialization for local variable debug attributes
			if (arguments != null) {
				for (int i = 0, max = arguments.length; i < max; i++) {
					LocalVariableBinding argBinding;
					codeStream.addVisibleLocalVariable(argBinding = arguments[i].binding);
					argBinding.recordInitializationStartPC(0);
				}
			}
			if (statements != null) {
				for (int i = 0, max = statements.length; i < max; i++)
					statements[i].generateCode(scope, codeStream);
			}
			if (needFreeReturn) {
				codeStream.return_();
			}
			// local variable attributes
			codeStream.exitUserScope(scope);
			codeStream.recordPositionsFrom(0, this.bodyEnd);
			classFile.completeCodeAttribute(codeAttributeOffset);
			attributeNumber++;
		}
		classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);

		// if a problem got reported during code gen, then trigger problem method creation
		if (ignoreFurtherInvestigation) {
			throw new AbortMethod(scope.referenceCompilationUnit().compilationResult);
		}
	}

	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	public boolean isAbstract() {

		if (binding != null)
			return binding.isAbstract();
		return (modifiers & AccAbstract) != 0;
	}

	public boolean isClinit() {

		return false;
	}

	public boolean isConstructor() {

		return false;
	}

	public boolean isDefaultConstructor() {

		return false;
	}

	public boolean isInitializationMethod() {

		return false;
	}

	public boolean isNative() {

		if (binding != null)
			return binding.isNative();
		return (modifiers & AccNative) != 0;
	}

	public boolean isStatic() {

		if (binding != null)
			return binding.isStatic();
		return (modifiers & AccStatic) != 0;
	}

	/**
	 * Fill up the method body with statement
	 */
	public abstract void parseStatements(
		Parser parser,
		CompilationUnitDeclaration unit);

	public void resolve(ClassScope upperScope) {

		if (binding == null) {
			ignoreFurtherInvestigation = true;
		}

		try {
			bindArguments(); 
			bindThrownExceptions();
			resolveStatements(upperScope);
		} catch (AbortMethod e) {	// ========= abort on fatal error =============
			this.ignoreFurtherInvestigation = true;
		} 
	}

	public void resolveStatements(ClassScope upperScope) {

		if (statements != null) {
			int i = 0, length = statements.length;
			while (i < length)
				statements[i++].resolve(scope);
		}
	}

	public String returnTypeToString(int tab) {

		return ""; //$NON-NLS-1$
	}

	public void tagAsHavingErrors() {

		ignoreFurtherInvestigation = true;
	}

	public String toString(int tab) {

		String s = tabString(tab);
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}

		s += returnTypeToString(0);
		s += new String(selector) + "("; //$NON-NLS-1$
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				s += arguments[i].toString(0);
				if (i != (arguments.length - 1))
					s = s + ", "; //$NON-NLS-1$
			};
		};
		s += ")"; //$NON-NLS-1$
		if (thrownExceptions != null) {
			s += " throws "; //$NON-NLS-1$
			for (int i = 0; i < thrownExceptions.length; i++) {
				s += thrownExceptions[i].toString(0);
				if (i != (thrownExceptions.length - 1))
					s = s + ", "; //$NON-NLS-1$
			};
		};

		s += toStringStatements(tab + 1);
		return s;
	}

	public String toStringStatements(int tab) {

		if (isAbstract() || (this.modifiers & AccSemicolonBody) != 0)
			return ";"; //$NON-NLS-1$

		String s = " {"; //$NON-NLS-1$
		if (statements != null) {
			for (int i = 0; i < statements.length; i++) {
				s = s + "\n" + statements[i].toString(tab); //$NON-NLS-1$
				if (!(statements[i] instanceof Block)) {
					s += ";"; //$NON-NLS-1$
				}
			}
		}
		s += "\n" + tabString(tab == 0 ? 0 : tab - 1) + "}"; //$NON-NLS-2$ //$NON-NLS-1$
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		ClassScope classScope) {
	}
	
	//*********************************************************************
	//Hooks for AspectJ
	/**
	 * Called at the end of resolving types
	 * @returns false if some error occurred
	 */
	public boolean finishResolveTypes(SourceTypeBinding sourceTypeBinding) {
		return true;
	}

	/**
	 * Just before building bindings, hook for subclasses
	 */
	public void postParse(TypeDeclaration typeDec) {
		// do nothing.  subclasses may override
	}
	
	/**
	 * Generates my info attributes, hook for subclasses
	 */
	protected int generateInfoAttributes(ClassFile classFile) {
		return classFile.generateMethodInfoAttribute(binding);
	}

}