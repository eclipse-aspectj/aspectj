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

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;

/**
 * AspectJ added template method for subclasses to insert more code.
 */
public class Clinit extends AbstractMethodDeclaration {
	
	public final static char[] ConstantPoolName = "<clinit>".toCharArray(); //$NON-NLS-1$

	private FieldBinding assertionSyntheticFieldBinding = null;
	private FieldBinding classLiteralSyntheticField = null;

	public Clinit(CompilationResult compilationResult) {
		super(compilationResult);
		modifiers = 0;
		selector = ConstantPoolName;
	}

	public void analyseCode(
		ClassScope classScope,
		InitializationFlowContext staticInitializerFlowContext,
		FlowInfo flowInfo) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			ExceptionHandlingFlowContext clinitContext =
				new ExceptionHandlingFlowContext(
					staticInitializerFlowContext.parent,
					this,
					NoExceptions,
					scope,
					FlowInfo.DeadEnd);

			// check for missing returning path
			needFreeReturn =
				!((flowInfo == FlowInfo.DeadEnd) || flowInfo.isFakeReachable());

			// check missing blank final field initializations
			flowInfo = flowInfo.mergedWith(staticInitializerFlowContext.initsOnReturn);
			FieldBinding[] fields = scope.enclosingSourceType().fields();
			for (int i = 0, count = fields.length; i < count; i++) {
				FieldBinding field;
				if ((field = fields[i]).isStatic()
					&& field.isFinal()
					&& (!flowInfo.isDefinitelyAssigned(fields[i]))) {
					scope.problemReporter().uninitializedBlankFinalField(
						field,
						scope.referenceType().declarationOf(field));
					// can complain against the field decl, since only one <clinit>
				}
			}
			// check static initializers thrown exceptions
			staticInitializerFlowContext.checkInitializerExceptions(
				scope,
				clinitContext,
				flowInfo);
		} catch (AbortMethod e) {
			this.ignoreFurtherInvestigation = true;
		}
	}

	/**
	 * Bytecode generation for a <clinit> method
	 *
	 * @param classScope org.eclipse.jdt.internal.compiler.lookup.ClassScope
	 * @param classFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {

		int clinitOffset = 0;
		if (ignoreFurtherInvestigation) {
			// should never have to add any <clinit> problem method
			return;
		}
		try {
			clinitOffset = classFile.contentsOffset;
			this.generateCode(classScope, classFile, clinitOffset);
		} catch (AbortMethod e) {
			// should never occur
			// the clinit referenceContext is the type declaration
			// All clinit problems will be reported against the type: AbortType instead of AbortMethod
			// reset the contentsOffset to the value before generating the clinit code
			// decrement the number of method info as well.
			// This is done in the addProblemMethod and addProblemConstructor for other
			// cases.
			if (e.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
				// a branch target required a goto_w, restart code gen in wide mode.
				try {
					if (statements != null) {
						for (int i = 0, max = statements.length; i < max; i++)
							statements[i].resetStateForCodeGeneration();
					}
					classFile.contentsOffset = clinitOffset;
					classFile.methodCount--;
					classFile.codeStream.wideMode = true; // request wide mode 
					this.generateCode(classScope, classFile, clinitOffset);
					// restart method generation
				} catch (AbortMethod e2) {
					classFile.contentsOffset = clinitOffset;
					classFile.methodCount--;
				}
			} else {
				// produce a problem method accounting for this fatal error
				classFile.contentsOffset = clinitOffset;
				classFile.methodCount--;
			}
		}
	}

	/**
	 * Bytecode generation for a <clinit> method
	 *
	 * @param classScope org.eclipse.jdt.internal.compiler.lookup.ClassScope
	 * @param classFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
	 */
	private void generateCode(
		ClassScope classScope,
		ClassFile classFile,
		int clinitOffset) {

		ConstantPool constantPool = classFile.constantPool;
		int constantPoolOffset = constantPool.currentOffset;
		int constantPoolIndex = constantPool.currentIndex;
		classFile.generateMethodInfoHeaderForClinit();
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		this.resolve(classScope);

		codeStream.reset(this, classFile);
		TypeDeclaration declaringType = classScope.referenceContext;

		// initialize local positions - including initializer scope.
		scope.computeLocalVariablePositions(0, codeStream); // should not be necessary
		MethodScope staticInitializerScope = declaringType.staticInitializerScope;
		staticInitializerScope.computeLocalVariablePositions(0, codeStream);
		// offset by the argument size

		//this.generateImplementationCode(


		// 1.4 feature
		// This has to be done before any other initialization
		generateSyntheticCode(classScope, codeStream);
		
		// generate initializers
		if (declaringType.fields != null) {
			for (int i = 0, max = declaringType.fields.length; i < max; i++) {
				FieldDeclaration fieldDecl;
				if ((fieldDecl = declaringType.fields[i]).isStatic()) {
					fieldDecl.generateCode(staticInitializerScope, codeStream);
				}
			}
		}
		
		generatePostSyntheticCode(classScope, codeStream);
		
		if (codeStream.position == 0) {
			// do not need to output a Clinit if no bytecodes
			// so we reset the offset inside the byte array contents.
			classFile.contentsOffset = clinitOffset;
			// like we don't addd a method we need to undo the increment on the method count
			classFile.methodCount--;
			// reset the constant pool to its state before the clinit
			constantPool.resetForClinit(constantPoolIndex, constantPoolOffset);
		} else {
			if (needFreeReturn) {
				int oldPosition = codeStream.position;
				codeStream.return_();
				codeStream.updateLocalVariablesAttribute(oldPosition);
			}
			// Record the end of the clinit: point to the declaration of the class
			codeStream.recordPositionsFrom(0, declaringType.sourceStart);
			classFile.completeCodeAttributeForClinit(codeAttributeOffset);
		}
	}

	protected void generateSyntheticCode(
		ClassScope classScope,
		CodeStream codeStream) {
		if (this.assertionSyntheticFieldBinding != null) {
			// generate code related to the activation of assertion for this class
			codeStream.generateClassLiteralAccessForType(
				classScope.enclosingSourceType(),
				classLiteralSyntheticField);
			codeStream.invokeJavaLangClassDesiredAssertionStatus();
			Label falseLabel = new Label(codeStream);
			codeStream.ifne(falseLabel);
			codeStream.iconst_1();
			Label jumpLabel = new Label(codeStream);
			codeStream.goto_(jumpLabel);
			falseLabel.place();
			codeStream.iconst_0();
			jumpLabel.place();
			codeStream.putstatic(this.assertionSyntheticFieldBinding);
		}
	}
	
	protected void generatePostSyntheticCode(
		ClassScope classScope,
		CodeStream codeStream) {
		}


	public boolean isClinit() {

		return true;
	}

	public boolean isInitializationMethod() {

		return true;
	}

	public boolean isStatic() {

		return true;
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		//the clinit is filled by hand .... 
	}

	public void resolve(ClassScope scope) {

		this.scope = new MethodScope(scope, scope.referenceContext, true);
	}

	public String toString(int tab) {

		String s = ""; //$NON-NLS-1$
		s = s + tabString(tab);
		s = s + "<clinit>()"; //$NON-NLS-1$
		s = s + toStringStatements(tab + 1);
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		ClassScope classScope) {

		visitor.visit(this, classScope);
		visitor.endVisit(this, classScope);
	}

	// 1.4 feature
	public void addSupportForAssertion(FieldBinding assertionSyntheticFieldBinding) {

		this.assertionSyntheticFieldBinding = assertionSyntheticFieldBinding;

		// we need to add the field right now, because the field infos are generated before the methods
		SourceTypeBinding sourceType =
			this.scope.outerMostMethodScope().enclosingSourceType();
		this.classLiteralSyntheticField =
			sourceType.addSyntheticField(sourceType, scope);
	}

}