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

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class LocalDeclaration extends AbstractVariableDeclaration {

	public LocalVariableBinding binding;

	public LocalDeclaration(
		Expression expr,
		char[] name,
		int sourceStart,
		int sourceEnd) {

		initialization = expr;
		this.name = name;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		if (initialization != null) {
			this.declarationSourceEnd = initialization.sourceEnd;
		} else {
			this.declarationSourceEnd = sourceEnd;
		}
		this.declarationEnd = this.declarationSourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// record variable initialization if any
		if (!flowInfo.isDeadEnd() && !flowInfo.isFakeReachable()) {
			bits |= IsLocalDeclarationReachableMASK; // only set if actually reached
		}
		if (initialization == null)
			return flowInfo;
		flowInfo =
			initialization
				.analyseCode(currentScope, flowContext, flowInfo)
				.unconditionalInits();
		flowInfo.markAsDefinitelyAssigned(binding);
		return flowInfo;
	}

	public void checkModifiers() {
		//only potential valid modifier is <<final>>

		if (((modifiers & AccJustFlag) | AccFinal) != AccFinal)
			//AccModifierProblem -> other (non-visibility problem)
			//AccAlternateModifierProblem -> duplicate modifier
			//AccModifierProblem | AccAlternateModifierProblem -> visibility problem"
			// -x-1 returns the bitInvert 

			modifiers =
				(modifiers & (-AccAlternateModifierProblem - 1)) | AccModifierProblem;
	}

	/**
	 * Code generation for a local declaration:
	 *	i.e.&nbsp;normal assignment to a local variable + unused variable handling 
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;
		Constant inlinedValue;
		// something to initialize?
		if (binding.resolvedPosition != -1) {
			codeStream.addVisibleLocalVariable(binding);
		}
		if (initialization != null) {
			// initialize to constant value?
			if ((inlinedValue = initialization.constant) != NotAConstant) {
				// forget initializing unused or final locals set to constant value (final ones are inlined)
				if (binding.resolvedPosition != -1) { // may need to preserve variable
					int initPC = codeStream.position;
					codeStream.generateConstant(inlinedValue, initialization.implicitConversion);
					codeStream.recordPositionsFrom(initPC, initialization.sourceStart);
					codeStream.store(binding, false);
					binding.recordInitializationStartPC(codeStream.position);
					//				codeStream.lastInitStateIndexWhenRemovingInits = -2; // reinitialize remove index 
					//				codeStream.lastInitStateIndexWhenAddingInits = -2; // reinitialize add index		
				}
			} else { // initializing to non-constant value
				initialization.generateCode(currentScope, codeStream, true);
				// if binding unused generate then discard the value
				if (binding.resolvedPosition != -1) {
					codeStream.store(binding, false);
					if (binding.initializationCount == 0) {
						/* Variable may have been initialized during the code initializing it
							e.g. int i = (i = 1);
						*/
						binding.recordInitializationStartPC(codeStream.position);
						//					codeStream.lastInitStateIndexWhenRemovingInits = -2; // reinitialize remove index 
						//					codeStream.lastInitStateIndexWhenAddingInits = -2; // reinitialize add index 
					}
				} else {
					if ((binding.type == LongBinding) || (binding.type == DoubleBinding)) {
						codeStream.pop2();
					} else {
						codeStream.pop();
					}
				}
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public String name() {

		return String.valueOf(name);
	}

	public void resolve(BlockScope scope) {

		// create a binding and add it to the scope
		TypeBinding tb = type.resolveType(scope);

		checkModifiers();

		if (tb != null) {
			if (tb == VoidBinding) {
				scope.problemReporter().variableTypeCannotBeVoid(this);
				return;
			}
			if (tb.isArrayType() && ((ArrayBinding) tb).leafComponentType == VoidBinding) {
				scope.problemReporter().variableTypeCannotBeVoidArray(this);
				return;
			}
		}

		// duplicate checks
		if ((binding = scope.duplicateName(name)) != null) {
			// the name already exists... may carry on with the first binding...
			scope.problemReporter().redefineLocal(this);
		} else {
			binding = new LocalVariableBinding(this, tb, modifiers, false);
			scope.addLocalVariable(binding);
			binding.constant = NotAConstant;
			// allow to recursivelly target the binding....
			// the correct constant is harmed if correctly computed at the end of this method
		}

		if (tb == null) {
			if (initialization != null)
				initialization.resolveType(scope); // want to report all possible errors
			return;
		}

		// store the constant for final locals 	
		if (initialization != null) {
			if (initialization instanceof ArrayInitializer) {
				TypeBinding initTb = initialization.resolveTypeExpecting(scope, tb);
				if (initTb != null) {
					((ArrayInitializer) initialization).binding = (ArrayBinding) initTb;
					initialization.implicitWidening(tb, initTb);
				}
			} else {
				TypeBinding initTb = initialization.resolveType(scope);
				if (initTb != null) {
					if (initialization.isConstantValueOfTypeAssignableToType(initTb, tb)
						|| (tb.isBaseType() && BaseTypeBinding.isWidening(tb.id, initTb.id))
						|| scope.areTypesCompatible(initTb, tb))
						initialization.implicitWidening(tb, initTb);
					else
						scope.problemReporter().typeMismatchError(initTb, tb, this);
				}
			}

			// change the constant in the binding when it is final
			// (the optimization of the constant propagation will be done later on)
			// cast from constant actual type to variable type
			binding.constant =
				binding.isFinal()
					? initialization.constant.castTo((tb.id << 4) + initialization.constant.typeID())
					: NotAConstant;
		}
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			type.traverse(visitor, scope);
			if (initialization != null)
				initialization.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}