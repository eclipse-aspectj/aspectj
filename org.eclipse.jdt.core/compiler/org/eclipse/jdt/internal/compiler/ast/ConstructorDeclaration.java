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
package org.eclipse.jdt.internal.compiler.ast;

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class ConstructorDeclaration extends AbstractMethodDeclaration {

	public ExplicitConstructorCall constructorCall;
	public final static char[] ConstantPoolName = "<init>".toCharArray(); //$NON-NLS-1$
	public boolean isDefaultConstructor = false;

	public int referenceCount = 0;
	// count how many times this constructor is referenced from other local constructors

	public ConstructorDeclaration(CompilationResult compilationResult){
		super(compilationResult);
	}
	
	public void analyseCode(
		ClassScope classScope,
		InitializationFlowContext initializerFlowContext,
		FlowInfo flowInfo) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			ExceptionHandlingFlowContext constructorContext =
				new ExceptionHandlingFlowContext(
					initializerFlowContext.parent,
					this,
					binding.thrownExceptions,
					scope,
					FlowInfo.DeadEnd);
			initializerFlowContext.checkInitializerExceptions(
				scope,
				constructorContext,
				flowInfo);

			// anonymous constructor can gain extra thrown exceptions from unhandled ones
			if (binding.declaringClass.isAnonymousType()) {
				ArrayList computedExceptions = constructorContext.extendedExceptions;
				if (computedExceptions != null){
					int size;
					if ((size = computedExceptions.size()) > 0){
						ReferenceBinding[] actuallyThrownExceptions;
						computedExceptions.toArray(actuallyThrownExceptions = new ReferenceBinding[size]);
						binding.thrownExceptions = actuallyThrownExceptions;
					}
				}
			}
			
			// propagate to constructor call
			if (constructorCall != null) {
				// if calling 'this(...)', then flag all non-static fields as definitely
				// set since they are supposed to be set inside other local constructor
				if (constructorCall.accessMode == ExplicitConstructorCall.This) {
					FieldBinding[] fields = binding.declaringClass.fields();
					for (int i = 0, count = fields.length; i < count; i++) {
						FieldBinding field;
						if (!(field = fields[i]).isStatic()) {
							flowInfo.markAsDefinitelyAssigned(field);
						}
					}
				}
				flowInfo = constructorCall.analyseCode(scope, constructorContext, flowInfo);
			}
			// propagate to statements
			if (statements != null) {
				for (int i = 0, count = statements.length; i < count; i++) {
					Statement stat;
					if (!flowInfo.complainIfUnreachable((stat = statements[i]), scope)) {
						flowInfo = stat.analyseCode(scope, constructorContext, flowInfo);
					}
				}
			}
			// check for missing returning path
			needFreeReturn =
				!((flowInfo == FlowInfo.DeadEnd) || flowInfo.isFakeReachable());

			// check missing blank final field initializations
			if ((constructorCall != null)
				&& (constructorCall.accessMode != ExplicitConstructorCall.This)) {
				flowInfo = flowInfo.mergedWith(initializerFlowContext.initsOnReturn);
				FieldBinding[] fields = binding.declaringClass.fields();
				for (int i = 0, count = fields.length; i < count; i++) {
					FieldBinding field;
					if ((!(field = fields[i]).isStatic())
						&& field.isFinal()
						&& (!flowInfo.isDefinitelyAssigned(fields[i]))) {
						scope.problemReporter().uninitializedBlankFinalField(
							field,
							isDefaultConstructor ? (AstNode) scope.referenceType() : this);
					}
				}
			}
		} catch (AbortMethod e) {
			this.ignoreFurtherInvestigation = true;
		}
	}

	/**
	 * Bytecode generation for a constructor
	 *
	 * @param classScope org.eclipse.jdt.internal.compiler.lookup.ClassScope
	 * @param classFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		int problemResetPC = 0;
		if (ignoreFurtherInvestigation) {
			if (this.binding == null)
				return; // Handle methods with invalid signature or duplicates
			int problemsLength;
			IProblem[] problems =
				scope.referenceCompilationUnit().compilationResult.getProblems();
			IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
			System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
			classFile.addProblemConstructor(this, binding, problemsCopy);
			return;
		}
		try {
			problemResetPC = classFile.contentsOffset;
			this.internalGenerateCode(classScope, classFile);
		} catch (AbortMethod e) {
			if (e.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
				// a branch target required a goto_w, restart code gen in wide mode.
				try {
					if (statements != null) {
						for (int i = 0, max = statements.length; i < max; i++)
							statements[i].resetStateForCodeGeneration();
					}
					classFile.contentsOffset = problemResetPC;
					classFile.methodCount--;
					classFile.codeStream.wideMode = true; // request wide mode 
					this.internalGenerateCode(classScope, classFile); // restart method generation
				} catch (AbortMethod e2) {
					int problemsLength;
					IProblem[] problems =
						scope.referenceCompilationUnit().compilationResult.getProblems();
					IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
					System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
					classFile.addProblemConstructor(this, binding, problemsCopy, problemResetPC);
				}
			} else {
				int problemsLength;
				IProblem[] problems =
					scope.referenceCompilationUnit().compilationResult.getProblems();
				IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
				System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
				classFile.addProblemConstructor(this, binding, problemsCopy, problemResetPC);
			}
		}
	}

	private void internalGenerateCode(ClassScope classScope, ClassFile classFile) {
		classFile.generateMethodInfoHeader(binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttribute(binding);
		if ((!binding.isNative()) && (!binding.isAbstract())) {
			TypeDeclaration declaringType = classScope.referenceContext;
			int codeAttributeOffset = classFile.contentsOffset;
			classFile.generateCodeAttributeHeader();
			CodeStream codeStream = classFile.codeStream;
			codeStream.reset(this, classFile);
			// initialize local positions - including initializer scope.
			ReferenceBinding declaringClass = binding.declaringClass;
			int argSize = 0;
			scope.computeLocalVariablePositions(// consider synthetic arguments if any
			argSize =
				declaringClass.isNestedType()
					? ((NestedTypeBinding) declaringClass).syntheticArgumentsOffset
					: 1,
				codeStream);
			if (arguments != null) {
				for (int i = 0, max = arguments.length; i < max; i++) {
					// arguments initialization for local variable debug attributes
					LocalVariableBinding argBinding;
					codeStream.addVisibleLocalVariable(argBinding = arguments[i].binding);
					argBinding.recordInitializationStartPC(0);
					TypeBinding argType;
					if ((argType = argBinding.type) == LongBinding || (argType == DoubleBinding)) {
						argSize += 2;
					} else {
						argSize++;
					}
				}
			}
			MethodScope initializerScope = declaringType.initializerScope;
			initializerScope.computeLocalVariablePositions(argSize, codeStream);
			// offset by the argument size (since not linked to method scope)

			// generate constructor call
			if (constructorCall != null) {
				constructorCall.generateCode(scope, codeStream);
			}
			// generate field initialization - only if not invoking another constructor call of the same class
			if ((constructorCall != null)
				&& (constructorCall.accessMode != ExplicitConstructorCall.This)) {
				// generate synthetic fields initialization
				if (declaringClass.isNestedType()) {
					NestedTypeBinding nestedType = (NestedTypeBinding) declaringClass;
					SyntheticArgumentBinding[] syntheticArgs =
						nestedType.syntheticEnclosingInstances();
					for (int i = 0, max = syntheticArgs == null ? 0 : syntheticArgs.length;
						i < max;
						i++) {
						if (syntheticArgs[i].matchingField != null) {
							codeStream.aload_0();
							codeStream.load(syntheticArgs[i]);
							codeStream.putfield(syntheticArgs[i].matchingField);
						}
					}
					syntheticArgs = nestedType.syntheticOuterLocalVariables();
					for (int i = 0, max = syntheticArgs == null ? 0 : syntheticArgs.length;
						i < max;
						i++) {
						if (syntheticArgs[i].matchingField != null) {
							codeStream.aload_0();
							codeStream.load(syntheticArgs[i]);
							codeStream.putfield(syntheticArgs[i].matchingField);
						}
					}
				}
				// generate user field initialization
				if (declaringType.fields != null) {
					for (int i = 0, max = declaringType.fields.length; i < max; i++) {
						FieldDeclaration fieldDecl;
						if (!(fieldDecl = declaringType.fields[i]).isStatic()) {
							fieldDecl.generateCode(initializerScope, codeStream);
						}
					}
				}
			}
			// generate statements
			if (statements != null) {
				for (int i = 0, max = statements.length; i < max; i++) {
					statements[i].generateCode(scope, codeStream);
				}
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

	public boolean isConstructor() {

		return true;
	}

	public boolean isDefaultConstructor() {

		return isDefaultConstructor;
	}

	public boolean isInitializationMethod() {

		return true;
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {

		//fill up the constructor body with its statements
		if (ignoreFurtherInvestigation)
			return;
		if (isDefaultConstructor){
			constructorCall =
				new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
			constructorCall.sourceStart = sourceStart;
			constructorCall.sourceEnd = sourceEnd; 
			return;
		}
		parser.parse(this, unit);

	}

	/*
	 * Type checking for constructor, just another method, except for special check
	 * for recursive constructor invocations.
	 */
	public void resolveStatements(ClassScope upperScope) {
/*
		// checking for recursive constructor call (protection)
		if (!ignoreFurtherInvestigation && constructorCall == null){
			constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
			constructorCall.sourceStart = sourceStart;
			constructorCall.sourceEnd = sourceEnd;
		}
*/
		if (!CharOperation.equals(scope.enclosingSourceType().sourceName, selector)){
			scope.problemReporter().missingReturnType(this);
		}

		// if null ==> an error has occurs at parsing time ....
		if (constructorCall != null) {
			// e.g. using super() in java.lang.Object
			if (binding != null
				&& binding.declaringClass.id == T_Object
				&& constructorCall.accessMode != ExplicitConstructorCall.This) {
					if (constructorCall.accessMode == ExplicitConstructorCall.Super) {
						scope.problemReporter().cannotUseSuperInJavaLangObject(constructorCall);
					}
					constructorCall = null;
			} else {
				constructorCall.resolve(scope);
			}
		}
		
		super.resolveStatements(upperScope);

		// indirect reference: increment target constructor reference count
		if (constructorCall != null){
			if (constructorCall.binding != null
				&& !constructorCall.isSuperAccess()
				&& constructorCall.binding.isValidBinding() &&
				!constructorCall.binding.alwaysNeedsAccessMethod()) {
				((ConstructorDeclaration)
						(upperScope.referenceContext.declarationOf(constructorCall.binding))).referenceCount++;
			}
		}
	}

	public String toStringStatements(int tab) {

		String s = " {"; //$NON-NLS-1$
		if (constructorCall != null) {
			s = s + "\n" + constructorCall.toString(tab) + ";"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (statements != null) {
			for (int i = 0; i < statements.length; i++) {
				s = s + "\n" + statements[i].toString(tab); //$NON-NLS-1$
				if (!(statements[i] instanceof Block)) {
					s += ";"; //$NON-NLS-1$
				}
			}
		}
		s += "\n" + tabString(tab == 0 ? 0 : tab - 1) + "}"; //$NON-NLS-1$ //$NON-NLS-2$
		//$NON-NLS-2$ //$NON-NLS-1$
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		ClassScope classScope) {

		if (visitor.visit(this, classScope)) {
			if (arguments != null) {
				int argumentLength = arguments.length;
				for (int i = 0; i < argumentLength; i++)
					arguments[i].traverse(visitor, scope);
			}
			if (thrownExceptions != null) {
				int thrownExceptionsLength = thrownExceptions.length;
				for (int i = 0; i < thrownExceptionsLength; i++)
					thrownExceptions[i].traverse(visitor, scope);
			}
			if (constructorCall != null)
				constructorCall.traverse(visitor, scope);
			if (statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	}
}